package edu.utrgv.cgwa.metrec;

import android.app.Activity;
import android.database.Cursor;

public class ProfileManager {
    private DbHelper mHelper = null;

    public ProfileManager(Activity activity) {
        mHelper = new DbHelper(activity);
    }

    public int getNumProfiles() {
        final String SQL = "SELECT Count(*) FROM " + DbProfileTable.ProfileEntry.TABLE_NAME;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();
        final int count = cursor.getInt(0);
        cursor.close();

        return count;
    }

    public DbProfileTable.ProfileEntry getProfileEntryByPosition(int position) {
        final String SQL = "SELECT * FROM " + DbProfileTable.ProfileEntry.TABLE_NAME;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();
        cursor.moveToPosition(position);

        DbProfileTable.ProfileEntry profile = getEntry(cursor);

        cursor.close();

        return profile;
    }

    public DbProfileTable.ProfileEntry getProfileEntryByProfileID(int profileID) {
        final String SQL = "SELECT * FROM " + DbProfileTable.ProfileEntry.TABLE_NAME + " WHERE _ID = " + profileID;
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(SQL, null);
        cursor.moveToFirst();

        DbProfileTable.ProfileEntry profile = getEntry(cursor);

        cursor.close();

        return profile;
    }

    private DbProfileTable.ProfileEntry getEntry(Cursor cursor) {
        int uniqueIndex = cursor.getInt(0);

        int dateIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_DATE);
        String dateString = cursor.getString(dateIndex);

        int timeIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_TIME);
        String timeString = cursor.getString(timeIndex);

        int filenamePrefixIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_FILENAMEPREFIX);
        String filenamePrefix = cursor.getString(filenamePrefixIndex);

        int bpmIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_BEATS_PER_MINUTE);
        int bpm = cursor.getInt(bpmIndex);

        int computedPeriodIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_COMPUTED_PERIOD);
        double computedPeriod = cursor.getDouble(computedPeriodIndex);

        int samplesPerSecondIndex = cursor.getColumnIndex(DbProfileTable.ProfileEntry.COLUMN_NAME_SAMPLES_PER_SECOND);
        int samplesPerSecond = cursor.getInt(samplesPerSecondIndex);

        return new DbProfileTable.ProfileEntry(uniqueIndex, dateString, timeString, filenamePrefix, bpm,
                computedPeriod, samplesPerSecond);
    }
}
