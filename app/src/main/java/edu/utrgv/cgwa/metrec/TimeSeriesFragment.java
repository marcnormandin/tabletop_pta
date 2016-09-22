package edu.utrgv.cgwa.metrec;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TimeSeriesFragment extends Fragment {
    public static final String ARG_AUDIOID = "audioID";

    private TimeSeriesFragmentPresenter mPresenter;

    public static TimeSeriesFragment newInstance(long audioID) {
        TimeSeriesFragment fragment = new TimeSeriesFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_AUDIOID, audioID);
        fragment.setArguments(args);
        return fragment;
    }

    public TimeSeriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long audioID = getArguments().getLong(ARG_AUDIOID, -1);
        mPresenter = new TimeSeriesFragmentPresenter(this, audioID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_series, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mPresenter != null) {
            mPresenter.onCreateView();
        }
    }

    public void playSound() {
        if (mPresenter != null) {
            mPresenter.playSound();
        }
    }
}
