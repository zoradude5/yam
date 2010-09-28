package media.yam;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

}
