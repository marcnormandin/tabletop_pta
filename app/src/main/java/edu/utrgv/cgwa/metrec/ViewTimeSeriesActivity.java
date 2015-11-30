package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
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
    private static final String TAG = "ViewTimeSeries";
    private static TimeSeriesFragment mFragment;
    private Toolbar mToolbar;
    private long mAudioID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_time_series);

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mAudioID = intent.getLongExtra("audioID", -1);
        if (mAudioID == -1) {
            Log.d(TAG, "Attempt to view a time series, but no audio ID given in intent.");
        } else {
            Log.d(TAG, "Adding time series fragment");
            mFragment = TimeSeriesFragment.newInstance(mAudioID);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.container, mFragment);
            ft.commit();
        }

        AudioRecordingManager manager = new AudioRecordingManager(this);
        DbAudioRecordingTable.AudioRecordingEntry entry = manager.getEntryByID(mAudioID);

        TextView date = (TextView) findViewById(R.id.date);
        date.setText("Date: " + entry.date());

        TextView time = (TextView) findViewById(R.id.time);
        time.setText("Time: " + entry.time());

        TextView samplesPerSecond = (TextView) findViewById(R.id.samplespersecond);
        samplesPerSecond.setText("Samples per second: " + entry.samplesPerSecond());

        TextView durationInSeconds = (TextView) findViewById(R.id.durationinseconds);
        durationInSeconds.setText("Duration (s): " + entry.durationInSeconds());
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
