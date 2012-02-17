package org.urbanstew.soundclouddroid;

import org.urbanstew.soundclouddroid.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class SoundCloudData extends org.urbanstew.SoundCloudBase.SoundCloudData
{
	@Override
	public boolean onCreate()
	{
		// Access the database.
		mOpenHelper = new DatabaseHelper(getContext());
		
		return true;
	}

	protected static class DatabaseHelper extends org.urbanstew.SoundCloudBase.SoundCloudData.DatabaseHelper
	{

		DatabaseHelper(Context context)
		{
			super(context);
		}
	
		public void onCreate(SQLiteDatabase db)
		{
			super.onCreate(db);
			createTrackListsTable(db);
		}

		void createTrackListsTable(SQLiteDatabase db)
		{
			db.execSQL("CREATE TABLE " + DB.TrackLists.TABLE_NAME + "("
					+ DB.TrackLists._ID + " INTEGER PRIMARY KEY,"
					+ DB.TrackLists.RESOURCE + " TEXT,"
					+ DB.TrackLists.TITLE + " TEXT"
					+ ");");
			
			ContentValues values = new ContentValues();
			
			values.put(DB.TrackLists._ID, 0);
			values.put(DB.TrackLists.RESOURCE, "me/tracks");
			values.put(DB.TrackLists.TITLE, "Play/Download/Upload My Tracks");
			db.insert(DB.TrackLists.TABLE_NAME, DB.TrackLists.RESOURCE, values);

			values.put(DB.TrackLists._ID, 1);
			values.put(DB.TrackLists.RESOURCE, "me/favorites");
			values.put(DB.TrackLists.TITLE, "My Favorites");
			db.insert(DB.TrackLists.TABLE_NAME, DB.TrackLists.RESOURCE, values);

			values.put(DB.TrackLists._ID, 99);
			values.put(DB.TrackLists.RESOURCE, "");
			values.put(DB.TrackLists.TITLE, "Last Viewed");
			db.insert(DB.TrackLists.TABLE_NAME, DB.TrackLists.RESOURCE, values);		
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
			super.onUpgrade(db, oldVersion, newVersion);

            switch(oldVersion)
            {
            case 13:
            	createTrackListsTable(db);
            	break;
            }
        }
	}
	
    private static final int TRACKLISTS = 5;
    private static final int TRACKLIST_ID = 6;

    static {
        sUriMatcher.addURI(DB.AUTHORITY, "tracklists", TRACKLISTS);
        sUriMatcher.addURI(DB.AUTHORITY, "tracklists/#", TRACKLIST_ID);
        sTableList.add(new TableListEntry(DB.TrackLists.TABLE_NAME, DB.TrackLists.CONTENT_URI, DB.TrackLists.DEFAULT_SORT_ORDER, DB.TrackLists.TITLE, false));
        sTableList.add(new TableListEntry(DB.TrackLists.TABLE_NAME, DB.TrackLists.CONTENT_URI, DB.TrackLists.DEFAULT_SORT_ORDER, DB.TrackLists.TITLE, true));
    }

	@Override
	public String getType(Uri uri)
	{
        switch (sUriMatcher.match(uri)) {
        case TRACKLISTS:
            return DB.TrackLists.CONTENT_TYPE;

        case TRACKLIST_ID:
            return DB.TrackLists.CONTENT_ITEM_TYPE;

        default:
            return super.getType(uri);
        }
	}


	
}


