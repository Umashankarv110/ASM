package com.kiratcoding.asm.SharedPreferencesClass;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.kiratcoding.asm.AuthActivity;
import com.kiratcoding.asm.ModelsClass.Employee;

public class SharedPrefLogin {

    //the constants
    private static final String SHARED_PREF_NAME = "logpref12";
    private static final String KEY_USERNAME = "keyusername12";
    private static final String KEY_EMAIL = "keyemail12";
    private static final String KEY_GENDER = "keygender12";
    private static final String KEY_ID = "keyid12";

    private static SharedPrefLogin mInstance;
    private static Context mCtx;

    private SharedPrefLogin(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefLogin getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefLogin(context);
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
