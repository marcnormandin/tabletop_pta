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

import edu.utrgv.cgwa.tabletoppta.PulseProfile;

public class ProfileFragmentPresenter {
    private final String TAG = "ProfilePresenter";
    private MetronomeModel mMetronome;
    private ProfileFragment mFragment;

    // Fixme
    private static final int MAX_PLOT_POINTS = 40000;

    public ProfileFragmentPresenter(ProfileFragment frag, long profileID) {
        mFragment = frag;
        ProfileManager manager = new ProfileManager(frag.getActivity());
        DbProfileTable.ProfileEntry entry = manager.getProfileEntryByProfileID(profileID);
        Log.d(TAG, "Profile fragment presenter created for: filenamePrefix = "  + entry.filenamePrefix());
        mMetronome = new MetronomeModel(entry.filenamePrefix());
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
                mMetronome.getTimeSeries();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgressDialog.setMessage("Drawing time series plot");
                refreshProfileView();
                mProgressDialog.dismiss();
            }
        }

        PreloadViews pl = new PreloadViews();
        pl.execute();
    }

    class RefreshPulseProfileViewAsync extends AsyncTask<Void, Void, Void> {
        private ArrayList<String> mXVals;
        private LineData mLineData;
        private PulseProfile mPF = null;

        @Override
        protected void onPostExecute(Void aVoid) {
            // set data
            LineChart plot = (LineChart) mFragment.getView().findViewById(R.id.metronomepulseprofilefoldedseries);
            if (plot == null) {
                Log.d(TAG, "Error: Unable to find profile line plot.");
                return;
            }

            plot.setData(mLineData);

            // update the axes
            XAxis xaxis = plot.getXAxis();
            xaxis.setValues(mXVals);

            plot.notifyDataSetChanged();
            plot.invalidate();
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
                set1.setColor(Color.RED);
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
