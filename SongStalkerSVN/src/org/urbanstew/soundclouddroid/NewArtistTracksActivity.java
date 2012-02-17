package org.urbanstew.soundclouddroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.urbanstew.SoundCloudBase.SoundCloudRequestClient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class NewArtistTracksActivity extends SoundCloudListActivity implements SoundCloudRequestClient, OnClickListener, OnEditorActionListener
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_artist_tracks);
        
        EditText edit = (EditText)findViewById(R.id.query_edit);
        edit.setOnEditorActionListener(this);
        
        mSearchButton = (Button)findViewById(R.id.query_button);
        mSearchButton.setOnClickListener(this);
        
    	arrayAdapter = new ArrayAdapter<Artist>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        setListAdapter(arrayAdapter);
    }

    public void onDestroy()
    {
    	super.onDestroy();
    	getSCApplication().cancel(this);
    }

	public void requestCompleted(HttpResponse response)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				mSearchButton.setEnabled(true);				
			}
		});

		if(response.getEntity() == null)
		{
			reportRequestFailure("no response received");
			return;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String result = null;
		try
		{
			response.getEntity().writeTo(out);
			result = out.toString("UTF-8");
		} catch (IOException e)
		{
			reportRequestFailure(e.getLocalizedMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		final JSONTokener tokener = new JSONTokener(result);
		
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				arrayAdapter.clear();
				try
				{
					JSONArray array = (JSONArray) tokener.nextValue();
					Log.d(NewArtistTracksActivity.class.getName(), "got artists: " + array.length());
	
					for(int i=0; i<array.length(); i++)
					{
						JSONObject object = array.getJSONObject(i);
						long id = object.getLong("id");
						String username = object.getString("username");
						String fullName = object.getString("full_name");
						arrayAdapter.add(new Artist(id, username, fullName));
					}
				}
				catch (JSONException e)
				{
					reportRequestFailure(e.getLocalizedMessage());
					e.printStackTrace();
				}
				arrayAdapter.notifyDataSetChanged();
			}	
		});
	}

	public void requestFailed(Exception e)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				mSearchButton.setEnabled(true);				
			}
		});
		reportRequestFailure(e.getLocalizedMessage());
	}

	private void reportRequestFailure(final String message)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(NewArtistTracksActivity.this, "The search could not be completed (" + message + ")", Toast.LENGTH_SHORT).show();			
			}				
		});
	}


	public void onClick(View v)
	{
		EditText edit = (EditText)findViewById(R.id.query_edit);
		try
		{
			getSCApplication().processRequest("users.json?q=" + 	java.net.URLEncoder.encode(edit.getText().toString(), "UTF-8"), this);
			mSearchButton.setEnabled(false);
		} catch (UnsupportedEncodingException e)
		{
		}
		edit.clearFocus();
	}
	
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
	{
		if(mSearchButton.isEnabled())
			this.onClick(null);
		return false;
	}

	public void onListItemClick(ListView parent, View v, int position, long id)
	{
		Artist artist = arrayAdapter.getItem(position);
		Intent intent = new Intent(getApplication(), ViewOtherTracksActivity.class);
		intent.putExtra(DB.TrackLists.RESOURCE, "users/" + artist.id + "/tracks");
		intent.putExtra(DB.TrackLists.TITLE, artist.toString());
		intent.putExtra(DB.TrackLists._ID, 99L);
		
		startActivity(intent);
	}

	Button mSearchButton;
	ArrayAdapter<Artist> arrayAdapter;
	boolean searchAfterResponseReceived;
}

class Artist
{
	Artist(long id, String username, String fullName)
	{
		this.id = id;
		this.username = username;
		this.fullName = fullName;
	}
	
	public String toString()
	{
		if(fullName != null && fullName.length() > 0)
			return fullName;
		return username;
	}
	
	long id;
	String username;
	String fullName;
}
