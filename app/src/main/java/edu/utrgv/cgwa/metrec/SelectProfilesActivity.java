package edu.utrgv.cgwa.metrec;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class SelectProfilesActivity extends AppCompatActivity implements ProfileListFragment.OnFragmentInteractionListener {
    private ProfileListFragment mProfileList;
    public static final String RESULT_SELECTED_PROFILES = "profileIDs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_profiles);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProfileList = (ProfileListFragment) getSupportFragmentManager().findFragmentById(R.id.listfragment);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ask the fragment for the selected profiles
                long[] ids = mProfileList.getSelectedProfileIDs();

                // Return the result
                Intent result = new Intent();
                result.putExtra(RESULT_SELECTED_PROFILES, ids);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
    }

    @Override
    public void onDisplayProfileClicked(long profileID) {

    }

    @Override
    public void onDisplayTimeSeriesClicked(long audioID) {

    }

    @Override
    public void onCheckboxChanged(int position, long audioID, long profileID, boolean isChecked) {

    }
}
