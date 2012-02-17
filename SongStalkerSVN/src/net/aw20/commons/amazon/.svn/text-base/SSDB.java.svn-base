package net.aw20.commons.amazon;

import android.location.Location;
import android.text.format.Time;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SSDB {
	
	public static SimpleDB sdb = new SimpleDB("AKIAIRKFCPTKLWAVFDKA", "K15TPx7iolYLWwl6bOg/bNCHeUbt7nHIOzCNvaNq");			
	public static double MAX_TIMEOUT = 600; // seconds
/**
 * 
 * @param soundCloudID
 * @param latitude
 * @param longitude
 * @return true if successful, false if error
 * Automatically updates if entry is already in the table
 */
	
	public static boolean updateLocation(int soundCloudID, double latitude, double longitude) {
		Map<String, String> map = new HashMap<String, String>();
		Set<String> replace = new HashSet<String>();
		replace.add("latitude");
		replace.add("longitude");
		replace.add("time");
		map.put("latitude", Double.toString(latitude));
		map.put("longitude", Double.toString(longitude));
		map.put("time", Double.toString(System.currentTimeMillis()));
		try {
			sdb.putAttributes("location", Integer.toString(soundCloudID), map, replace);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
/**
 * 
 * @param myID
 * @param theirID
 * @return returns distance in meters
 */
	public static float getDistance(int myID, int theirID) {
		double myLatitude, myLongitude, theirLatitude, theirLongitude;
		List<HashMap> res = null;
		
		try {
			res = sdb.select("select * from location where itemName() ='" + Integer.toString(myID) + "'");
			myLatitude = Double.parseDouble(((String[]) res.get(0).get("latitude"))[0]);
			myLongitude = Double.parseDouble(((String[]) res.get(0).get("longitude"))[0]);

			res = sdb.select("select * from location where itemName() ='" + Integer.toString(theirID) + "'");
			theirLatitude = Double.parseDouble(((String[]) res.get(0).get("latitude"))[0]);
			theirLongitude = Double.parseDouble(((String[]) res.get(0).get("longitude"))[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
		float[] results = new float[3];
		Location.distanceBetween(myLatitude, myLongitude, theirLatitude, theirLongitude, results);
		return results[0];
	}
	
	/**
	 * 
	 * @param userID
	 * @return time in miliseconds when the user last logged in
	 */
	public static double getLastCheckIn(int userID) {
		double time;
		List<HashMap> res = null;
		
		try {
			res = sdb.select("select * from location where itemName() ='" + Integer.toString(userID) + "'");
			time = Double.parseDouble(((String[]) res.get(0).get("time"))[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		return time;
	}
	
	/**
	 * 
	 * @param userID
	 * @return true or false depending on whether the user logged in within MAX_TIMEOUT seconds
	 */
	
	public static boolean isLoggedIn(int userID) {
		double lastLogIn = getLastCheckIn(userID);
		double now = System.currentTimeMillis();
		return (now - lastLogIn) / 1000 < MAX_TIMEOUT;
	}
	
	public static boolean setDefaultSong(int soundCloudID, int songID) {
		Map<String, String> map = new HashMap<String, String>();
		Set<String> replace = new HashSet<String>();
		replace.add("songID");
		map.put("songID", Integer.toString(songID));
		try {
			sdb.putAttributes("defaultSong", Integer.toString(soundCloudID), map, replace);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}	
	
	public static int getDefaultSong(int soundCloudID) {
		int songID;
		List<HashMap> res = null;
		
		try {
			res = sdb.select("select * from defaultSong where itemName() ='" + Integer.toString(soundCloudID) + "'");
			songID = Integer.parseInt(((String[]) res.get(0).get("songID"))[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
		return songID;
	}
}
