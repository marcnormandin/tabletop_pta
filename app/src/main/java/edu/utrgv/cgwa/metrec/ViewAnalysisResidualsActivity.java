package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import edu.utrgv.cgwa.tabletoppta.Routines;
import edu.utrgv.cgwa.tabletoppta.Utility;

public class ViewAnalysisResidualsActivity extends AppCompatActivity
implements FitSinusoidFragment.Listener, AnalysisResidualsFragment.Listener {
    public static final String ARG_ANALYSIS_ID = "analysisID";
    public static final String ARG_ANALYSIS_FILENAME_RESULT = "analysisFilenameResult";


    private Toolbar mToolbar;
    private long mAnalysisID;
    private String mAnalysisFilenameResult;

    private final String mControlOneTag = "fitOneTag";
    private final String mChartOneTag = "chartOneTag";
    private FitSinusoidFragment mControlOne;
    private AnalysisResidualsFragment mChartOne;
    private InfoPanelFragment mInfoPanel;

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
            // Fixme
        } else {
            SingleMetronomeAnalysisManager manager = new SingleMetronomeAnalysisManager(this);
            DbSingleMetronomeAnalysisTable.Entry entry = manager.getEntryByID(mAnalysisID);
            Routines.CalMeasuredTOAsResult result = new Routines.CalMeasuredTOAsResult(entry.filenameResult());

            //AnalysisResidualsFragment frag = AnalysisResidualsFragment.newInstance("notag", mAnalysisFilenameResult);
            FragmentManager fm = getSupportFragmentManager();

            mControlOne = FitSinusoidFragment.newInstance(mControlOneTag);
            mChartOne = AnalysisResidualsFragment.newInstance(mChartOneTag, entry.filenameResult());
            //mInfoPanel = InfoPanelFragment.newInstance();

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.container, mControlOne, mControlOneTag);
            ft.add(R.id.container, mChartOne, mChartOneTag);

            //ft.add(R.id.container, mInfoPanel, "infopanel");

            ft.commit();


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

            //TextView timePerSample = (TextView) findViewById(R.id.timepersample);
            //timePerSample.setText("" + String.format("%.2f", result.computationTimeSeconds()));
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

    // Called by the control fragment
    @Override
    public void onFit(String controlTag, double amplitude, double frequency) {
        //Toast.makeText(this, "FIT: id = " + controlTag + ", amp = " + amplitude + ", freq = " + frequency,
        //        Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Fit updated", Toast.LENGTH_SHORT).show();

        FragmentManager fm = getSupportFragmentManager();
        AnalysisResidualsFragment f;
        f = (AnalysisResidualsFragment) fm.findFragmentByTag(mChartOneTag);
        f.computeFit(amplitude, frequency, true);
    }

    // Called by the chart fragment (which updates the fit parameters)
    @Override
    public void onFitParametersUpdated(String tag, double amplitude, double frequency,
                                       double phase, double offset) {
        // Update the controls
        FragmentManager fm = getSupportFragmentManager();
        FitSinusoidFragment frag;
        frag = (FitSinusoidFragment) fm.findFragmentByTag(mControlOneTag);
        frag.setAmplitude(amplitude);
        frag.setFrequency(frequency);

    }
}
