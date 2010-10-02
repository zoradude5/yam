package media.yam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import media.yam.MediaDB.SongInfo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

public class PlayerService extends Service {
	private MultiPlayer mp;
	private MediaDB db;
	private List<Long> playlist = new ArrayList<Long>();
	private int position;
	private boolean shuffle = false;
	private boolean repeat = false;
	private String playlistType;
	private long playlistId;
	private NotificationManager nm;
	private SongInfo currentSong;
	private List<Long> unplayed;
	private Random rnd = new Random();

	public static String PLAYLIST_POSITION = "position";
	public static final String ACTION_PAUSE = "media.yam.action.PAUSE";
	public static final String ACTION_PLAY = "media.yam.action.PLAY";
	public static final String ACTION_PLAY_NEXT = "media.yam.action.PLAY_NEXT";

	public static final String METADATA_CHANGED = "media.yam.broadcast.METADATA_CHANGED";

	@Override
	public void onCreate() {
		super.onCreate();
		if (mp == null) {
			mp = new MultiPlayer();
		}
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		db = new MediaDB(this);
		db.open();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			if (ACTION_PLAY_NEXT.equals(intent.getAction())) {
				if (extras.containsKey(Media._ID)) {
					playNext(extras.getLong(Media._ID));
				}
			}
			boolean changes = false;
			if (extras.containsKey(Media.ARTIST_ID)) {
				if (playlistType != Media.ARTIST_ID
						|| playlistId != extras.getLong(Media.ARTIST_ID)) {
					playlistType = Media.ARTIST_ID;
					playlistId = extras.getLong(Media.ARTIST_ID);
					Cursor results = getContentResolver().query(
							Media.EXTERNAL_CONTENT_URI,
							new String[] { Media._ID },
							Media.ARTIST_ID + "=?",
							new String[] { String.valueOf(extras
									.getLong(Media.ARTIST_ID)) }, null);
					setPlaylist(results);
					results.close();
					changes = true;
				}
			} else if (extras.containsKey(Media.ALBUM_ID)) {
				if (playlistType != Media.ALBUM_ID
						|| playlistId != extras.getLong(Media.ALBUM_ID)) {
					playlistType = Media.ALBUM_ID;
					playlistId = extras.getLong(Media.ALBUM_ID);
					Cursor results = getContentResolver().query(
							Media.EXTERNAL_CONTENT_URI,
							new String[] { Media._ID },
							Media.ALBUM_ID + "=?",
							new String[] { String.valueOf(extras
									.getLong(Media.ALBUM_ID)) }, null);
					setPlaylist(results);
					results.close();
					changes = true;
				}
			}

			if (extras.containsKey(PLAYLIST_POSITION)) {
				if (position != extras.getInt(PLAYLIST_POSITION)) {
					position = extras.getInt(PLAYLIST_POSITION);
					changes = true;
				}
			}

			if (changes) {
				changeCurrentlyPlaying(true);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	void changeCurrentlyPlaying(boolean play) {
		mp.setDataSource(Uri.withAppendedPath(Media.EXTERNAL_CONTENT_URI,
				String.valueOf(playlist.get(position))).toString());
		mp.prepare();

		currentSong = MediaDB.getSong(getContentResolver(),
				playlist.get(position));

		Intent i = new Intent(METADATA_CHANGED);
		i.putExtra("id", Long.valueOf(currentSong.id));
		i.putExtra("artist", currentSong.artist);
		i.putExtra("album", currentSong.album);
		i.putExtra("title", currentSong.title);
		i.putExtra("albumId", currentSong.albumId);
		sendBroadcast(i);
		
		// this should move to play, but the issue is play after a pause -- we
		// don't wnat to start form the beginning after a pause
		if(play)
			play();
	}

	void play() {		
		mp.play();
		Notification n = new Notification(); // make notification a static
												// variable thing?  TODO
		n.icon = R.drawable.icon;
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this,
				Player.class), 0);
		n.flags |= Notification.FLAG_ONGOING_EVENT;
		n.setLatestEventInfo(this, currentSong.title, currentSong.artist, pi);
		startForeground(0, n);
		nm.notify(0, n);
	}

	void pause() {
		mp.pause();
		nm.cancel(0);
		stopForeground(true);
	}

	void next(boolean force) {//took idea for name "force" from music app
		if (shuffle) {/// TODO
			if(unplayed.size() == 0) {
				shuffle = false;
				shuffle();
				int i = rnd.nextInt(unplayed.size());
				this.position = playlist.indexOf(unplayed.get(i));
				unplayed.remove(i);
				if(repeat || force) {// do something smarter here so you don't get repeats? TODO
					changeCurrentlyPlaying(true);
				}
				else {
					changeCurrentlyPlaying(false);
				}
			}
			else {
				int i = rnd.nextInt(unplayed.size());
				this.position = playlist.indexOf(unplayed.get(i));
				unplayed.remove(i);
				changeCurrentlyPlaying(true);
			}
		} else {
			if (position == playlist.size() - 1) {
				position = 0;
				if(repeat || force) {
					changeCurrentlyPlaying(true);
				}
				else {
					pause();
					changeCurrentlyPlaying(false);
				}
			} else {
				position++;
				changeCurrentlyPlaying(true);
			}
		}
	}
	
	void shuffleToggle() {
		if(shuffle) {
			unshuffle();
		}
		else {
			shuffle();
		}
	}
	
	boolean getShuffle() { return shuffle; }
	
	void shuffle() {
		if(!shuffle) {
			unplayed = new ArrayList<Long>();
			unplayed.addAll(playlist);
		}
		shuffle = true;
	}
	
	void unshuffle() {
		unplayed = null;
		shuffle = false;
	}

	void seekPercent(int progress, int max) {
		int newTime = progress * mp.duration() / max;
		mp.seek(newTime);
	}

	boolean isPlaying() {
		return mp.isPlaying();
	}

	public int getPosition() {
		return this.position;
	}

	void setPlaylist(Long[] playlist) {
		this.playlist = Arrays.asList(playlist);
	}

	void setPlaylist(Cursor results) {
		if (results.getCount() > 0) {
			playlist = new ArrayList<Long>(results.getCount());
			results.moveToFirst();
			do {
				playlist.add(results.getLong(results
						.getColumnIndexOrThrow(Media._ID)));
			} while (results.moveToNext());
		} else {
			Log.w(PlayerService.class.getSimpleName(),
					"Service was given a query that produced no results. ");
		}
	}

	void playNext(long song) {
		playlist.add(position + 1, song);
		if(shuffle) {
			unplayed.add(song);
		}
	}

	public Long[] getPlaylist() {
		return playlist.toArray(new Long[] {});
	}

	public int getCurrentPosition() {
		return mp.currentPosition();
	}

	public int getDuration() {
		return mp.duration();
	}
	
	public SongInfo getCurrentSong() {
		return currentSong;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		nm.cancel(0);
		mp.release();
		db.close();
	}

	MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			db.increment(playlist.get(position), MediaDB.ACTION_PLAY);
			next(false);
		}
	};

	private class MultiPlayer {
		private MediaPlayer mp = new MediaPlayer();
		private boolean initialized = false;

		public MultiPlayer() {
			mp.setOnCompletionListener(completionListener);
		}

		void setDataSource(String path) {
			if (this.isInitialized() || this.isPlaying()) {
				mp.reset();
				initialized = false;
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

		void prepare() { // I'm only keeping this separate because I want to
							// consider async prep
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
			if (this.isInitialized()) {
				mp.start();
			}
		}

		void pause() {
			if (this.isInitialized()) {
				mp.pause();
			}
		}

		void seek(int msec) {
			mp.seekTo(msec);
		}

		int duration() {
			return mp.getDuration();
		}

		int currentPosition() {
			return mp.getCurrentPosition();
		}

		boolean isInitialized() {
			return initialized;
		}

		boolean isPlaying() {
			return mp.isPlaying();
		}

		void release() {
			mp.stop();
			mp.release();
			initialized = false;
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
