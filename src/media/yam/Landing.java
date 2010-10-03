package media.yam;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class Landing extends TabActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landing);
		
		Resources res = getResources();
		TabHost tabHost = getTabHost();	
		Intent intent;
		TabHost.TabSpec spec;
		
		

		intent = new Intent(this, ArtistList.class);
		spec = tabHost.newTabSpec("artists").setIndicator("Artists",
                res.getDrawable(R.drawable.play))
            .setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent(this, AlbumList.class);
		spec = tabHost.newTabSpec("albums").setIndicator("Albums",
                res.getDrawable(R.drawable.next))
            .setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent(this, SongList.class);
		spec = tabHost.newTabSpec("songs").setIndicator("Songs",
                res.getDrawable(R.drawable.library))
            .setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
	
	}
}
