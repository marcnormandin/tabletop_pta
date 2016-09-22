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
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.utrgv.cgwa.tabletoppta.PulseProfile;
import edu.utrgv.cgwa.tabletoppta.Routines;
import edu.utrgv.cgwa.tabletoppta.TimeSeries;


public class NewSingleAnalysisFragment extends Fragment implements View.OnClickListener {

    private NewSingleAnalysisFragmentListener mListener;

    private long mAudioID = -1;
    private long mProfileID = -1;

    private Button mButtonSelectAudio;
    private Button mButtonSelectProfile;
    private Button mButtonCompute;

    public interface NewSingleAnalysisFragmentListener {
        void onCancel();

        void onSelectAudioRecord();

        void onSelectProfile();

        void onNewSingleAnalysisCreated(long newAudioID);
    }

    public NewSingleAnalysisFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_new_single_analysis, container, false);

        Button buttonCancel = (Button) rootView.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        mButtonSelectAudio = (Button) rootView.findViewById(R.id.buttonSelectAudioRecord);
        mButtonSelectAudio.setOnClickListener(this);

        mButtonSelectProfile = (Button) rootView.findViewById(R.id.buttonSelectProfile);
        mButtonSelectProfile.setOnClickListener(this);

        mButtonCompute = (Button) rootView.findViewById(R.id.buttonCompute);
        mButtonCompute.setOnClickListener(this);
        updateGUI();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (NewSingleAnalysisFragmentListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(e.toString() + "unable to cast to NewSingleAnalysisFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
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
            case R.id.buttonSelectProfile:
                if (mListener != null) {
                    mListener.onSelectProfile();
                }
                break;
            case R.id.buttonCompute:
                buttonCompute();
                break;
        }
    }

    public boolean loadAudioRecord(long audioID) {
        mAudioID = -1;

        AudioRecordingManager man = new AudioRecordingManager(getActivity());
        try {
            DbAudioRecordingTable.AudioRecordingEntry entry = man.getEntryByID(audioID);
            mAudioID = audioID;
            updateGUI();
            return true;
        }
        catch (AudioRecordingManager.InvalidRecordException e) {
            updateGUI();
            return false;
        }
    }

    public boolean loadProfileRecord(long profileID) {
        mProfileID = -1;

        ProfileManager man = new ProfileManager(getActivity());

        // Fixme
        // This can be false if an invalid profileID is given
        DbProfileTable.ProfileEntry entry = man.getEntryByID(profileID);
        mProfileID = profileID;

        updateGUI();

        return true;
    }

    private void updateGUI() {
        if (mAudioID == -1 || mProfileID == -1) {
            mButtonCompute.setEnabled(false);
        } else {
            mButtonCompute.setEnabled(true);
        }

        if (mAudioID == -1) {
            mButtonSelectAudio.setText("Select Audio");
        } else {
            mButtonSelectAudio.setText("Audio (" + mAudioID + ")");
        }

        if (mProfileID == -1) {
            mButtonSelectProfile.setText("Select Profile");
        } else {
            mButtonSelectProfile.setText("Profile (" + mProfileID + ")");
        }
    }

    public PulseProfile getPulseProfile() {
        ProfileManager manager = new ProfileManager(getActivity());
        DbProfileTable.ProfileEntry entry = manager.getEntryByID(mProfileID);
        ProfileModel model = new ProfileModel(entry.filenamePF());
        // Fixme We should load the pulse profile directly from file using the stored filename
        return model.getPulseProfile();
    }

    // Fixme
    // This is hackish
    private String getFilenamePrefix() {
        String uniquePrefix = new SimpleDateFormat("MM-dd-yyyy-hh-mm-ss").format(new Date());
        String commonPrefix = "/singleanalysis_";
        String filesDirectory = getActivity().getFilesDir() + "";
        String filenamePrefix = filesDirectory + commonPrefix + uniquePrefix;

        return filenamePrefix;
    }

    // This is called when both an audio record and a pulse profile have been selected
    private void buttonCompute() {
        class AnalysisAsync extends AsyncTask<Void, String, Void> {
            private ProgressDialog mProgress;

            private long mNewAnalysisID = -1;

            @Override
            protected void onPreExecute() {
                mProgress = ProgressDialog.show(getActivity(), "Performing analysis", "Please be patient");
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgress.dismiss();
                if (mListener != null) {
                    mListener.onNewSingleAnalysisCreated(mNewAnalysisID);
                }
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mProgress.setMessage(values[0]);
            }

            @Override
            protected Void doInBackground(Void... params) {
                // Perform the long computation and get the result
                Routines.CalMeasuredTOAsResult result = runAnalysis();

                // Save the result to a unique filename
                String filenameResult = getFilenamePrefix() + ".sar";
                result.saveToFile(filenameResult);

                // Save the analysis results in the database
                Date date = new Date();
                String dateString = new SimpleDateFormat("MM-dd-yyyy").format(date);
                String timeString = new SimpleDateFormat("hh:mm").format(date);
                SingleMetronomeAnalysisManager manager = new SingleMetronomeAnalysisManager(getActivity());

                // Fixme
                // Give the tag a meaningful message
                String tag = "";

                mNewAnalysisID = manager.addEntry(mAudioID, mProfileID, dateString, timeString,
                        filenameResult, result.computationTimeSeconds(), tag);

                return null;
            }
        }

        new AnalysisAsync().execute();
    }

    public Routines.CalMeasuredTOAsResult runAnalysis() {
        // Pulse profile to be extended as the template
        PulseProfile pulseProfile = getPulseProfile();

        try {
            // Audio recording as the time series
            AudioRecordingManager man = new AudioRecordingManager(getActivity());
            DbAudioRecordingTable.AudioRecordingEntry entry = man.getEntryByID(mAudioID);
            TimeSeries singleMovingMetronome = new TimeSeries(entry.filenameTS());

            // Create the template
            TimeSeries template = Routines.caltemplate(pulseProfile, singleMovingMetronome);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final double tcorr = Double.parseDouble(sp.getString("correlationTime", "4e-4"));
            final boolean useBrent = sp.getBoolean("useBrent", false);

            Routines.CalMeasuredTOAsResult result = Routines.calmeasuredTOAs(singleMovingMetronome,
                    template, pulseProfile.T, tcorr, useBrent);

            return result;
        }
        catch (AudioRecordingManager.InvalidRecordException e) {
            return null;
        }
    }
}
