package media.yam;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Media;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SongList extends ListActivity {
	private static final int PLAY_SONG = 1;
	
	public static final String KEY_PATH = "path";
	private Bundle extras;



	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs);
        extras = getIntent().getExtras();
        
        String selection = null;
        
        if(extras.containsKey(Media.ARTIST)) {
        	selection = Media.ARTIST_ID + "=" + extras.getLong(Media.ARTIST);
        }
        else if(extras.containsKey(Media.ALBUM)) {
        	selection = Media.ALBUM_ID + "=" + extras.getLong(Media.ALBUM);
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
        	  launchPlayer(position);
          }
        });
    }
    


	private void launchPlayer(int position) {
		Intent i = new Intent(this, Player.class);
		if(extras.containsKey(Media.ARTIST)) {
			i.putExtra(Media.ARTIST, extras.getLong(Media.ARTIST));
			i.putExtra(PlayerService.PLAYLIST_POSITION, position);
		}
		else if(extras.containsKey(Media.ALBUM)) {
			i.putExtra(Media.ALBUM, extras.getLong(Media.ALBUM));
			i.putExtra(PlayerService.PLAYLIST_POSITION, position);
		}
		else {
			i.putExtra(KEY_PATH, position);
		}
		startActivityForResult(i, PLAY_SONG);
	}
}