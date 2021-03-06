package edu.utrgv.cgwa.metrec;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.io.File;

public class ProfileManager {
    private DbHelper mHelper = null;

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
                         final String filenamePF,
                         final int beatsPerMinute, final double computedPeriod,
                         final double frequency) {

        ContentValues values = new ContentValues();

        values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_AUDIO_RECORDING_TABLE_ID, audioRecordID);
        values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_DATE, date);
        values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_TIME, time);
        values.put(DbProfileTable.ProfileEntry.COLUMN_NAME_FILENAME_PF, filenamePF);
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

        int filenamePFIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_FILENAME_PF);
        String filenamePF = cursor.getString(filenamePFIndex);

        int bpmIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_BEATS_PER_MINUTE);
        int bpm = cursor.getInt(bpmIndex);

        int computedPeriodIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_COMPUTED_PERIOD);
        double computedPeriod = cursor.getDouble(computedPeriodIndex);

        int frequencyIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_FREQUENCY);
        double frequency = cursor.getDouble(frequencyIndex);

        return new DbProfileTable.ProfileEntry(profileID, audioID, dateString, timeString, filenamePF, bpm,
                computedPeriod, frequency);
    }

    private void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            f.delete();
        }
    }

    public void deleteEntryByID(long profileID) {
        DbProfileTable.ProfileEntry e = getEntryByID(profileID);
        deleteFile(e.filenamePF());

        final String SQL = "DELETE FROM " + DbProfileTable.ProfileEntry.TABLE_NAME
                + " WHERE _ID = " + profileID;

        mHelper.getWritableDatabase().execSQL(SQL);
    }
}
