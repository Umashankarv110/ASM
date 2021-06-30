package com.kiratcoding.asm.SharedPreferencesClass;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.kiratcoding.asm.AuthActivity;
import com.kiratcoding.asm.ModelsClass.Employee;

public class SharedPrefPayrollLogin {

    //the constants
    private static final String SHARED_PREF_NAME = "payrollpref1";
    private static final String KEY_USERNAME = "payrollusername";
    private static final String KEY_EMAIL = "payrollemail";
    private static final String KEY_GENDER = "payrollgender";
    private static final String KEY_ID = "payrollid";

    private static SharedPrefPayrollLogin mInstance;
    private static Context mCtx;

    private SharedPrefPayrollLogin(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefPayrollLogin getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefPayrollLogin(context);
        }
        return mInstance;
    }

    //method to let the user login
    //this method will store the user data in shared preferences
    public void userLogin(Employee employee) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ID, employee.getUniquenumber());
        editor.putString(KEY_USERNAME, employee.getName());
        editor.putString(KEY_EMAIL, employee.getEmail());
        editor.putString(KEY_GENDER, employee.getGender());
        editor.apply();
    }

    //this method will checker whether user is already logged in or not
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, null) != null;
    }

    //this method will give the logged in user
    public Employee getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return new Employee(
                sharedPreferences.getInt(KEY_ID, -1),
                sharedPreferences.getString(KEY_USERNAME, null),
                sharedPreferences.getString(KEY_EMAIL, null),
                sharedPreferences.getString(KEY_GENDER, null)
        );
    }

    //this method will logout the user
    public void logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        mCtx.startActivity(new Intent(mCtx, AuthActivity.class));
    }
}
