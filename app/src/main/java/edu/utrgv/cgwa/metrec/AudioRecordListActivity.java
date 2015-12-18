package edu.utrgv.cgwa.metrec;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


public class AudioRecordListActivity extends AppCompatActivity implements AudioRecordListFragment.OnFragmentInteractionListener {
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
        switch(item.getItemId()) {
            case R.id.action_new:
                Toast.makeText(this, "Adding audio is not yet implemented", Toast.LENGTH_LONG).show();
                return true;

            case R.id.action_delete:
                // Make sure that there are items checked
                FragmentManager fm = getSupportFragmentManager();
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
    }
}
