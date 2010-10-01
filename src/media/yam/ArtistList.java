package media.yam;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ArtistList extends ListActivity {
	private static final int PLAY_SONG = 1;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs);
        
        Cursor c = getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, 
        		new String[]{Media._ID, Media.ARTIST}, null, null, null);
        SimpleCursorAdapter adapter = new SimpleAlphabetCursorAdapter(this, R.layout.song, c, 
        		new String[]{Media.ARTIST}, new int[]{R.id.song_title}, 
        		c.getColumnIndexOrThrow(Media.ARTIST));
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
		Intent i = new Intent(this, AlbumList.class);
		i.putExtra(Media.ARTIST_ID, id);
		startActivityForResult(i, PLAY_SONG);
	}
}