package edu.utrgv.cgwa.metrec;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class AudioRecordListActivity extends AppCompatActivity implements AudioRecordListFragment.OnFragmentInteractionListener,
        NewAudioRecordingFragment.NewAudioRecordingListener
{
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiorecordlist);

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_audiorecordlist, menu);
        return true;
    }

    @Override
    public void onDisplayTimeSeriesClicked(long audioID) {
        Intent intent = new Intent(this, ViewTimeSeriesActivity.class);
        intent.putExtra(TimeSeriesFragment.ARG_AUDIOID, audioID);
        startActivity(intent);
    }

    @Override
    public void onCheckboxChanged(int position, long audioID, boolean isChecked) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();

        switch(item.getItemId()) {
            case android.R.id.home:
                finish();

            case R.id.action_new:
                //Toast.makeText(this, "Adding audio is not yet implemented", Toast.LENGTH_LONG).show();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.recordFragmentContainer, new NewAudioRecordingFragment(), "recordingControls");
                ft.commit();
                return true;

            case R.id.action_delete:
                // Make sure that there are items checked
                AudioRecordListFragment frag = (AudioRecordListFragment)
                        fm.findFragmentById(R.id.listfragment);
                long[] ids = frag.getSelectedEntryIDs();
                if (ids.length > 0) {
                    popupMenuDelete();
                } else {
                    Toast.makeText(this, "There are no records selected.", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return false;
    }

    private void popupMenuDelete() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Alert!!");
        alert.setMessage("Are you sure that you want to delete the selected records?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteRecords();
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void deleteRecords() {
        FragmentManager fm = getSupportFragmentManager();
        AudioRecordListFragment frag = (AudioRecordListFragment) fm.findFragmentById(R.id.listfragment);
        frag.deleteSelectedIDs();

        AudioRecordListFragment listfrag = (AudioRecordListFragment) fm.findFragmentById(R.id.listfragment);
        listfrag.refresh();
    }

    @Override
    public void cancelRecording() {
        FragmentManager fm = getSupportFragmentManager();
        NewAudioRecordingFragment frag = (NewAudioRecordingFragment) fm.findFragmentByTag("recordingControls");
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(frag);
        ft.commit();
    }

    @Override
    public void recordingFinished() {
        FragmentManager fm = getSupportFragmentManager();
        NewAudioRecordingFragment frag = (NewAudioRecordingFragment) fm.findFragmentByTag("recordingControls");
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(frag);
        ft.commit();

        AudioRecordListFragment listfrag = (AudioRecordListFragment) fm.findFragmentById(R.id.listfragment);
        listfrag.refresh();
    }
}
