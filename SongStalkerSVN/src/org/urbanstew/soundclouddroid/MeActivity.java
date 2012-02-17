package org.urbanstew.soundclouddroid;

//make sure to import SoundCloudApplicationBase
import java.io.InputStream;
import java.net.URL;

import org.urbanstew.SoundCloudBase.SoundCloudApplicationBase;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MeActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get the application context so that we can call the getStringFromAPI method from SoundCloudApplicationBase within an activity
        SoundCloudApplicationBase appState = ((SoundCloudApplicationBase)getApplicationContext());
       
        //in getStringFromAPI, 1st arg is the API request, 2nd arg is the key you're looking for in the response
        //*note: this method will only return the values for the first node*
        //some sample calls 
        String userAvatarURL = appState.getStringFromAPI("me", "avatar-url"); 
        String userID = appState.getStringFromAPI("me", "id");
        String userName = appState.getStringFromAPI("me", "username");
        String firstFavoriteID = appState.getStringFromAPI("me/favorites", "id");
        String firstFavoriteTitle = appState.getStringFromAPI("me/favorites", "title");

        setContentView(R.layout.me);
        ImageView imgView =(ImageView)findViewById(R.id.ImageView01);
        Drawable drawable = LoadImageFromWebOperations(userAvatarURL);
        imgView.setImageDrawable(drawable);

        TextView textview = (TextView)findViewById(R.id.TextView01);
        textview.setText("The avatar URL is "+userAvatarURL+". The user ID is "+userID+". The username is "+userName+". First song in Favorites has ID of "+firstFavoriteID+" and is called "+firstFavoriteTitle+". Look in MeActivity.java for this code.");
    }
    private Drawable LoadImageFromWebOperations(String url)
    {
 		try
 		{
 			InputStream is = (InputStream) new URL(url).getContent();
 			Drawable d = Drawable.createFromStream(is, "src name");
 			return d;
 		}catch (Exception e) {
 			System.out.println("Exc="+e);
 			return null;
 		}
 	}
 }
