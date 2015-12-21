package edu.utrgv.cgwa.metrec;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.io.File;

public class DoubleMetronomeAnalysisManager {

    private DbHelper mHelper = null;
    private static final String TAG = "DoubleAnalysisManager";

    public DoubleMetronomeAnalysisManager(Context context) {
        mHelper = new DbHelper(context);
    }

    public int getNumRecords() {
        final String SQL = "SELECT Count(*) FROM " + DbDoubleMetronomeAnalysisTable.Entry.TABLE_NAME;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();
        final int count = cursor.getInt(0);
        cursor.close();

        return count;
    }

    public long addEntry(final long audioID, final long profileOneID, final long profileTwoID,
                         final String date, final String time,
                         final String filenameResultOne, final String filenameResultTwo,
                         final double computationTimeSeconds,
                         final String tag) {

        ContentValues values = new ContentValues();

        values.put(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_AUDIO_ID, audioID);
        values.put(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_PROFILE_ONE_ID, profileOneID);
        values.put(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_PROFILE_TWO_ID, profileTwoID);
        values.put(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_DATE, date);
        values.put(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_TIME, time);
        values.put(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_FILENAME_RESULT_ONE, filenameResultOne);
        values.put(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_FILENAME_RESULT_TWO, filenameResultTwo);
        values.put(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_COMPUTATION_TIME_SECONDS, computationTimeSeconds);
        values.put(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_TAG, tag);


        long analysisID = mHelper.getWritableDatabase().insert(DbDoubleMetronomeAnalysisTable.Entry.TABLE_NAME, "null", values);

        return analysisID;
    }

    public DbDoubleMetronomeAnalysisTable.Entry getEntryByPosition(final int position) {
        final String SQL = "SELECT * FROM " + DbDoubleMetronomeAnalysisTable.Entry.TABLE_NAME;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();
        cursor.moveToPosition(position);

        DbDoubleMetronomeAnalysisTable.Entry entry = getEntry(cursor);

        cursor.close();

        return entry;
    }

    public DbDoubleMetronomeAnalysisTable.Entry getEntryByID(final long id) {
        final String SQL = "SELECT * FROM " + DbDoubleMetronomeAnalysisTable.Entry.TABLE_NAME + " WHERE _ID = " + id;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();

        DbDoubleMetronomeAnalysisTable.Entry entry = getEntry(cursor);

        cursor.close();

        return entry;
    }

    private DbDoubleMetronomeAnalysisTable.Entry getEntry(Cursor cursor) {
        long analysisID = cursor.getLong(0);

        int audioIDIndex = cursor.getColumnIndex(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_AUDIO_ID);
        long audioID = cursor.getLong(audioIDIndex);

        int profileOneIDIndex = cursor.getColumnIndex(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_PROFILE_ONE_ID);
        long profileOneID = cursor.getLong(profileOneIDIndex);

        int profileTwoIDIndex = cursor.getColumnIndex(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_PROFILE_TWO_ID);
        long profileTwoID = cursor.getLong(profileTwoIDIndex);

        int dateIndex = cursor.getColumnIndex(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_DATE);
        String dateString = cursor.getString(dateIndex);

        int timeIndex = cursor.getColumnIndex(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_TIME);
        String timeString = cursor.getString(timeIndex);

        int filenameResultOneIndex = cursor.getColumnIndex(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_FILENAME_RESULT_ONE);
        String filenameResultOne = cursor.getString(filenameResultOneIndex);

        int filenameResultTwoIndex = cursor.getColumnIndex(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_FILENAME_RESULT_TWO);
        String filenameResultTwo = cursor.getString(filenameResultTwoIndex);

        int computationTimeSecondsIndex = cursor.getColumnIndex(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_COMPUTATION_TIME_SECONDS);
        double computationTimeSeconds = cursor.getDouble(computationTimeSecondsIndex);

        int tagIndex = cursor.getColumnIndex(DbDoubleMetronomeAnalysisTable.Entry.COLUMN_NAME_TAG);
        String tag = cursor.getString(tagIndex);

        return new DbDoubleMetronomeAnalysisTable.Entry(analysisID, audioID, profileOneID, profileTwoID,
                dateString, timeString,
                filenameResultOne, filenameResultTwo, computationTimeSeconds, tag);
    }

    private void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            Log.d(TAG, "deleting " + f.getPath());
            f.delete();
        }
    }

    public void deleteEntryByID(final long id) {
        // First delete the files referenced from the entry
        DbDoubleMetronomeAnalysisTable.Entry e = getEntryByID(id);
        deleteFile(e.filenameResultOne());
        deleteFile(e.filenameResultTwo());

        final String SQL = "DELETE FROM " + DbDoubleMetronomeAnalysisTable.Entry.TABLE_NAME
                + " WHERE _ID = " + id;
        Log.d(TAG, "Deleting Double Metronome Analysis with _ID = " + id + " from the database.");

        mHelper.getWritableDatabase().execSQL(SQL);
    }
}
