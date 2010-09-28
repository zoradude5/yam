package media.yam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

public class PlayerService extends Service {
	private MultiPlayer mp;
	private List<Long> playlist;
	private int position;
	private boolean shuffle = false;
	public static String PLAYLIST_POSITION = "position";
	
	@Override
	public void onCreate() {
		super.onCreate();
		if(mp == null) {
			mp = new MultiPlayer();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();
		if(extras != null) {
			boolean changes = false;
			if(extras.containsKey(PLAYLIST_POSITION)) {
				position = extras.getInt(PLAYLIST_POSITION);
				changes = true;
			}
			
	
			if(extras.containsKey(Audio.Artists.ARTIST)) {
				Cursor results = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, 
						new String[]{Media._ID}, Media.ARTIST_ID + "=?", 
						new String[]{String.valueOf(extras.getLong(Audio.Artists.ARTIST))}, null);
				setPlaylist(results);
				results.close();
				changes = true;
			}
			else if(extras.containsKey(SongList.KEY_PATH)) {
				setPlaylist(new Long[]{extras.getLong(SongList.KEY_PATH)});
				changes = true;
			}
			
			if(changes) {
				changeSong();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	void changeSong() {
		mp.setDataSource(Uri.withAppendedPath(Media.EXTERNAL_CONTENT_URI, 
				String.valueOf(playlist.get(position))).toString());
		mp.prepare();
		play();
	}

	void play() {
		mp.play();
	}
	
	void pause() {
		mp.pause();
	}
	
	void seekPercent(int percent) {
		int newTime = (int) (percent * mp.duration() / 100); 
		mp.seek(newTime);
	}
	
	boolean isPlaying() {
		return mp.isPlaying();
	}
	
	void next() {
		if(shuffle) {
			
		}
		else { // this should only happen in case of repeat!!!!! remove this soon TODO TODO
			if(position == playlist.size() - 1) {
				position = 0; 
			}
			else {
				position++;
			}
			changeSong();
		}
	}
	
	void setPlaylist(Long[] playlist) {
		this.playlist = Arrays.asList(playlist);
	}
	
	void setPlaylist(Cursor results) {
		if(results.getCount() > 0) {
			playlist = new ArrayList<Long>(results.getCount());
			results.moveToFirst();
			do {
				playlist.add(results.getLong(results.getColumnIndexOrThrow(Media._ID)));
			} while(results.moveToNext());
		}
		else {
			Log.w(PlayerService.class.getSimpleName(), "Service was given a query that produced no results. ");
		}
	}
	
	void appendToPlaylist(long song) {
		playlist.add(song);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mp.release();
	}
	
	MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			next();
		}
	};
	
	private class MultiPlayer {
		private MediaPlayer mp = new MediaPlayer();
		private boolean initialized = false;
		private boolean playing = false;
		
		public MultiPlayer() {
			mp.setOnCompletionListener(completionListener);
		}
		
		void setDataSource(String path) {
			if(this.isInitialized() || this.isPlaying()) {
				mp.reset();
				initialized = false;
				playing = false;
			}
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
		
		void prepare() { // I'm only keeping this separate because I want to consider async prep
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
		
		void seek(int msec) {
			mp.seekTo(msec);
		}
		
		long duration() {
			return mp.getDuration();
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

    private final IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
    public class LocalBinder extends Binder {
    	PlayerService getService() {
            return PlayerService.this;
        }
    }

}
