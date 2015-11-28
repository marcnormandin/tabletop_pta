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
                    ProfileEntry._ID + " INTEGER PRIMARY KEY, " +
                    ProfileEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_FILENAMEPREFIX + TEXT_TYPE + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_BEATS_PER_MINUTE + TEXT_TYPE + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_COMPUTED_PERIOD + TEXT_TYPE + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_SAMPLES_PER_SECOND + TEXT_TYPE + COMMA_SEP +
                    ProfileEntry.COLUMN_NAME_FREQUENCY + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProfileEntry.TABLE_NAME;

    public static class ProfileEntry implements BaseColumns {
        public static final String TABLE_NAME = "profile";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_FILENAMEPREFIX = "filenamePrefix";
        public static final String COLUMN_NAME_BEATS_PER_MINUTE = "beatsPerMinute";
        public static final String COLUMN_NAME_COMPUTED_PERIOD = "computedPeriod";
        public static final String COLUMN_NAME_SAMPLES_PER_SECOND = "samplesPerSecond";
        public static final String COLUMN_NAME_FREQUENCY = "frequency";

        private long mID;
        private String mDate, mTime, mFilenamePrefix;
        private int mBeatsPerMinute;
        private double mComputedPeriod;
        private int mSamplesPerSecond;
        private double mFrequency;

        public ProfileEntry(long id, String date, String time, String filenamePrefix,
                            int bpm, double computedPeriod, int samplesPerSecond,
                            double frequency) {
            mID = id;
            mDate = date;
            mTime = time;
            mFilenamePrefix = filenamePrefix;
            mBeatsPerMinute = bpm;
            mComputedPeriod = computedPeriod;
            mSamplesPerSecond = samplesPerSecond;
            mFrequency = frequency;
        }

        public long id() { return mID; }
        public String date() { return mDate; }
        public String time() { return mTime; }
        public String filenamePrefix() { return mFilenamePrefix; }
        public int beatsPerMinute() { return mBeatsPerMinute; }
        public double computedPeriod() { return mComputedPeriod; }
        public int samplesPerSecond() { return mSamplesPerSecond; }
        public double frequency() { return mFrequency; }
    }
}
