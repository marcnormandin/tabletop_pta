package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


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
    public void onDisplayProfileClicked(int position) {
        ProfileManager manager = new ProfileManager(this);
        DbProfileTable.ProfileEntry profile = manager.getProfileEntryByPosition(position);

        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra(ProfileFragment.ARG_PROFILEID, profile.id());
        startActivity(intent);
    }

    @Override
    public void onDisplayTimeSeriesClicked(int position) {
        ProfileManager manager = new ProfileManager(this);
        DbProfileTable.ProfileEntry profile = manager.getProfileEntryByPosition(position);

        Intent intent = new Intent(this, ViewTimeSeriesActivity.class);
        intent.putExtra(ProfileFragment.ARG_PROFILEID, profile.id());
        startActivity(intent);
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
        }
        return false;
    }
}