package edu.utrgv.cgwa.metrec;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.utrgv.cgwa.tabletoppta.PulseProfile;
import edu.utrgv.cgwa.tabletoppta.TimeSeries;

public class NewProfileActivityPresenter {
    private final String TAG = "NewProfileActivityPresenter";
    private MetronomeModel mMetronome;
    private NewProfileActivityFragment mFragment;

    private boolean mRecorded = false;

    // Fixme
    private static final int MAX_PLOT_POINTS = 40000;


    public NewProfileActivityPresenter(NewProfileActivityFragment frag, String filenamePrefix) {
        Log.d(TAG, "Constructing a NewProfileActivityPresenter given prefix = " + filenamePrefix);

        mFragment = frag;
        mMetronome = new MetronomeModel(filenamePrefix);
    }

    public void onCreateViews() {


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
                mMetronome.getTimeSeries();
                publishProgress("Reading previous pulse profile");
                mMetronome.getPulseProfile();
                publishProgress("Reading previous self-correlation series");
                mMetronome.getSelfCorrelation();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgressDialog.setMessage("Drawing time series plot");
                refreshTimeseriesView();

                mProgressDialog.setMessage("Drawing pulse profile plot");
                refreshProfileView();

                mProgressDialog.setMessage("Drawing self-correlation plot");
                refreshSelfCorrelationView();

                mProgressDialog.dismiss();
            }
        }

        PreloadViews pl = new PreloadViews();
        pl.execute();

        setupSwitchFrequency();
    }

    private double mFrequency;

    private void setFrequency(double frequency) {
        mFrequency = frequency;
    }

    private double getFrequency() {
        return mFrequency;
    }

    private void setupSwitchFrequency() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mFragment.getActivity());
        final double modeA = Double.parseDouble(sp.getString("mode_a_frequency", "900"));
        final double modeB = Double.parseDouble(sp.getString("mode_b_frequency", "1200"));

        // Frequency SeekBar
        Switch sf = (Switch) mFragment.getActivity().findViewById(R.id.frequencySwitch);
        sf.setTextOn(modeB + "Hz");
        sf.setTextOff(modeA + "Hz");

        sf.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    // mode A
                    setFrequency(modeB);
                } else {
                    // mode B
                    setFrequency(modeA);
                }
            }
        });

        double mode;
        if (sf.isChecked()) {
            mode = modeB;
        } else {
            mode = modeA;
        }

        setFrequency(mode);
    }

    public void close() {
        mMetronome.close();
    }

    public void buttonClicked() {
        if (mRecorded) {
            play();
        } else {
            record();
        }
    }

    public void record() {
        // Be careful because getActivity() may return null if this fragment is not attached
        // to an activity (but rare I think).
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mFragment.getActivity());
        final int sampleRate = Integer.parseInt(sp.getString("samplerate", "8000"));
        final double desiredRuntime = Double.parseDouble(sp.getString("pulserecordingduration", "8.0"));


        class NewRecordingAsync extends AsyncTask<Void, String, Void> {
            private ProgressDialog mProgress = null;
            private double mBeatsPerMinute = 0;

            @Override
            protected void onPreExecute() {
                mProgress = ProgressDialog.show(mFragment.getActivity(), "Working...", "Please wait");

                Button btnRec = (Button) mFragment.getView().findViewById(R.id.btnMain);
                btnRec.setEnabled(false);
                btnRec.setText("Recording...");

                Spinner bpm = (Spinner) mFragment.getView().findViewById(R.id.beatsPerMinuteSpinner);
                mBeatsPerMinute = Double.parseDouble((String)bpm.getSelectedItem());
            }

            @Override
            protected Void doInBackground(Void... params) {
                publishProgress("Recording sound + computing");
                mMetronome.newRecording(sampleRate, desiredRuntime, mBeatsPerMinute);

                // Save the data into the database
                DbHelper mDbHelper = new DbHelper(mFragment.getContext());
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_FILENAMEPREFIX, mMetronome.getFilenamePrefix());
                Date date = new Date();

                String dateString = new SimpleDateFormat("MM-dd-yyyy").format(date);
                values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_DATE, dateString);

                String timeString = new SimpleDateFormat("hh:mm").format(date);
                values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_TIME, timeString);

                values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_BEATS_PER_MINUTE, mMetronome.getPulseProfile().bpm);
                values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_COMPUTED_PERIOD, mMetronome.getPulseProfile().T);
                values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_SAMPLES_PER_SECOND, mMetronome.getTimeSeries().getSampleRate());
                values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_FREQUENCY, getFrequency());
                long newRowId = db.insert(DbProfileTable.ProfileEntry.TABLE_NAME, "null", values);

                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mProgress.setMessage(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Button btnRec = (Button) mFragment.getView().findViewById(R.id.btnMain);
                btnRec.setEnabled(true);
                btnRec.setText("Play");
                mRecorded = true;


                refreshTimeseriesView();
                refreshProfileView();
                refreshSelfCorrelationView();

                mProgress.dismiss();
            }
        }

        new NewRecordingAsync().execute();


    }

    public void play() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mFragment.getActivity());
        final int sampleRate = Integer.parseInt(sp.getString("samplerate", "8000"));
        mMetronome.playRecording(sampleRate);
    }

    class RefreshTimeSeriesViewAsync extends AsyncTask<Void, Void, Void> {
        private TimeSeries mTS = null;
        private LineChart mLineChart = null;
        private MetronomeModel mMetronome = null;

        public RefreshTimeSeriesViewAsync(LineChart lc, MetronomeModel metronome) {
            mLineChart = lc;
            mMetronome = metronome;
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
            if (mMetronome.hasTimeSeries()) {
                mTS = mMetronome.getTimeSeries();
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
        new RefreshTimeSeriesViewAsync(plot, mMetronome).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // Fixme
    class RefreshSelfCorrelationViewAsync extends AsyncTask<Void, Void, Void> {
        private TimeSeries mTS = null;
        private LineChart mLineChart = null;
        private MetronomeModel mMetronome = null;

        public RefreshSelfCorrelationViewAsync(LineChart lc, MetronomeModel metronome) {
            mLineChart = lc;
            mMetronome = metronome;
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
            set1.setColor(Color.GREEN);
            set1.setLineWidth(1f);
            set1.setDrawCircles(false);
            set1.setDrawValues(false);

            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData mLineData = new LineData(mXVals, dataSets);

            mLineChart.setData(mLineData);

            // update the axes
            XAxis xaxis = mLineChart.getXAxis();
            xaxis.setValues(mXVals);

            mLineChart.notifyDataSetChanged();
            mLineChart.invalidate();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mMetronome.hasSelfCorrelation()) {
                mTS = mMetronome.getSelfCorrelation();
            }
            return null;
        }
    }

    public void refreshSelfCorrelationView() {
        Log.d(TAG, "refreshing metronome self-correlation view");

        LineChart plot = (LineChart) mFragment.getView().findViewById(R.id.metronomeselfcorrelation);
        if (plot == null) {
            Log.d(TAG, "plot is null!");
        }
        new RefreshSelfCorrelationViewAsync(plot, mMetronome).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    class RefreshPulseProfileViewAsync extends AsyncTask<Void, Void, Void> {
        private ArrayList<String> mXVals;
        private LineData mLineData;
        private PulseProfile mPF = null;

        @Override
        protected void onPostExecute(Void aVoid) {
            // set data
            LineChart plot = (LineChart) mFragment.getView().findViewById(R.id.metronomepulseprofilefoldedseries);
            plot.setData(mLineData);

            // update the axes
            XAxis xaxis = plot.getXAxis();
            xaxis.setValues(mXVals);

            plot.notifyDataSetChanged();
            plot.invalidate();

            // Update the pulse period text
            TextView tv = (TextView) mFragment.getView().findViewById(R.id.metronomepulseprofileperiod);
            if (mPF != null) {
                tv.setText("Best period: T = " + mPF.T + " (s)");
            }

            // Update the beats per minute spinner
            Spinner bpm = (Spinner) mFragment.getView().findViewById(R.id.beatsPerMinuteSpinner);
            if (mPF != null) {
                // We need to find the array index for the profiles BPM
                for (int i = 0; i < bpm.getCount(); i++) {
                    if (Integer.parseInt((String)bpm.getItemAtPosition(i)) == mPF.bpm) {
                        // i is the correct index
                        bpm.setSelection(i);
                    }
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mMetronome.hasProfile()) {

                mPF = mMetronome.getPulseProfile();

                int count = mPF.ts.t.length;
                // Fixme
                if (count > MAX_PLOT_POINTS) {
                    count = MAX_PLOT_POINTS;
                }

                mXVals = new ArrayList<String>();
                for (int i = 0; i < count; i++) {
                    mXVals.add(mPF.ts.t[i] + "");
                }

                ArrayList<Entry> yVals = new ArrayList<Entry>();

                for (int i = 0; i < count; i++) {
                    float val = (float) mPF.ts.h[i];
                    yVals.add(new Entry(val, i));
                }

                // create a dataset and give it a type
                LineDataSet set1 = new LineDataSet(yVals, "Normalized Volume");
                set1.setColor(Color.BLUE);
                set1.setLineWidth(1f);
                set1.setDrawCircles(false);

                ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
                dataSets.add(set1); // add the datasets

                // create a data object with the datasets
                mLineData = new LineData(mXVals, dataSets);
            }

            return null;
        }
    }

    public void refreshProfileView() {
        Log.d(TAG, "refreshing metronome profile view");

        new RefreshPulseProfileViewAsync().execute();
    }
}
