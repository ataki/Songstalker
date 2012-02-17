package org.urbanstew.soundclouddroid;

import org.apache.http.HttpResponse;
import org.urbanstew.SoundCloudBase.DB;
import org.urbanstew.SoundCloudBase.SoundCloudRequestClient;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import android.widget.AdapterView.OnItemSelectedListener;

public class UploadActivity extends SoundCloudActivity implements SoundCloudRequestClient
{
	protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.upload);
        
        mUploadBundle = new Bundle();

        mTitleEdit=(EditText)findViewById(R.id.title_edit);
        mTitleEdit.selectAll();
        
        mUploadButton = (Button) findViewById(R.id.upload_button);
        mUploadButton
	    	.setOnClickListener(new OnClickListener()
	    	{
				public void onClick(View arg0)
				{
					chooseFile();
				}
	    	});
        
        mTextAttribute = (EditText) findViewById(R.id.text_attribute);
        mSpinnerAttribute = (Spinner) findViewById(R.id.spinner_attribute);
        mAnimator = (ViewAnimator) findViewById(R.id.animator);
        	
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.attributes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mExtraAttribute = (Spinner) findViewById(R.id.extra_attributes);
        mExtraAttribute.setAdapter(adapter);
        mExtraAttribute.setOnItemSelectedListener(new OnItemSelectedListener()
        {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id)
			{
				commitSelectedAttribute();
				switch(position)
				{
				case 0:
					selectedSpinnerAttribute("sharing", R.array.sharing_options);
					break;
				case 1:
					selectedTextAttribute("description");
					break;
				case 2:
					selectedTextAttribute("genre");
					break;
				case 3:
					selectedSpinnerAttribute("track_type", R.array.track_type_options);
				}
				mLastExtraAttributePosition = position;
			}

			public void onNothingSelected(AdapterView<?> arg0)
			{
				commitSelectedAttribute();
			}
        });
        
        mFileUri = (TextView) findViewById(R.id.file_uri);

        if(getIntent() != null && getIntent().getAction() != null)
        {
        	if(getIntent().getAction().equals(Intent.ACTION_SEND) && getIntent().getExtras().containsKey(Intent.EXTRA_STREAM))
        		setFileUri((Uri)getIntent().getExtras().get(Intent.EXTRA_STREAM));
        	else if(getIntent().getAction().equals(Intent.ACTION_VIEW))
        	{
                Cursor cursor = getContentResolver().query(getIntent().getData(), sUploadsProjection, null, null, null);
                if(cursor.getCount()>0)
                {
                	cursor.moveToFirst();
                	setFileUri(Uri.parse(cursor.getString(PATH)));
                	mTitleEdit.setText(cursor.getString(TITLE));
                	if(!cursor.isNull(SHARING))
                	{
                		mUploadBundle.putCharSequence("sharing", cursor.getString(SHARING));
    					selectedSpinnerAttribute("sharing", R.array.sharing_options);
                	}
                	if(!cursor.isNull(DESCRIPTION))
                		mUploadBundle.putString("description", cursor.getString(DESCRIPTION));
                	if(!cursor.isNull(GENRE))
                		mUploadBundle.putString("genre", cursor.getString(GENRE));
                	if(!cursor.isNull(TRACK_TYPE))
                		mUploadBundle.putCharSequence("track_type", cursor.getString(TRACK_TYPE));
                }
                else
                	Log.w(UploadActivity.class.getSimpleName(), "Uri " + getIntent().getData() + " not found");
                cursor.close();
        	}
        }
	}
	
	void commitSelectedAttribute()
	{
		switch(mLastExtraAttributePosition)
		{
		case 0:
			mUploadBundle.putCharSequence("sharing", (CharSequence)mSpinnerAttribute.getSelectedItem());
			break;
		case 1:
			mUploadBundle.putString("description", mTextAttribute.getText().toString());
			break;
		case 2:
			mUploadBundle.putString("genre", mTextAttribute.getText().toString());
			break;
		case 3:
			mUploadBundle.putCharSequence("track_type", (CharSequence)mSpinnerAttribute.getSelectedItem());
			break;					
		}
		mLastExtraAttributePosition = Spinner.INVALID_POSITION;
	}
	void selectedTextAttribute(String parameter)
	{
		if(mUploadBundle.containsKey(parameter))
			mTextAttribute.setText(mUploadBundle.getString(parameter));
		else
			mTextAttribute.setText("");
		mAnimator.setDisplayedChild(1); // text
	}
	
	void selectedSpinnerAttribute(String parameter, int options)
	{
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerAttribute.setAdapter(adapter);
		mAnimator.setDisplayedChild(0); // spinner
		if(mUploadBundle.containsKey(parameter))
			for(int i=0; i<adapter.getCount(); i++)
				if(mUploadBundle.getCharSequence(parameter).equals(adapter.getItem(i)))
				{
					mSpinnerAttribute.setSelection(i);
					break;
				}
	}
    /**
     * The method called when the upload button is pressed.
     * <p>
     * Invokes OIFileManager to select the file to be uplaoded, or
     * if OIFileManager is not installed it starts the browser
     * to download it.
     */
    public void chooseFile()
    {
    	Intent intent = new Intent("org.openintents.action.PICK_FILE");
    	intent.setData(Uri.parse("file:///sdcard/"));
    	intent.putExtra("org.openintents.extra.TITLE", "Please select a file");
    	try
    	{
    		startActivityForResult(intent, 1);
    	} catch (ActivityNotFoundException e)
    	{
    		Toast.makeText(this, "When OIFileManager is finished downloading, please select it to install it, and then try uploading again from SoundCloud Droid.", Toast.LENGTH_LONG).show();
    		Intent downloadOIFM = new Intent("android.intent.action.VIEW");
    		downloadOIFM.setData(Uri.parse("http://openintents.googlecode.com/files/FileManager-1.0.0.apk"));
    		startActivity(downloadOIFM);
    	}
    }

    /**
     * The method called when the upload button is pressed.
     * <p>
     * Uploads the chosen file.
     */
    public void uploadFile()
    {
    	commitSelectedAttribute();
    	mUploadBundle.putString("title", mTitleEdit.getText().toString());

		getSCApplication().uploadFile(mFile, mUploadBundle, this);
		finish();
    }
    
    /**
     * The method called when the file to be uploaded is selected.
     */
    protected void onActivityResult(int requestCode,
            int resultCode, Intent data)
    {
    	if(data == null)
    		return;
    	
    	setFileUri(data.getData());
    }
	
    protected void setFileUri(Uri uri)
    {
    	mFile = uri;
    	if(uri != null)
    	{
    		mFileUri.setText("Chosen file: " + mFile.toString());
    		mUploadButton
	    	.setOnClickListener(new OnClickListener()
	    	{
	    		public void onClick(View arg0)
	    		{
	    			uploadFile();
	    		}
	    	});
    		mUploadButton.setText("Upload File");
    		
    		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    		String fileName = mFile.getLastPathSegment();
        	if(preferences.getBoolean("display_3gpp_warning", true) && (fileName != null) && (fileName.endsWith(".3gp") || fileName.endsWith(".3gpp")))
        	{
        		new AlertDialog.Builder(this)
            	.setTitle(R.string.warning)
            	.setMessage(R.string.warning_3gpp_upload)
		    	.setPositiveButton(android.R.string.ok,null)
		    	.setNegativeButton
		    	(
		    		R.string.never_show,
		    		new DialogInterface.OnClickListener()
		    		{
		    		    public void onClick(DialogInterface dialog, int whichButton)
		    		    {
		    		    	preferences.edit().putBoolean("display_3gpp_warning", false).commit();
		    		    }
		    		}
		    	)
		    	.create().show();
        	}
    	}
    }
    
	public void requestCompleted(HttpResponse response)
	{
	}

	public void requestFailed(Exception e)
	{
	} 

    Bundle mUploadBundle;
    
    int mLastExtraAttributePosition = Spinner.INVALID_POSITION;
	EditText mTitleEdit, mTextAttribute;
	Button mUploadButton;
	TextView mFileUri;
	Spinner mExtraAttribute, mSpinnerAttribute;
	ViewAnimator mAnimator;
	Uri mFile;
	
    protected static final String[] sUploadsProjection = new String[]
	{
    	DB.Uploads._ID, // 0
	    DB.Uploads.PATH, // 1
	    DB.Uploads.TITLE, // 2
	    DB.Uploads.SHARING, // 3
	    DB.Uploads.DESCRIPTION, // 4
	    DB.Uploads.GENRE, // 5
	    DB.Uploads.TRACK_TYPE, // 6
	};

    static final int _ID = 0;
    static final int PATH = 1;
    static final int TITLE = 2;
    static final int SHARING = 3;
    static final int DESCRIPTION = 4;
    static final int GENRE = 5;
    static final int TRACK_TYPE = 6;
}
