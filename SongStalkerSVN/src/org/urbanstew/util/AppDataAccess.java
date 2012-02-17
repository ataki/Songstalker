package org.urbanstew.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppDataAccess {
	public AppDataAccess(Context context) {
		mContext = context;
	}

	public float getVisitedVersion() {
		return getVisitedVersion("");
	}

	public void setVisitedVersion(float version) {
		setVisitedVersion("", version);
	}

	public float getVisitedVersion(String what) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		return preferences.getFloat("app_visited_version" + what, 0);
	}

	public void setVisitedVersion(String what, float version) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		preferences.edit().putFloat("app_visited_version" + what, version)
				.commit();
	}

	public boolean lastVisitedVersionOlderThan(float thanVersion,
			float currentVersion) {
		return lastVisitedVersionOlderThan("", thanVersion, currentVersion);
	}

	public boolean lastVisitedVersionOlderThan(String what, float thanVersion,
			float currentVersion) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		float oldVersion = preferences
				.getFloat("app_visited_version" + what, 0);
		preferences.edit()
				.putFloat("app_visited_version" + what, currentVersion)
				.commit();
		return oldVersion < thanVersion;
	}

	Context mContext;
}
