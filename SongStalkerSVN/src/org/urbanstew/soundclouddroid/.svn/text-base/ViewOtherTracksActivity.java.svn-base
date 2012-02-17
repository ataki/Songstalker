package org.urbanstew.soundclouddroid;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ViewOtherTracksActivity extends org.urbanstew.SoundCloudBase.ViewTracksActivity
{
	public void onCreate(Bundle savedInstanceState)
	{
		String title = getIntent().getStringExtra(DB.TrackLists.TITLE);
		setTitle(title);
		String resource = getIntent().getStringExtra(DB.TrackLists.RESOURCE);
		long _class = getIntent().getLongExtra(DB.TrackLists._ID, 1);
		
		Log.d(ViewOtherTracksActivity.class.getSimpleName(), "Using resource " + resource + " and class " + _class);
		super.setQueryAndClass(resource, _class);
		super.onCreate(savedInstanceState);
		if(_class == 99)
		{
			if(!super.requestIssued())
				super.requestOffset(0);
			Toast.makeText(this, "Use the MENU to save this to your Track Lists", Toast.LENGTH_SHORT).show();
		}
	}
	
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        if(this.getQueryClass() == 99)
        {
        	mSaveMenuItem = menu.add("Save to Track Lists").setIcon(android.R.drawable.ic_menu_save);
        }
        else if(this.getQueryClass() >= 100)
        	mDeleteMenuItem = menu.add("Remove from Track Lists").setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }
    
    /**
     * Processes menu options.
     */
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	if(!super.onOptionsItemSelected(item))
    	{
    		if(item == mSaveMenuItem)
    		{
    			ContentValues values = new ContentValues();
    			
    			values.put(DB.TrackLists.RESOURCE, this.getQuery());
    			values.put(DB.TrackLists.TITLE, this.getTitle().toString());
    			
	    		Uri uri = getContentResolver().insert(DB.TrackLists.CONTENT_URI, values);
	    		
	    		if(uri != null)
	    			Toast.makeText(this, "You can now access this artist's tracks from your Tracks List", Toast.LENGTH_SHORT).show();
    		}
    		else if(item == mDeleteMenuItem)
    		{
    			getContentResolver().delete(ContentUris.withAppendedId(DB.TrackLists.CONTENT_URI, this.getQueryClass()), null, null);
    			getContentResolver().delete(org.urbanstew.SoundCloudBase.DB.Tracks.CONTENT_URI, org.urbanstew.SoundCloudBase.DB.Tracks.CLASS + " = '" + this.getQueryClass() + "'", null);
    			finish();
    		}
    		else
    			return false;
    	}
    	return true;
    }
    
    MenuItem mSaveMenuItem, mDeleteMenuItem;
}
