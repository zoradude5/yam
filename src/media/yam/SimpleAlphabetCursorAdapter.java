package media.yam;

import android.content.Context;
import android.database.Cursor;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;

public class SimpleAlphabetCursorAdapter extends SimpleCursorAdapter implements SectionIndexer {
	private AlphabetIndexer indexer;

	public SimpleAlphabetCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int sortedColumnIndex) {
		super(context, layout, c, from, to);
		indexer = new AlphabetIndexer(c, sortedColumnIndex, " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	}

	@Override
	public int getPositionForSection(int arg0) {
		return indexer.getPositionForSection(arg0);
	}

	@Override
	public int getSectionForPosition(int position) {
		return indexer.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		return indexer.getSections();
	}

}
