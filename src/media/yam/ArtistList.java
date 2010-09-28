package media.yam;

import java.io.File;
import java.util.Arrays;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Artists;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ArtistList extends ListActivity {
	private static final int PLAY_SONG = 1;
	
	public static final String KEY_PATH = "path";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs);
        
        Cursor c = getContentResolver().query(Artists.EXTERNAL_CONTENT_URI, 
        		new String[]{Artists._ID, Artists.ARTIST}, null, null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.song, c, 
        		new String[]{Artists.ARTIST}, new int[]{R.id.song_title});
        setListAdapter(adapter);
        
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
        	  launchArtistsSongs(id);
          }
        });
    }
    


	private void launchArtistsSongs(long id) {
		Intent i = new Intent(this, SongList.class);
		i.putExtra(Artists.ARTIST, id);
		startActivityForResult(i, PLAY_SONG);
	}
}