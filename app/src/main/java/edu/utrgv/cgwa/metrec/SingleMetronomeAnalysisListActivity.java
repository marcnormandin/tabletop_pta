package edu.utrgv.cgwa.metrec;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.AlertDialog;

public class SingleMetronomeAnalysisListActivity extends AppCompatActivity implements SingleMetronomeAnalysisListFragment.OnFragmentInteractionListener {
    private Toolbar mToolbar;

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
        Toast.makeText(this, "Position (" + position + "), Analysis ID (" + analysisID + "),  checked = " + isChecked, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewPulseOverlayClicked(int position, long analysisID) {
        Intent intent = new Intent(this, ViewAnalysisPulsesActivity.class);
        intent.putExtra(ViewAnalysisPulsesActivity.ARG_ANALYSIS_ID, analysisID);
        startActivity(intent);
    }

    @Override
    public void onViewResidualsClicked(int position, long analysisID) {
        Intent intent = new Intent(this, ViewAnalysisResidualsActivity.class);
        intent.putExtra(ViewAnalysisResidualsActivity.ARG_ANALYSIS_ID, analysisID);
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
            case R.id.action_new:
                Intent intent = new Intent(this, SingleMetronomeAnalysisActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_delete:
                popupMenuDelete();
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
}
