package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

public class ViewAnalysisResidualsActivity extends AppCompatActivity {
    private static final String TAG = "ViewResiduals";
    public static final String ARG_ANALYSIS_ID = "analysisID";

    private Toolbar mToolbar;
    private long mAnalysisID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_analysis_residuals);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mAnalysisID = intent.getLongExtra(ARG_ANALYSIS_ID, -1);
        if (mAnalysisID == -1) {
            Log.d(TAG, "Attempt to view residuals, but no analysis ID given in intent.");
        } else {
            Log.d(TAG, "Adding residuals fragment");
            AnalysisResidualsFragment frag = AnalysisResidualsFragment.newInstance(mAnalysisID);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.container, frag);
            ft.commit();


            SingleMetronomeAnalysisManager manager = new SingleMetronomeAnalysisManager(this);
            DbSingleMetronomeAnalysisTable.Entry entry = manager.getEntryByID(mAnalysisID);

            TextView date = (TextView) findViewById(R.id.analysisdate);
            date.setText("Date: " + entry.date());

            TextView time = (TextView) findViewById(R.id.analysistime);
            time.setText("Time: " + entry.time());
        }
    }
}
