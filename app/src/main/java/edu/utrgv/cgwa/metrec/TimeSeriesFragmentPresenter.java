package edu.utrgv.cgwa.metrec;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import edu.utrgv.cgwa.tabletoppta.TimeSeries;

public class TimeSeriesFragmentPresenter {
    private final String TAG = "TimeSeriesPresenter";
    private AudioRecordingModel mAudioRecording;
    private DbAudioRecordingTable.AudioRecordingEntry mEntry;
    private TimeSeriesFragment mFragment;
    private final long mAudioID;

    // Fixme
    private static final int MAX_PLOT_POINTS = 40000;

    public TimeSeriesFragmentPresenter(TimeSeriesFragment frag, final long audioID) {
        mFragment = frag;
        mAudioID = audioID;
        AudioRecordingManager manager = new AudioRecordingManager(frag.getActivity());
        mEntry = manager.getEntryByID(mAudioID);
        mAudioRecording = new AudioRecordingModel(mEntry.filenamePrefix());

        Log.d(TAG, "Timeseries fragment presenter created for: filenamePrefix = "  + mEntry.filenamePrefix());
    }

    void onCreateView() {
        class PreloadViews extends AsyncTask<Void, String, Void> {
            private ProgressDialog mProgressDialog;

            @Override
            protected void onPreExecute() {
                mProgressDialog = ProgressDialog.show(mFragment.getActivity(), "Loading data...", "Reading previous data");
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mProgressDialog.setMessage(values[0]);
            }

            @Override
            protected Void doInBackground(Void... params) {
                publishProgress("Reading previous time series");
                mAudioRecording.getTimeSeries();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgressDialog.setMessage("Drawing time series plot");
                refreshTimeseriesView();

                mProgressDialog.dismiss();
            }
        }

        PreloadViews pl = new PreloadViews();
        pl.execute();
    }

    class RefreshTimeSeriesViewAsync extends AsyncTask<Void, Void, Void> {
        private TimeSeries mTS = null;
        private LineChart mLineChart = null;
        private AudioRecordingModel mAudioRecording = null;

        public RefreshTimeSeriesViewAsync(LineChart lc, AudioRecordingModel audioRecording) {
            mLineChart = lc;
            mAudioRecording = audioRecording;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(mTS == null) {
                return;
            }

            int count = mTS.t.length;
            // Fixme
            if (count > MAX_PLOT_POINTS) {
                count = MAX_PLOT_POINTS;
            }

            ArrayList<String> mXVals = new ArrayList<String>();
            for (int i = 0; i < count; i++) {
                mXVals.add(mTS.t[i] + "");
            }

            ArrayList<Entry> yVals = new ArrayList<Entry>();

            for (int i = 0; i < count; i++) {
                float val = (float) mTS.h[i];
                yVals.add(new Entry(val, i));
            }

            // create a dataset and give it a type
            LineDataSet set1 = new LineDataSet(yVals, "Volume");
            set1.setColor(Color.RED);
            set1.setLineWidth(2f);
            set1.setDrawCircles(false);
            set1.setDrawValues(false);

            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData mLineData = new LineData(mXVals, dataSets);

            mLineChart.setData(mLineData);
            // Fixme
            mLineChart.setVisibleXRangeMaximum(8000);

            // update the axes
            XAxis xaxis = mLineChart.getXAxis();
            xaxis.setValues(mXVals);

            mLineChart.notifyDataSetChanged();
            mLineChart.invalidate();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mAudioRecording.hasTimeSeries()) {
                mTS = mAudioRecording.getTimeSeries();
            }
            return null;
        }
    }

    public void refreshTimeseriesView() {
        Log.d(TAG, "refreshing metronome timeseries view");

        LineChart plot = (LineChart) mFragment.getView().findViewById(R.id.metronomepulseprofiletimeseries);
        if (plot == null) {
            Log.d(TAG, "plot is null!");
        }
        new RefreshTimeSeriesViewAsync(plot, mAudioRecording).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void playSound() {
        mAudioRecording.playRecording(mEntry.samplesPerSecond());
    }
}
