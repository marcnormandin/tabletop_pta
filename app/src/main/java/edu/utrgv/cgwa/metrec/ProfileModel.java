package edu.utrgv.cgwa.metrec;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;

import edu.utrgv.cgwa.tabletoppta.PulseProfile;
import edu.utrgv.cgwa.tabletoppta.Routines;
import edu.utrgv.cgwa.tabletoppta.TimeSeries;

public class ProfileModel {
    private PulseProfile mPulseProfile = null;
    private String mFilenamePF;
    private ProfileProgressListener mProfileProgressListener = null;

    public interface ProfileProgressListener {
        void onProfileComputationStarted();
        void onProfileComputationFinished();
    }

    public void setProfileProgressListener(ProfileProgressListener listener) {
        mProfileProgressListener = listener;
    }

    public ProfileModel(String filenamePF) {
        mFilenamePF = filenamePF;
    }

    private String getFilenamePF() {
        return mFilenamePF;
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

    protected void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            f.delete();
        }
    }

    public void clearAll() {
        deleteFile(getFilenamePF());
    }

    public void newProfile(double beatsPerMinute, TimeSeries ts, final double Tcorr, final boolean useBrent) {
        if (mProfileProgressListener != null) {
            mProfileProgressListener.onProfileComputationStarted();
        }

        // Get the folded time series
        mPulseProfile = Routines.calpulseprofile(ts, beatsPerMinute, Tcorr, useBrent);
        mPulseProfile.saveToFile(getFilenamePF());

        if (mProfileProgressListener != null) {
            mProfileProgressListener.onProfileComputationFinished();
        }
    }
}
