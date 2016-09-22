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

public class ViewDoubleAnalysisResidualsActivity extends AppCompatActivity
implements FitSinusoidFragment.Listener, AnalysisResidualsFragment.Listener {
    public static final String ARG_ANALYSIS_ID = "analysisID";

    private Toolbar mToolbar;
    private long mAnalysisID;

    private final String mControlOneTag = "fitOneTag";
    private final String mChartOneTag = "chartOneTag";

    private final String mControlTwoTag = "fitTwoTag";
    private final String mChartTwoTag = "chartTwoTag";

    private FitSinusoidFragment mControlOne;
    private AnalysisResidualsFragment mChartOne;

    private FitSinusoidFragment mControlTwo;
    private AnalysisResidualsFragment mChartTwo;

    private TextView mTextCorrelation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_double_analysis_residuals);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mAnalysisID = intent.getLongExtra(ARG_ANALYSIS_ID, -1);

        mTextCorrelation = (TextView) findViewById(R.id.textCorrelation);

        if (mAnalysisID == -1) {
            // Fixme
        } else {
            DoubleMetronomeAnalysisManager manager = new DoubleMetronomeAnalysisManager(this);
            DbDoubleMetronomeAnalysisTable.Entry entry = manager.getEntryByID(mAnalysisID);

            mControlOne = FitSinusoidFragment.newInstance(mControlOneTag);
            mChartOne = AnalysisResidualsFragment.newInstance(mChartOneTag, entry.filenameResultOne());

            mControlTwo = FitSinusoidFragment.newInstance(mControlTwoTag);
            mChartTwo = AnalysisResidualsFragment.newInstance(mChartTwoTag, entry.filenameResultTwo());

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            // First plot
            ft.add(R.id.containerOne, mControlOne, mControlOneTag);
            ft.add(R.id.containerOne, mChartOne, mChartOneTag);

            // Second plot
            ft.add(R.id.containerTwo, mControlTwo, mControlTwoTag);
            ft.add(R.id.containerTwo, mChartTwo, mChartTwoTag);

            ft.commit();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send_screenshot:
                Screenshot.send(this);
                return true;
        }
        return false;
    }

    // Called by the FitSinusoidFragment when the "FIT" button is clicked on
    @Override
    public void onFit(String controlTag, double amplitude, double frequency) {
        //Toast.makeText(this, "FIT: id = " + controlTag + ", amp = " + amplitude + ", freq = " + frequency,
        //        Toast.LENGTH_SHORT).show();

        Toast.makeText(this, "Fit updated", Toast.LENGTH_SHORT).show();

        FragmentManager fm = getSupportFragmentManager();
        AnalysisResidualsFragment f;
        if (controlTag.equals(mControlOneTag)) {
            f = (AnalysisResidualsFragment) fm.findFragmentByTag(mChartOneTag);
        } else {
            f = (AnalysisResidualsFragment) fm.findFragmentByTag(mChartTwoTag);
        }
        f.computeFit(amplitude, frequency, true);
    }

    // Called by the AnalysisResidualsFragment after it computes the fit parameters
    @Override
    public void onFitParametersUpdated(String tag, double amplitude, double frequency,
                                       double phase, double offset) {
        // Update the controls
        FragmentManager fm = getSupportFragmentManager();
        FitSinusoidFragment frag;
        if (tag.equals(mChartOneTag)) {
            frag = (FitSinusoidFragment) fm.findFragmentByTag(mControlOneTag);
        } else {
            frag = (FitSinusoidFragment) fm.findFragmentByTag(mControlTwoTag);
        }
        frag.setAmplitude(amplitude);
        frag.setFrequency(frequency);

        updateCorrelation();
    }

    private double[] genSinusoid(final AnalysisResidualsFragment frag) {
        final int N = 1000;

        // Fixme
        // This should use a range around the two recording times
        double[] t = Utility.linspace(0.0, 8.0, N);

        final double a = frag.getAmplitude();
        final double f = frag.getFrequency();
        final double p = frag.getPhase();
        final double o = frag.getOffset();

        return Routines.genSinusoid(t, a, f, p, o);
    }

    private void updateCorrelation() {
        double[] sinusoidOne = genSinusoid(mChartOne);
        double[] sinusoidTwo = genSinusoid(mChartTwo);
        double corr = Routines.computeCorrelation(sinusoidOne, sinusoidTwo);

        mTextCorrelation.setText("Correlation\n (" + corr + ")");
    }
}

