package media.yam;

import java.io.File;
import java.util.Arrays;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SongList extends ListActivity {
	private static final int PLAY_SONG = 1;
	
	public static final String KEY_PATH = "path";



	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ArrayAdapter<File> adapter = new ArrayAdapter<File>(this, R.layout.song, Arrays.asList(new File("/sdcard/download").listFiles()));
        setListAdapter(adapter);
        
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
        	  launchPlayer(((TextView)view).getText().toString());
          }
        });
    }
    


	private void launchPlayer(String path) {
		Intent i = new Intent(this, Player.class);
		i.putExtra(KEY_PATH, path);
		startActivityForResult(i, PLAY_SONG);
	}
}