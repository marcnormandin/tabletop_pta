package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ViewTimeSeriesActivity extends AppCompatActivity {
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
            // Fixme
        } else {
            mFragment = TimeSeriesFragment.newInstance(mAudioID);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.container, mFragment);
            ft.commit();
        }

        AudioRecordingManager manager = new AudioRecordingManager(this);
        try {
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
        catch (AudioRecordingManager.InvalidRecordException e) {
            Toast.makeText(this, "Error: Unable to load audio record.", Toast.LENGTH_LONG).show();
        }
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
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_playsound:
                Toast.makeText(this, "Playing recording", Toast.LENGTH_LONG).show();
                mFragment.playSound();
                return true;
            case R.id.action_share_audio_file:
                shareAudioFile();
                return true;
        }
        return false;
    }

    private void shareAudioFile() {
        Intent ei = new Intent(android.content.Intent.ACTION_SEND);
        ei.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "normandin.utb@gmail.com" });
        ei.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tabletop PTA Sound Recording (AudioID: " + mAudioID + ")");
        ei.putExtra(android.content.Intent.EXTRA_TEXT, "The attached screenshot was taken of the Tabletop PTA phone app.");
        ei.setType("message/rfc822");
        //ei.setType("image/png");

        // Get the PCM filename
        AudioRecordingManager manager = new AudioRecordingManager(this);
        try {
            DbAudioRecordingTable.AudioRecordingEntry entry = manager.getEntryByID(mAudioID);
            String fullPathAndFilename = entry.filenamePCM();
            int n = fullPathAndFilename.lastIndexOf("/");
            String filename = fullPathAndFilename.substring(n);
            File file = new File(this.getFilesDir(), filename);

            Uri uri = FileProvider.getUriForFile(this, "edu.utrgv.cgwa.metrec.fileprovider", file);


            // Add it to the intent
            ei.putExtra(Intent.EXTRA_STREAM, uri);

            startActivity(Intent.createChooser(ei, "Send audio recording..."));
        }
        catch (AudioRecordingManager.InvalidRecordException e) {
            Toast.makeText(this, "Error: Unable to load audio record.", Toast.LENGTH_LONG).show();
        }
    }
}
