/*
 *  Author:
 *      Stjepan Rajko
 *      urbanSTEW
 *
 *  Copyright 2008,2009 Stjepan Rajko.
 *
 *  This file is part of the Android version of Rehearsal Assistant.
 *
 *  Rehearsal Assistant is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the License,
 *  or (at your option) any later version.
 *
 *  Rehearsal Assistant is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rehearsal Assistant.
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package org.urbanstew.SoundCloudBase;

import android.net.Uri;
import android.provider.BaseColumns;
import org.urbanstew.SoundCloudBase.Config;

public class DB {
	public static final String AUTHORITY = Config.AUTHORITY;
	public static final String PACKAGE = Config.PACKAGE;

	// This class cannot be instantiated
	protected DB() {
	}

	public static final class Uploads implements BaseColumns {
		// This class cannot be instantiated
		private Uploads() {
		}

		public static final String TABLE_NAME = "uploads";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/uploads");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of app
		 * data entries.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"
				+ PACKAGE + ".uploads";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single app
		 * data entry.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/"
				+ PACKAGE + ".uploads";

		public static final String TITLE = "title";
		public static final String PATH = "path";
		public static final String STATUS = "status";
		public static final String SHARING = "sharing";
		public static final String DESCRIPTION = "description";
		public static final String GENRE = "genre";
		public static final String TRACK_TYPE = "track_type";

		public static final String DEFAULT_SORT_ORDER = _ID + " DESC";
	}

	public static final class Tracks implements BaseColumns {
		// This class cannot be instantiated
		private Tracks() {
		}

		public static final String TABLE_NAME = "tracks";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/tracks");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of app
		 * data entries.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"
				+ PACKAGE + ".tracks";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single app
		 * data entry.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/"
				+ PACKAGE + ".tracks";

		public static final String TITLE = "title";
		public static final String ID = "id";
		public static final String STREAM_URL = "stream_url";
		public static final String DURATION = "duration";
		public static final String CLASS = "class";

		public static final String DEFAULT_SORT_ORDER = ID + " DESC";
	}
}
