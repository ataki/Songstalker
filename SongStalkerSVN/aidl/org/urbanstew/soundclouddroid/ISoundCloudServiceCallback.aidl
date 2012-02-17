package org.urbanstew.soundclouddroid;

interface ISoundCloudServiceCallback
{
	void openAuthorizationUrl(String url);
	void authorizationCompleted(boolean success);
	void meCompleted(String username);
}