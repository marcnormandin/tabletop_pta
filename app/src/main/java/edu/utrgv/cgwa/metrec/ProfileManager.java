package edu.utrgv.cgwa.metrec;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class ProfileManager {
    private DbHelper mHelper = null;
    private static final String TAG = "ProfileManager";

    public ProfileManager(Context context) {
        mHelper = new DbHelper(context);
    }

    public int getNumProfiles() {
        final String SQL = "SELECT Count(*) FROM " + DbProfileTable.ProfileEntry.TABLE_NAME;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();
        final int count = cursor.getInt(0);
        cursor.close();

        return count;
    }

    public long addEntry(final long audioRecordID, final String date, final String time,
                         final String filenamePrefix,
                         final int beatsPerMinute, final double computedPeriod,
                         final double frequency) {

        ContentValues values = new ContentValues();

        values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_AUDIO_RECORDING_TABLE_ID, audioRecordID);
        values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_DATE, date);
        values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_TIME, time);
        values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_FILENAME_PREFIX, filenamePrefix);
        values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_BEATS_PER_MINUTE, beatsPerMinute);
        values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_COMPUTED_PERIOD, computedPeriod);
        values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_FREQUENCY, frequency);

        long profileID = mHelper.getWritableDatabase().insert(DbProfileTable.ProfileEntry.TABLE_NAME, "null", values);

        return profileID;
    }

    public DbProfileTable.ProfileEntry getEntryByPosition(int position) {
        final String SQL = "SELECT * FROM " + DbProfileTable.ProfileEntry.TABLE_NAME;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();
        cursor.moveToPosition(position);

        DbProfileTable.ProfileEntry profile = getEntry(cursor);

        cursor.close();

        return profile;
    }

    public DbProfileTable.ProfileEntry getEntryByID(long profileID) {
        final String SQL = "SELECT * FROM " + DbProfileTable.ProfileEntry.TABLE_NAME + " WHERE _ID = " + profileID;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();

        DbProfileTable.ProfileEntry profile = getEntry(cursor);

        cursor.close();

        return profile;
    }

    private DbProfileTable.ProfileEntry getEntry(Cursor cursor) {
        long profileID = cursor.getLong(0);

        int audioIDIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_AUDIO_RECORDING_TABLE_ID);
        long audioID = cursor.getLong(audioIDIndex);

        int dateIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_DATE);
        String dateString = cursor.getString(dateIndex);

        int timeIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_TIME);
        String timeString = cursor.getString(timeIndex);

        int filenamePrefixIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_FILENAME_PREFIX);
        String filenamePrefix = cursor.getString(filenamePrefixIndex);

        int bpmIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_BEATS_PER_MINUTE);
        int bpm = cursor.getInt(bpmIndex);

        int computedPeriodIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_COMPUTED_PERIOD);
        double computedPeriod = cursor.getDouble(computedPeriodIndex);

        int frequencyIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_FREQUENCY);
        double frequency = cursor.getDouble(frequencyIndex);

        return new DbProfileTable.ProfileEntry(profileID, audioID, dateString, timeString, filenamePrefix, bpm,
                computedPeriod, frequency);
    }

    public void deleteEntryByID(long profileID) {
        final String SQL = "DELETE FROM " + DbProfileTable.ProfileEntry.TABLE_NAME
                + " WHERE _ID = " + profileID;
        Log.d(TAG, "Deleting Profile with _ID = " + profileID + " from the database.");

        mHelper.getWritableDatabase().execSQL(SQL);
    }
}
