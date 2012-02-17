package org.urbanstew.SoundCloudBase;

import org.urbanstew.android.util.ForegroundService;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;

public class SoundCloudService extends ForegroundService
{
	public void onStart (Intent intent, int startId)
	{
		super.onStart(intent, startId);

		startForeground();
	}
	public void onDestroy ()
	{
		super.onDestroy();
	}
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	protected Notification newNotification()
	{
		Notification notification = new Notification
		(
			android.R.drawable.stat_notify_sync,
			"",
			System.currentTimeMillis()
		);			
		notification.setLatestEventInfo
		(
			getApplicationContext(),
			getString(R.string.app_name),
			"Communicating with SoundCloud...",
			PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0)
		);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		return notification;
	}
}

