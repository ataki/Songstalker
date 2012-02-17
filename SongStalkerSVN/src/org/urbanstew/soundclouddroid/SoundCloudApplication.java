package org.urbanstew.soundclouddroid;

import java.io.File;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.urbanstew.SoundCloudBase.DB;
import org.urbanstew.SoundCloudBase.Progressable;
import org.urbanstew.SoundCloudBase.SoundCloudApplicationBase;
import org.urbanstew.SoundCloudBase.SoundCloudRequestClient;
import org.urbanstew.SoundCloudBase.ViewTracksActivity;
import org.urbanstew.soundcloudapi.ProgressFileBody;
import org.urbanstew.soundcloudapi.SoundCloudAPI;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class SoundCloudApplication extends SoundCloudApplicationBase
{
	
	public void uploadFile(Uri uri, final Bundle extras, final SoundCloudRequestClient client)
	{
		Log.d(getClass().getName(), "Uploading file data:" + uri.getPath());
		
    	final File file = new File(uri.getPath());
    	final ProgressFileBody fileBody = new ProgressFileBody(file);
    	
    	Log.d(getClass().getName(), "Uploading file:" + file.getAbsolutePath());

		if(!extras.containsKey("title"))
			extras.putString("title", "Uploaded from SoundCloud Droid service");

		boolean authorized = (getSoundCloudAPI().getState() == SoundCloudAPI.State.AUTHORIZED);
		
    	ContentValues values = new ContentValues();
    	final String title = extras.getString("title");
    	values.put(DB.Uploads.TITLE, title);
    	values.put(DB.Uploads.STATUS, authorized ? "uploading" : "unauthorized");
    	values.put(DB.Uploads.PATH, file.getAbsolutePath());
    	if(extras.containsKey("sharing"))
    		values.put(DB.Uploads.SHARING, extras.getString("sharing"));
    	if(extras.containsKey("description"))
    		values.put(DB.Uploads.DESCRIPTION, extras.getString("description"));
    	if(extras.containsKey("genre"))
    		values.put(DB.Uploads.GENRE, extras.getString("genre"));    	
    	if(extras.containsKey("track_type"))
    		values.put(DB.Uploads.TRACK_TYPE, extras.getString("track_type"));

    	// insert the values
    	final Uri upload = getContentResolver().insert(DB.Uploads.CONTENT_URI, values);
    	if(!authorized)
    	{
    		Toast.makeText(this, R.string.unauthorized_upload, Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	final int notificationId = Integer.parseInt(upload.getLastPathSegment()) * 2;

    	final SoundCloudAPI request = new SoundCloudAPI(mSoundCloud);
    	
    	//NotificationManager 
        Notification notification = new Notification(android.R.drawable.stat_sys_upload,"Uploading " + title + "...",System.currentTimeMillis());        
         
        RemoteViews remoteView = new RemoteViews(this.getPackageName(), R.layout.progress);
        remoteView.setCharSequence(R.id.progressText, "setText", title + " - SoundCloud");
        notification.contentView = remoteView;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        
        Intent notificationIntent = new Intent(getApplicationContext(), UploadsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;    
               
        mNotificationManager.notify(notificationId, notification); 

		final ProgressUpdater progress = new ProgressUpdater
		(
			remoteView,
			notification,
			new UploadProgressable(fileBody),
			notificationId
		);
		
		mHandler.postDelayed
		(
			progress,
			1000
		);
    	
    	Thread thread = new Thread(new Runnable()
    	{
			public void run()
			{
				String failureString = "";
				
				List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();

				for (String key : extras.keySet())
					params.add(new BasicNameValuePair("track["+key+"]", extras.getString(key)));
				boolean success = false;
	    		try
				{
	    			HttpResponse response = request.upload(fileBody, params);
					if(response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
					{
						processTracks(response, 0);
						success = true;
					}
					else
					{
						Log.e(SoundCloudApplication.class.getName(), "Unexpected response on upload: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
						failureString = ". SoundCloud response: " + response.getStatusLine().getReasonPhrase();
					}
				} catch (Exception e)
				{
					Log.e(SoundCloudApplication.class.getName(), "Exception on upload: " + e.toString());
					failureString = ". Exception: " + e.toString();
				}
				progress.finish();
				
	    		ContentValues values = new ContentValues();
	        	values.put(DB.Uploads.STATUS, success ? "uploaded" : "failed");
	    		getContentResolver().update(upload, values, null, null);
	    		
	    		String notificationString = title + " upload " + (success ? "completed" : "failed" + failureString);
	    		Notification notification = new Notification
	    		(
	    			success ? android.R.drawable.stat_sys_upload_done : android.R.drawable.stat_notify_error,
	    			notificationString,
	    			System.currentTimeMillis()
	    		);			
	    		notification.setLatestEventInfo
	    		(
	    			getApplicationContext(),
	    			"SoundCloud Droid",
	    			notificationString,
	    			PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), success ? ViewTracksActivity.class : UploadsActivity.class), 0)
	    		);
	    		notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    		mNotificationManager.notify(notificationId+1, notification);
	    		
				complete(client, Thread.currentThread());
			}
    	});
    	
    	launch(client, thread);
	}

}

class UploadProgressable implements Progressable
{
	UploadProgressable(ProgressFileBody fileBody)
	{
		mFileBody = fileBody;
	}
	
	public int getProgress()
	{
		return (int) (mFileBody.getBytesTransferred() * 100 / mFileBody.getContentLength());
	}	
	
	ProgressFileBody mFileBody;
}