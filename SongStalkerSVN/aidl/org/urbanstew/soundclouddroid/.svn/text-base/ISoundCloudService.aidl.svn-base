package org.urbanstew.soundclouddroid;

import org.urbanstew.soundclouddroid.ISoundCloudServiceCallback;

interface ISoundCloudService
{
	void registerCallback(ISoundCloudServiceCallback callback);
	void unregisterCallback(ISoundCloudServiceCallback callback);
	
	void getUserName();
	int getState();
	String obtainRequestToken();
	void obtainAccessToken(String verificationCode);
	void authorize();
}