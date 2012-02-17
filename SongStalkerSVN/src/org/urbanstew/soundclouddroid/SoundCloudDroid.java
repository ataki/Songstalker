package org.urbanstew.soundclouddroid;


import org.urbanstew.SoundCloudBase.R;
import org.urbanstew.SoundCloudBase.SoundCloudMainActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * SoundCloudDroid is the main SoundCloud Droid activity.
 * <p>
 * It shows
 * whether SoundCloud Droid has been authorized to access a user
 * account, can initiate the authorization process, and can upload
 * a file to SoundCloud.
 * 
 * @author      Stjepan Rajko
 */
public class SoundCloudDroid extends SoundCloudMainActivity
{
	static
	{
		CURRENT_VERSION = 1.0f;
	}

	/**
     * The method called when the Activity is created.
     * <p>
     * Initializes the user interface.
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        openedTrackListsAutomatically = false;
        
        ((Button) findViewById(R.id.custom_queries_button))
    	.setOnClickListener(new OnClickListener()
    	{
			public void onClick(View arg0)
			{
				startActivity(new Intent(getApplication(), CustomTrackListsActivity.class));					
			}
    	});

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	if(preferences.getBoolean("check_old_version", true))
    	{
	        try
	        {
	        	getPackageManager().getPackageInfo("org.urbanstew.SoundCloudDroid", 0);
	        	
	        	new AlertDialog.Builder(this)
	        	.setTitle("Package Name Changed")
	        	.setMessage("For reasons beyond our control, we had to change the package name for version 1.0 of SoundCloud Droid.\n\nThis means you have to manually uninstall your old version of SoundCloud Droid. Select OK to do so.")
	        	.setPositiveButton
	        	(
	        		getString(android.R.string.ok),
	        		new DialogInterface.OnClickListener()
	        		{
						public void onClick(DialogInterface dialog, int which)
						{
						    Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", "org.urbanstew.SoundCloudDroid", null)); 
						    startActivity(intent);
		    		    	preferences.edit().putBoolean("check_old_version", false).commit();
						}
	        		}
	        	).show();
	        } catch(PackageManager.NameNotFoundException e)
	        {
		    	preferences.edit().putBoolean("check_old_version", false).commit();
	        }
    	}
    }
    
	public void setUserName(String userName)
	{
		super.setUserName(userName);
		if(userName != null && !openedTrackListsAutomatically)
		{
			startActivity(new Intent(getApplication(), TabMenuActivity.class));
			openedTrackListsAutomatically = true;
		}
	}
	
	boolean openedTrackListsAutomatically;
	
}


