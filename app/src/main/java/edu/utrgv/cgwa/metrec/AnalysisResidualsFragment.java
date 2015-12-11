

package edu.utrgv.cgwa.metrec;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class AnalysisResidualsFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "ResidualsFragment";
    private static final String ARG_ANALYSIS_ID = "audioID";

    private long mAnalysisID = -1;

    private LineChart mLineChart = null;

    private Routines.CalMeasuredTOAsResult mAnalysisResult = null;

    // Fixme
    private static final int MAX_PLOT_POINTS = 40000;

    public static AnalysisResidualsFragment newInstance(final long analysisID) {
        AnalysisResidualsFragment fragment = new AnalysisResidualsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ANALYSIS_ID, analysisID);
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
            mAnalysisID = getArguments().getLong(ARG_ANALYSIS_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_analysis_residuals, container, false);

        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Get the analysis record
                SingleMetronomeAnalysisManager analysisManager = new SingleMetronomeAnalysisManager(getActivity());
                DbSingleMetronomeAnalysisTable.Entry analysisEntry = analysisManager.getEntryByID(mAnalysisID);

                // Load the analysis result from file
                mAnalysisResult = new Routines.CalMeasuredTOAsResult(analysisEntry.filenameResult());

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                setupLineChart(rootView);
                displayResiduals();
            }
        }).execute();

        return rootView;
    }

    public void displayResiduals() {
        final boolean showResiduals = false;

        if (mAnalysisResult == null) {
            return;
        }

        double[] x = mAnalysisResult.measuredTOAs();
        double[] residuals = mAnalysisResult.residuals();
        double[] detrendedResiduals = mAnalysisResult.detrendedResiduals();

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
        for (int i = 0; i < count; i++) {
            float val = (float) detrendedResiduals[i];
            detrendedResidualsVals.add(new Entry(val, i));
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

