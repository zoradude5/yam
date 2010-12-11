package media.yam;

import java.util.HashMap;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class AlbumList<K> extends ListActivity {
	private static final int PLAY_SONG = 1;
	
	private Bundle extras;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs);
        extras = getIntent().getExtras();
        
        String selection = null;
        String orderBy = Media.ALBUM;
        ListView lv = getListView();
        
        if(extras != null && extras.containsKey(Media.ARTIST_ID)) {
        	selection = Media.ARTIST_ID + "=" + extras.getLong(Media.ARTIST_ID);
            
            LayoutInflater li = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View allSongs = li.inflate(R.layout.song, null);
            ((TextView)allSongs.findViewById(R.id.song_title)).setText("All Songs");
            lv.addHeaderView(allSongs);
            lv.setTextFilterEnabled(true);

        }
        else {
        }
        
        
        Cursor c = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
        		new String[]{Media._ID, Media.ALBUM}, selection, null, orderBy);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.album, c, 
        		new String[]{Media.ALBUM}, new int[]{R.id.song_title}) {

					@Override
					public void bindView(View view, Context context,
							Cursor cursor) {
						super.bindView(view, context, cursor);

						long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(Media._ID));
						ImageView v = (ImageView) view.findViewById(R.id.albumArt);
						Bitmap big = MediaDB.getAlbumArt(getContentResolver(), albumId);
						BitmapDrawable b = new BitmapDrawable(context.getResources(), big);
						b.setFilterBitmap(false);
						b.setDither(false);
						v.setImageDrawable(b);
						
						//v.setImageDrawable(Utils.getCachedArtwork(AlbumList.this, albumId, (BitmapDrawable) context.getResources().getDrawable(R.drawable.play)));
					}
        	
        };
        //adapter.setViewBinder(albumArtViewBinder);
        setListAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
          	  launchArtistsSongs(id);
            }
        });
    }
    


	private void launchArtistsSongs(long id) {
		Intent i = new Intent(this, SongList.class);
		if(id == -1) {
			i.putExtra(Media.ARTIST_ID, extras.getLong(Media.ARTIST_ID));
		}
		else {
			i.putExtra(Media.ALBUM_ID, id);
		}
		startActivityForResult(i, PLAY_SONG);
	}
	

    final HashMap<Long, Bitmap> albumArt = new HashMap<Long, Bitmap>();
	SimpleCursorAdapter.ViewBinder albumArtViewBinder = new SimpleCursorAdapter.ViewBinder() {
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if(columnIndex == cursor.getColumnIndexOrThrow(Media._ID)) {
				long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(Media._ID));
				Bitmap b = null;
				ImageView v = (ImageView) view;
				/*
				if(albumArt.containsKey(albumId)) {
					b = albumArt.get(albumId);
					if(b == null) {
						return true;
					}
				}
				else {
					Bitmap big = MediaDB.getAlbumArt(getContentResolver(), albumId);
					if(big == null) {
						return true;
					}
					b = Bitmap.createScaledBitmap(big, 50, 50, false);
					big.recycle();
					albumArt.put(albumId, b);
				}
				v.setImageBitmap(b);
				*/
				Bitmap big = MediaDB.getAlbumArt(getContentResolver(), albumId);
				v.setImageBitmap(big);
				
				return true;
			}
			else {
				return false;
			}
		}
	};

}
