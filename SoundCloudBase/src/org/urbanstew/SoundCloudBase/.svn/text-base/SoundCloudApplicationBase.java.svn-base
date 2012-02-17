package org.urbanstew.SoundCloudBase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.urbanstew.SoundCloudBase.SoundCloudAuthorizationClient.AuthorizationStatus;
import org.urbanstew.soundcloudapi.CountingOutputStream;
import org.urbanstew.soundcloudapi.SoundCloudAPI;
import org.urbanstew.SoundCloudBase.ViewTracksActivity;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class SoundCloudApplicationBase extends Application
{
	public final static boolean useSandbox = false;
	
	public void onCreate()
	{
		super.onCreate();

    	mSoundCloud = newSoundCloudRequest();
    	mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	SoundCloudAPI newSoundCloudRequest()
	{
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        // WARNING: the following resources are not a part of the repository for security reasons
        // to build and test this app, you should register your build of the app with SoundCloud:
        //  http://soundcloud.com/settings/applications/new
        // and add your Consumer Key and Consumer Secret as string resources to the project.
        // (with names "consumer_key" and "s5rmEGv9Rw7iulickCZl", respectively)
        String consumerKey, consumerSecret;
        if (!useSandbox)
        {
        	consumerKey = getResources().getString(R.string.consumer_key);
        	consumerSecret  = getResources().getString(R.string.s5rmEGv9Rw7iulickCZl);
        }
        else
        {
        	consumerKey = getResources().getString(R.string.sandbox_consumer_key);
        	consumerSecret  = getResources().getString(R.string.sandbox_consumer_secret);        	
        }

    	SoundCloudAPI soundCloud = new SoundCloudAPI
    	(
    		consumerKey,
    		consumerSecret,
    		preferences.getString("oauth_access_token", ""),
    		preferences.getString("oauth_access_token_secret", ""),
    		(useSandbox ? SoundCloudAPI.USE_SANDBOX : SoundCloudAPI.USE_PRODUCTION).with(SoundCloudAPI.OAuthVersion.V2_0)
    	);
    	    	
    	return soundCloud;
	}
	
	public enum RequestType
	{
		GET,
		GET_STREAM,
		GET_STREAM_REDIRECT,
		DELETE
	}
	
	public Thread processRequest(final String request, final SoundCloudRequestClient client)
	{
		return processRequest(request, client, RequestType.GET);
	}

	public Thread processRequest(final String request, final SoundCloudRequestClient client, final RequestType type)
	{
    	Thread thread = new Thread(new Runnable()
    	{
			public void run()
			{
		    	HttpResponse response = null;
				try
				{
					switch(type)
					{
					case GET:
						response = mSoundCloud.get(request);
						break;
					case GET_STREAM:
						response = mSoundCloud.getStream(request);
						break;
					case GET_STREAM_REDIRECT:
						response = mSoundCloud.getStreamRedirect(request);
						break;
					case DELETE:
						response = mSoundCloud.delete(request);
						break;
					}
				} catch (Exception e)
				{
					client.requestFailed(e);
				}
				if(response != null)
					client.requestCompleted(response);
				complete(client, Thread.currentThread());
			}
    	});
    	
    	launch(client, thread);
    	return thread;
	}

	public void authorizeWithoutCallback(final SoundCloudAuthorizationClient client)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				AuthorizationStatus status = AuthorizationStatus.FAILED;

				try
				{
					String url = mSoundCloud.obtainRequestToken("http://soundcloud.urbanstew.org/");
					client.openAuthorizationURL(url);
					String verificationCode = client.getVerificationCode();
					if(verificationCode != null)
					{
						mSoundCloud.obtainAccessToken(verificationCode);
						status = AuthorizationStatus.SUCCESSFUL;
						storeAuthorization();
					}
				} catch (Exception e)
				{
					client.exceptionOccurred(e);
				} finally
				{
					final AuthorizationStatus finalStatus = status;
					client.authorizationCompleted(finalStatus);
					complete(client, Thread.currentThread());
				}
			}
		});
		
		launch(client, thread);
	}

	public void authorize(final SoundCloudAuthorizationClient client, final String username, final String password)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				AuthorizationStatus status = AuthorizationStatus.FAILED;

				try
				{
					mSoundCloud.obtainAccessToken(username, password);
					if(mSoundCloud.getState() == SoundCloudAPI.State.AUTHORIZED)
					{
						status = AuthorizationStatus.SUCCESSFUL;
						storeAuthorization();
					}
				} catch (Exception e)
				{
					// failed
				} finally
				{
					final AuthorizationStatus finalStatus = status;
					client.authorizationCompleted(finalStatus);
					complete(client, Thread.currentThread());
				}
			}
		});
		
		launch(client, thread);
	}

	public String downloadStream(final String url, final String title)
	{
		final String filename = title.replaceAll(" ", "_").replaceAll("[^a-zA-Z0-9_]", "") + ".mp3";
		
		final Notification notification = new Notification(android.R.drawable.stat_sys_download,"Downloading " + title + "...",System.currentTimeMillis());
		
		final RemoteViews remoteView = new RemoteViews(this.getPackageName(), R.layout.progress);
        remoteView.setCharSequence(R.id.progressText, "setText", title + " - SoundCloud");
        remoteView.setImageViewResource(R.id.progress_icon,android.R.drawable.stat_sys_download);
        notification.contentView = remoteView;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        Intent notificationIntent = new Intent(getApplicationContext(), ViewTracksActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;  
                
		final int notificationId = mDownloadNotificationId++;		
		
		String downloadDirectory = getString(R.string.SCB_download_directory);
		final File newFile = new File(Environment.getExternalStorageDirectory() + downloadDirectory + "/" + filename);
		boolean fileCreated;

		File directory = new File(Environment.getExternalStorageDirectory() + downloadDirectory);
		if(!directory.mkdirs())
		{
			Log.e(SoundCloudApplicationBase.class.getName(), "Failed mkdirs: " + directory.getAbsolutePath());
		}
		newFile.delete();
		try
		{
			fileCreated = newFile.createNewFile();
			Log.d("file created: ", "" + fileCreated);
			final CountingOutputStream out = new CountingOutputStream (new FileOutputStream(newFile));
		
			Thread thread = new Thread(new Runnable()
	    	{
				public void run()
				{
			    	HttpResponse response = null;
			    	ProgressUpdater progress = null;
			    	boolean success = false;
					try
					{
						response = mSoundCloud.getStream(url);
	
						progress = new ProgressUpdater
						(
							remoteView,
							notification,
							new DownloadProgressable(out, response.getEntity().getContentLength()),
							notificationId
						);
	
						mHandler.postDelayed
						(
							progress,
							1000
						);
						
						Log.d("received stream", response.getStatusLine().getReasonPhrase());
	
						if(response.getStatusLine().getStatusCode()== 200)
						{
							try
							{
								response.getEntity().writeTo(out);
								success = true;
							} catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					
					if(progress != null)
					{
						progress.finish();
			    		String notificationString = title + " download " + (success ? "completed" : "failed");
			    		Notification notification = new Notification
			    		(
			    			success ? android.R.drawable.stat_sys_download_done : android.R.drawable.stat_notify_error,
			    			notificationString,
			    			System.currentTimeMillis()
			    		);
			    		Intent intent = new Intent(Intent.ACTION_VIEW);
			    		intent.setDataAndType(Uri.parse("file://" + newFile.getAbsolutePath()), "audio/mp3");
			    		notification.setLatestEventInfo
			    		(
			    			getApplicationContext(),
			    			getString(R.string.app_name),
			    			notificationString,
			    			PendingIntent.getActivity(getApplicationContext(), 0, intent, 0)
			    		);
			    		notification.flags |= Notification.FLAG_AUTO_CANCEL;
			    		mNotificationManager.notify(mDownloadNotificationId++, notification);

					}
				}
	    	});
			thread.start();
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}
		return newFile.getAbsolutePath();
	}
	protected void launch(Object object, Thread thread)
	{
		synchronized(mThreads)
		{
			Set<Thread> threads;
			if(mThreads.containsKey(object))
				threads = mThreads.get(object);
			else
				threads = new HashSet<Thread>();
			threads.add(thread);

			thread.start();
			Log.d(SoundCloudApplicationBase.class.getSimpleName(), "Starting SoundCloudService");
			startService(new Intent(this, SoundCloudService.class));
		}
	}
	
	protected void complete(Object object, Thread thread)
	{
		synchronized(mThreads)
		{
			if(mThreads.containsKey(object))
			{
				Set<Thread> threads = mThreads.get(object);
				threads.remove(thread);
				if(threads.size()==0)
					mThreads.remove(object);
			}
			if(mThreads.size()==0)
			{
				Log.d(SoundCloudApplicationBase.class.getSimpleName(), "Stopping SoundCloudService");
				stopService(new Intent(this, SoundCloudService.class));
			}
		}
	}
	
	public void cancel(Object object)
	{
		synchronized(mThreads)
		{
			if(mThreads.containsKey(object))
			{
				for(Thread thread : mThreads.get(object))
				{
					thread.interrupt();
					complete(object, thread);
				}
			}
		}
	}

	private void storeAuthorization()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	preferences.edit()
    	.putString("oauth_access_token", mSoundCloud.getToken())
    	.putString("oauth_access_token_secret", mSoundCloud.getTokenSecret())
    	.commit();
	}
    
    protected SoundCloudAPI mSoundCloud;
	protected NotificationManager mNotificationManager;
    
	public SoundCloudAPI getSoundCloudAPI()
	{
		return mSoundCloud;
	};
	
	int mDownloadNotificationId = Integer.MAX_VALUE / 2;
	Map<Object, Set<Thread>> mThreads = new HashMap<Object, Set<Thread>>();
	protected Handler mHandler = new Handler();
	
	protected class ProgressUpdater implements Runnable
    {
		public ProgressUpdater(RemoteViews remoteView, Notification notification, Progressable progressable, int notificationId)
    	{
    		mRemoteView = remoteView;
    		mNotification = notification;
    		mProgressable = progressable;
    		mContinue = true;
    		mId = notificationId;
    	}
    	
    	public void run()
		{
    		if(!mContinue)
    	    	return;

    		int percent = mProgressable.getProgress();
    		update(percent);
            
            mHandler.postDelayed
	    	(
	    		this,
	    		1000
	    	);
		}
    	
    	public void finish()
    	{
    		mContinue = false;
    		update(100);
    		mNotificationManager.cancel(mId);
    	}
    	
    	private void update(int percent)
    	{
            mRemoteView.setProgressBar(R.id.progressBar, 100, percent, false);
            mRemoteView.setCharSequence(R.id.progressPercentage, "setText", percent + "%");
            mNotificationManager.notify(mId, mNotification);
    	}
    	
    	RemoteViews mRemoteView;
    	Notification mNotification;
    	Progressable mProgressable;
    	boolean mContinue;
    	int mId;
    }
	
	public int processTracks(HttpResponse response, long class1)
	{
		return processTracks(response, class1, false);
	}
	
	public int processTracks(HttpResponse response, long class1, boolean update)
	{
		try
		{
			SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            TracksHandler handler = new TracksHandler(update, class1);

            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(handler);       
            xr.parse(new InputSource(response.getEntity().getContent()));
			
			return handler.getTracksProcessed();

/*			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document dom = db.parse(response.getEntity().getContent());
		       
			NodeList tracks = dom.getElementsByTagName("track");

			for(int i=0; i<tracks.getLength(); i++)
			{
				Node track = tracks.item(i);
				ContentValues values = new ContentValues();
				values.put(DB.Tracks.CLASS, class_);
				NodeList trackElements = track.getChildNodes();
				for(int j=0; j<trackElements.getLength(); j++)
				{
					Node item = trackElements.item(j);
					String value = item.getFirstChild() == null ? "" : item.getFirstChild().getNodeValue();
					if(item.getNodeName().equals("id"))
						values.put(DB.Tracks.ID, value);
					else if(item.getNodeName().equals("title"))
					{
						values.put(DB.Tracks.TITLE, value);
						Log.d(SoundCloudApplicationBase.class.getName(), "Track title set to:" + value + "..");
						//Log.d(SoundCloudApplicationBase.class.getName(), item.getFirstChild().getNodeValue());
					}
					else if(item.getNodeName().equals("stream-url"))
						values.put(DB.Tracks.STREAM_URL, value);
					else if(item.getNodeName().equals("duration"))
						values.put(DB.Tracks.DURATION, value);
				}
	    		boolean updateSucceeded = false;
		    	if(update)
		    	{
		    		Cursor c = getContentResolver().query(DB.Tracks.CONTENT_URI, sTracksIDProjection, DB.Tracks.ID + " = " + values.getAsString("id"), null, null);
		    		if(c.getCount()>0)
		    		{
		    			c.moveToFirst();
		    			Uri uri = ContentUris.withAppendedId(DB.Tracks.CONTENT_URI, c.getLong(0));
		    			Log.d(SoundCloudApplicationBase.class.getSimpleName(), "Updating track " + uri);
		    			getContentResolver().update(uri, values, null, null);
		    			updateSucceeded = true;
		    		}
		    		c.close();
		    	}
		    	if(!updateSucceeded) // || !update
		    		getContentResolver().insert(DB.Tracks.CONTENT_URI, values);

			}
			return tracks.getLength();
			
			*/
			
		}catch(Exception e) {
			e.printStackTrace();
			return -1;
		}		
	}
    
	static String[] sTracksIDProjection = new String[] {DB.Tracks._ID};
	
	
	class TracksHandler extends DefaultHandler
	{
		ContentValues values;
		int levelsUnderTrack = -1;
		StringBuilder mLastCharacters = new StringBuilder();
		int tracksProcessed=0;
		boolean update;
		long class_;

		TracksHandler(boolean update, long class1)
		{
			this.update = update;
			this.class_ = class1;
		}
		public int getTracksProcessed()
		{
			return tracksProcessed;
		}
		
	    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	    {
	        if (localName.equals("track"))
	        {
	        	if(levelsUnderTrack==-1)
	        	{
	        		levelsUnderTrack=0;
	            	values = new ContentValues();
					values.put(DB.Tracks.CLASS, class_);
	        	}
	        }
	        else if(levelsUnderTrack >= 0)
	        	levelsUnderTrack++;
	    
	        mLastCharacters.setLength(0);
	    }
	   
	    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	    {
	        if (localName.equals("track") && levelsUnderTrack == 0)
	        {
	        	tracksProcessed++;
	    		boolean updateSucceeded = false;
		    	if(update)
		    	{
		    		Cursor c = getContentResolver().query(DB.Tracks.CONTENT_URI, sTracksIDProjection, DB.Tracks.ID + " = " + values.getAsString("id"), null, null);
		    		if(c.getCount()>0)
		    		{
		    			c.moveToFirst();
		    			Uri uri = ContentUris.withAppendedId(DB.Tracks.CONTENT_URI, c.getLong(0));
		    			Log.d(SoundCloudApplicationBase.class.getSimpleName(), "Updating track " + uri);
		    			getContentResolver().update(uri, values, null, null);
		    			updateSucceeded = true;
		    		}
		    		c.close();
		    	}
		    	if(!updateSucceeded) // || !update
		    		getContentResolver().insert(DB.Tracks.CONTENT_URI, values);
	        } else if (levelsUnderTrack == 1)
	        {
	        	if(localName.equals("id"))
					values.put(DB.Tracks.ID, mLastCharacters.toString());
	        	else if(localName.equals("title"))
					values.put(DB.Tracks.TITLE, mLastCharacters.toString());
				else if(localName.equals("stream-url"))
					values.put(DB.Tracks.STREAM_URL, mLastCharacters.toString());
				else if(localName.equals("duration"))
					values.put(DB.Tracks.DURATION, mLastCharacters.toString());
	        }
	        if(levelsUnderTrack>=0)
	        	levelsUnderTrack--;
	    }
	   
	    public void characters(char ch[], int start, int length)
	    {
	        if(levelsUnderTrack == 1)
	        	mLastCharacters.append(ch, start, length);
	    }
	}

}

class DownloadProgressable implements Progressable
{
	DownloadProgressable(CountingOutputStream out, long contentLength)
	{
		mOutputStream = out;
		mContentLength = contentLength;
	}
	
	public int getProgress()
	{
		return (int) (mOutputStream.getCount() * 100 / mContentLength);
	}	
	
	CountingOutputStream mOutputStream;
	long mContentLength;
}