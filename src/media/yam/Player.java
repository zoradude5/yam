package media.yam;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

public class Player extends Activity {
	public static final String PLAYLIST = "playlist";
	
	private PlayerService player;
	private boolean playerIsBound;
	private Bundle extras;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		extras = getIntent().getExtras();
		doBindService();
		Intent i = new Intent(this, PlayerService.class);
		if(extras != null) {
			i.putExtras(extras);
		}
		startService(i);

		ImageView play = (ImageView) findViewById(R.id.playButton);
	    play.setOnClickListener(playOnClickListener);
	    play.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
					if(player.isPlaying()) {
						((ImageView) v).setImageResource(R.drawable.pause_depressed);
					}
					else {
						((ImageView) v).setImageResource(R.drawable.play_depressed);
					}
				}
				else {
					if(player.isPlaying()) {//change this to rely on a message or something sent by service
						((ImageView) v).setImageResource(R.drawable.play);
					}
					else {
						((ImageView) v).setImageResource(R.drawable.pause);
					}
				}
				return false;
			}
		});
	    ImageView next = (ImageView) findViewById(R.id.nextButton);
	    next.setOnClickListener(nextOnClickListener);
	    next.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
					((ImageView) v).setImageResource(R.drawable.next_depressed);
				}
				else {
					((ImageView) v).setImageResource(R.drawable.next);
				}
				return false;
			}
		});
	    ImageView library = (ImageView) findViewById(R.id.libraryButton);
	    library.setOnClickListener(libraryOnClickListener);
	    library.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
					((ImageView) v).setImageResource(R.drawable.library_depressed);
				}
				else {
					((ImageView) v).setImageResource(R.drawable.library);
				}
				return false;
			}
		});
	    Button current = (Button) findViewById(R.id.currentButton);
	    current.setOnClickListener(currentOnClickListener);
	    SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
	    seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	    doUnbindService();
	}
	
	private ServiceConnection playerConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			player = ((PlayerService.LocalBinder)service).getService();
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
			player.next();
		}
	};

	View.OnClickListener playOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(player.isPlaying()) {
				player.pause(); // add button changing code to the player. playlisteneer?
			}
			else {
				player.play();// change this to a message passing thing!!!
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
				.putExtra(PLAYLIST, playlist), 0);
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
			player.seekPercent(progress);
		}
	};
}
