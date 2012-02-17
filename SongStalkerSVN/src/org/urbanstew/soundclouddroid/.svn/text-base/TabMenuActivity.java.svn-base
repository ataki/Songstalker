package org.urbanstew.soundclouddroid;

import org.urbanstew.SoundCloudBase.R;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.content.res.Resources;

public class TabMenuActivity extends TabActivity {    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_menu);

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, NearbyActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("nearby").setIndicator("Nearby",
                          res.getDrawable(R.drawable.ic_tab_nearby))
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, MeActivity.class);
        spec = tabHost.newTabSpec("me").setIndicator("Me",
                          res.getDrawable(R.drawable.ic_tab_nearby))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, StalkersActivity.class);
        spec = tabHost.newTabSpec("stalkers").setIndicator("Stalkers",
                          res.getDrawable(R.drawable.ic_tab_nearby))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(1);
    }
}

