package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class ViewTimeSeriesActivity extends AppCompatActivity implements TimeSeriesFragment.OnFragmentInteractionListener {
    private static final String TAG = "ViewProfile";
    private static TimeSeriesFragment mFragment;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_time_series);

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        int profileID = intent.getIntExtra("profileID", -1);
        if (profileID == -1) {
            Log.d(TAG, "Attempt to view a time series, but no profile ID given in intent.");
        } else {
            Log.d(TAG, "Adding time series fragment");
            mFragment = TimeSeriesFragment.newInstance(profileID);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.container, mFragment);
            ft.commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_timeseries, menu);
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
