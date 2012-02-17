package org.urbanstew.SoundCloudBase;

import java.util.Vector;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class SoundCloudData extends ContentProvider {

	@Override
	public boolean onCreate() {
		// Access the database.
		mOpenHelper = new DatabaseHelper(getContext());

		return true;
	}

	enum Uploads {
		_ID, TITLE, PATH, SHARING, DESCRIPTION, GENRE, TRACK_TYPE
	}

	protected static class DatabaseHelper extends SQLiteOpenHelper {
		static int currentVersion = 14;

		protected DatabaseHelper(Context context) {
			super(context, "soundcloud_droid.db", null, currentVersion);
			mContext = context;
		}

		public void onCreate(SQLiteDatabase db) {
			createUploadedFilesTable(db);
			createTracksTable(db);
		}

		void createUploadedFilesTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + DB.Uploads.TABLE_NAME + "("
					+ DB.Uploads._ID + " INTEGER PRIMARY KEY,"
					+ DB.Uploads.TITLE + " TEXT," + DB.Uploads.PATH + " TEXT,"
					+ DB.Uploads.STATUS + " TEXT," + DB.Uploads.SHARING
					+ " TEXT," + DB.Uploads.DESCRIPTION + " TEXT,"
					+ DB.Uploads.GENRE + " TEXT," + DB.Uploads.TRACK_TYPE
					+ " TEXT" + ");");
		}

		void createTracksTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + DB.Tracks.TABLE_NAME + "("
					+ DB.Tracks._ID + " INTEGER PRIMARY KEY," + DB.Tracks.TITLE
					+ " TEXT," + DB.Tracks.ID + " INTEGER,"
					+ DB.Tracks.STREAM_URL + " TEXT," + DB.Tracks.DURATION
					+ " INTEGER," + DB.Tracks.CLASS + " INTEGER DEFAULT 0"
					+ ");");
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(this.getClass().getName(), "Upgrading database from version "
					+ oldVersion + " to " + newVersion);

			switch (oldVersion) {
			case 10:
				createTracksTable(db);
				break;
			case 11:
				db.execSQL("ALTER TABLE " + DB.Tracks.TABLE_NAME
						+ " ADD COLUMN " + DB.Tracks.DURATION + " INTEGER");
			case 12:
				db.execSQL("ALTER TABLE " + DB.Tracks.TABLE_NAME
						+ " ADD COLUMN " + DB.Tracks.CLASS
						+ " INTEGER DEFAULT 0");
				break;
			case 13:
				break; // change only in soundclouddroid
			default:
				db.execSQL("DROP TABLE IF EXISTS " + DB.Uploads.TABLE_NAME);
				onCreate(db);
			}
		}

		Context mContext;
	}

	private static final int UPLOADS = 1;
	private static final int UPLOAD_ID = 2;
	private static final int TRACKS = 3;
	private static final int TRACK_ID = 4;

	public static class TableListEntry {
		public TableListEntry(String TABLE_NAME, Uri CONTENT_URI,
				String DEFAULT_SORT_ORDER, String nullColumnHack, boolean isItem) {
			this.TABLE_NAME = TABLE_NAME;
			this.CONTENT_URI = CONTENT_URI;
			this.DEFAULT_SORT_ORDER = DEFAULT_SORT_ORDER;
			this.nullColumnHack = nullColumnHack;
			this.isItem = isItem;
		}

		public String TABLE_NAME;
		public Uri CONTENT_URI;
		public String DEFAULT_SORT_ORDER;
		public String nullColumnHack;
		public boolean isItem;
	}

	protected static final UriMatcher sUriMatcher;
	protected static final Vector<TableListEntry> sTableList;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(DB.AUTHORITY, "uploads", UPLOADS);
		sUriMatcher.addURI(DB.AUTHORITY, "uploads/#", UPLOAD_ID);
		sUriMatcher.addURI(DB.AUTHORITY, "tracks", TRACKS);
		sUriMatcher.addURI(DB.AUTHORITY, "tracks/#", TRACK_ID);

		sTableList = new Vector<TableListEntry>();
		sTableList.add(new TableListEntry(DB.Uploads.TABLE_NAME,
				DB.Uploads.CONTENT_URI, DB.Uploads.DEFAULT_SORT_ORDER,
				DB.Uploads.TITLE, false));
		sTableList.add(new TableListEntry(DB.Uploads.TABLE_NAME,
				DB.Uploads.CONTENT_URI, DB.Uploads.DEFAULT_SORT_ORDER, null,
				true));
		sTableList.add(new TableListEntry(DB.Tracks.TABLE_NAME,
				DB.Tracks.CONTENT_URI, DB.Tracks.DEFAULT_SORT_ORDER,
				DB.Uploads.TITLE, false));
		sTableList
				.add(new TableListEntry(DB.Tracks.TABLE_NAME,
						DB.Tracks.CONTENT_URI, DB.Tracks.DEFAULT_SORT_ORDER,
						null, true));
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;

		int index = sUriMatcher.match(uri);
		if (index > 0) {
			TableListEntry entry = sTableList.elementAt(index - 1);
			String where = (!TextUtils.isEmpty(selection) ? "(" + selection
					+ ')' : null);
			if (entry.isItem) {
				String whereUploadId = BaseColumns._ID + "="
						+ uri.getPathSegments().get(1);
				if (where != null)
					where = whereUploadId + " AND " + where;
				else
					where = whereUploadId;
			}

			count = db.delete(entry.TABLE_NAME, where, selectionArgs);
		} else
			throw new IllegalArgumentException("Unknown URI " + uri);

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case UPLOADS:
			return DB.Uploads.CONTENT_TYPE;

		case UPLOAD_ID:
			return DB.Uploads.CONTENT_ITEM_TYPE;

		case TRACKS:
			return DB.Tracks.CONTENT_TYPE;

		case TRACK_ID:
			return DB.Tracks.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		ContentValues values;
		if (initialValues != null)
			values = new ContentValues(initialValues);
		else
			values = new ContentValues();

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		long rowId = 0;
		Uri contentURI;

		int index = sUriMatcher.match(uri);
		if (index > 0) {
			TableListEntry entry = sTableList.elementAt(index - 1);
			rowId = db.insert(entry.TABLE_NAME, entry.nullColumnHack, values);
			contentURI = entry.CONTENT_URI;
		} else
			throw new IllegalArgumentException("Unknown URI " + uri);

		if (rowId >= 0) {
			Uri noteUri = ContentUris.withAppendedId(contentURI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String orderBy;

		int index = sUriMatcher.match(uri);
		if (index > 0) {
			TableListEntry entry = sTableList.elementAt(index - 1);
			qb.setTables(entry.TABLE_NAME);
			if (entry.isItem)
				qb.appendWhere(BaseColumns._ID + "="
						+ uri.getPathSegments().get(1));
			orderBy = entry.DEFAULT_SORT_ORDER;
		} else
			throw new IllegalArgumentException("Unknown URI " + uri);

		// If no sort order is specified use the default
		if (!TextUtils.isEmpty(sortOrder))
			orderBy = sortOrder;

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		if (uri.getPathSegments().size() > 1)
			selection = BaseColumns._ID
					+ "="
					+ uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : "");

		switch (sUriMatcher.match(uri)) {
		case UPLOAD_ID:
			count = db.update(DB.Uploads.TABLE_NAME, values, selection,
					selectionArgs);
			break;

		case TRACK_ID:
			count = db.update(DB.Tracks.TABLE_NAME, values, selection,
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	protected DatabaseHelper mOpenHelper;
}
