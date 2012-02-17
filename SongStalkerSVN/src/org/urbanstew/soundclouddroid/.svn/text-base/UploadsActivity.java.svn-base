package org.urbanstew.soundclouddroid;

import org.urbanstew.SoundCloudBase.DB;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class UploadsActivity extends Activity
{
	protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploads);

        // Read uploads
        mCursor = getContentResolver().query(DB.Uploads.CONTENT_URI, sUploadsProjection, null, null, null);
        
        Log.w(getClass().getName(), "Read " + mCursor.getCount() + " uploads.");
        
        // Map uploads to ListView
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.uploads_item, mCursor,
                new String[] { DB.Uploads.TITLE, DB.Uploads.STATUS }, new int[] { android.R.id.text1, android.R.id.text2 });
        ListView list = (ListView)findViewById(R.id.uploads_list);
        list.setAdapter(adapter);
        
        list.setOnItemClickListener(new OnItemClickListener()
        {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long id)
			{
				startActivity
		        (
		        	new Intent
		        	(
		        		Intent.ACTION_VIEW,
		        		ContentUris.withAppendedId(DB.Uploads.CONTENT_URI, id)
		        	)
		        );
			}
        	
        });
        
        ((Button) findViewById(R.id.clear_button)).setOnClickListener(new OnClickListener()
        {
			public void onClick(View v)
			{
				getContentResolver().delete(DB.Uploads.CONTENT_URI, null, null);
			}        	
        });
        
        mNoUploads = (TextView) findViewById(R.id.no_uploads);
        mNoUploads.setVisibility(mCursor.getCount()==0 ? View.VISIBLE : View.INVISIBLE);
        mCursor.registerContentObserver(new ContentObserver(mHandler)
        {
        	public void onChange(boolean selfChange)
        	{
        		mNoUploads.setVisibility(mCursor.getCount()==0 ? View.VISIBLE : View.INVISIBLE);
        	}
        });
	}
	
	public void onDestroy()
	{
		mCursor.close();
		super.onDestroy();
	}
	
	Cursor mCursor;
    Handler mHandler = new Handler();
    TextView mNoUploads;
	
    protected static final String[] sUploadsProjection = new String[]
	{
	      DB.Uploads._ID, // 0
	      DB.Uploads.TITLE, // 1
	      DB.Uploads.STATUS, // 2
	};
}
