package edu.utrgv.cgwa.metrec;

import android.graphics.Color;
import android.os.AsyncTask;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import edu.utrgv.cgwa.tabletoppta.PulseProfile;

public class ProfileFragmentPresenter {
    private final String TAG = "ProfilePresenter";
    private ProfileModel mMetronome;
    private ProfileFragment mFragment;

    // Fixme
    private static final int MAX_PLOT_POINTS = 40000;

    public ProfileFragmentPresenter(ProfileFragment frag, long profileID) {
        mFragment = frag;
        ProfileManager manager = new ProfileManager(frag.getActivity());
        DbProfileTable.ProfileEntry entry = manager.getEntryByID(profileID);
        mMetronome = new ProfileModel(entry.filenamePF());
    }

    void onCreateView() {
        refreshProfileView();
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
        new RefreshPulseProfileViewAsync().execute();
    }
}
