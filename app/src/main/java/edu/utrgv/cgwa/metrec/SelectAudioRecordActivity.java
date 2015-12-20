package edu.utrgv.cgwa.metrec;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class SelectAudioRecordActivity extends AppCompatActivity implements AudioRecordListFragment.OnFragmentInteractionListener {
    private static final String TAG = "SelectAudioRecord";
    private AudioRecordListFragment mList;
    public static final String RESULT_SELECTED_AUDIO_RECORDS = "audioIDs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_audio_record);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mList = (AudioRecordListFragment) getSupportFragmentManager().findFragmentById(R.id.listfragment);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ask the fragment for the selected audio records
                long[] ids = mList.getSelectedEntryIDs();
                if (ids == null) {
                    Log.d(TAG, "Returned ID array is null!");
                } else {
                    for (int i = 0; i < ids.length; i++) {
                        Log.d(TAG, "selected id: " + ids[i]);
                    }
                }

                // Return the result
                Intent result = new Intent();
                result.putExtra(RESULT_SELECTED_AUDIO_RECORDS, ids);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
    }

    @Override
    public void onDisplayTimeSeriesClicked(long audioID) {

    }

    @Override
    public void onCheckboxChanged(int position, long audioID, boolean isChecked) {
        //Toast.makeText(this, "Audio record (" + audioID + ") changed.", Toast.LENGTH_LONG).show();
    }
}
