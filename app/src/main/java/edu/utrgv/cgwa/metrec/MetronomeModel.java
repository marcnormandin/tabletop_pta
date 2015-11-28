package edu.utrgv.cgwa.metrec;

import android.util.Log;

import java.io.File;

import edu.utrgv.cgwa.tabletoppta.PulseProfile;
import edu.utrgv.cgwa.tabletoppta.Routines;
import edu.utrgv.cgwa.tabletoppta.TimeSeries;

import static edu.utrgv.cgwa.tabletoppta.Routines.caltemplate;

public class MetronomeModel extends BaseModel {
    private static final String TAG = "MetronomeModel";

    private PulseProfile mPulseProfile = null;

    private TimeSeries mSelfCorrelation = null;

    public MetronomeModel(String filenamePrefix) {
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
        if (hasProfile() && mPulseProfile == null) {
            loadProfile();
        }
        return mPulseProfile;
    }



    private String getFilenameCorr() {
        return getFilenamePrefix() + ".corr";
    }

    public boolean hasSelfCorrelation() {
        File f = new File(getFilenameCorr());
        return f.exists();
    }

    private void loadSelfCorrelation() {
        if (hasProfile()) {
            mSelfCorrelation = new TimeSeries(getFilenameCorr());
        }
    }

    public TimeSeries getSelfCorrelation() {
        if (hasSelfCorrelation() && mSelfCorrelation == null) {
            loadSelfCorrelation();
        }
        return mSelfCorrelation;
    }


    @Override
    public void clearAll() {
        super.clearAll();
        deleteFile(getFilenamePF());
        deleteFile(getFilenameCorr());
    }

    public void newRecording(int sampleRate, double desiredRuntime, double beatsPerMinute) {
        super.newRecording(sampleRate, desiredRuntime);
        newProfile(beatsPerMinute);
    }

    private void newProfile(double beatsPerMinute) {
        Log.d(TAG, "Calculating profile with beats-per-minute = " + beatsPerMinute);

        // Get the folded time series
        mPulseProfile = Routines.calpulseprofile(getTimeSeries(), beatsPerMinute);
        mPulseProfile.saveToFile(getFilenamePF());

        newSelfCorrelation();
    }

    // Fixme
    // This was written as a fast check of correlate
    public void newSelfCorrelation() {
        TimeSeries ts = getTimeSeries();
        PulseProfile pf = getPulseProfile();

        TimeSeries template = caltemplate(pf, ts);


        double[] correlation = Routines.calmeasuredTOAs(ts, template, pf.T);

        mSelfCorrelation = new TimeSeries( ts.h, correlation );
        mSelfCorrelation.saveToFile( getFilenameCorr() );
    }
}
