package org.urbanstew.soundclouddroid;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class NearbyActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        
        URL url;
		try {
			url = new
				URL("http://api.soundcloud.com/users/1192040?consumer_key=Xag1mZnA9h1WwdOwXMyOdQ");

		        HttpURLConnection urlConn;
				ClientResource cr = new ClientResource("http://api.soundcloud.com/users/1192040?consumer_key=Xag1mZnA9h1WwdOwXMyOdQ");
				 try {
				 String img_url = cr.get().toString();
				  textview.setText("Image URL - "+img_url);
				  setContentView(textview);
				 } catch (ResourceException e) {
						e.printStackTrace();
				 }
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
}