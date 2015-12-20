package edu.utrgv.cgwa.metrec;

import android.provider.BaseColumns;

public class DbProfileTable {
    public DbProfileTable() {
        // This class should NOT be instantiated
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProfileEntry.TABLE_NAME + " (" +
                    ProfileEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ProfileEntry.COLUMN_NAME_AUDIO_RECORDING_TABLE_ID + " INTEGER" + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_FILENAME_PF + TEXT_TYPE + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_BEATS_PER_MINUTE + TEXT_TYPE + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_COMPUTED_PERIOD + TEXT_TYPE + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_FREQUENCY + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProfileEntry.TABLE_NAME;

    public static class ProfileEntry implements BaseColumns {
        public static final String TABLE_NAME = "profile";
        public static final String COLUMN_NAME_AUDIO_RECORDING_TABLE_ID = "audioRecordingTableID";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_FILENAME_PF = "filenamePF";
        public static final String COLUMN_NAME_BEATS_PER_MINUTE = "beatsPerMinute";
        public static final String COLUMN_NAME_COMPUTED_PERIOD = "computedPeriod";
        public static final String COLUMN_NAME_FREQUENCY = "frequency";

        private long mProfileID, mAudioID;
        private String mDate, mTime, mFilenamePF;
        private int mBeatsPerMinute;
        private double mComputedPeriod;
        private double mFrequency;

        public ProfileEntry(long profileID, long audioID, String date, String time, String filenamePF,
                            int bpm, double computedPeriod, double frequency) {
            mProfileID = profileID;
            mAudioID = audioID;
            mDate = date;
            mTime = time;
            mFilenamePF = filenamePF;
            mBeatsPerMinute = bpm;
            mComputedPeriod = computedPeriod;
            mFrequency = frequency;
        }

        public long profileID() { return mProfileID; }
        public long audioID() { return mAudioID; }
        public String date() { return mDate; }
        public String time() { return mTime; }
        public String filenamePF() { return mFilenamePF; }
        public int beatsPerMinute() { return mBeatsPerMinute; }
        public double computedPeriod() { return mComputedPeriod; }
        public double frequency() { return mFrequency; }
    }
}
