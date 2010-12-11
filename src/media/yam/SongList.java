package media.yam;

import android.app.Activity;
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
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RemoteViews.ActionException;
import android.widget.SimpleCursorAdapter;

public class SongList extends ListActivity {
	private static final int PLAY_SONG = 1;
	
	public static final String ACTION_TOP = "media.yam.action.TOP";
	
	private static final int MENU_PLAY_NOW = Menu.FIRST;
	private static final int MENU_PLAY_NEXT = Menu.FIRST + 1;
	private Bundle extras;
	private Long[] topPlaylist;
	private MediaDB db;



	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	db = new MediaDB(this);
    	db.open();
        setContentView(R.layout.songs);
        registerForContextMenu(getListView());
        extras = getIntent().getExtras();
        
        String selection = null;
        String orderBy = null;
        Cursor c = null;
        ListAdapter adapter = null;
        if(extras != null) {
	        if(extras.containsKey(Media.ARTIST_ID)) {
	        	selection = Media.ARTIST_ID + "=" + extras.getLong(Media.ARTIST_ID);
	        	orderBy = Media.TITLE;
	        }
	        else if(extras.containsKey(Media.ALBUM_ID)) {
	        	selection = Media.ALBUM_ID + "=" + extras.getLong(Media.ALBUM_ID);
	        	orderBy = Media.TRACK;
	        }
        }
        else if(ACTION_TOP.equals(getIntent().getAction())) {
        	topPlaylist = db.top();

        	adapter = new ArrayAdapter<Long>(this, R.layout.song, topPlaylist) {
	        	
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					TextView v = (TextView) super.getView(position, convertView, parent).findViewById(R.id.song_title);
					MediaDB.SongInfo si = MediaDB.getSong(SongList.this.getContentResolver(), topPlaylist[position]);

		        	//int playCount = db.getPlayCount(topPlaylist[position]);
		        	
					v.setText(si.title);// + " (" + playCount + ")");//playlist[position].toString());//si.title);

					return v;
				}
        	};
        }
        else {
        	orderBy = Media.TITLE;
        	
        }
        
        if(c == null) {
	        c = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, 
	        		new String[]{Media._ID, Media.TITLE}, selection, null, orderBy);
        }
        
        if(adapter == null) {
		    adapter = new SimpleCursorAdapter(this, R.layout.song, c, 
		    		new String[]{Media.TITLE}, new int[]{R.id.song_title});
        }
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
	protected void onDestroy() {
    	db.close();
		super.onDestroy();
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
		if(ACTION_TOP.equals(getIntent().getAction())) {
			Intent service = new Intent(this, PlayerService.class);
			service.setAction(PlayerService.ACTION_PLAY_PLAYLIST);
			long[] t = new long[topPlaylist.length];
			for(int i = 0; i < t.length; i++) {
				t[i] = topPlaylist[i];
			}
			service.putExtra(PlayerService.PLAYLIST, t);
			service.putExtra(PlayerService.PLAYLIST_POSITION, position);
			this.startService(service);
			Intent i = new Intent(this, Player.class);
			this.startActivityForResult(i, PLAY_SONG);
		}
		else if(extras == null){
			Intent service = new Intent(this, PlayerService.class);
			service.setAction(PlayerService.ACTION_PLAY_ALLSONGS);
			service.putExtra(PlayerService.PLAYLIST_POSITION, position);
			this.startService(service);
			Intent i = new Intent(this, Player.class);
			this.startActivityForResult(i, PLAY_SONG);
			
		}
		else if(extras.containsKey(Media.ARTIST_ID)) {
			startPlayer(this, PlayerService.ACTION_PLAY_ARTIST, extras, Media.ARTIST_ID, position, id);
		}
		else if(extras.containsKey(Media.ALBUM_ID)) {
			startPlayer(this, PlayerService.ACTION_PLAY_ALBUM, extras, Media.ALBUM_ID, position, id);
		}
	}
	
	public static void startPlayer(Activity c, String action, Bundle extras, String key, int position, long id) {
		Intent service = new Intent(c, PlayerService.class);
		service.setAction(action);
		service.putExtra(key, extras.getLong(key));
		service.putExtra(PlayerService.PLAYLIST_POSITION, position);
		c.startService(service);
		Intent i = new Intent(c, Player.class);
		c.startActivityForResult(i, PLAY_SONG);
	}//TODO refactor can't have this copy paste BS
	
	public static void startPlayer(Activity c, int position, long id) {
		Intent service = new Intent(c, PlayerService.class);
		service.setAction(PlayerService.ACTION_CHANGE_TRACK);
		service.putExtra(PlayerService.PLAYLIST_POSITION, position);
		c.startService(service);
		Intent i = new Intent(c, Player.class);
		c.startActivityForResult(i, PLAY_SONG);
	}
}