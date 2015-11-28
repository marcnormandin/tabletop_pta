package edu.utrgv.cgwa.metrec;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

public class MetronomeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MetronomeFragment";
    private static final String ARG_FILENAME = "filename";
    private MetronomePresenter mPresenter;

    private static class MyChartGestureListener implements OnChartGestureListener {
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartLongPressed(MotionEvent me) {

        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

        }

        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

        }

        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
            Log.d(TAG, "Chart scaled: (" + scaleX + ", " + scaleY + ")");
        }

        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {
            Log.d(TAG, "Chart translated: (" + dX + ", " + dY + ")");
        }
    }

    public static MetronomeFragment newInstance(String filename) {
        MetronomeFragment fragment = new MetronomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILENAME, filename);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPresenter = new MetronomePresenter(this, getArguments().getString(ARG_FILENAME));
        } else {
            Log.d(TAG, "Fragment created without filename for the presenter");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_metronome, container, false);



        // Manage the time series plot
        LineChart plotTimeSeries = (LineChart) rootView.findViewById(R.id.metronomepulseprofiletimeseries);
        if (plotTimeSeries == null) {
            Log.d(TAG, "chart not found");
            return rootView;
        }

        plotTimeSeries.setDescription("");
        plotTimeSeries.setNoDataTextDescription("You need to make a recording.");
        plotTimeSeries.setBackgroundColor(Color.WHITE);
        plotTimeSeries.setDrawGridBackground(false);

        plotTimeSeries.setDrawMarkerViews(false);
        plotTimeSeries.setVisibleXRangeMaximum(10);
        plotTimeSeries.setOnChartGestureListener( new MyChartGestureListener());

        // Manage the profile plot
        LineChart plotProfile = (LineChart) rootView.findViewById(R.id.metronomepulseprofilefoldedseries);
        if (plotProfile == null) {
            Log.d(TAG, "chart not found");
            return rootView;
        }

        plotProfile.setDescription("");
        plotProfile.setNoDataTextDescription("You need to compute a profile.");
        plotProfile.setBackgroundColor(Color.WHITE);
        plotProfile.setDrawGridBackground(false);

        plotProfile.setVisibleXRangeMaximum(500);


        // Manage the self correlation plot
        LineChart plotSelfCorrelation = (LineChart) rootView.findViewById(R.id.metronomeselfcorrelation);
        if (plotSelfCorrelation == null) {
            Log.d(TAG, "chart not found");
            return rootView;
        }

        plotSelfCorrelation.setDescription("");
        plotSelfCorrelation.setNoDataTextDescription("You need to make a recording.");
        plotSelfCorrelation.setBackgroundColor(Color.WHITE);
        plotSelfCorrelation.setDrawGridBackground(false);


        Button btnRec = (Button)rootView.findViewById(R.id.btnMain);
        btnRec.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.onCreateViews();

        // Setup the beats per minute spinner
        Spinner bpm = (Spinner) getActivity().findViewById(R.id.beatsPerMinuteSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.beats_per_minute, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bpm.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMain: {
                mPresenter.buttonClicked();
                break;
            }
        }
    }
}
