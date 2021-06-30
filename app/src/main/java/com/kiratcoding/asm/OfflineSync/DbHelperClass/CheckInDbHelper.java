package com.kiratcoding.asm.OfflineSync.DbHelperClass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class CheckInDbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    static final String DB_NAME="ArunodyaDB";
    public static final String TABLE_NAME = "Att_endace";

    public static final String ID = "id";
    public static final String CHECKIN_STATUS = "checkin_status";
    public static final String VID = "vid";
    public static final String UID = "uniquenumber";
    public static final String UNAME = "employeeName";
    public static final String CURRENT_DATE = "date";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String START_READING = "reading";
    public static final String START_IMAGE = "image";
    public static final String CURRENT_TIME = "time";
    public static final String NOTE = "notes";
    public static final String FROM = "fromLoc";
    public static final String TO = "toLoc";
    public static final String SYNC_STATUS = "status";

    //Create Attendance Query
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
            + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CHECKIN_STATUS + " VARCHAR, " +
            VID + " VARCHAR, " +
            UID + " VARCHAR, " +
            UNAME + " VARCHAR, " +
            CURRENT_DATE + " VARCHAR, " +
            LATITUDE + " VARCHAR, " +
            LONGITUDE + " VARCHAR, " +
            START_READING + " VARCHAR, " +
            START_IMAGE + " VARCHAR, " +
            CURRENT_TIME + " VARCHAR, " +
            NOTE + " VARCHAR, " +
            FROM + " VARCHAR, " +
            TO  + " VARCHAR, " +
            SYNC_STATUS + " TINYINT " +
            ");";


    public CheckInDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.i("Database", DB_NAME+" Created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME
                + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CHECKIN_STATUS + " VARCHAR, " +
                VID + " VARCHAR, " +
                UID + " VARCHAR, " +
                UNAME + " VARCHAR, " +
                CURRENT_DATE + " VARCHAR, " +
                LATITUDE + " VARCHAR, " +
                LONGITUDE + " VARCHAR, " +
                START_READING + " VARCHAR, " +
                START_IMAGE + " VARCHAR, " +
                CURRENT_TIME + " VARCHAR, " +
                NOTE + " VARCHAR, " +
                FROM + " VARCHAR, " +
                TO  + " VARCHAR, " +
                SYNC_STATUS + " TINYINT " +
                ");";
        Log.i("TABLE", TABLE_NAME+" Created");
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(db);
    }

    public boolean addCheckIn(String status, String vehicleId, String userID,String userName,String currentDate, String latitude, String longitude,String startReading, String startImage, String currentTime, String note, String fromLocation, String toLocation, int isSynced) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(CHECKIN_STATUS , status);
        contentValues.put(VID, vehicleId);
        contentValues.put(UID, userID);
        contentValues.put(UNAME, userName);
        contentValues.put(CURRENT_DATE, currentDate);
        contentValues.put(LATITUDE, latitude);
        contentValues.put(LONGITUDE, longitude);
        contentValues.put(START_READING, startReading);
        contentValues.put(START_IMAGE, startImage);
        contentValues.put(CURRENT_TIME, currentTime);
        contentValues.put(NOTE, note);
        contentValues.put(FROM, fromLocation);
        contentValues.put(TO, toLocation);
        contentValues.put(SYNC_STATUS, isSynced);

        Log.i("CheckIn Details:", " Inserted");
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public boolean updateCheckInSyncStatus(int id, int syncStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SYNC_STATUS, syncStatus);
        db.update(TABLE_NAME, contentValues, ID + "=" + id, null);
        db.close();
        return true;
    }

    public Cursor getNotSyncAttendance() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + SYNC_STATUS + " = 0;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }



}
