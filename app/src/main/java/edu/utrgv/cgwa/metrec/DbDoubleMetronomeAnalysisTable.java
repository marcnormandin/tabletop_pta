package edu.utrgv.cgwa.metrec;

import android.provider.BaseColumns;

public class DbDoubleMetronomeAnalysisTable {

    public DbDoubleMetronomeAnalysisTable() {
        // This class should NOT be instantiated
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Entry.TABLE_NAME + " (" +
                    Entry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Entry.COLUMN_NAME_AUDIO_ID + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_PROFILE_ONE_ID + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_PROFILE_TWO_ID + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_FILENAME_RESULT_ONE + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_FILENAME_RESULT_TWO + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_COMPUTATION_TIME_SECONDS + TEXT_TYPE + COMMA_SEP +
                    Entry.COLUMN_NAME_TAG + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Entry.TABLE_NAME;

    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "double_metronome_analysis";
        public static final String COLUMN_NAME_AUDIO_ID = "audioID";
        public static final String COLUMN_NAME_PROFILE_ONE_ID = "profileOneID";
        public static final String COLUMN_NAME_PROFILE_TWO_ID = "profileTwoID";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_FILENAME_RESULT_ONE = "filenameResultOne";
        public static final String COLUMN_NAME_FILENAME_RESULT_TWO = "filenameResultTwo";
        public static final String COLUMN_NAME_COMPUTATION_TIME_SECONDS = "computationTimeSeconds";
        public static final String COLUMN_NAME_TAG = "tag";


        private long mID, mAudioID, mProfileOneID, mProfileTwoID;
        private String mDate, mTime, mFilenameResultOne, mFilenameResultTwo;
        private double mComputationTimeSeconds;
        private String mTag;

        public Entry(long id, long audioID, long profileOneID, long profileTwoID, String date, String time,
                     String filenameResultOne, String filenameResultTwo, double computationTimeSeconds,
                     String tag) {
            mID = id;
            mAudioID = audioID;
            mProfileOneID = profileOneID;
            mProfileTwoID = profileTwoID;
            mDate = date;
            mTime = time;
            mFilenameResultOne = filenameResultOne;
            mFilenameResultTwo = filenameResultTwo;
            mComputationTimeSeconds = computationTimeSeconds;
            mTag = tag;
        }

        public long id() {
            return mID;
        }

        public long audioID() {
            return mAudioID;
        }

        public long profileOneID() {
            return mProfileOneID;
        }

        public long profileTwoID() {
            return mProfileTwoID;
        }

        public String date() {
            return mDate;
        }

        public String time() {
            return mTime;
        }

        public String filenameResultOne() {
            return mFilenameResultOne;
        }

        public String filenameResultTwo() {
            return mFilenameResultTwo;
        }

        public double computationTimeSeconds() {
            return mComputationTimeSeconds;
        }

        public String tag() {
            return mTag;
        }
    }
}
