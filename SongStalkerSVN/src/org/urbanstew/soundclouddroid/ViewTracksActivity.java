package org.urbanstew.soundclouddroid;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class ViewTracksActivity extends org.urbanstew.SoundCloudBase.ViewTracksActivity
{
    public boolean onCreateOptionsMenu(Menu menu)
    {
		super.setQueryAndClass("me/tracks", 0);
        super.onCreateOptionsMenu(menu);
        
        mUploadMenuItem = menu.add("Upload").setIcon(android.R.drawable.ic_menu_share);
        mUploadStatusMenuItem = menu.add("Upload Status").setIcon(android.R.drawable.ic_menu_upload);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	if(item == mUploadMenuItem)
    	{
    		startActivity(new Intent(getApplication(), UploadActivity.class));
    		return true;
    	}
    	else if(item == mUploadStatusMenuItem)
    	{
    		startActivity(new Intent(getApplication(), UploadsActivity.class));
    		return true;    		
    	}
    	else return super.onOptionsItemSelected(item);
    }
    
	private MenuItem mUploadMenuItem, mUploadStatusMenuItem;

}
