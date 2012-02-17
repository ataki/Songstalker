package org.urbanstew.SoundCloudBase;

import org.apache.http.HttpResponse;
import org.urbanstew.SoundCloudBase.SoundCloudRequestClient;
import org.urbanstew.SoundCloudBase.SoundCloudApplicationBase.RequestType;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class ViewTracksActivity extends ListActivity implements
		SoundCloudRequestClient, PlaybackDialog.OnCancelListener {
	public SoundCloudApplicationBase getSCApplicationBase() {
		return (SoundCloudApplicationBase) getApplication();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.view_tracks);

		// Read uploads
		mCursor = getContentResolver().query(DB.Tracks.CONTENT_URI,
				sTracksProjection, DB.Tracks.CLASS + "=" + mClass, null, null);

		Log.w(getClass().getName(), "Read " + mCursor.getCount() + " tracks.");

		// Map uploads to ListView
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.tracks_item, mCursor, new String[] { DB.Tracks.TITLE,
						DB.Tracks.DURATION, DB.Tracks.STREAM_URL }, new int[] {
						android.R.id.text1, android.R.id.text2,
						android.R.id.icon });
		setListAdapter(adapter);
		getListView().setTextFilterEnabled(true);

		getListView()
				.setOnCreateContextMenuListener(mCreateContextMenuListener);
		adapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				if (view.getId() == android.R.id.icon) {
					ImageView v = (ImageView) view;
					v.setVisibility(TextUtils.isEmpty(cursor
							.getString(columnIndex)) ? View.INVISIBLE
							: View.VISIBLE);
					return true;
				} else if (view.getId() == android.R.id.text1)
					return false;

				TextView v = (TextView) view;

				long duration = cursor.getLong(4);

				String text;
				if (duration == 0)
					text = "Processing (play/synchronize to check again)";
				else if (duration < 1000)
					text = duration + " ms";
				else if (duration < 60000)
					text = String.format("%.1f s", duration / 1000.0f);
				else
					text = String.format("%.1f min", duration / 60000.0f);

				v.setText(text);
				return true;
			}
		});

		if (mCursor.getCount() == 0)
			requestOffset(0);
		else
			requestedOffset = -1;

		mPlaybackDialog = new PlaybackDialog(this);
		mPlaybackDialog.setOnCancelListener(this);
	}

	public void onPause() {
		mPlaybackDialog.onPause();
		super.onPause();
	}

	public void onResume() {
		super.onResume();
		mPlaybackDialog.onResume();
	}

	public void onDestroy() {
		mPlaybackDialog.onDestroy();
		mCursor.close();
		super.onDestroy();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		mSynchronizeMenuItem = menu.add("Synchronize").setIcon(
				android.R.drawable.ic_popup_sync);
		mHelpMenuItem = menu.add("Help").setIcon(
				android.R.drawable.ic_menu_help);
		return true;
	}

	/**
	 * Processes menu options.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == mSynchronizeMenuItem)
			requestOffset(0);
		else if (item == mHelpMenuItem) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Select a track to play it, or long-press to download an mp3 or delete the track.\n\nUse \"Synchronize\" from the MENU to synchronize track status with SoundCloud.")
					.create().show();

		} else
			return false;
		return true;
	}

	public void requestOffset(int offset) {
		requestedOffset = offset;
		if (requestedOffset == 0)
			getContentResolver().delete(DB.Tracks.CONTENT_URI,
					DB.Tracks.CLASS + "=" + mClass, null);
		getSCApplicationBase().processRequest(
				mQuery + "?offset=" + offset + "&limit=" + limit,
				tracksRequestClient);
	}

	SoundCloudRequestClient tracksRequestClient = new SoundCloudRequestClient() {
		public void requestCompleted(HttpResponse response) {
			int x = response.getStatusLine().getStatusCode();
			if (x != 200) {
				Log.e(ViewTracksActivity.class.getSimpleName(),
						"Tracks request returned with response " + x + ", "
								+ response.getStatusLine().getReasonPhrase());
				return;
			}

			int numTracks = getSCApplicationBase().processTracks(response,
					mClass);

			if (numTracks == limit)
				requestOffset(requestedOffset + limit);
		}

		public void requestFailed(Exception e) {
			Log.e(ViewTracksActivity.class.getSimpleName(),
					"Tracks request failed with exception");
			e.printStackTrace();
		}
	};

	SoundCloudRequestClient trackRequestClient = new SoundCloudRequestClient() {
		public void requestCompleted(HttpResponse response) {
			int x = response.getStatusLine().getStatusCode();
			if (x != 200) {
				Log.e(ViewTracksActivity.class.getSimpleName(),
						"Track request returned with response " + x + ", "
								+ response.getStatusLine().getReasonPhrase());
				return;
			}
			getSCApplicationBase().processTracks(response, mClass, true);
		}

		public void requestFailed(Exception e) {
			Log.d(ViewTracksActivity.class.getSimpleName(),
					"Track request failed with exception");
			e.printStackTrace();
		}
	};

	class DeleteRequestClient implements SoundCloudRequestClient {
		DeleteRequestClient(long id) {
			mId = id;
		}

		public void requestCompleted(HttpResponse response) {
			int x = response.getStatusLine().getStatusCode();
			if (x != 200) {
				reportRequestFailure(response.getStatusLine().getReasonPhrase());
				Log.e(ViewTracksActivity.class.getSimpleName(),
						"Delete request returned with response " + x + ", "
								+ response.getStatusLine().getReasonPhrase());
				return;
			}

			getContentResolver().delete(
					ContentUris.withAppendedId(DB.Tracks.CONTENT_URI, mId),
					null, null);
		}

		public void requestFailed(Exception e) {
			reportRequestFailure(e.getLocalizedMessage());
			Log.d(ViewTracksActivity.class.getSimpleName(),
					"Delete request failed with exception");
			e.printStackTrace();
		}

		private void reportRequestFailure(final String message) {
			ViewTracksActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(ViewTracksActivity.this,
							"The track could not be deleted (" + message + ")",
							Toast.LENGTH_SHORT).show();
				}
			});
		}

		private long mId;
	};

	public void onListItemClick(ListView parent, View v, int position, long id) {
		playback(position);

		/*
		 * try { String signedUrl =
		 * getSCApplicationBase().getSoundCloudAPI().signStreamUrl(streamUrl);
		 * Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(signedUrl));
		 * intent.setType("audio/mp3"); startActivity(intent); } catch
		 * (Exception e) { }
		 */

	}

	public static final int MENU_ITEM_PLAYBACK = Menu.FIRST;
	public static final int MENU_ITEM_DOWNLOAD = Menu.FIRST + 1;
	public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;

	View.OnCreateContextMenuListener mCreateContextMenuListener = new View.OnCreateContextMenuListener() {
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			mCursor.moveToPosition(info.position);
			if (!TextUtils.isEmpty(mCursor.getString(3))) {
				menu.add(Menu.NONE, MENU_ITEM_PLAYBACK, 0, "Play");
				menu.add(Menu.NONE, MENU_ITEM_DOWNLOAD, 1, "Download MP3");
			}
			menu.add(Menu.NONE, MENU_ITEM_DELETE, 2, "Delete from SoundCloud");
		}
	};

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(ViewTracksActivity.class.getSimpleName(), "bad menuInfo", e);
			return false;
		}
		final AdapterView.AdapterContextMenuInfo finalinfo = info;

		switch (item.getItemId()) {
		case MENU_ITEM_PLAYBACK:
			playback(info.position);
			return true;
		case MENU_ITEM_DOWNLOAD:
			mCursor.moveToPosition(info.position);
			String streamUrl = mCursor.getString(3);
			Log.d(ViewTracksActivity.class.getSimpleName(), "getting "
					+ streamUrl);
			String file = getSCApplicationBase().downloadStream(streamUrl,
					mCursor.getString(1));
			Toast.makeText(this, "Downloading file to " + file,
					Toast.LENGTH_LONG).show();
			return true;

		case MENU_ITEM_DELETE:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"This will permanently delete the track from SoundCloud.  ARE YOU SURE YOU WANT TO DO THIS?")
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									delete(finalinfo.position);
								}
							}).setNegativeButton(android.R.string.cancel, null)
					.create().show();
			return true;
		}
		return false;
	}

	public void playback(int position) {
		mCursor.moveToPosition(position);
		long duration = mCursor.getLong(4);
		if (duration == 0)
			getSCApplicationBase().processRequest(
					"tracks/" + mCursor.getLong(2), trackRequestClient);

		String streamUrl = mCursor.getString(3);
		try {
			Log.d(ViewTracksActivity.class.getName(), "getting " + streamUrl);
			// if(Integer.parseInt(android.os.Build.VERSION.SDK) >= 4)
			// {
			// streamUrl =
			// getSCApplicationBase().getSoundCloudAPI().signStreamUrl(streamUrl);
			// Log.d(ViewTracksActivity.class.getName(), "signed as " +
			// streamUrl);
			//
			// mPlaybackDialog.displayPlaybackDialog(streamUrl);
			// }
			// else
			// {
			getSCApplicationBase().processRequest(streamUrl, this,
					RequestType.GET_STREAM_REDIRECT);
			mPlaybackDialog.displayPlaybackDialog();
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delete(int position) {
		mCursor.moveToPosition(position);
		getSCApplicationBase()
				.processRequest("tracks/" + mCursor.getLong(2),
						new DeleteRequestClient(mCursor.getLong(0)),
						RequestType.DELETE);
	}

	protected void setQueryAndClass(String query, long _class) {
		mQuery = query;
		mClass = _class;
	}

	protected long getQueryClass() {
		return mClass;
	}

	protected String getQuery() {
		return mQuery;
	}

	protected boolean requestIssued() {
		return requestedOffset != -1;
	}

	Cursor mCursor;
	int requestedOffset;
	int limit = 50;

	private MenuItem mSynchronizeMenuItem, mHelpMenuItem;
	PlaybackDialog mPlaybackDialog;
	private String mQuery = "me/tracks"; // default query value
	private long mClass = 0L; // default class value

	protected static final String[] sTracksProjection = new String[] {
			DB.Tracks._ID, // 0
			DB.Tracks.TITLE, // 1
			DB.Tracks.ID, // 2
			DB.Tracks.STREAM_URL, // 3
			DB.Tracks.DURATION, // 4
	};

	public void requestCompleted(HttpResponse response) {
		final String redirectUrl = getSCApplicationBase().getSoundCloudAPI()
				.parseRedirectResponse(response);
		Log.d(ViewTracksActivity.class.getName(), "redirected as "
				+ redirectUrl);

		runOnUiThread(new Runnable() {
			public void run() {
				if (redirectUrl != null)
					mPlaybackDialog.provideStreamUrl(redirectUrl);
				else
					mPlaybackDialog.error();
			}
		});
	}

	public void requestFailed(Exception e) {
		Log.d(ViewTracksActivity.class.getName(),
				"ViewTracksActivity request failed with Exception");
		e.printStackTrace();
		runOnUiThread(new Runnable() {
			public void run() {
				mPlaybackDialog.error();
			}
		});
	}

	public void onPlaybackDialogCancel() {
		getSCApplicationBase().cancel(this);
	}
}