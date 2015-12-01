package edu.utrgv.cgwa.metrec;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "pta.db";


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the Profile table
        db.execSQL(DbProfileTable.SQL_CREATE_ENTRIES);
        db.execSQL(DbAudioRecordingTable.SQL_CREATE_ENTRIES);
        db.execSQL(DbSingleMetronomeAnalysisTable.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // If upgrading the database, then delete the old database and make a new one
        db.execSQL(DbProfileTable.SQL_DELETE_ENTRIES);
        db.execSQL(DbAudioRecordingTable.SQL_DELETE_ENTRIES);
        db.execSQL(DbSingleMetronomeAnalysisTable.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
