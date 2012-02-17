package org.urbanstew.soundclouddroid;

import org.urbanstew.soundclouddroid.DB;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class CustomTrackListsActivity extends ListActivity
{
	/**
     * The method called when the Activity is created.
     * <p>
     * Initializes the user interface.
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.custom_track_lists);
        
        // Read uploads
        mCursor = getContentResolver().query(DB.TrackLists.CONTENT_URI, null, DB.TrackLists.RESOURCE + "<>\"\"", null, null);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.track_list_item, mCursor,
                new String[] { DB.TrackLists.TITLE }, new int[] { android.R.id.text1 });
        setListAdapter(adapter);

        findViewById(R.id.use_menu).setVisibility(mCursor.getCount()<=2 ? View.VISIBLE : View.INVISIBLE);
    }
    
    public void onDestroy()
    {
    	super.onDestroy();
    	mCursor.close();
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        
        mArtistTracksMenuItem = menu.add("Search for Artists").setIcon(android.R.drawable.ic_menu_add);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	if(item == mArtistTracksMenuItem)
    	{
    		startActivity(new Intent(getApplication(), NewArtistTracksActivity.class));
    		return true;
    	}
    	else return super.onOptionsItemSelected(item);
    }
    
	public void onListItemClick(ListView parent, View v, int position, long id)
	{
		mCursor.moveToPosition(position);
		Intent intent;
		if(id == 0)
			intent = new Intent(getApplication(), ViewTracksActivity.class);
		else
		{
			intent = new Intent(getApplication(), ViewOtherTracksActivity.class);
			intent.putExtra(DB.TrackLists.RESOURCE, mCursor.getString(1));
			intent.putExtra(DB.TrackLists.TITLE, mCursor.getString(2));
			intent.putExtra(DB.TrackLists._ID, id);
		}
		
		startActivity(intent);
	}
    
	private MenuItem mArtistTracksMenuItem;
	private Cursor mCursor;


}
