package media.yam;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
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

	    Button play = (Button) findViewById(R.id.playButton);
	    play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(player.isPlaying()) {
					player.pause(); // add button changing code to the player. playlisteneer?
				}
				else {
					player.play();// change this to a message passing thing!!!
				}
			}
		});
	    Button next = (Button) findViewById(R.id.nextButton);
	    next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				player.next();
			}
		});
	    Button library = (Button) findViewById(R.id.libraryButton);
	    library.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(Player.this, ArtistList.class), 0);
			}
		});
	    Button current = (Button) findViewById(R.id.currentButton);
	    current.setOnClickListener(new View.OnClickListener() {
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
		});
	    SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
	    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
		});
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
	    doUnbindService();
	}

}
