package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

public class ViewProfileActivity extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener {
    private static final String TAG = "ViewProfile";
    private Toolbar mToolbar;
    private int mProfileID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mProfileID = intent.getIntExtra("profileID", -1);
        if (mProfileID == -1) {
            Log.d(TAG, "Attempt to view a profile, but no profile ID given in intent.");
        } else {
            Log.d(TAG, "Adding profile fragment");
            ProfileFragment frag = ProfileFragment.newInstance(mProfileID);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.container, frag);
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
    public void onFragmentInteraction(Uri uri) {

    }
}
