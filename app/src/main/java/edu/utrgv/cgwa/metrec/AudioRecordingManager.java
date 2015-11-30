package edu.utrgv.cgwa.metrec;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class AudioRecordingManager {
    private DbHelper mHelper = null;
    private static final String TAG = "AudioRecordingManager";

    public AudioRecordingManager(Context context) {
        mHelper = new DbHelper(context);
    }

    public int getNumRecordings() {
        final String SQL = "SELECT Count(*) FROM " + DbAudioRecordingTable.AudioRecordingEntry.TABLE_NAME;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();
        final int count = cursor.getInt(0);
        cursor.close();

        return count;
    }

    public long addEntry(final String date, final String time,
                         final String filenamePrefix,
                         final String filenamePCM,
                         final String filenameTS,
                         final int samplesPerSecond,
                         final double durationInSeconds) {

        ContentValues values = new ContentValues();

        values.put(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_DATE, date);
        values.put(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_TIME, time);
        values.put(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_FILENAME_PREFIX, filenamePrefix);
        values.put(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_FILENAME_PCM, filenamePCM);
        values.put(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_FILENAME_TS, filenameTS);
        values.put(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_SAMPLES_PER_SECOND, samplesPerSecond);
        values.put(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_DURATION_IN_SECONDS, durationInSeconds);


        long audioID = mHelper.getWritableDatabase().insert(DbAudioRecordingTable.AudioRecordingEntry.TABLE_NAME, "null", values);

        return audioID;
    }

    public DbAudioRecordingTable.AudioRecordingEntry getEntryByPosition(final int position) {
        final String SQL = "SELECT * FROM " + DbAudioRecordingTable.AudioRecordingEntry.TABLE_NAME;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();
        cursor.moveToPosition(position);

        DbAudioRecordingTable.AudioRecordingEntry entry = getEntry(cursor);

        cursor.close();

        return entry;
    }

    public DbAudioRecordingTable.AudioRecordingEntry getEntryByID(final long id) {
        final String SQL = "SELECT * FROM " + DbAudioRecordingTable.AudioRecordingEntry.TABLE_NAME + " WHERE _ID = " + id;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();

        DbAudioRecordingTable.AudioRecordingEntry entry = getEntry(cursor);

        cursor.close();

        return entry;
    }

    private DbAudioRecordingTable.AudioRecordingEntry getEntry(Cursor cursor) {
        long uniqueIndex = cursor.getLong(0);

        int dateIndex = cursor.getColumnIndex(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_DATE);
        String dateString = cursor.getString(dateIndex);

        int timeIndex = cursor.getColumnIndex(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_TIME);
        String timeString = cursor.getString(timeIndex);

        int filenamePrefixIndex = cursor.getColumnIndex(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_FILENAME_PREFIX);
        String filenamePrefix = cursor.getString(filenamePrefixIndex);

        int filenamePCMIndex = cursor.getColumnIndex(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_FILENAME_PCM);
        String filenamePCM = cursor.getString(filenamePCMIndex);

        int filenameTSIndex = cursor.getColumnIndex(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_FILENAME_TS);
        String filenameTS = cursor.getString(filenameTSIndex);

        int samplesPerSecondIndex = cursor.getColumnIndex(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_SAMPLES_PER_SECOND);
        int samplesPerSecond = cursor.getInt(samplesPerSecondIndex);

        int durationIndex = cursor.getColumnIndex(DbAudioRecordingTable.AudioRecordingEntry.COLUMN_NAME_DURATION_IN_SECONDS);
        double durationInSeconds = cursor.getDouble(durationIndex);

        return new DbAudioRecordingTable.AudioRecordingEntry(uniqueIndex, dateString, timeString,
                filenamePrefix, filenamePCM, filenameTS, samplesPerSecond, durationInSeconds);
    }

    public void deleteEntryByID(final long id) {
        final String SQL = "DELETE FROM " + DbAudioRecordingTable.AudioRecordingEntry.TABLE_NAME
                + " WHERE _ID = " + id;
        Log.d(TAG, "Deleting Audio Recording with _ID = " + id + " from the database.");

        mHelper.getWritableDatabase().execSQL(SQL);
    }
}