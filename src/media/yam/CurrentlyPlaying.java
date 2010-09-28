package media.yam;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public class CurrentlyPlaying extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		long[] playlist1 = getIntent().getExtras().getLongArray(Player.PLAYLIST);
		Long[] playlist = new Long[playlist1.length];
		for(int i = 0; i < playlist1.length; i++) {
			playlist[i] = playlist1[i];
		}
		ArrayAdapter<Long> adapter = new ArrayAdapter<Long>(this, R.layout.song, playlist);
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
