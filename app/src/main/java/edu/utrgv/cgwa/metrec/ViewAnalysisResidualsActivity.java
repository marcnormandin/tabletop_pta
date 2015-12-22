package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import edu.utrgv.cgwa.tabletoppta.Routines;

public class ViewAnalysisResidualsActivity extends AppCompatActivity
implements AnalysisResidualsFragment.Listener {
    private static final String TAG = "ViewResiduals";
    public static final String ARG_ANALYSIS_ID = "analysisID";
    public static final String ARG_ANALYSIS_FILENAME_RESULT = "analysisFilenameResult";


    private Toolbar mToolbar;
    private long mAnalysisID;
    private String mAnalysisFilenameResult;

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
        mAnalysisFilenameResult = intent.getStringExtra(ARG_ANALYSIS_FILENAME_RESULT);

        if (mAnalysisID == -1) {
            Log.d(TAG, "Attempt to view residuals, but no analysis ID given in intent.");
        } else {
            Log.d(TAG, "Adding residuals fragment");
            AnalysisResidualsFragment frag = AnalysisResidualsFragment.newInstance("notag", mAnalysisFilenameResult);
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

            TextView computationTime = (TextView) findViewById(R.id.computationtimeseconds);
            computationTime.setText("" + String.format("%.2f", result.computationTimeSeconds()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_residuals, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send_screenshot:
                Screenshot.send(this);
                return true;
        }
        return false;
    }

    @Override
    public void onFitParametersUpdated(String tag, double amplitude, double frequency,
                                       double phase, double offset) {

    }
}
