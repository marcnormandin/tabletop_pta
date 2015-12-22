package edu.utrgv.cgwa.metrec;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class BuiltinMetronomeService extends Service {

    private final String TAG = "BuiltinMetronomeService";

    class SoundThread extends Thread {
        boolean mKeepLooping = true;

        AudioTrack mAudioTrack = null;

        SoundThread(AudioTrack at) {
            mAudioTrack = at;
        }

        @Override
        public void run() {
            while(mKeepLooping) {
                long last = System.currentTimeMillis();
                playSound();
                do {
                    // no op
                } while ((System.currentTimeMillis() - last) < mMillisPerBeat);
            }
            mAudioTrack.stop();
            Log.d(TAG, "mThread.run() end reached.");
        }

        @Override
        public void interrupt() {
            mKeepLooping = false;
        }

        private void playSound()
        {
            if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                mAudioTrack.stop();
            }

            if (mAudioTrackIsDirty) {
                genTone();
                mAudioTrackIsDirty = false;
            }

            mAudioTrack.reloadStaticData();
            mAudioTrack.play();
        }
    }

    private SoundThread mSoundThread = null;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BuiltinMetronomeService getService() {
            return BuiltinMetronomeService.this;
        }
    }



    SharedPreferences mPreferences;

    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    private double mDuration; // seconds
    private int mSampleRate;
    private double mFreqOfTone; // hz
    private int mBeatsPerMinute;
    private float mVolume;

    private int mNumSamples;
    private long mMillisPerBeat;

    private double mSample[];
    private byte mGeneratedSnd[];

    private boolean mIsPlaying = false;
    private boolean mAudioTrackIsDirty = true;

    AudioTrack mAudioTrack;

    public BuiltinMetronomeService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        // Settings
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Fixme
        // These names are not the same as used in the preferences activity!
        mDuration = mPreferences.getFloat("duration", 0.01f);
        mSampleRate = mPreferences.getInt("sampleRate", 44100);
        mFreqOfTone = mPreferences.getFloat("freqOfTone", 640);
        mBeatsPerMinute = mPreferences.getInt("beatsPerMinute", 120);
        mVolume = mPreferences.getFloat("volume", 1.0f);

        mMillisPerBeat = (long) (1.0 / ((double)mBeatsPerMinute /(double)(60*1000)));
        mNumSamples = (int) (mDuration * ((double)mSampleRate));
        mSample = new double[mNumSamples];
        mGeneratedSnd = new byte[2 * mNumSamples];

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mSampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, mGeneratedSnd.length,
                AudioTrack.MODE_STATIC);

        // Generate the initial audio track
        genTone();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        stopPlaying();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");

        stopPlaying();
        return false;
    }

    private void genTone()
    {
        // fill out the array
        double percent = 0.10;
        double Trise = percent * mDuration;
        int Nw = (int) (mSampleRate * Trise);

        for (int i = 0; i < mNumSamples; ++i) {
            mSample[i] = Math.sin(2 * Math.PI * i / (mSampleRate/mFreqOfTone));

            if (i < Nw) {
                mSample[i] *= Math.sin(Math.PI/2.0 * i / Nw);
            }
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : mSample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            mGeneratedSnd[idx++] = (byte) (val & 0x00ff);
            mGeneratedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }

        Log.d(TAG, "sampling rate for tone = " + mSampleRate);

        mAudioTrack.write(mGeneratedSnd, 0, mGeneratedSnd.length);
        mAudioTrack.setStereoVolume(mVolume, mVolume);
        //mAudioTrack.setVolume(); // API 21
    }

    public void startPlaying() {
        if (!mIsPlaying) {
            mIsPlaying = true;
            mSoundThread = new SoundThread(mAudioTrack);
            mSoundThread.start();
            Log.d(TAG, "thread started");
        }
    }

    public void stopPlaying() {
        mIsPlaying = false;
        // This should stop the thread
        if (mSoundThread != null) {
            mSoundThread.interrupt();
            mSoundThread = null;
        }
    }

    public void setBeatsPerMinute(int bpm) {
        mBeatsPerMinute = bpm;
        mMillisPerBeat = (long) (1.0 / ((double)mBeatsPerMinute /(double)(60*1000)));
    }

    public void setVolumePercent(float volumePercent) {
        if (volumePercent > 100.0) {
            volumePercent = 100.0f;
        } else if (volumePercent < 0.0) {
            volumePercent = 0.0f;
        }

        mVolume = (volumePercent/100.0f) * mAudioTrack.getMaxVolume();
        mAudioTrack.setStereoVolume(mVolume, mVolume);
    }

    public void setFrequency(double frequency) {
        mFreqOfTone = frequency;
        mAudioTrackIsDirty = true;
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }
}
