package media.yam;

import java.util.Calendar;

import media.yam.MediaDB.SongInfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class Player extends Activity {
	public static final String PLAYLIST = "playlist";
	
	private PlayerService player;
	private boolean playerIsBound;
	private TextView songTitle;
	private TextView songAlbum;
	private TextView songArtist;
	private SeekBar seekBar;
	private ImageView play;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		doBindService();
		
		IntentFilter f = new IntentFilter();
		f.addAction(PlayerService.METADATA_CHANGED);
		registerReceiver(broadcastReceiver, new IntentFilter(f));
		
		play = (ImageView) findViewById(R.id.playButton);
	    play.setOnClickListener(playOnClickListener);
	    ImageView next = (ImageView) findViewById(R.id.nextButton);
	    next.setOnClickListener(nextOnClickListener);
	    ImageView library = (ImageView) findViewById(R.id.libraryButton);
	    library.setOnClickListener(libraryOnClickListener);
	    Button current = (Button) findViewById(R.id.currentButton);
	    current.setOnClickListener(currentOnClickListener);
	    
	    final Button shuffleButton = (Button) findViewById(R.id.shuffleButton);
	    shuffleButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				player.shuffleToggle();
				if(player.getShuffle()) {
					shuffleButton.setText("shuffle on");
				}
				else {
					shuffleButton.setText("shuffle off");
				}
			}
		});
	    
	    seekBar = (SeekBar) findViewById(R.id.seekBar);
	    seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
	    seekBar.setMax(1000);

	    songTitle = (TextView) Player.this.findViewById(R.id.songTitle);
	    //songAlbum = (TextView) Player.this.findViewById(R.id.songAlbum);
	    songArtist = (TextView) Player.this.findViewById(R.id.songArtist);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	    doUnbindService();
	}
	
	void refreshInfo() {
		seekBar.setProgress(player.getCurrentPosition() * seekBar.getMax() / player.getDuration() );
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(player.getCurrentPosition());
		Calendar d = Calendar.getInstance();
		d.setTimeInMillis(player.getDuration());
		
		((TextView) findViewById(R.id.songProgress)).setText(
				DateFormat.format("m:ss", c) + "/" + DateFormat.format("m:ss", d));
		
		int remaining = 1000 - (player.getCurrentPosition() % 1000);
		if(!player.isPlaying()) {
			remaining = 500;			
		}
		
		handler.postDelayed(new Runnable() {
			public void run() {
				refreshInfo();
			}
		},remaining);
	}
	
	void setMeta(Bundle extras) {
		setMeta(extras.getString("title"), extras.getString("album"), 
				extras.getString("artist"), extras.getLong("albumId"));
	}
	
	void setMeta(SongInfo si) {
		setMeta(si.title, si.album, si.artist, si.albumId);
	}
	
	void setMeta(String title, String album, String artist, long albumId) {
		songTitle.setText(title);
		//songAlbum.setText(album);
		songArtist.setText(artist);
		setAlbumArt(albumId);
	}
	
	void setAlbumArt(long albumId) {
		ImageView albumArt = (ImageView) findViewById(R.id.albumArt);
		albumArt.setImageBitmap(MediaDB.getAlbumArt(getContentResolver(), albumId));
	}
	
	void setPlayButton() {
		play.setImageResource(R.drawable.play_image_button);
	}
	
	void setPauseButton() {
		play.setImageResource(R.drawable.pause_image_button);
	}
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(PlayerService.METADATA_CHANGED)) {
				setMeta(intent.getExtras());
			}
		}
	};
	
	private ServiceConnection playerConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			player = ((PlayerService.LocalBinder)service).getService();
			setMeta(player.getCurrentSong());
			refreshInfo();
			if(player.isPlaying()) {
				setPauseButton();
			}
			else {
				setPlayButton();
			}
		}
		public void onServiceDisconnected(ComponentName name) {
			player = null;
		}
	};
	
	public void doBindService() {
		bindService(new Intent(this, PlayerService.class), playerConnection, BIND_AUTO_CREATE);
		playerIsBound = true;
	}
	
	public void doUnbindService() {
		if(playerIsBound) {
			unbindService(playerConnection);
			playerIsBound = false;
		}
	}
	
	View.OnClickListener nextOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			player.next(true);
		}
	};

	View.OnClickListener playOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(player.isPlaying()) {
				player.pause(); // add button changing code to the player. playlisteneer?
				setPlayButton();
			}
			else {
				player.play();// change this to a message passing thing!!!
				setPauseButton();
			}
		}
	};
	
	View.OnClickListener libraryOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivityForResult(new Intent(Player.this, ArtistList.class), 0);
		}
	};
	
	View.OnClickListener currentOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Long[] pl = player.getPlaylist();
			long[] playlist = new long[pl.length];
			for(int i = 0; i < pl.length; i++) {
				playlist[i] = pl[i];
			}
			startActivityForResult(new Intent(Player.this, CurrentlyPlaying.class)
				.putExtra(PLAYLIST, playlist).putExtra(PlayerService.PLAYLIST_POSITION, player.getPosition()), 0);
		}
	};
	
	SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if(!fromUser) return;
			if(player != null) {
				player.seekPercent(progress,seekBar.getMax());
			}
		}
	};
	
	Handler handler = new Handler();
}
