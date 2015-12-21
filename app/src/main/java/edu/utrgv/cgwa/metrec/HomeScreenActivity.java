package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;


public class HomeScreenActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    public void startBuiltinMetronomeActivity(View view) {
        Intent intent = new Intent(HomeScreenActivity.this, BuiltinMetronomeActivity.class);
        HomeScreenActivity.this.startActivity(intent);
    }

    public void viewAudioRecordings(View view) {
        Intent intent = new Intent(HomeScreenActivity.this, AudioRecordListActivity.class);
        startActivity(intent);
    }

    public void viewProfiles(View view) {
        Intent intent = new Intent(HomeScreenActivity.this, ProfileListActivity.class);
        startActivity(intent);
    }

    public void viewSingleMetronomeAnalyses(View view) {
        Intent intent = new Intent(HomeScreenActivity.this, SingleMetronomeAnalysisListActivity.class);
        startActivity(intent);
    }

    public void viewDoubleMetronomeAnalyses(View view) {
        Intent intent = new Intent(HomeScreenActivity.this, DoubleMetronomeAnalysisListActivity.class);
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
