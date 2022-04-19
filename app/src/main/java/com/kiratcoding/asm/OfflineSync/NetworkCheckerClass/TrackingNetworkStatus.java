package com.kiratcoding.asm.OfflineSync.NetworkCheckerClass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kiratcoding.asm.OfflineSync.DbHelperClass.TrackingDbHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TrackingNetworkStatus extends BroadcastReceiver {

    //context and database helper object
    private Context context;
    private TrackingDbHelper db;

    //Broadcast receiver to know the sync status
    public static final String TRACK_DATA_BROADCAST = "com.kiratcoding.asm.tracking";

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        db = new TrackingDbHelper(context);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        //if there is a network
        if (activeNetwork != null) {
            //if connected to wifi or mobile data plan
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

                //getting all the unsynced names
                Cursor cursor = db.getUnsyncedNames();
                if (cursor.moveToFirst()) {
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        Log.i("saveToServer","Uploaded...");
                        saveName(
                                cursor.getInt(cursor.getColumnIndex(TrackingDbHelper.COLUMN_ID)),
                                cursor.getString(cursor.getColumnIndex(TrackingDbHelper.COLUMN_UID)),
                                cursor.getString(cursor.getColumnIndex(TrackingDbHelper.COLUMN_UNAME)),
                                cursor.getString(cursor.getColumnIndex(TrackingDbHelper.COLUMN_TYPE)),
                                cursor.getString(cursor.getColumnIndex(TrackingDbHelper.COLUMN_LATITUDE)),
                                cursor.getString(cursor.getColumnIndex(TrackingDbHelper.COLUMN_LONGITUDE)),
                                cursor.getString(cursor.getColumnIndex(TrackingDbHelper.COLUMN_DATE)),
                                cursor.getString(cursor.getColumnIndex(TrackingDbHelper.COLUMN_TIME))
                        );
                    }
                }
                cursor.close();
                db.close();
            }
        }else{
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveName(int id, String uid, String uname, String type, String latitude, String longitude, String date, String time) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/Tracking.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //updating the status in sqlite
                                db.updateNameStatus(id,1);
                                Log.i("saveName","Saved");
                                Log.i("DeleteAfter","Saved | "+id);
                                db.deleteTitle(id,1);
                                //sending the broadcast to refresh the list
                                context.sendBroadcast(new Intent(TRACK_DATA_BROADCAST));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("uniquenumber", uid);
                hashMap.put("employeeName",uname);
                hashMap.put("type",type);
                hashMap.put("latitude",latitude);
                hashMap.put("longitude",longitude);
                hashMap.put("date",date);
                hashMap.put("time",time);
                return hashMap;
            }
        };

        RequestQueue rQeue= Volley.newRequestQueue(context);
        rQeue.add(stringRequest);
    }


    /*
     * method taking two arguments
     * name that is to be saved and id of the name from SQLite
     * if the name is successfully sent
     * we will update the status as synced in SQLite
     * */

}