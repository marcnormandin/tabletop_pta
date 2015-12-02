package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import edu.utrgv.cgwa.tabletoppta.Routines;

public class ViewAnalysisPulsesActivity extends AppCompatActivity {
    private static final String TAG = "ViewAnalysis";
    public static final String ARG_ANALYSIS_ID = "analysisID";

    private Toolbar mToolbar;
    private long mAnalysisID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_analysis);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mAnalysisID = intent.getLongExtra(ARG_ANALYSIS_ID, -1);
        if (mAnalysisID == -1) {
            Log.d(TAG, "Attempt to view an analysis, but no analysis ID given in intent.");
        } else {
            Log.d(TAG, "Adding pulse overlay fragment");
            AnalysisPulseOverlayFragment frag = AnalysisPulseOverlayFragment.newInstance(mAnalysisID);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.container, frag);
            ft.commit();


            SingleMetronomeAnalysisManager manager = new SingleMetronomeAnalysisManager(this);
            DbSingleMetronomeAnalysisTable.Entry entry = manager.getEntryByID(mAnalysisID);
            Routines.CalMeasuredTOAsResult result = new Routines.CalMeasuredTOAsResult(entry.filenameResult());

            TextView date = (TextView) findViewById(R.id.analysisdate);
            date.setText("" + entry.date());

            TextView time = (TextView) findViewById(R.id.analysistime);
            time.setText("" + entry.time());

            TextView referencePulseNumber = (TextView) findViewById(R.id.analysisreferencepulsenumber);
            referencePulseNumber.setText("" + result.n0());

            TextView numPulses = (TextView) findViewById(R.id.analysisnumpulses);
            numPulses.setText("" + result.numPulses());
        }
    }
}
