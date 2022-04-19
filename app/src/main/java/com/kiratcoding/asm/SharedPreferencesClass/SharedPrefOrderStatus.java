package com.kiratcoding.asm.SharedPreferencesClass;

import android.content.Context;
import android.content.SharedPreferences;

import com.kiratcoding.asm.ModelsClass.Order;

public class SharedPrefOrderStatus {
    private static final String SHARED_PREF_NAME = "orderpref1";
    private static final String KEY_PARTYNAME = "keypartyname1";
    private static final String KEY_ORDERSTATUS = "keystatus1";
    private static final String KEY_DATE = "keydate1";
    private static final String KEY_ID = "keyid1";

    private static SharedPrefOrderStatus mInstance;
    private static Context mCtx;

    private SharedPrefOrderStatus(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefOrderStatus getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefOrderStatus(context);
        }
        return mInstance;
    }

    public void orderPending(Order order) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ID, order.getUniquenumber());
        editor.putString(KEY_PARTYNAME, order.getEmployeeName());
        editor.putString(KEY_ORDERSTATUS, order.getOrderStatus());
        editor.putString(KEY_DATE, order.getDate());
        editor.apply();
    }

    //this method will checker whether user is already logged in or not
    public boolean isPending() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ORDERSTATUS, null) != null;
    }

    //this method will give the logged in user
    public Order getPendingOrders() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return new Order(
                sharedPreferences.getInt(KEY_ID, -1),
                sharedPreferences.getString(KEY_PARTYNAME, null),
                sharedPreferences.getString(KEY_ORDERSTATUS, null),
                sharedPreferences.getString(KEY_DATE, null)
        );
    }

    //this method will logout the user
    public void deletePendingOrder() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
//        mCtx.startActivity(new Intent(mCtx, AuthActivity.class));
    }
}
