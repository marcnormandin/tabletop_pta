package edu.utrgv.cgwa.metrec;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ProfileFragment extends Fragment {
    public static final String ARG_PROFILEID = "profileID";
    private ProfileFragmentPresenter mPresenter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(long profileID) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PROFILEID, profileID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long profileID = getArguments().getLong(ARG_PROFILEID, -1);
        mPresenter = new ProfileFragmentPresenter(this, profileID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.onCreateView();
    }
}
