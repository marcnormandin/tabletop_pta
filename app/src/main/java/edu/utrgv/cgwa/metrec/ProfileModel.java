package edu.utrgv.cgwa.metrec;

import android.util.Log;

import java.io.File;

import edu.utrgv.cgwa.tabletoppta.PulseProfile;
import edu.utrgv.cgwa.tabletoppta.Routines;

public class ProfileModel extends AudioRecordingModel {
    private static final String TAG = "ProfileModel";
    private PulseProfile mPulseProfile = null;
    private ProfileProgressListener mProfileProgressListener = null;

    public interface ProfileProgressListener {
        void onProfileComputationStarted();
        void onProfileComputationFinished();
    }

    public void setProfileProgressListener(ProfileProgressListener listener) {
        mProfileProgressListener = listener;
    }

    public ProfileModel(String filenamePrefix) {
        super(filenamePrefix);
    }

    private String getFilenamePF() {
        return getFilenamePrefix() + ".pf";
    }

    public boolean hasProfile() {
        File f = new File(getFilenamePF());
        return f.exists();
    }

    private void loadProfile() {
        if (hasProfile()) {
            mPulseProfile = new PulseProfile(getFilenamePF());
        }
    }

    public PulseProfile getPulseProfile() {
        if (mPulseProfile == null) {
            loadProfile();
        }

        if (mPulseProfile != null) {
            // Return a copy
            return new PulseProfile(mPulseProfile);
        } else {
            return null;
        }
    }


    @Override
    public void clearAll() {
        super.clearAll();
        deleteFile(getFilenamePF());
    }

    public void newRecording(int sampleRate, double desiredRuntime, double beatsPerMinute) {
        super.newRecording(sampleRate, desiredRuntime);

        newProfile(beatsPerMinute);
    }

    private void newProfile(double beatsPerMinute) {
        Log.d(TAG, "Calculating profile with beats-per-minute = " + beatsPerMinute);

        if (mProfileProgressListener != null) {
            mProfileProgressListener.onProfileComputationStarted();
        }

        // Get the folded time series
        mPulseProfile = Routines.calpulseprofile(getTimeSeries(), beatsPerMinute);
        mPulseProfile.saveToFile(getFilenamePF());

        if (mProfileProgressListener != null) {
            mProfileProgressListener.onProfileComputationFinished();
        }
    }
}
