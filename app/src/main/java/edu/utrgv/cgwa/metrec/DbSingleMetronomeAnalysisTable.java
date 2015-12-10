package edu.utrgv.cgwa.metrec;

import android.provider.BaseColumns;

public class DbSingleMetronomeAnalysisTable {

    public DbSingleMetronomeAnalysisTable() {
        // This class should NOT be instantiated
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Entry.TABLE_NAME + " (" +
                    Entry._ID + " INTEGER PRIMARY KEY, " +
                    Entry.COLUMN_NAME_AUDIO_ID + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_PROFILE_ID + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_FILENAME_RESULT + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_COMPUTATION_TIME_SECONDS + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_TAG + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Entry.TABLE_NAME;

    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "single_metronome_analysis";
        public static final String COLUMN_NAME_AUDIO_ID = "audioID";
        public static final String COLUMN_NAME_PROFILE_ID = "profileID";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_FILENAME_RESULT = "filenameResult";
        public static final String COLUMN_NAME_COMPUTATION_TIME_SECONDS = "computationTimeSeconds";
        public static final String COLUMN_NAME_TAG = "tag";


        private long mID, mAudioID, mProfileID;
        private String mDate, mTime, mFilenameResult;
        private double mComputationTimeSeconds;
        private String mTag;

        public Entry(long id, long audioID, long profileID, String date, String time,
                     String filenameResult, double computationTimeSeconds,
                     String tag) {
            mID = id;
            mAudioID = audioID;
            mProfileID = profileID;
            mDate = date;
            mTime = time;
            mFilenameResult = filenameResult;
            mComputationTimeSeconds = computationTimeSeconds;
            mTag = tag;
        }

        public long id() { return mID; }
        public long audioID() { return mAudioID; }
        public long profileID() { return mProfileID; }
        public String date() { return mDate; }
        public String time() { return mTime; }
        public String filenameResult() { return mFilenameResult; }
        public double computationTimeSeconds() { return mComputationTimeSeconds; }
        public String tag() { return mTag; }
    }
}
