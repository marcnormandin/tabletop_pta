package edu.utrgv.cgwa.metrec;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class ProfileListActivity extends AppCompatActivity
        implements ProfileListFragment.OnFragmentInteractionListener,
        NewProfileFragment.NewProfileFragmentListener {

    private Toolbar mToolbar;

    static public final int PICK_AUDIO_RECORD_REQUEST = 2;  // The request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilelist);

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onDisplayProfileClicked(final long profileID) {
        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra(ProfileFragment.ARG_PROFILEID, profileID);
        startActivity(intent);
    }

    @Override
    public void onDisplayTimeSeriesClicked(final long audioID) {
        // Make sure that the audio hasn't been deleted
        // before attempting to display it.
        AudioRecordingManager am = new AudioRecordingManager(this);
        try {
            DbAudioRecordingTable.AudioRecordingEntry e = am.getEntryByID(audioID);
            Intent intent = new Intent(this, ViewTimeSeriesActivity.class);
            intent.putExtra(TimeSeriesFragment.ARG_AUDIOID, audioID);
            startActivity(intent);
        }
        catch (AudioRecordingManager.InvalidRecordException e) {
            Toast.makeText(this, "The audio time series does not exist.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCheckboxChanged(final int position, final long audioID, final long profileID, final boolean isChecked) {
        //Toast.makeText(this, "Position (" + position + "), AudioID (" + audioID
        //        + "), ProfileID (" + profileID + "),  checked = " + isChecked, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profilelist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_newprofile:
                actionNewProfile();
                return true;

            case R.id.action_delete:
                // Make sure that there are items checked
                FragmentManager fm = getSupportFragmentManager();
                ProfileListFragment frag = (ProfileListFragment)
                        fm.findFragmentById(R.id.listfragment);
                long[] ids = frag.getSelectedProfileIDs();
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
        ProfileListFragment frag = (ProfileListFragment) fm.findFragmentById(R.id.listfragment);
        frag.deleteSelectedIDs();
    }

    private void actionNewProfile() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NewProfileFragment frag = NewProfileFragment.newInstance();
        ft.replace(R.id.newProfileFragmentContainer, frag, "profileFragment");
        ft.commit();
    }

    @Override
    public void onCancel() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NewProfileFragment f = (NewProfileFragment) fm.findFragmentByTag("profileFragment");
        ft.remove(f);
        ft.commit();
    }

    @Override
    public void onSelectAudioRecord() {
        Intent intent = new Intent(this, SelectAudioRecordActivity.class);
        startActivityForResult(intent, PICK_AUDIO_RECORD_REQUEST);
    }

    @Override
    public void onNewProfileCreated() {
        // Remove the "new profile" fragment
        FragmentManager fm = getSupportFragmentManager();
        NewProfileFragment frag = (NewProfileFragment) fm.findFragmentByTag("profileFragment");
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(frag);
        ft.commit();

        // Tell the list to update itself since a new record was created
        ProfileListFragment list = (ProfileListFragment) fm.findFragmentById(R.id.listfragment);
        list.refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_AUDIO_RECORD_REQUEST) {
            if (resultCode == RESULT_OK) {
                long[] ids = data.getLongArrayExtra(SelectAudioRecordActivity.RESULT_SELECTED_AUDIO_RECORDS);
                if (ids.length != 1) {
                    Toast.makeText(this, "SELECT ONLY ONE AUDIO RECORD!", Toast.LENGTH_LONG).show();
                } else {
                    long audioID = ids[0];
                    Toast.makeText(this, "Selected audio record: " + audioID, Toast.LENGTH_LONG).show();
                    createNewProfileRecord(audioID);
                }
            }
        }
    }

    private void createNewProfileRecord(final long audioID) {
        FragmentManager fm = getSupportFragmentManager();
        NewProfileFragment f = (NewProfileFragment) fm.findFragmentByTag("profileFragment");
        f.createNewProfileRecord(audioID);
    }
}
