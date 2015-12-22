package edu.utrgv.cgwa.metrec;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class FitSinusoidFragment extends Fragment implements View.OnClickListener {

    public static final String ARG_CONTROL_TAG = "ARG_CONTROL_TAG";
    private String mControlTag;
    private EditText mEditAmplitude;
    private EditText mEditFrequency;

    private Listener mListener;

    public interface Listener {
        void onFit(String controlTag, double amplitude, double frequency);
    }

    public static FitSinusoidFragment newInstance(final String controlTag) {
        FitSinusoidFragment f = new FitSinusoidFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONTROL_TAG, controlTag);
        f.setArguments(args);
        return f;
    }

    public FitSinusoidFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_fit_sinusoid, container, false);

        mControlTag = getArguments().getString(ARG_CONTROL_TAG);

        mEditAmplitude = (EditText) rootView.findViewById(R.id.editAmplitude);
        mEditFrequency = (EditText) rootView.findViewById(R.id.editFrequency);

        Button buttonFit = (Button) rootView.findViewById(R.id.buttonFit);
        buttonFit.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonFit:
                if (mListener != null) {
                    double amplitude = Double.parseDouble(mEditAmplitude.getText().toString());
                    double frequency = Double.parseDouble(mEditFrequency.getText().toString());
                    mListener.onFit(mControlTag, amplitude, frequency);
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (Listener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(e.getMessage() + " unable to cast to FitSinusoidFragment.Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setAmplitude(double a) {
        mEditAmplitude.setText(Double.toString(a));
    }

    public void setFrequency(double f) {
        mEditFrequency.setText(Double.toString(f));
    }
}
