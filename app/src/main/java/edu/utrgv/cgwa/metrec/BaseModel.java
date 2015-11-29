package edu.utrgv.cgwa.metrec;

import android.util.Log;

import java.io.File;

import edu.utrgv.cgwa.tabletoppta.TimeSeries;

public class BaseModel {
    private static final String TAG = "BaseModel";
    private String mFilenamePrefix;
    private PCMSoundSystem mPCMSoundSystem = null;
    private TimeSeries mTimeSeries = null;
    private ProgressListener mProgressListener = null;

    public interface ProgressListener {
        void onRecordingStarted();
        void onRecordingFinished();

        void onTimeSeriesStart();
        void onTimeSeriesFinished();
    }

    public BaseModel(String filenamePrefix) {
        mFilenamePrefix = filenamePrefix;
    }

    public void setProgressListener(ProgressListener listener) {
        mProgressListener = listener;
    }

    public void close() {
        if (mPCMSoundSystem != null) {
            if (mPCMSoundSystem.isRecording()) {
                if (mPCMSoundSystem.isRecording()) {
                    mPCMSoundSystem.stopRecording();
                }
            }
        }
    }

    protected void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            Log.d(TAG, "deleting " + f.getPath());
            f.delete();
        }
    }

    public void clearAll() {
        deleteFile(getFilenamePCM());
        deleteFile(getFilenameTS());
    }



    protected String getFilenamePrefix() {
        return mFilenamePrefix;
    }

    protected String getFilenamePCM() {
        return getFilenamePrefix() + ".pcm";
    }

    protected String getFilenameTS() {
        return getFilenamePrefix() + ".ts";
    }

    public boolean hasTimeSeries() {
        File f = new File(getFilenameTS());
        return f.exists();
    }

    private void loadTimeSeries() {
        if (hasTimeSeries()) {
            mTimeSeries = new TimeSeries(getFilenameTS());
        }
    }

    public TimeSeries getTimeSeries() {
        if (mTimeSeries == null) {
            loadTimeSeries();
        }

        if (mTimeSeries != null) {
            return new TimeSeries(mTimeSeries);
        } else {
            return null;
        }
    }

    protected void newTimeSeries() {
        if (mProgressListener != null) {
            mProgressListener.onTimeSeriesStart();
        }

        // Careful if mPCMSoundSystem is null
        final int sampleRate = mPCMSoundSystem.getSampleRate(); // WARNING

        String filePath = getFilenamePCM();
        File file = new File(filePath);
        if(!file.exists()) {
            return;
        }

        double[] data = PCMUtilities.readFileIntoArray(filePath);
        double[] samplet = new double[data.length];

        for (int i = 0; i < samplet.length; i++) {
            samplet[i] = (((float)i)/(sampleRate));
        }

        mTimeSeries = new TimeSeries(samplet, data);
        mTimeSeries.saveToFile(getFilenameTS());

        if (mProgressListener != null) {
            mProgressListener.onTimeSeriesFinished();
        }
    }

    public void newRecording(int sampleRate, double desiredRuntime) {

        if (mProgressListener != null) {
            mProgressListener.onRecordingStarted();
        }

        if (mPCMSoundSystem != null) {
            if (mPCMSoundSystem.isRecording()) {
                mPCMSoundSystem.stopRecording();
            }
            // Fixme
            // What if sound is being played?
            mPCMSoundSystem = null;
        }

        // Clear all previous data
        // Fixme
        //clearAll();

        mPCMSoundSystem = new PCMSoundSystem( sampleRate, getFilenamePCM() );

        mPCMSoundSystem.startRecording();

        long start = System.currentTimeMillis();
        long last = start;
        while (true) {
            long curr = System.currentTimeMillis();
            double elapsed = (curr - start) / 1000.0;

            if ((curr - last)/1000.0 >= 0.25) {
                //publishProgress(mDesiredRuntime - elapsed);
                last = curr;
            }

            if ( (desiredRuntime - elapsed) <= 0.0) {
                break;
            }
        }

        mPCMSoundSystem.stopRecording();

        if (mProgressListener != null) {
            mProgressListener.onRecordingFinished();
        }

        newTimeSeries();
    }

    public void playRecording(int sampleRate) {
        if (mPCMSoundSystem == null) {
            mPCMSoundSystem = new PCMSoundSystem( sampleRate, getFilenamePCM() );
        } else if (mPCMSoundSystem.isRecording()) {
            return;
        }

        mPCMSoundSystem.play();
    }
}
