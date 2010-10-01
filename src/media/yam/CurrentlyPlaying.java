package media.yam;

import media.yam.MediaDB.SongInfo;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CurrentlyPlaying extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		long[] playlist1 = getIntent().getExtras().getLongArray(Player.PLAYLIST);
		int position = getIntent().getExtras().getInt(PlayerService.PLAYLIST_POSITION);
		final SongInfo[] playlist = new SongInfo[playlist1.length];
		for(int i = 0; i < playlist1.length; i++) {
			playlist[i] = MediaDB.getSong(getContentResolver(), playlist1[i]);
		}
		
		//this is pretty useless right now. SongInfo has a toString() that will work for us
		// but I got this to work, so I'd like to use it for other things if we need that
		ArrayAdapter<SongInfo> adapter = new ArrayAdapter<SongInfo>(this, R.layout.song, playlist) {
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView = super.getView(position, convertView, parent);
				((TextView) convertView).setText(playlist[position].title);
				return convertView;
			}
		};
		setListAdapter(adapter);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				startActivityForResult(new Intent(CurrentlyPlaying.this, Player.class)
					.putExtra(PlayerService.PLAYLIST_POSITION, position), 0);
			}
		});
		setSelection(position);
	}
}
