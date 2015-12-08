package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


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
    public void onDisplayTimeSeriesClicked(long audioID) {
        Intent intent = new Intent(this, ViewTimeSeriesActivity.class);
        intent.putExtra(TimeSeriesFragment.ARG_AUDIOID, audioID);
        startActivity(intent);
    }

    @Override
    public void onCheckboxChanged(int position, long audioID, boolean isChecked) {

    }
}
