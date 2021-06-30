package com.kiratcoding.asm.OfflineSync.DbHelperClass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VehicleDbHelper extends SQLiteOpenHelper {
    //Database information
    static final String DB_NAME="VehicleDB";
    //table name
    public static final String TABLE_NAME = "Vehi_cle";

    //table columns
    public static final String ID= "_id" ;
    public static final String VID= "vehicle_id" ;
    public static final String TYPE = "type";


    //database version
    static final int DB_VERSION= 1 ;

    //Creating table query:
    private  static  final String CREATE_TABLE = " create table " + TABLE_NAME
            + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            VID + " TEXT NOT NULL , " +
            TYPE +" TEXT" +
            ");";

    //Constructor:
    public VehicleDbHelper(Context context){
        super(context, DB_NAME,null ,DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
        Log.i(TABLE_NAME," Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean columnExists(String vid) {
        SQLiteDatabase database = this.getWritableDatabase();
        String[] columns = { VehicleDbHelper.VID };
        String selection = VehicleDbHelper.VID + " =?";
        String[] selectionArgs = { vid };
        String limit = "1";

        Cursor cursor = database.query(VehicleDbHelper.TABLE_NAME, columns, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public void insertVehicle(String vid, String type){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(VID,vid);
        contentValues.put(TYPE,type);
        database.insert(TABLE_NAME,null,contentValues);
        database.close();
    }

    public Cursor getSyncVehicle() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME;
        Cursor c = db.rawQuery(sql, null);
        return c;
    }
}
