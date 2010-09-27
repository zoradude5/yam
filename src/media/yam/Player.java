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

public class Player extends Activity {
	private PlayerService player;
	private boolean playerIsBound;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		String path = extras.getString(SongList.KEY_PATH);

		doBindService();

	    Button play = (Button) findViewById(R.id.playButton);
	    play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(player.isPlaying()) {
					player.pause();
				}
				else {
					player.play();
				}
			}
		});
	}
	
	private ServiceConnection playerConnection = new ServiceConnection() {
		public void onServiceDisconnected(ComponentName name) {
			player = null;
		}
		public void onServiceConnected(ComponentName name, IBinder service) {
			player = ((PlayerService.LocalBinder)service).getService();
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
