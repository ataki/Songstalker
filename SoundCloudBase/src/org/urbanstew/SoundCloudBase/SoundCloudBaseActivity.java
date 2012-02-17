package org.urbanstew.SoundCloudBase;

import org.urbanstew.soundcloudapi.SoundCloudAPI;

import android.app.Activity;

public class SoundCloudBaseActivity extends Activity
{
	public SoundCloudAPI getSoundCloudAPI()
	{
		return ((SoundCloudApplicationBase)getApplication()).getSoundCloudAPI();
	}

	public SoundCloudApplicationBase getSCApplicationBase()
	{
		return (SoundCloudApplicationBase)getApplication();
	}
}
