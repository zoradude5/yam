package media.yam;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class AlbumList extends ListActivity {
	private static final int PLAY_SONG = 1;
	
	public static final String KEY_PATH = "path";
	private Bundle extras;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs);
        extras = getIntent().getExtras();
        
        ListView lv = getListView();
        LayoutInflater li = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View allSongs = li.inflate(R.layout.song, null);
        ((TextView)allSongs.findViewById(R.id.song_title)).setText("All Songs");
        lv.addHeaderView(allSongs);
        lv.setTextFilterEnabled(true);
        
        String selection = null;
        if(extras.containsKey(Media.ARTIST_ID)) {
        	selection = Media.ARTIST_ID + "=" + extras.getLong(Media.ARTIST_ID);
        }
        Cursor c = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
        		new String[]{Media._ID, Media.ALBUM}, selection, null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.song, c, 
        		new String[]{Media.ALBUM}, new int[]{R.id.song_title});
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

}