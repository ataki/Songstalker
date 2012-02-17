package org.urbanstew.soundclouddroid;

import android.net.Uri;
import android.provider.BaseColumns;

public class DB extends org.urbanstew.SoundCloudBase.DB
{
	protected DB()
	{
		super();
	}
	
    public static final class TrackLists implements BaseColumns
    {
        // This class cannot be instantiated
        private TrackLists() {}

        public static final String TABLE_NAME = "tracklists";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/tracklists");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of entries.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + PACKAGE + ".tracklists";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + PACKAGE + ".tracklists";

        public static final String RESOURCE = "resource";
        
        public static final String TITLE = "title";
        
        public static final String DEFAULT_SORT_ORDER = _ID + " ASC";
    }
}
