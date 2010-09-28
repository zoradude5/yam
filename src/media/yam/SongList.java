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
import android.provider.MediaStore.Audio.Media;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SongList extends ListActivity {
	private static final int PLAY_SONG = 1;
	
	public static final String KEY_PATH = "path";
	private Bundle extras;



	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extras = getIntent().getExtras();
        
        String selection = null;
        
        if(extras.containsKey(Audio.Artists.ARTIST)) {
        	selection = Media.ARTIST_ID + "=" + extras.getLong(Media.ARTIST);
        }
        
        
        Cursor c = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, 
        		new String[]{Media._ID, Media.TITLE}, selection, null, null);
        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.song, c, 
        		new String[]{Media.TITLE}, new int[]{R.id.song_title});
        setListAdapter(adapter);
        
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
        	  launchPlayer(id);
          }
        });
    }
    


	private void launchPlayer(long id) {
		Intent i = new Intent(this, Player.class);
		if(extras.containsKey(Audio.Artists.ARTIST)) {
			i.putExtra(Audio.Artists.ARTIST, extras.getLong(Media.ARTIST));
			i.putExtra(PlayerService.PLAYLIST_POSITION, id);
		}
		else {
			i.putExtra(KEY_PATH, id);
		}
		startActivityForResult(i, PLAY_SONG);
	}
}