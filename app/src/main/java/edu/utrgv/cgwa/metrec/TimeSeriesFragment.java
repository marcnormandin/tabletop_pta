package edu.utrgv.cgwa.metrec;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TimeSeriesFragment extends Fragment {
    private static final String ARG_PROFILEID = "profileID";

    private TimeSeriesFragmentPresenter mPresenter;

    public static TimeSeriesFragment newInstance(long profileID) {
        TimeSeriesFragment fragment = new TimeSeriesFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PROFILEID, profileID);
        fragment.setArguments(args);
        return fragment;
    }

    public TimeSeriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long profileID = getArguments().getLong(ARG_PROFILEID, -1);
        mPresenter = new TimeSeriesFragmentPresenter(this, profileID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_series, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.onCreateView();
    }

    public void playSound() {
        mPresenter.playSound();
    }
}
