

package edu.utrgv.cgwa.metrec;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import edu.utrgv.cgwa.tabletoppta.Routines;
import edu.utrgv.cgwa.tabletoppta.Utility;

public class AnalysisResidualsFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "ResidualsFragment";
    private static final String ARG_ANALYSIS_FILENAME_RESULT = "analysisFilenameResult";

    private String mAnalysisFilenameResult;

    private LineChart mLineChart = null;

    private Routines.CalMeasuredTOAsResult mAnalysisResult = null;

    // Fixme
    private static final int MAX_PLOT_POINTS = 40000;

    public static AnalysisResidualsFragment newInstance(final String analysisFilenameResult) {
        AnalysisResidualsFragment fragment = new AnalysisResidualsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ANALYSIS_FILENAME_RESULT, analysisFilenameResult);
        fragment.setArguments(args);
        return fragment;
    }

    public AnalysisResidualsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAnalysisFilenameResult = getArguments().getString(ARG_ANALYSIS_FILENAME_RESULT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_analysis_residuals, container, false);

        (new AsyncTask<Void, Void, Void>() {
            private double[] mSinusoidParameters;

            @Override
            protected Void doInBackground(Void... params) {
                // Load the analysis result from file
                Log.d(TAG, "Loading result: " + mAnalysisFilenameResult);
                mAnalysisResult = new Routines.CalMeasuredTOAsResult( mAnalysisFilenameResult );

                // Compute the sinusoid parameters
                mSinusoidParameters = Routines.fitSinusoid(mAnalysisResult.measuredTOAs(), mAnalysisResult.detrendedResiduals());

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                setupLineChart(rootView);
                displayResiduals(mSinusoidParameters);
            }
        }).execute();

        return rootView;
    }

    public void displayResiduals(double[] sinusoidParameters) {
        final boolean showResiduals = false;

        if (mAnalysisResult == null) {
            return;
        }

        double[] toas = mAnalysisResult.measuredTOAs();
        double[] x = Utility.linspace(toas[0], toas[toas.length-1], 1000);

        double[] residuals = mAnalysisResult.residuals();

        // Fixme
        // Correct: double[] detrendedResiduals = mAnalysisResult.detrendedResiduals();

        double[] detrendedResiduals = mAnalysisResult.residuals();

        /*double[] detrendedResiduals = new double[toas.length];
        // Make sinusoid
        for (int i = 0; i < toas.length; i++) {
            double p0 = 2.0e-4;
            double p1 = 0.4;
            double p2 = 0.0;
            double p3 = 0.00005;

            double max = 5.0e-5;
            double min = -5.0e-5;
            double range = (max - min);
            double r = Math.random() * range + min;
            Log.d(TAG, "r = " + r);

            detrendedResiduals[i] = p0*Math.sin(2.0*Math.PI*toas[i]*p1 + p2) + p3 + r;
        }
        sinusoidParameters = Routines.fitSinusoid(toas, detrendedResiduals);
        */


        // The number of points to display
        int count = x.length;
        // Fixme
        if (count > MAX_PLOT_POINTS) {
            count = MAX_PLOT_POINTS;
        }

        // Add the datasets for display
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();

        // The time values (X-Axis) to display on the plot
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add(x[i] + "");
        }

        if (showResiduals) {
            // Residual series
            ArrayList<Entry> residualsVals = new ArrayList<Entry>();
            for (int i = 0; i < count; i++) {
                float val = (float) residuals[i];
                residualsVals.add(new Entry(val, i));
            }

            // create the audio dataset and give it a type
            LineDataSet residualsSet = new LineDataSet(residualsVals, "Residuals");
            residualsSet.setColor(Color.GRAY);
            residualsSet.setCircleColor(Color.DKGRAY);
            residualsSet.setLineWidth(0f);
            residualsSet.setDrawCircles(true);
            residualsSet.setDrawValues(false);
            dataSets.add(residualsSet);
        }

        // Detrended Residual series
        ArrayList<Entry> detrendedResidualsVals = new ArrayList<Entry>();
        for (int i = 0; i < detrendedResiduals.length; i++) {
            float val = (float) detrendedResiduals[i];
            // find which index in the x-array our i resides at
            int j;
            for (j = 0; j < count; j++) {
                if (toas[i] <= x[j]) break;
            }
            detrendedResidualsVals.add(new Entry(val, j));
        }

        // create the audio dataset and give it a type
        LineDataSet detrendedResidualsSet = new LineDataSet(detrendedResidualsVals, "Detrended residuals");
        detrendedResidualsSet.setColor(Color.BLUE);
        detrendedResidualsSet.setCircleColor(Color.RED);
        detrendedResidualsSet.setCircleColorHole(Color.RED);
        detrendedResidualsSet.setLineWidth(1f);
        detrendedResidualsSet.setDrawCircles(true);
        detrendedResidualsSet.setDrawValues(false);
        detrendedResidualsSet.setDrawCircleHole(true);
        dataSets.add(detrendedResidualsSet);


        // Fitted sinsoid series
        ArrayList<Entry> sinusoidVals = new ArrayList<Entry>();
        for (int i = 0; i < count; i++) {
            double p0 = sinusoidParameters[0];
            double p1 = sinusoidParameters[1];
            double p2 = sinusoidParameters[2];
            double p3 = sinusoidParameters[3];
            double val = p0 * Math.sin(2.0*Math.PI*x[i]*p1 + p2) + p3;
            sinusoidVals.add(new Entry((float)val, i));
        }

        // create the audio dataset and give it a type
        LineDataSet sinusoidSet = new LineDataSet(sinusoidVals, "Fit");
        sinusoidSet.setColor(Color.RED);
        sinusoidSet.setCircleColor(Color.RED);
        sinusoidSet.setCircleColorHole(Color.RED);
        sinusoidSet.setLineWidth(1f);
        sinusoidSet.setDrawCircles(false);
        sinusoidSet.setDrawValues(false);
        sinusoidSet.setDrawCircleHole(false);
        dataSets.add(sinusoidSet);

        // create a data object with the datasets
        LineData mLineData = new LineData(xVals, dataSets);

        mLineChart.setData(mLineData);
        // Fixme
        //mLineChart.setVisibleXRangeMaximum(8000);

        // update the axes
        XAxis xaxis = mLineChart.getXAxis();
        xaxis.setValues(xVals);

        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
    }

    public void setupLineChart(View rootView) {
        mLineChart = (LineChart) rootView.findViewById(R.id.charttimeseries);

        mLineChart.setDescription("");
        mLineChart.setNoDataTextDescription("Error");
        mLineChart.setBackgroundColor(Color.WHITE);
        mLineChart.setDrawGridBackground(true);
        mLineChart.setDrawMarkerViews(true);
    }
}

