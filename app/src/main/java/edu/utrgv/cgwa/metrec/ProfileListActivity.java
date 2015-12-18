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


public class ProfileListActivity extends AppCompatActivity implements ProfileListFragment.OnFragmentInteractionListener {
    private Toolbar mToolbar;

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
        Intent intent = new Intent(this, ViewTimeSeriesActivity.class);
        intent.putExtra(TimeSeriesFragment.ARG_AUDIOID, audioID);
        startActivity(intent);
    }

    @Override
    public void onCheckboxChanged(final int position, final long audioID, final long profileID, final boolean isChecked) {
        Toast.makeText(this, "Position (" + position + "), AudioID (" + audioID
                + "), ProfileID (" + profileID + "),  checked = " + isChecked, Toast.LENGTH_SHORT).show();
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
            case R.id.action_newprofile:
                Intent intent = new Intent(this, NewProfileActivity.class);
                startActivity(intent);
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
}
