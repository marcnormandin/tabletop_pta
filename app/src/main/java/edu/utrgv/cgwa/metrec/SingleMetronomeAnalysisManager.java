package edu.utrgv.cgwa.metrec;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class SingleMetronomeAnalysisManager {

    private DbHelper mHelper = null;
    private static final String TAG = "SingleAnalysisManager";

    public SingleMetronomeAnalysisManager(Context context) {
        mHelper = new DbHelper(context);
    }

    public int getNumRecordings() {
        final String SQL = "SELECT Count(*) FROM " + DbSingleMetronomeAnalysisTable.Entry.TABLE_NAME;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();
        final int count = cursor.getInt(0);
        cursor.close();

        return count;
    }

    public long addEntry(final long audioID, final long profileID,
                         final String date, final String time,
                         final String filenameResult,
                         final String tag) {

        ContentValues values = new ContentValues();

        values.put(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_AUDIO_ID, audioID);
        values.put(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_PROFILE_ID, profileID);
        values.put(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_DATE, date);
        values.put(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_TIME, time);
        values.put(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_FILENAME_RESULT, filenameResult);

        values.put(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_TAG, tag);


        long analysisID = mHelper.getWritableDatabase().insert(DbSingleMetronomeAnalysisTable.Entry.TABLE_NAME, "null", values);

        return analysisID;
    }

    public DbSingleMetronomeAnalysisTable.Entry getEntryByPosition(final int position) {
        final String SQL = "SELECT * FROM " + DbSingleMetronomeAnalysisTable.Entry.TABLE_NAME;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();
        cursor.moveToPosition(position);

        DbSingleMetronomeAnalysisTable.Entry entry = getEntry(cursor);

        cursor.close();

        return entry;
    }

    public DbSingleMetronomeAnalysisTable.Entry getEntryByID(final long id) {
        final String SQL = "SELECT * FROM " + DbSingleMetronomeAnalysisTable.Entry.TABLE_NAME + " WHERE _ID = " + id;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();

        DbSingleMetronomeAnalysisTable.Entry entry = getEntry(cursor);

        cursor.close();

        return entry;
    }

    private DbSingleMetronomeAnalysisTable.Entry getEntry(Cursor cursor) {
        long analysisID = cursor.getLong(0);

        int audioIDIndex = cursor.getColumnIndex(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_AUDIO_ID);
        long audioID = cursor.getLong(audioIDIndex);

        int profileIDIndex = cursor.getColumnIndex(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_PROFILE_ID);
        long profileID = cursor.getLong(profileIDIndex);

        int dateIndex = cursor.getColumnIndex(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_DATE);
        String dateString = cursor.getString(dateIndex);

        int timeIndex = cursor.getColumnIndex(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_TIME);
        String timeString = cursor.getString(timeIndex);

        int filenameResultIndex = cursor.getColumnIndex(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_FILENAME_RESULT);
        String filenameResult = cursor.getString(filenameResultIndex);

        int tagIndex = cursor.getColumnIndex(DbSingleMetronomeAnalysisTable.Entry.COLUMN_NAME_TAG);
        String tag = cursor.getString(tagIndex);

        return new DbSingleMetronomeAnalysisTable.Entry(analysisID, audioID, profileID,
                dateString, timeString,
                filenameResult, tag);
    }

    public void deleteEntryByID(final long id) {
        final String SQL = "DELETE FROM " + DbSingleMetronomeAnalysisTable.Entry.TABLE_NAME
                + " WHERE _ID = " + id;
        Log.d(TAG, "Deleting Single Metronome Analysis with _ID = " + id + " from the database.");

        mHelper.getWritableDatabase().execSQL(SQL);
    }
}
