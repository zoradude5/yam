package media.yam;

import media.yam.MediaDB.SongInfo;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public class CurrentlyPlaying extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		long[] playlist1 = getIntent().getExtras().getLongArray(Player.PLAYLIST);
		SongInfo[] playlist = new SongInfo[playlist1.length];
		for(int i = 0; i < playlist1.length; i++) {
			playlist[i] = MediaDB.getSong(getContentResolver(), playlist1[i]);
		}
		//ArrayAdapter<Long> adapter = new ArrayAdapter<Long>(this, R.layout.song, playlist);// {
		ArrayAdapter<SongInfo> adapter = new ArrayAdapter<SongInfo>(this, R.layout.song, playlist);// {
			/*public View getView(int position, View convertView, ViewGroup parent) {
				
				return super.getView(position, convertView, parent);
			}*/
		//};
		setListAdapter(adapter);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				startActivityForResult(new Intent(CurrentlyPlaying.this, Player.class)
					.putExtra(PlayerService.PLAYLIST_POSITION, position), 0);
			}
		});
	}
}
