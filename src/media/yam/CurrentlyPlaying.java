package media.yam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import media.yam.MediaDB.SongInfo;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CurrentlyPlaying extends ListActivity {
	private SongInfo[] playlist;
	private ArrayAdapter<SongInfo> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.current);
		long[] playlist1 = getIntent().getExtras().getLongArray(Player.PLAYLIST);
		int position = getIntent().getExtras().getInt(PlayerService.PLAYLIST_POSITION);
		playlist = new SongInfo[playlist1.length];
		for(int i = 0; i < playlist1.length; i++) {
			playlist[i] = MediaDB.getSong(getContentResolver(), playlist1[i]);
		}
		
		final TouchInterceptor t = (TouchInterceptor) getListView();
		t.setDropListener(new TouchInterceptor.DropListener() {
			@Override
			public void drop(int from, int to) {
				ArrayList<SongInfo> l = new ArrayList<SongInfo>();
				l.addAll(Arrays.asList(playlist));
				SongInfo si = l.remove(from);
				l.add(to,si);
				playlist = l.toArray(playlist);
				adapter.notifyDataSetChanged();
				t.invalidateViews();
			}
		});
		
		//this is pretty useless right now. SongInfo has a toString() that will work for us
		// but I got this to work, so I'd like to use it for other things if we need that
		adapter = new ArrayAdapter<SongInfo>(this, R.layout.album, playlist) {
			public View getView(int position, View convertView, ViewGroup parent) {
				//convertView = super.getView(position, convertView, parent);
				convertView = CurrentlyPlaying.this.getLayoutInflater().inflate(R.layout.album,null);
				((TextView) convertView.findViewById(R.id.song_title)).setText(playlist[position].title);
				return convertView;
			}
		};
		setListAdapter(adapter);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				SongList.startPlayer(CurrentlyPlaying.this, position, playlist[position].id);
			}
		});
		setSelection(position);
	}
}
