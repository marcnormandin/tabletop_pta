package edu.utrgv.cgwa.metrec;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import edu.utrgv.cgwa.tabletoppta.PulseProfile;
import edu.utrgv.cgwa.tabletoppta.Routines;
import edu.utrgv.cgwa.tabletoppta.TimeSeries;

public class AnalysisPulseOverlayFragment extends Fragment {
    private static final String ARG_ANALYSIS_ID = "audioID";

    private long mAnalysisID = -1;

    private Routines.CalMeasuredTOAsResult mAnalysisResult = null;
    private double[] mPulseTimes = null;

    private Spinner mPulseSpinner = null;
    private LineChart mLineChart = null;

    private TimeSeries mTimeSeries = null;
    private PulseProfile mPulseProfile = null;

    // Fixme
    private static final int MAX_PLOT_POINTS = 40000;

    public static AnalysisPulseOverlayFragment newInstance(final long analysisID) {
        AnalysisPulseOverlayFragment fragment = new AnalysisPulseOverlayFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ANALYSIS_ID, analysisID);
        fragment.setArguments(args);
        return fragment;
    }

    public AnalysisPulseOverlayFragment() {
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
        final View rootView = inflater.inflate(R.layout.fragment_view_pulse_overlay, container, false);

        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Get the analysis record
                SingleMetronomeAnalysisManager analysisManager = new SingleMetronomeAnalysisManager(getActivity());
                DbSingleMetronomeAnalysisTable.Entry analysisEntry = analysisManager.getEntryByID(mAnalysisID);

                // Load the analysis result from file
                mAnalysisResult = new Routines.CalMeasuredTOAsResult(analysisEntry.filenameResult());
                mPulseTimes = mAnalysisResult.measuredTOAs();

                // Get the audio record
                final long audioID = analysisEntry.audioID();
                AudioRecordingManager audioManager = new AudioRecordingManager(getActivity());
                try {
                    DbAudioRecordingTable.AudioRecordingEntry audioEntry = audioManager.getEntryByID(audioID);
                    mTimeSeries = new AudioRecordingModel(audioEntry.filenamePrefix()).getTimeSeries();
                }
                catch (AudioRecordingManager.InvalidRecordException e) {
                    // Fixme
                }

                // Get the pulse profile record
                final long profileID = analysisEntry.profileID();
                ProfileManager profileManager = new ProfileManager(getActivity());
                DbProfileTable.ProfileEntry profileEntry = profileManager.getEntryByID(profileID);
                mPulseProfile = new ProfileModel(profileEntry.filenamePF()).getPulseProfile();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                setupSpinner(rootView);
                setupLineChart(rootView);
            }
        }).execute();

        return rootView;
    }

    public void displayPulse(final int pulseNumber) {
        // Fixme
        // This function should still display the location
        // of the pulses, even if the audio has been deleted.
        if(mTimeSeries == null) {
            return;
        }

        double dt = 1.0 / mTimeSeries.getSampleRate();

        // Measured pulse time
        double measuredPulseTime = mAnalysisResult.measuredTOAs()[pulseNumber];
        // Get the closest time index for the measured pulse time
        int measuredStartIndex = (int) Math.round(measuredPulseTime / dt);

        // Expected pulse time
        double expectedPulseTime = mAnalysisResult.expectedTOAs()[pulseNumber];
        // Get the closest time index for the expected pulse time
        int expectedStartIndex = (int) Math.round(expectedPulseTime / dt);


        // Pick the start time with the smallest index so that
        // both pulses can be drawn
        int startIndex = -1;
        int count = -1;
        int paddingLeft = 100; // number of samples before the measured/expected indicies

        if (expectedStartIndex < measuredStartIndex) {
            startIndex = expectedStartIndex - paddingLeft;
            if (startIndex < 0) {
                startIndex = 0;
            }

            count = (measuredStartIndex - expectedStartIndex) + mPulseProfile.ts.t.length + paddingLeft;
        } else {
            startIndex = measuredStartIndex - paddingLeft;
            if (startIndex < 0) {
                startIndex = 0;
            }
            count = (expectedStartIndex - measuredStartIndex) + mPulseProfile.ts.t.length + paddingLeft;
        }

        if (count > MAX_PLOT_POINTS) {
            count = MAX_PLOT_POINTS;
        }


        // If the pulse goes outside of the time series, we need to
        // readjust the count
        if (startIndex + count >= mTimeSeries.t.length-1) {
            // Fixme Verify this math
            count = mTimeSeries.t.length - startIndex - 1;
        }

        // The time values (X-Axis) to display on the plot
        ArrayList<String> timeVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            int timeIndex = startIndex + i;
            timeVals.add(mTimeSeries.t[timeIndex] + "");
        }

        // Determine the scale value to use because
        // the audio has values -30,000 to +30,000 and the
        // pulse profile is normalized
        double max = 0;
        for (int i = 0; i < count; i++) {
            int timeIndex = startIndex + i;
            if (mTimeSeries.h[timeIndex] > max) {
                max = mTimeSeries.h[timeIndex];
            }
        }
        double scaleFactor = 1.0 / max;

        // Audio series
        ArrayList<Entry> audioVals = new ArrayList<Entry>();
        for (int i = 0; i < count; i++) {
            int timeIndex = startIndex + i;
            float val = (float) (mTimeSeries.h[timeIndex] * scaleFactor);
            audioVals.add(new Entry(val, i));
        }

        // create the audio dataset and give it a type
        LineDataSet audioSet = new LineDataSet(audioVals, "Audio");
        audioSet.setColor(Color.BLUE);
        audioSet.setLineWidth(2f);
        audioSet.setDrawCircles(false);
        audioSet.setDrawValues(false);



        // Correlation series
        ArrayList<Entry> correlationVals = new ArrayList<Entry>();
        double[] correlation = mAnalysisResult.correlation();
        double maxCorrelation = correlation[mAnalysisResult.ind0()];

        for (int i = 0; i < count; i++) {
            int timeIndex = startIndex + i;
            float val = (float) (correlation[timeIndex] / maxCorrelation);
            correlationVals.add(new Entry(val, i));
        }

        // create the correlation dataset and give it a type
        LineDataSet correlationSet = new LineDataSet(correlationVals, "Correlation");
        correlationSet.setColor(Color.MAGENTA);
        correlationSet.setLineWidth(2f);
        correlationSet.setDrawCircles(false);
        correlationSet.setDrawValues(false);



        // Measured pulse series
        ArrayList<Entry> measuredVals = new ArrayList<Entry>();
        for (int i = 0; i < count; i++) {
            int timeIndex = startIndex + i;
            if ((timeIndex < measuredStartIndex)
                    || (timeIndex >= (measuredStartIndex+mPulseProfile.ts.t.length))) {
                measuredVals.add(new Entry(0.0f, i));
            } else {
                float val = (float) mPulseProfile.ts.h[timeIndex-measuredStartIndex];
                measuredVals.add(new Entry(val, i));
            }
        }

        // create the pulse dataset and give it a type
        LineDataSet measuredSet = new LineDataSet(measuredVals, "Measured");
        measuredSet.setColor(Color.RED);
        measuredSet.setLineWidth(1f);
        measuredSet.setDrawCircles(false);
        measuredSet.setDrawValues(false);

        // Measured pulse time (Show a vertical bar)
        //LimitLine llMeasured = new LimitLine((float)measuredPulseTime, "Measured Pulse Time");
        //LimitLine llMeasured = new LimitLine((float)(measuredStartIndex-startIndex), "Measured Pulse Time");
        LimitLine llMeasured = new LimitLine((float)(measuredPulseTime / dt - startIndex), "Measured Pulse Time");
        llMeasured.setLineWidth(2f);
        llMeasured.enableDashedLine(10f, 10f, 0f);
        llMeasured.setLabel("");
        llMeasured.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        llMeasured.setTextSize(10f);
        llMeasured.setLineColor(Color.RED);

        // Expected pulse series
        ArrayList<Entry> expectedVals = new ArrayList<Entry>();
        for (int i = 0; i < count; i++) {
            int timeIndex = startIndex + i;
            if ((timeIndex < expectedStartIndex)
                    || (timeIndex >= (expectedStartIndex+mPulseProfile.ts.t.length))) {
                expectedVals.add(new Entry(0.0f, i));
            } else {
                float val = (float) mPulseProfile.ts.h[timeIndex-expectedStartIndex];
                expectedVals.add(new Entry(val, i));
            }
        }

        // create the pulse dataset and give it a type
        LineDataSet expectedSet = new LineDataSet(expectedVals, "Expected");
        expectedSet.setColor(Color.GREEN);
        expectedSet.setLineWidth(2f);
        expectedSet.setDrawCircles(false);
        expectedSet.setDrawValues(false);

        // Expected pulse time (Show a vertical bar)
        //LimitLine llExpected = new LimitLine((float)expectedPulseTime, "Expected Pulse Time");
        //LimitLine llExpected = new LimitLine(100.0f, "Expected Pulse Time");
        //LimitLine llExpected = new LimitLine((float)(expectedStartIndex-startIndex), "Expected Pulse Time");
        LimitLine llExpected = new LimitLine((float)(expectedPulseTime/dt - startIndex), "Expected Pulse Time");
        llExpected.setLineWidth(2f);
        llExpected.enableDashedLine(10f, 10f, 0f);
        llExpected.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        llExpected.setLabel("");
        llExpected.setTextSize(10f);
        llExpected.setLineColor(Color.GREEN);


        // Add the datasets for display
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(audioSet);
        dataSets.add(measuredSet);
        dataSets.add(expectedSet);
        dataSets.add(correlationSet);

        // create a data object with the datasets
        LineData mLineData = new LineData(timeVals, dataSets);

        mLineChart.setData(mLineData);
        // Fixme
        //mLineChart.setVisibleXRangeMaximum(8000);

        // update the axes
        XAxis xaxis = mLineChart.getXAxis();
        xaxis.setValues(timeVals);
        xaxis.removeAllLimitLines();
        xaxis.addLimitLine(llExpected);
        xaxis.addLimitLine(llMeasured);

        float maxVisible = (float)Math.floor(0.2*mPulseProfile.ts.t.length);
        mLineChart.setVisibleXRangeMaximum(maxVisible);

        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
    }

    public void setupSpinner(View rootView) {
        mPulseSpinner = (Spinner) rootView.findViewById(R.id.spinnerPulseNumber);

        ArrayList<String> pulseArray = new ArrayList<>();
        for (int i = 0; i < mPulseTimes.length; i++) {
            pulseArray.add( "Pulse " + (i+1) + ": (" + String.format("%.2f", mPulseTimes[i]) + " s)" );
        }

        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getActivity(), R.layout.spinner_layout, pulseArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPulseSpinner.setAdapter(spinnerArrayAdapter);

        mPulseSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int pulseNumber = mPulseSpinner.getSelectedItemPosition();
                displayPulse(pulseNumber);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setupLineChart(View rootView) {
        mLineChart = (LineChart) rootView.findViewById(R.id.charttimeseries);

        mLineChart.setDescription("");
        mLineChart.setNoDataTextDescription("Error");
        mLineChart.setBackgroundColor(Color.WHITE);
        mLineChart.setDrawGridBackground(true);
        mLineChart.setDrawMarkerViews(false);
    }
}
