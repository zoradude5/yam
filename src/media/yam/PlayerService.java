package media.yam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class PlayerService extends Service {
	private MultiPlayer mp;
	private ArrayList<Long> playlist;
	private int position;
	private boolean shuffle = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		if(mp == null) {
			mp = new MultiPlayer();
		}
	}
	
	void play() {
		mp.play();
	}
	
	void pause() {
		mp.pause();
	}
	
	boolean isPlaying() {
		return mp.isPlaying();
	}
	
	void next() {
		if(shuffle) {
			
		}
		else {
			position++;//TODO NOT WORKING
		}
	}
	
	void setPlaylist(Long[] playlist) {
		this.playlist = (ArrayList<Long>) Arrays.asList(playlist);
	}
	
	void appendToPlaylist(long song) {
		playlist.add(song);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mp.release();
	}

    private final IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	private class MultiPlayer {
		private MediaPlayer mp = new MediaPlayer();
		private boolean initialized = false;
		private boolean playing = false;
		
		void setDataSource(String path) {
			try {
				mp.setDataSource(path);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		void prepare() {
			try {
				mp.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.initialized = true;
		}
		
		void play() {
			if(this.isInitialized()) {
				mp.start();
				playing = true;
			}
		}
		
		void pause() {
			if(this.isInitialized()) {
				mp.pause();
				playing = false;
			}
		}
		
		boolean isInitialized() {
			return initialized;
		}
		
		boolean isPlaying() {
			return playing;
		}
		
		void release() {
			mp.stop();
			mp.release();
			initialized = playing = false;
		}
	}
	
    public class LocalBinder extends Binder {
    	PlayerService getService() {
            return PlayerService.this;
        }
    }

}
