package edu.utrgv.cgwa.metrec;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;


public class HomeScreenActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);
    }

    public void startBuiltinMetronomeActivity(View view) {
        Intent intent = new Intent(HomeScreenActivity.this, BuiltinMetronomeActivity.class);
        HomeScreenActivity.this.startActivity(intent);
    }

    public void viewProfiles(View view) {
        Intent intent = new Intent(HomeScreenActivity.this, ProfileListActivity.class);
        startActivity(intent);
    }

    public void launchWebsite(View view) {
        final String url = "http://nanograv.org/outreach/ptademo/";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        HomeScreenActivity.this.startActivity(intent);
    }

    public void viewSettings(View view) {
        Intent intent = new Intent(HomeScreenActivity.this, PreferencesActivity.class);
        HomeScreenActivity.this.startActivity(intent);
    }
}
