package edu.utrgv.cgwa.metrec;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.AlertDialog;

public class SingleMetronomeAnalysisListActivity extends AppCompatActivity
        implements SingleMetronomeAnalysisListFragment.OnFragmentInteractionListener,
        NewSingleAnalysisFragment.NewSingleAnalysisFragmentListener {

    private Toolbar mToolbar;

    static public final int PICK_AUDIO_RECORD_REQUEST = 1;  // The request code
    static public final int PICK_PROFILE_RECORD_REQUEST = 2;  // The request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_metronome_analysis_list);

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCheckboxChanged(final int position, final long analysisID, final boolean isChecked) {
        //Toast.makeText(this, "Position (" + position + "), Analysis ID (" + analysisID + "),  checked = " + isChecked, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewPulseOverlayClicked(int position, long analysisID) {
        Intent intent = new Intent(this, ViewAnalysisPulsesActivity.class);
        intent.putExtra(ViewAnalysisPulsesActivity.ARG_ANALYSIS_ID, analysisID);
        startActivity(intent);
    }

    @Override
    public void onViewResidualsClicked(int position, long analysisID, String analysisFilenameResult) {
        Intent intent = new Intent(this, ViewAnalysisResidualsActivity.class);
        intent.putExtra(ViewAnalysisResidualsActivity.ARG_ANALYSIS_ID, analysisID);
        intent.putExtra(ViewAnalysisResidualsActivity.ARG_ANALYSIS_FILENAME_RESULT, analysisFilenameResult);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_single_metronome_analysis_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_new:
                actionNewSingleAnalysis();
                return true;

            case R.id.action_delete:
                // Make sure that there are items checked
                FragmentManager fm = getSupportFragmentManager();
                SingleMetronomeAnalysisListFragment frag = (SingleMetronomeAnalysisListFragment)
                        fm.findFragmentById(R.id.listfragment);
                long[] ids = frag.getSelectedIDs();
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
        alert.setMessage("Are you sure that you want to delete records?");
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
        SingleMetronomeAnalysisListFragment frag = (SingleMetronomeAnalysisListFragment) fm.findFragmentById(R.id.listfragment);
        frag.deleteSelectedIDs();
    }

    private void actionNewSingleAnalysis() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NewSingleAnalysisFragment frag = new NewSingleAnalysisFragment();
        ft.replace(R.id.newAnalysisFragmentContainer, frag, "analysisFragment");
        ft.commit();
    }

    @Override
    public void onCancel() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NewSingleAnalysisFragment frag = (NewSingleAnalysisFragment) fm.findFragmentByTag("analysisFragment");
        ft.remove(frag);
        ft.commit();
    }

    @Override
    public void onSelectAudioRecord() {
        Intent intent = new Intent(this, SelectAudioRecordActivity.class);
        startActivityForResult(intent, PICK_AUDIO_RECORD_REQUEST);
    }

    @Override
    public void onSelectProfile() {
        Intent intent = new Intent(this, SelectProfilesActivity.class);
        startActivityForResult(intent, PICK_PROFILE_RECORD_REQUEST);
    }

    @Override
    public void onNewSingleAnalysisCreated(final long audioID) {
        Toast.makeText(this, "NEW ANALYSIS RECORD (" + audioID + ") CREATED!", Toast.LENGTH_LONG).show();

        // Remove the "new analysis" fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NewSingleAnalysisFragment frag = (NewSingleAnalysisFragment) fm.findFragmentByTag("analysisFragment");
        ft.remove(frag);
        ft.commit();

        // Update the list fragment
        SingleMetronomeAnalysisListFragment list = (SingleMetronomeAnalysisListFragment)
                fm.findFragmentById(R.id.listfragment);
        list.refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PROFILE_RECORD_REQUEST) {
            if (resultCode == RESULT_OK) {
                long[] ids = data.getLongArrayExtra(SelectProfilesActivity.RESULT_SELECTED_PROFILES);

                if (ids.length != 1) {
                    Toast.makeText(this, "SELECT ONLY ONE PROFILE!", Toast.LENGTH_LONG).show();
                } else {
                    final long profileID = ids[0];

                    FragmentManager fm = getSupportFragmentManager();
                    NewSingleAnalysisFragment frag = (NewSingleAnalysisFragment) fm.findFragmentByTag("analysisFragment");
                    if (frag.loadProfileRecord(profileID)) {
                        Toast.makeText(this, "Profile record loaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Unable to load the selected profile record.", Toast.LENGTH_LONG).show();
                    }

                }
            }
        } else if (requestCode == PICK_AUDIO_RECORD_REQUEST) {
            if (resultCode == RESULT_OK) {
                long[] ids = data.getLongArrayExtra(SelectAudioRecordActivity.RESULT_SELECTED_AUDIO_RECORDS);
                if (ids.length != 1) {
                    Toast.makeText(this, "SELECT ONLY ONE AUDIO RECORD!", Toast.LENGTH_LONG).show();
                } else {
                    final long audioID = ids[0];

                    FragmentManager fm = getSupportFragmentManager();
                    NewSingleAnalysisFragment frag = (NewSingleAnalysisFragment) fm.findFragmentByTag("analysisFragment");
                    if (frag.loadAudioRecord(audioID)) {
                        Toast.makeText(this, "Audio record loaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Unable to load the selected audio record.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
}
