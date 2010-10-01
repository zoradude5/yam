package media.yam;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Audio.Media;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SongList extends ListActivity {
	private static final int PLAY_SONG = 1;
	
	public static final String KEY_PATH = "path";
	
	private static final int MENU_PLAY_NOW = Menu.FIRST;
	private static final int MENU_PLAY_NEXT = Menu.FIRST + 1;
	private Bundle extras;



	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs);
        registerForContextMenu(getListView());
        extras = getIntent().getExtras();
        
        String selection = null;
        
        if(extras.containsKey(Media.ARTIST_ID)) {
        	selection = Media.ARTIST_ID + "=" + extras.getLong(Media.ARTIST_ID);
        }
        else if(extras.containsKey(Media.ALBUM_ID)) {
        	selection = Media.ALBUM_ID + "=" + extras.getLong(Media.ALBUM_ID);
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
        	  launchPlayer(position, id);
          }
        });
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, MENU_PLAY_NOW, 0, "Play Now");
        menu.add(0, MENU_PLAY_NEXT, 0, "Play Next");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
        case MENU_PLAY_NOW:
        	launchPlayer(info.position, info.id);
        	break;
        case MENU_PLAY_NEXT:
        	Intent i = new Intent(this, PlayerService.class);
        	i.setAction(PlayerService.ACTION_PLAY_NEXT);
        	i.putExtra(Media._ID, info.id);
        	startService(i);
            return true;
	    }
	    return super.onContextItemSelected(item);
	}
    


	private void launchPlayer(int position, long id) {
		Intent service = new Intent(this, PlayerService.class);
		if(extras.containsKey(Media.ARTIST_ID)) {
			service.putExtra(Media.ARTIST_ID, extras.getLong(Media.ARTIST_ID));
			service.putExtra(PlayerService.PLAYLIST_POSITION, position);
		}
		else if(extras.containsKey(Media.ALBUM_ID)) {
			service.putExtra(Media.ALBUM_ID, extras.getLong(Media.ALBUM_ID));
			service.putExtra(PlayerService.PLAYLIST_POSITION, position);
		}
		else {
			service.putExtra(KEY_PATH, position);
		}
		startService(service);
		Intent i = new Intent(this, Player.class);
		i.putExtra(Media._ID, id);
		startActivityForResult(i, PLAY_SONG);
	}
}