package com.kiratcoding.asm.OfflineSync.NetworkCheckerClass;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kiratcoding.asm.AttendanceOptionActivity;
import com.kiratcoding.asm.OfflineSync.DbHelperClass.CheckInDbHelper;
import com.kiratcoding.asm.PayrollAttendanceActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckInNetworkStatus extends BroadcastReceiver {
    //try once
    // https://stackoverflow.com/questions/26360034/how-to-send-data-to-server-from-android-when-no-internet-is-available

    public static final String DATA_SAVED_BROADCAST = "com.kiratcoding.asm.checkindata";
    private Context context;
    private CheckInDbHelper db;

    private static final int NO_CONNECTION_TYPE = -1;
    private static int sLastType = NO_CONNECTION_TYPE;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        db = new CheckInDbHelper(context);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) {
            //if connected to wifi or mobile data plan
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                //getting all the unsynced names
                Cursor cursor = db.getNotSyncAttendance();
                if (cursor.moveToFirst()) {
                    do {
                        Log.i("DataCall", "from Sqlite Db");
                        //calling the method to save the unsynced name to MySQL
                        uploadToPhp(
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.ID)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.CHECKIN_STATUS)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.VID)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.UID)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.UNAME)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.CURRENT_DATE)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.LATITUDE)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.LONGITUDE)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.START_READING)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.START_IMAGE)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.CURRENT_TIME)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.NOTE)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.FROM)),
                                cursor.getString(cursor.getColumnIndex(CheckInDbHelper.TO))
                        );
                    } while (cursor.moveToNext());
                }
            }
        }
    }

    private void uploadToPhp(String id,String checkin_status,String vid,String uniquenumber,String employeeName,String date,String latitude,String longitude,String reading,String start_image,String time,String notes,String fromLoc,String toLoc) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/CheckIn.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //updating the status in sqlite
                                db.updateCheckInSyncStatus(Integer.valueOf(id), 1 );
                                Log.i("Data", "ID: "+id+ " Uploaded To Php");
                                //sending the broadcast to refresh the list
                                context.sendBroadcast(new Intent(DATA_SAVED_BROADCAST));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("status",checkin_status);
                map.put("vehicleId", vid);
                map.put("uniquenumber", uniquenumber);
                map.put("employeeName", employeeName);
                map.put("currentDate", date);
                map.put("startLatitude", latitude);
                map.put("startLongitude", longitude);
                map.put("startReading", reading);
                map.put("startImage", start_image);
                map.put("startTime", time);
                map.put("note", notes);
                map.put("fromLocation", fromLoc);
                map.put("toLocation", toLoc);
                map.put("closeLatitude", "");
                map.put("closeLongitude", "");
                map.put("closeReading", "");
                map.put("closeImage", "");
                map.put("closeTime", "");
                map.put("timestamp", "");
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context, new HurlStack());
        requestQueue.add(stringRequest).setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 0;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0; //retry turn off
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

    }
}
