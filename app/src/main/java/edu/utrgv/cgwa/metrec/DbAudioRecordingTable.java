package edu.utrgv.cgwa.metrec;

import android.provider.BaseColumns;
import android.provider.MediaStore;

public class DbAudioRecordingTable {

    public DbAudioRecordingTable() {
        // This class should NOT be instantiated
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + AudioRecordingEntry.TABLE_NAME + " (" +
                    AudioRecordingEntry._ID + " INTEGER PRIMARY KEY, " +
                    AudioRecordingEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    AudioRecordingEntry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    AudioRecordingEntry.COLUMN_NAME_FILENAME_PREFIX + TEXT_TYPE + COMMA_SEP +
                    AudioRecordingEntry.COLUMN_NAME_FILENAME_PCM + TEXT_TYPE + COMMA_SEP +
                    AudioRecordingEntry.COLUMN_NAME_FILENAME_TS + TEXT_TYPE + COMMA_SEP +
                    AudioRecordingEntry.COLUMN_NAME_SAMPLES_PER_SECOND + TEXT_TYPE  + COMMA_SEP +
                    AudioRecordingEntry.COLUMN_NAME_DURATION_IN_SECONDS + TEXT_TYPE + COMMA_SEP +
                    AudioRecordingEntry.COLUMN_NAME_TAG + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AudioRecordingEntry.TABLE_NAME;

    public static class AudioRecordingEntry implements BaseColumns {
        public static final String TABLE_NAME = "audio_recording";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_FILENAME_PREFIX = "filenamePrefix";
        public static final String COLUMN_NAME_FILENAME_PCM = "filenamePCM";
        public static final String COLUMN_NAME_FILENAME_TS = "filenameTS";
        public static final String COLUMN_NAME_SAMPLES_PER_SECOND = "samplesPerSecond";
        public static final String COLUMN_NAME_DURATION_IN_SECONDS = "durationInSeconds";
        public static final String COLUMN_NAME_TAG = "tag";


        private long mID;
        private String mDate, mTime, mFilenamePrefix, mFilenamePCM, mFilenameTS;
        private int mSamplesPerSecond;
        private double mDurationInSeconds;
        private String mTag;

        public AudioRecordingEntry(long id, String date, String time,
                                   String filenamePrefix, String filenamePCM, String filenameTS,
                                   int samplesPerSecond, double durationInSeconds, String tag) {
            mID = id;
            mDate = date;
            mTime = time;
            mFilenamePrefix = filenamePrefix;
            mFilenamePCM = filenamePCM;
            mFilenameTS = filenameTS;
            mSamplesPerSecond = samplesPerSecond;
            mDurationInSeconds = durationInSeconds;
            mTag = tag;
        }

        public long id() { return mID; }
        public String date() { return mDate; }
        public String time() { return mTime; }
        public String filenamePrefix() { return mFilenamePrefix; }
        public String filenamePCM() { return mFilenamePCM; }
        public String filenameTS() { return mFilenameTS; }
        public int samplesPerSecond() { return mSamplesPerSecond; }
        public double durationInSeconds() { return mDurationInSeconds; }
        public String tag() { return mTag; }
    }
}
