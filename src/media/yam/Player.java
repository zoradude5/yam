package media.yam;

import java.io.IOException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;

public class Player extends Activity {
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
