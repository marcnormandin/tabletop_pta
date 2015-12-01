package edu.utrgv.cgwa.metrec;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import edu.utrgv.cgwa.tabletoppta.PulseProfile;
import edu.utrgv.cgwa.tabletoppta.Routines;
import edu.utrgv.cgwa.tabletoppta.TimeSeries;

public class ViewPulseOverlayFragment extends Fragment {
    private static final String TAG = "ViewPulseOverlay";
    private static final String ARG_ANALYSIS_ID = "audioID";

    private long mAnalysisID = -1;

    private double[] mPulseTimes = null;

    private Spinner mPulseSpinner = null;
    private LineChart mLineChart = null;

    private TimeSeries mTimeSeries = null;
    private PulseProfile mPulseProfile = null;

    // Fixme
    private static final int MAX_PLOT_POINTS = 40000;

    public static ViewPulseOverlayFragment newInstance(final long analysisID) {
        ViewPulseOverlayFragment fragment = new ViewPulseOverlayFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ANALYSIS_ID, analysisID);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewPulseOverlayFragment() {
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
                Routines.CalMeasuredTOAsResult analysisResult = new Routines.CalMeasuredTOAsResult(analysisEntry.filenameResult());
                mPulseTimes = analysisResult.measuredTOAs();

                // Get the audio record
                final long audioID = analysisEntry.audioID();
                AudioRecordingManager audioManager = new AudioRecordingManager(getActivity());
                DbAudioRecordingTable.AudioRecordingEntry audioEntry = audioManager.getEntryByID(audioID);
                mTimeSeries = new AudioRecordingModel(audioEntry.filenamePrefix()).getTimeSeries();

                // Get the pulse profile record
                final long profileID = analysisEntry.profileID();
                ProfileManager profileManager = new ProfileManager(getActivity());
                DbProfileTable.ProfileEntry profileEntry = profileManager.getEntryByID(profileID);
                mPulseProfile = new ProfileModel(profileEntry.filenamePrefix()).getPulseProfile();

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

    public void displayPulse(double pulseTime) {
        if(mTimeSeries == null) {
            return;
        }

        // The number of points to display
        int count = mPulseProfile.ts.t.length;
        // Fixme
        if (count > MAX_PLOT_POINTS) {
            count = MAX_PLOT_POINTS;
        }

        // Get the closest time index for the pulseTime
        double dt = 1.0 / mTimeSeries.getSampleRate();
        int startIndex = (int) Math.round(pulseTime / dt);

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


        // Pulse series
        ArrayList<Entry> pulseVals = new ArrayList<Entry>();
        for (int i = 0; i < count; i++) {
            float val = (float) mPulseProfile.ts.h[i];
            pulseVals.add(new Entry(val, i));
        }

        // create the pulse dataset and give it a type
        LineDataSet pulseSet = new LineDataSet(pulseVals, "Pulse");
        pulseSet.setColor(Color.RED);
        pulseSet.setLineWidth(1f);
        pulseSet.setDrawCircles(false);
        pulseSet.setDrawValues(false);


        // Add the datasets for display
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(audioSet);
        dataSets.add(pulseSet);

        // create a data object with the datasets
        LineData mLineData = new LineData(timeVals, dataSets);

        mLineChart.setData(mLineData);
        // Fixme
        //mLineChart.setVisibleXRangeMaximum(8000);

        // update the axes
        XAxis xaxis = mLineChart.getXAxis();
        xaxis.setValues(timeVals);

        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
    }

    public void setupSpinner(View rootView) {
        mPulseSpinner = (Spinner) rootView.findViewById(R.id.spinnerPulseNumber);

        ArrayList<Double> pulseArray = new ArrayList<>();
        for (int i = 0; i < mPulseTimes.length; i++) {
            pulseArray.add( mPulseTimes[i] );
        }

        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, pulseArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPulseSpinner.setAdapter(spinnerArrayAdapter);

        mPulseSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the value
                double pulseTime = (Double) mPulseSpinner.getSelectedItem();

                displayPulse(pulseTime);

                Log.d(TAG, "Selected pulsetime = " + pulseTime);
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
