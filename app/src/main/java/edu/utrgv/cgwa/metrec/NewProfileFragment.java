package edu.utrgv.cgwa.metrec;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.utrgv.cgwa.tabletoppta.PulseProfile;

public class NewProfileFragment extends Fragment implements View.OnClickListener {

    private double mFrequency;

    public interface NewProfileFragmentListener {
        void onCancel();

        void onSelectAudioRecord();

        void onNewProfileCreated();
    }

    private NewProfileFragmentListener mListener;

    public NewProfileFragment() {
        // Required empty public constructor
    }

    public static NewProfileFragment newInstance() {
        Bundle args = new Bundle();
        NewProfileFragment frag = new NewProfileFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_new_profile, container, false);

        Button buttonCancel = (Button) rootView.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        Button buttonSelect = (Button) rootView.findViewById(R.id.buttonSelectAudioRecord);
        buttonSelect.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initSpinnerBPM();
        initSwitchFrequency();
    }

    private void setFrequency(double frequency) {
        mFrequency = frequency;
    }

    private void initSwitchFrequency() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final double modeA = Double.parseDouble(sp.getString("mode_a_frequency", "900"));
        final double modeB = Double.parseDouble(sp.getString("mode_b_frequency", "1200"));

        // Frequency SeekBar
        Switch sf = (Switch) getActivity().findViewById(R.id.frequencySwitch);
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

    private void initSpinnerBPM() {
        // Setup the beats per minute spinner
        Spinner bpm = (Spinner) getActivity().findViewById(R.id.beatsPerMinuteSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.beats_per_minute, R.layout.spinner_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bpm.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (NewProfileFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement NewProfileFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonCancel:
                if (mListener != null) {
                    mListener.onCancel();
                }
                break;
            case R.id.buttonSelectAudioRecord:
                if (mListener != null) {
                    mListener.onSelectAudioRecord();
                }
                break;
        }
    }

    private double getBeatsPerMinute() {
        Spinner bpm = (Spinner) getView().findViewById(R.id.beatsPerMinuteSpinner);
        double beatsPerMinute = Double.parseDouble((String) bpm.getSelectedItem());
        return beatsPerMinute;
    }

    private String getFilenamePF() {
        String uniquePrefix = new SimpleDateFormat("MM-dd-yyyy-hh-mm-ss").format(new Date());
        String commonPrefix = "/pulseprofile_";
        String filesDirectory = getActivity().getFilesDir() + "";
        String filenamePrefix = filesDirectory + commonPrefix + uniquePrefix;
        return filenamePrefix;
    }

    // Called by ProfileListActivity
    public void createNewProfileRecord(final long audioID) {
        final String filenamePF = getFilenamePF();

        class NewProfileAsync extends AsyncTask<Void, String, Void>
                implements ProfileModel.ProfileProgressListener {
            private ProgressDialog mProgress = null;
            private double mBeatsPerMinute = 0;

            @Override
            public void onProfileComputationStarted() {
                publishProgress("Computing pulse profile...");
            }

            @Override
            public void onProfileComputationFinished() {
                publishProgress("Pulse profile computed!");
            }

            @Override
            protected void onPreExecute() {
                mProgress = ProgressDialog.show(getActivity(), "Working...", "Please wait");
                mBeatsPerMinute = getBeatsPerMinute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                Date date = new Date();
                String dateString = new SimpleDateFormat("MM-dd-yyyy").format(date);
                String timeString = new SimpleDateFormat("hh:mm").format(date);

                // Load the audio time series from the database
                AudioRecordingManager audioManager = new AudioRecordingManager(getContext());
                try {
                    DbAudioRecordingTable.AudioRecordingEntry audioEntry = audioManager.getEntryByID(audioID);
                    AudioRecordingModel audioModel = new AudioRecordingModel(audioEntry.filenamePrefix());

                    // Create a new profile model based on the audio time series
                    ProfileModel profileModel = new ProfileModel(filenamePF);
                    profileModel.setProfileProgressListener(this);

                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    final double tcorr = Double.parseDouble(sp.getString("correlationTime", "4.0e-4"));
                    final boolean useBrent = false; //!Fixme Boolean.valueOf(sp.getString("useBrent", "false"));

                    profileModel.newProfile(mBeatsPerMinute, audioModel.getTimeSeries(), tcorr, useBrent);
                    PulseProfile pulse = profileModel.getPulseProfile();

                    // Save the profile to the database
                    ProfileManager profileManager = new ProfileManager(getContext());
                    profileManager.addEntry(audioID, dateString, timeString, filenamePF,
                            (int) pulse.bpm, pulse.T, mFrequency);

                    publishProgress("All results have been saved to database!");
                }
                catch (AudioRecordingManager.InvalidRecordException e) {
                    Toast.makeText(getActivity(), "Error: Unable to load audio time series", Toast.LENGTH_LONG).show();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mProgress.setMessage(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgress.dismiss();
                if (mListener != null) {
                    mListener.onNewProfileCreated();
                }
            }
        }

        new NewProfileAsync().execute();
    }
}
