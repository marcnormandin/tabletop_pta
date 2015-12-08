package edu.utrgv.cgwa.metrec;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PCMSoundSystem {
    private static final String TAG = "PCMSoundSystem";

    // Sound system properties
    private static int RECORDER_SAMPLERATE;
    private static final int RECORDER_IN_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_OUT_CHANNELS = AudioFormat.CHANNEL_OUT_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mRecord = null;
    private Thread      recordingThread = null;
    private boolean     isRecording = false;

    private static final int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    private static final int BytesPerElement = 2; // 2 bytes in 16bit format

    String mFilename;

    PCMSoundSystem(int sampleRate, String filename) {
        mFilename = filename;
        RECORDER_SAMPLERATE = sampleRate;

        Log.d(TAG, "Sound settings set for " + RECORDER_SAMPLERATE + " samples per second.");
    }

    public void startRecording() {
        int bufferSizeWanted = BufferElements2Rec * BytesPerElement;
        int bufferSizeMin = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_IN_CHANNELS, RECORDER_AUDIO_ENCODING);
        int bufferSize = bufferSizeWanted;
        if (bufferSizeWanted < bufferSizeMin) {
            bufferSize = bufferSizeMin;
        }

        mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_IN_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferSize);

        if (mRecord.getState() != AudioRecord.STATE_INITIALIZED){
            //Toast.makeText(this, "Error initializing the mRecord", Toast.LENGTH_SHORT).show();
            return;
        }

        mRecord.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    public String getFileName() {
        return mFilename;
    }

    public int getSampleRate() { return RECORDER_SAMPLERATE; }


    public void stopRecording() {
        if (null != mRecord) {
            isRecording = false;

            if (mRecord.getState() ==  AudioRecord.RECORDSTATE_RECORDING) {
                mRecord.stop();
            }
            mRecord.release();
            mRecord = null;
            recordingThread = null;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void play() {
        String FILENAME = getFileName();
        File file = new File(FILENAME);
        int audioLength = (int) file.length();
        byte filedata[] = new byte[audioLength];
        AudioTrack player = null;
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(FILENAME));
            int lengthOfAudioClip = inputStream.read(filedata, 0, audioLength);
            player = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE, RECORDER_OUT_CHANNELS, RECORDER_AUDIO_ENCODING, audioLength, AudioTrack.MODE_STREAM);
            player.write(filedata, 0, lengthOfAudioClip);
            player.setPlaybackRate(RECORDER_SAMPLERATE);
            player.play();
        } catch (FileNotFoundException e) {
        } catch (java.io.IOException e) {
        }
    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte
        String filePath = getFileName();
        short sData[] = new short[BufferElements2Rec];

        Log.d(TAG, "recording to " + filePath);

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            mRecord.read(sData, 0, BufferElements2Rec);
            try {
                byte bData[] = short2byte(sData);
                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //convert short to byte
    // short is 16bits
    // byte is 8 bits
    // in RAM high byte first (called big-endian format).
    // http://www.java-samples.com/showtutorial.php?tutorialid=260
    private static byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }
}
