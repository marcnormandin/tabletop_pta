package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class ViewTimeSeriesActivity extends AppCompatActivity {
    private static final String TAG = "ViewProfile";
    private static TimeSeriesFragment mFragment;
    private Toolbar mToolbar;
    private long mProfileID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_time_series);

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mProfileID = intent.getLongExtra("profileID", -1);
        if (mProfileID == -1) {
            Log.d(TAG, "Attempt to view a time series, but no profile ID given in intent.");
        } else {
            Log.d(TAG, "Adding time series fragment");
            mFragment = TimeSeriesFragment.newInstance(mProfileID);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.container, mFragment);
            ft.commit();
        }

        ProfileManager manager = new ProfileManager(this);
        DbProfileTable.ProfileEntry profile = manager.getProfileEntryByProfileID(mProfileID);

        // Update the pulse period text
        TextView tv = (TextView) findViewById(R.id.metronomepulseprofileperiod);
        tv.setText("Computed period: " + profile.computedPeriod() + " (s)");

        // Update the beats per minute box
        TextView bpm = (TextView) findViewById(R.id.metronomepulseprofilebpm);
        bpm.setText("Beats per minute: " + profile.beatsPerMinute());

        TextView date = (TextView) findViewById(R.id.metronomepulseprofiledate);
        date.setText("Date: " + profile.date());

        TextView time = (TextView) findViewById(R.id.metronomepulseprofiletime);
        time.setText("Time: " + profile.time());

        TextView samplesPerSecond = (TextView) findViewById(R.id.metronomepulseprofilesamplespersecond);
        samplesPerSecond.setText("Samples per second: " + profile.samplesPerSecond());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_time_series, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_playsound:
                Toast.makeText(this, "Playing recording", Toast.LENGTH_LONG).show();
                mFragment.playSound();
                return true;
        }
        return false;
    }
}