package media.yam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import media.yam.MediaDB.SongInfo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore.Audio.Media;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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
	public static String PLAYLIST = "playlist";
	public static final String ACTION_PAUSE = "media.yam.action.PAUSE";
	public static final String ACTION_PLAY = "media.yam.action.PLAY";
	public static final String ACTION_PLAY_NEXT = "media.yam.action.PLAY_NEXT";
	public static final String ACTION_CHANGE_TRACK = "media.yam.action.CHANGE_TRACK";
	public static final String ACTION_PLAY_ALBUM = "media.yam.action.PLAY_ALBUM";
	public static final String ACTION_PLAY_ARTIST = "media.yam.action.PLAY_ARTIST";
	public static final String ACTION_PLAY_PLAYLIST = "media.yam.action.PLAY_PLAYLIST";
	public static final String ACTION_PLAY_ALLSONGS = "media.yam.action.PLAY_ALL_SONGS";

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
		

		IntentFilter f = new IntentFilter();
		f.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(broadcastReceiver, new IntentFilter(f));

		TelephonyManager telephony = (TelephonyManager)
			this.getSystemService(Context.TELEPHONY_SERVICE);
		telephony.listen(new PhoneStateListener() {
			boolean unpause = false;
			public void onCallStateChanged(int state, String incomingNumber) {
				super.onCallStateChanged(state, incomingNumber);
				switch(state) {
				case TelephonyManager.CALL_STATE_IDLE:
					if(unpause) {
						unpause = false;
						play();	
					}
					break;
				default:
					if(isPlaying()) {
						unpause = true;
						pause();	
					}
					break;
				}
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();
		List<Long> newPlaylist;
		if (extras != null) {
			String action = intent.getAction();
			if (ACTION_PLAY_NEXT.equals(action)) {
				if (extras.containsKey(Media._ID)) {
					playNext(extras.getLong(Media._ID));
				}
			}
			else if(ACTION_PLAY_ARTIST.equals(action)) {
				newPlaylist = albumArtistQuery(extras, Media.ARTIST_ID, Media.TITLE);
				changeToPlaylist(newPlaylist, extras, Media.ARTIST_ID);
			}
			else if(ACTION_PLAY_ALBUM.equals(action)) {
				newPlaylist = albumArtistQuery(extras, Media.ALBUM_ID, Media.TRACK);
				changeToPlaylist(newPlaylist, extras, Media.ALBUM_ID);
			}
			else if(ACTION_PLAY_PLAYLIST.equals(action)) {
				newPlaylist = toPlaylist(extras.getLongArray(PLAYLIST));
				changeToPlaylist(newPlaylist, extras, "");
			}
			else if(ACTION_PLAY_ALLSONGS.equals(action)) {
				Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, 
		        		new String[]{Media._ID, Media.TITLE}, null, null, Media.TITLE); 
				newPlaylist = toPlaylist(cursor);
				changeToPlaylist(newPlaylist, extras, "");
			}
			else if(ACTION_CHANGE_TRACK.equals(action)) {
				if(position == extras.getInt(PLAYLIST_POSITION)) {
					play();
				}
				else {
					position = extras.getInt(PLAYLIST_POSITION);
					changeCurrentlyPlaying(true);
					if(shuffle) {
						shuffle();
					}
				}
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
		//if(!shuffle) {
			unplayed = new ArrayList<Long>();
			unplayed.addAll(playlist);
		//}
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

	List<Long> toPlaylist(Long[] playlist) {
		return Arrays.asList(playlist);
	}

	List<Long> toPlaylist(long[] playlist) {
		Long[] t = new Long[playlist.length];
		for(int i = 0; i < playlist.length; i++) {
			t[i] = playlist[i];
		}
		return toPlaylist(t);
	}

	List<Long> toPlaylist(Cursor results) {
		ArrayList<Long> pl = null;
		if (results.getCount() > 0) {
			pl = new ArrayList<Long>(results.getCount());
			results.moveToFirst();
			do {
				pl.add(results.getLong(results
						.getColumnIndexOrThrow(Media._ID)));
			} while (results.moveToNext());
		} else {
			Log.e(PlayerService.class.getSimpleName(),
					"Service was given a query that produced no results. ");
		}
		return pl;
	}
	
	private List<Long> albumArtistQuery(Bundle extras, String key, String orderBy) {
		Cursor results = getContentResolver().query(
				Media.EXTERNAL_CONTENT_URI,
				new String[] { Media._ID },
				key + "=?",
				new String[] { String.valueOf(extras
						.getLong(key)) }, orderBy);
		List<Long> l = toPlaylist(results);
		results.close();
		return l;
	}

	private void changeToPlaylist(List<Long> playlist, Bundle extras, String key) {
		if(key == playlistType && playlistId != -1 && playlistId == extras.getLong(key) 
				&& position == extras.getInt(PLAYLIST_POSITION)) {
			play();
		}
		else {
			this.playlist = playlist;
			playlistType = key;
			playlistId = extras.containsKey(key) ? extras.getLong(key) : -1;
			position = extras.getInt(PLAYLIST_POSITION);
			changeCurrentlyPlaying(true);
			if(shuffle) {
				shuffle();
			}
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

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				if(intent.getExtras().getInt("state") == 0 && isPlaying()) {
					pause();
				}
			}
		}
	};

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
