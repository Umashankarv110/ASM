package com.kiratcoding.asm.SharedPreferencesClass;

import android.content.Context;
import android.content.SharedPreferences;

import com.kiratcoding.asm.ModelsClass.Attendance;

public class SharedPrefCheckIn {

    //the constants
    private static final String SHARED_PREF_CHECKIN = "checkinref1";
    private static final String KEY_ATTENDANCE_STATUS = "keyastatus1";
    private static final String KEY_VEHICLE_ID = "keyvehicleid1";
    private static final String KEY_UID = "keyuid1";

    private static SharedPrefCheckIn mInstance;
    private static Context mCtx;

    private SharedPrefCheckIn(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefCheckIn getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefCheckIn(context);
        }
        return mInstance;
    }

    //method to let the user login
    //this method will store the user data in shared preferences
    public void userCheckIn(Attendance attendance) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHECKIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_UID, attendance.getUniquenumber());
        editor.putInt(KEY_VEHICLE_ID, attendance.getVehicleId());
        editor.putString(KEY_ATTENDANCE_STATUS, attendance.getStatus());
        editor.apply();
    }

    //this method will checker whether user is already logged in or not
    public boolean isCheckIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHECKIN, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ATTENDANCE_STATUS, null) != null;
    }

    //this method will give the logged in user
    public Attendance getAttendance() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHECKIN, Context.MODE_PRIVATE);
        return new Attendance(
                sharedPreferences.getInt(KEY_UID, -1),
                sharedPreferences.getInt(KEY_VEHICLE_ID, -1),
                sharedPreferences.getString(KEY_ATTENDANCE_STATUS, null)
        );
    }

    //this method will logout the user
    public void checkout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHECKIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
