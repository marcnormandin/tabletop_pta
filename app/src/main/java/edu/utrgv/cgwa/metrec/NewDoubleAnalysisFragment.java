package edu.utrgv.cgwa.metrec;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.utrgv.cgwa.tabletoppta.PulseProfile;
import edu.utrgv.cgwa.tabletoppta.Routines;
import edu.utrgv.cgwa.tabletoppta.TimeSeries;


public class NewDoubleAnalysisFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    private NewDoubleAnalysisFragmentListener mListener;

    private long mAudioID = -1;
    private long mProfileOneID = -1;
    private long mProfileTwoID = -1;


    private Button mButtonSelectAudio;
    private Button mButtonSelectProfileOne;
    private Button mButtonSelectProfileTwo;
    private Button mButtonCompute;

    public interface NewDoubleAnalysisFragmentListener {
        void onCancel();

        void onSelectAudioRecord();

        void onSelectProfileOne();
        void onSelectProfileTwo();

        void onNewDoubleAnalysisCreated(long newAnalysisID);
    }

    public NewDoubleAnalysisFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_new_double_analysis, container, false);

        Button buttonCancel = (Button) rootView.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        mButtonSelectAudio = (Button) rootView.findViewById(R.id.buttonSelectAudioRecord);
        mButtonSelectAudio.setOnClickListener(this);

        mButtonSelectProfileOne = (Button) rootView.findViewById(R.id.buttonSelectProfileOne);
        mButtonSelectProfileOne.setOnClickListener(this);

        mButtonSelectProfileTwo = (Button) rootView.findViewById(R.id.buttonSelectProfileTwo);
        mButtonSelectProfileTwo.setOnClickListener(this);

        mButtonCompute = (Button) rootView.findViewById(R.id.buttonCompute);
        mButtonCompute.setOnClickListener(this);
        updateGUI();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (NewDoubleAnalysisFragmentListener) context;
        } catch (ClassCastException e) {
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
            case R.id.buttonSelectProfileOne:
                if (mListener != null) {
                    mListener.onSelectProfileOne();
                }
                break;
            case R.id.buttonSelectProfileTwo:
                if (mListener != null) {
                    mListener.onSelectProfileTwo();
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
        } catch (AudioRecordingManager.InvalidRecordException e) {
            updateGUI();
            return false;
        }
    }

    public boolean loadProfileRecordOne(long profileID) {
        mProfileOneID = -1;

        ProfileManager man = new ProfileManager(getActivity());

        // Fixme
        // This can be false if an invalid profileID is given
        DbProfileTable.ProfileEntry entry = man.getEntryByID(profileID);
        mProfileOneID = profileID;

        updateGUI();

        return true;
    }

    public boolean loadProfileRecordTwo(long profileID) {
        mProfileTwoID = -1;

        ProfileManager man = new ProfileManager(getActivity());

        // Fixme
        // This can be false if an invalid profileID is given
        DbProfileTable.ProfileEntry entry = man.getEntryByID(profileID);
        mProfileTwoID = profileID;

        updateGUI();

        return true;
    }

    private void updateGUI() {
        if (mAudioID == -1 || mProfileOneID == -1 || mProfileTwoID == -1) {
            mButtonCompute.setEnabled(false);
        } else {
            mButtonCompute.setEnabled(true);
        }

        if (mAudioID == -1) {
            mButtonSelectAudio.setText("Select Audio");
        } else {
            mButtonSelectAudio.setText("Audio (" + mAudioID + ")");
        }

        if (mProfileOneID == -1) {
            mButtonSelectProfileOne.setText("Select Profile");
        } else {
            mButtonSelectProfileOne.setText("Profile (" + mProfileOneID + ")");
        }

        if (mProfileTwoID == -1) {
            mButtonSelectProfileTwo.setText("Select Profile");
        } else {
            mButtonSelectProfileTwo.setText("Profile (" + mProfileTwoID + ")");
        }
    }

    public PulseProfile getPulseProfile(long profileID) {
        ProfileManager manager = new ProfileManager(getActivity());
        DbProfileTable.ProfileEntry entry = manager.getEntryByID(profileID);
        ProfileModel model = new ProfileModel(entry.filenamePF());
        // Fixme We should load the pulse profile directly from file using the stored filename
        return model.getPulseProfile();
    }

    // Fixme
    // This is hackish
    private String getFilenamePrefix() {
        String uniquePrefix = new SimpleDateFormat("MM-dd-yyyy-hh-mm-ss").format(new Date());
        String commonPrefix = "/doubleanalysis_";
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
                    mListener.onNewDoubleAnalysisCreated(mNewAnalysisID);
                }
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mProgress.setMessage(values[0]);
            }

            @Override
            protected Void doInBackground(Void... params) {
                // Perform the long computation and get the result
                Routines.CalMeasuredTOAsResult resultOne = runAnalysis(mProfileOneID);
                Routines.CalMeasuredTOAsResult resultTwo = runAnalysis(mProfileTwoID);

                // Save the result to a unique filename
                String filenameResultOne = getFilenamePrefix() + "_one.sar";
                resultOne.saveToFile(filenameResultOne);
                String filenameResultTwo = getFilenamePrefix() + "_two.sar";
                resultTwo.saveToFile(filenameResultTwo);

                // Save the analysis results in the database
                Date date = new Date();
                String dateString = new SimpleDateFormat("MM-dd-yyyy").format(date);
                String timeString = new SimpleDateFormat("hh:mm").format(date);
                DoubleMetronomeAnalysisManager manager = new DoubleMetronomeAnalysisManager(getActivity());

                // Fixme
                // Give the tag a meaningful message
                String tag = "";

                mNewAnalysisID = manager.addEntry(mAudioID, mProfileOneID, mProfileTwoID, dateString, timeString,
                        filenameResultOne, filenameResultTwo, resultOne.computationTimeSeconds()
                        + resultTwo.computationTimeSeconds(), tag);

                return null;
            }
        }

        new AnalysisAsync().execute();
    }

    public Routines.CalMeasuredTOAsResult runAnalysis(long profileID) {
        // Pulse profile to be extended as the template
        PulseProfile pulseProfile = getPulseProfile(profileID);

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
        } catch (AudioRecordingManager.InvalidRecordException e) {
            return null;
        }
    }
}
