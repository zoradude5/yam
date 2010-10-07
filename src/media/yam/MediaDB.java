package media.yam;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

public class MediaDB {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_MEDIA_ID = "media_id";
    public static final String KEY_TYPE = "type";
    public static final String KEY_TIME = "time";
    
    public static final int ACTION_PLAY = 0;
    public static final int ACTION_SKIP = 1;
    public static final int ACTION_CHOOSE = 2;
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "media";
    private static final int DATABASE_VERSION = 2;
    private static final String TAG = "TextDbAdapter";
    
    private static final String DATABASE_CREATE =
        "create table media (_id integer primary key autoincrement, "
        + "media_id integer not null, type integeger not null, " +
          "time bigint(64) not null);";

    private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS media");
            onCreate(db);
		}
    }

    public MediaDB(Context ctx) {
        this.mCtx = ctx;
    }

    public MediaDB open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    public long add(long id, int type, long time) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_MEDIA_ID, id);
		cv.put(KEY_TYPE, type);
		cv.put(KEY_TIME, time);
		return mDb.insert(DATABASE_TABLE, null, cv);
    }
    
    public long increment(long id, int type) {
    	return add(id, type, System.currentTimeMillis());
    }
    
    public Long[] top(int threshhold) {
    	Cursor c = mDb.query(DATABASE_TABLE, new String[]{KEY_MEDIA_ID, "COUNT("+KEY_MEDIA_ID+")"}, 
    			KEY_TYPE+"=?", 
    			new String[]{String.valueOf(ACTION_PLAY)}, 
    			KEY_MEDIA_ID, null, null);
    	ArrayList<Long> result = new ArrayList<Long>();
    	int i=0;
    	c.moveToFirst();
    	do {
    		if(c.getInt(1) > 3) {
    			result.add(c.getLong(c.getColumnIndexOrThrow(KEY_MEDIA_ID)));
    		}
    	}
    	while(c.moveToNext());
    	return result.toArray(new Long[]{});
    }
    
    public static class SongInfo {
    	long id, albumId;
    	String title, album, artist;
    	SongInfo(String artist, String album, String title, long albumId) {
    		this.artist = artist;
    		this.album = album;
    		this.title = title;
    		this.albumId = albumId;
    	}
    	public String toString() {
    		return title;
    	}
    }
    
    public static SongInfo getSong(ContentResolver cr, long id) {
    	Cursor c = cr.query(Uri.withAppendedPath(Media.EXTERNAL_CONTENT_URI, String.valueOf(id)), 
			new String[]{Media.ARTIST, Media.ALBUM, Media.TITLE, Media.ALBUM_ID}, 
			null, null, null);
    	c.moveToFirst();
    	SongInfo si = new SongInfo(c.getString(0), c.getString(1), c.getString(2), c.getLong(3));
    	si.id = id;
    	c.close();
    	return si;
    }

	public static Bitmap getAlbumArt(ContentResolver cr, long albumId) {
		Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
		Uri uri = ContentUris.withAppendedId(sArtworkUri, albumId);
		InputStream in = null;
		try {
			in = cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Bitmap artwork = BitmapFactory.decodeStream(in);
		return artwork;
	}

}
