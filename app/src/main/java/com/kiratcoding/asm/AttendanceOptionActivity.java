package com.kiratcoding.asm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.kiratcoding.asm.HelperClass.HttpsTrustManager;
import com.kiratcoding.asm.ModelsClass.Attendance;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.OfflineSync.DbHelperClass.CheckInDbHelper;
import com.kiratcoding.asm.OfflineSync.NetworkCheckerClass.CheckInNetworkStatus;
import com.kiratcoding.asm.OfflineSync.NetworkCheckerClass.TrackingNetworkStatus;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefCheckIn;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AttendanceOptionActivity extends AppCompatActivity {

    private Button Checkin, Checkout,reCheckin;
    private String currentDate,currentTime, uniqueNumber, attendanceStatus="", attendanceId="", fromLocation="", toLocation="", vehicleId="";
    private ProgressDialog pd;

    private Employee employee;
    private Attendance attendance;

    private CheckInDbHelper checkInDb;

    private String et_vehicle="",from="",to="",note="",date="",vid="";
    private BroadcastReceiver broadcastReceiver;

    private String intentMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_option);
        Toolbar toolbar = findViewById(R.id.attendance_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Attendance");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        HttpsTrustManager.allowAllSSL();

        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait.. ");
        pd.setCanceledOnTouchOutside(false);

        checkInDb = new CheckInDbHelper(this);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());
        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss");
        currentTime = time.format(calendar.getTime());

        employee = SharedPrefLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());

        Checkin = findViewById(R.id.checkin_btn);
        Checkout = findViewById(R.id.checkout_btn);
        reCheckin = findViewById(R.id.reCheckin_btn);

        reCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = checkInDb.getNotSyncAttendance();
                if (cursor.getCount() == 0){
                    showMessage("Opps..", "Data not Available");
                }else {
                    StringBuffer buffer = new StringBuffer();
                    while (cursor.moveToNext()){
                        buffer.append("Id :"+cursor.getString(0)+"\n");
                        buffer.append("Attendance Status :"+cursor.getString(1)+"\n");
                        buffer.append("Vehicle Id :"+cursor.getString(2)+"\n");
                        buffer.append("uniquenumber :"+cursor.getString(3)+"\n");
                        buffer.append("employeeName :"+cursor.getString(4)+"\n");
                        buffer.append("date :"+cursor.getString(5)+"\n");
                        buffer.append("latitude :"+cursor.getString(6)+"\n");
                        buffer.append("longitude :"+cursor.getString(7)+"\n");
                        buffer.append("reading :"+cursor.getString(8)+"\n");
                        buffer.append("time :"+cursor.getString(10)+"\n");
                        buffer.append("notes :"+cursor.getString(11)+"\n");
                        buffer.append("fromLoc :"+cursor.getString(12)+"\n");
                        buffer.append("toLoc :"+cursor.getString(13)+"\n");
                        buffer.append("Status :"+cursor.getString(14)+"\n");
                    }
                    showMessage("Data", buffer.toString());
                }
            }
        });

        Checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog typeDialog = new Dialog(AttendanceOptionActivity.this);
                typeDialog.setContentView(R.layout.layout_options);
                typeDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                typeDialog.setTitle("");
                typeDialog.setCancelable(true);
                typeDialog.show();

                TextView t1 = typeDialog.findViewById(R.id.Toptext);
                Button b1 = typeDialog.findViewById(R.id.btnOption1);
                Button b2 = typeDialog.findViewById(R.id.btnOption2);
                Button b3 = typeDialog.findViewById(R.id.btnOption3);
                b3.setVisibility(View.VISIBLE);
                t1.setText("Select Travel Type");
                b1.setText("Private/Company vehicle");
                b2.setText("Public Transport");
                b3.setText("Work from Home");

                b1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
                        intent.putExtra("TravelType", "Own vehicle");
                        intent.putExtra("Msg", "CheckIn");
                        startActivity(intent);
                        typeDialog.dismiss();
                        finish();
                    }
                });

                b2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
                        intent.putExtra("TravelType", "Public Transport");
                        intent.putExtra("Msg", "CheckIn");
                        startActivity(intent);
                        typeDialog.dismiss();
                        finish();
                    }
                });

                b3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
                        intent.putExtra("TravelType", "Work from Home");
                        intent.putExtra("Msg", "CheckIn");
                        startActivity(intent);
                        typeDialog.dismiss();
                        finish();
                    }
                });

            }
        });

        Checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNetworkConnectionStatus()) {
                    if (!vehicleId.equalsIgnoreCase("")) {
                        CheckoutAction(vehicleId);
                    }else {
                        recreate();
                        Animatoo.animateFade(AttendanceOptionActivity.this);
                    }
                }
            }
        });

        registerReceiver(new CheckInNetworkStatus(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(broadcastReceiver, new IntentFilter(CheckInNetworkStatus.DATA_SAVED_BROADCAST));

    }

    private void CheckoutAction(String vId) {
        if (vId.equalsIgnoreCase("0")) {
            Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
            intent.putExtra("Msg", "CheckOut");
            intent.putExtra("TravelType", "Public Transport");
            startActivity(intent);
            finish();
            Log.i("PublicvehicleId", vId);
        } else if (!vId.equalsIgnoreCase("0")) {
            if (vId.equalsIgnoreCase("123456")) {
                Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
                intent.putExtra("Msg", "CheckOut");
                intent.putExtra("TravelType", "Work from Home");
                startActivity(intent);
                finish();
                Log.i("vIdWFH", vId);
            } else {
                Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
                intent.putExtra("Msg", "CheckOut");
                intent.putExtra("TravelType", "Own vehicle");
                startActivity(intent);
                finish();
                Log.i("OwnVehicleId", vId);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        attendance = SharedPrefCheckIn.getInstance(this).getAttendance();
        attendanceStatus = String.valueOf(attendance.getStatus());
        intentMsg = getIntent().getStringExtra("successMsg");
        if (intentMsg.equalsIgnoreCase("attendance")){
            pd.show();
            CheckInVehicleId();
            getAttendanceStatus();
        }else if (intentMsg.equalsIgnoreCase("CheckedIn")){
            Checkin.setEnabled(false);
            Checkout.setEnabled(true);
        }else if (intentMsg.equalsIgnoreCase("CheckedOut")){
            Checkin.setEnabled(true);
            Checkout.setEnabled(false);
        }
    }

    private void CheckInVehicleId() {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/NewScript/Attendance.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Response////  ",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("Attendance");
                                JSONObject object;
                                for (int i=0; i<jsonArray.length();i++){
                                    object = jsonArray.getJSONObject(i);
                                    vehicleId = String.valueOf(object.getInt("vehicleId"));
                                    Log.i("CheckInVehicle",vehicleId);
                                }
                                pd.dismiss();
                            }else if (jsonObject.optString("status").equals("false")){
                                pd.dismiss();
                            }
                        } catch (JSONException e) {
                            pd.dismiss();
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pd.dismiss();
                String message = null;
                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                    Toast.makeText(AttendanceOptionActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found. Please try again later";
                    Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                map.put("uniquenumber",String.valueOf(employee.getUniquenumber()));
                map.put("currentDate",currentDate);
                return map;
            }
        };

        RequestQueue rQeue= Volley.newRequestQueue(AttendanceOptionActivity.this);
        rQeue.add(request);

    }
    private void getAttendanceStatus() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://arunodyafeeds.com/sales/android/NewScript/AttendanceStatus.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("AStatusResponse  ",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("Attendance");
                            for (int i=0; i<jsonArray.length();i++){
                                JSONObject object = jsonArray.getJSONObject(i);
                                String status = object.getString("status");
                                date = object.getString("date");
                                vid = object.getString("vid");
                                from = object.getString("from");
                                to = object.getString("to");
                                note = object.getString("note");

                                if (status.equalsIgnoreCase("previousCheckIn")){
                                    pd.dismiss();
                                    Checkin.setEnabled(false);
                                    Checkout.setEnabled(true);
                                    final Dialog dialog = new Dialog(AttendanceOptionActivity.this);
                                    dialog.setContentView(R.layout.layout_checkin_notify);
                                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    dialog.setCancelable(false);
                                    dialog.show();

                                    Button update = dialog.findViewById(R.id.updateStatusBtn);
                                    Button cancel = dialog.findViewById(R.id.cancelStatusBtn);
                                    update.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (vid.equalsIgnoreCase("0")) {
                                                Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
                                                intent.putExtra("TravelType", "Previous Transport");
                                                intent.putExtra("Msg", "CheckIn");
                                                intent.putExtra("prevFrom", from);
                                                intent.putExtra("prevTo", to);
                                                intent.putExtra("prevNote", note);
                                                intent.putExtra("prevVid", vid);
                                                intent.putExtra("prevDate", date);
                                                startActivity(intent);
                                                finish();
                                                Log.e("Previous Transport", vid);
                                            } else if (!vid.equalsIgnoreCase("0")) {
                                                if (vid.equalsIgnoreCase("123456")) {
                                                    Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
                                                    intent.putExtra("TravelType", "Previous WFH");
                                                    intent.putExtra("Msg", "CheckIn");
                                                    intent.putExtra("prevFrom", from);
                                                    intent.putExtra("prevTo", to);
                                                    intent.putExtra("prevNote", note);
                                                    intent.putExtra("prevVid", vid);
                                                    intent.putExtra("prevDate", date);
                                                    startActivity(intent);
                                                    finish();
                                                    Log.e("Previous WFH", vid);
                                                } else {
                                                    Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
                                                    intent.putExtra("TravelType", "Previous Vehicle");
                                                    intent.putExtra("Msg", "CheckIn");
                                                    intent.putExtra("prevFrom", from);
                                                    intent.putExtra("prevTo", to);
                                                    intent.putExtra("prevNote", note);
                                                    intent.putExtra("prevVid", vid);
                                                    intent.putExtra("prevDate", date);
                                                    startActivity(intent);
                                                    finish();
                                                    Log.e("Previous Vehicle", vid);
                                                }
                                            }

                                            dialog.dismiss();
                                        }
                                    });
                                    cancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            CheckoutAction(vid);
                                            dialog.dismiss();
                                        }
                                    });
                                } else if (status.equalsIgnoreCase("CheckIn")){
                                    pd.dismiss();
                                    Log.i("StatusCheckIn  ","Executed");
                                    Checkin.setEnabled(false);
                                    Checkout.setEnabled(true);
                                }else if (status.equalsIgnoreCase("CheckOut")){
                                    pd.dismiss();
                                    Log.i("StatusCheckOut  ","Executed");
                                    SharedPrefCheckIn.getInstance(getApplicationContext()).checkout();
                                    Checkin.setEnabled(true);
                                    Checkout.setEnabled(false);
                                }

                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                            pd.dismiss();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pd.dismiss();
                if (attendanceStatus.equalsIgnoreCase("CheckIn")) {
                    Checkin.setEnabled(false);
                    Checkout.setEnabled(true);
                }else{
                    Checkin.setEnabled(true);
                    Checkout.setEnabled(false);
                }
                String message = null;
                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                    Toast.makeText(AttendanceOptionActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found. Please try again later";
                    Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                map.put("uniquenumber",uniqueNumber);
                map.put("currentDate",currentDate);
                return map;
            }
        };

        RequestQueue rQeue= Volley.newRequestQueue(AttendanceOptionActivity.this);
        rQeue.add(request);

    }

    private boolean checkNetworkConnectionStatus() {
        final Dialog dialog = new Dialog(AttendanceOptionActivity.this);
        dialog.setContentView(R.layout.layout_connectivity);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();

        ImageView mCloseIv = dialog.findViewById(R.id.conClose);
        mCloseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                recreate();
                Animatoo.animateFade(AttendanceOptionActivity.this);
            }
        });

        ImageView mConStatusIv = dialog.findViewById(R.id.conStatusIv);
        TextView mConStatusTv = dialog.findViewById(R.id.conStatusTv);
        TextView mConStatusCloseBtn = dialog.findViewById(R.id.close);
        TextView mConStatusDataBtn = dialog.findViewById(R.id.setting_btn);
        mConStatusTv.setText("For Check-out You have to connect with Internet");
        mConStatusCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                recreate();
                Animatoo.animateFade(AttendanceOptionActivity.this);
            }
        });

        boolean wifiConnected;
        boolean mobileConnected;
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()){ //connected with either mobile or wifi
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if (wifiConnected){ //wifi connected
                Log.i("Connected with ", "Connected with Wifi");
                dialog.dismiss();
                return true;
            }
            else if (mobileConnected){ //mobile data connected
                Log.i("Connected with ", "Connected with Mobile Data Connection");
                dialog.dismiss();
                return true;
            }
        } else { //no internet connection
            mConStatusIv.setImageResource(R.drawable.ic_wifi_off_24);
            mConStatusDataBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                    dialog.dismiss();
                }
            });
            return false;
        }

        return false;
    }

    public void showMessage(String Title, String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(Title);
        builder.setMessage(Message);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void ViewAttendance(View view) {
        startActivity(new Intent(getApplicationContext(),AttendanceReportActivity.class));
    }

    public void ViewMonthlyAttendance(View view) {
        startActivity(new Intent(getApplicationContext(),MonthlyCalenderActivity.class));
    }

    private boolean ConnectionStatus() {
        boolean wifiConnected;
        boolean mobileConnected;
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()){ //connected with either mobile or wifi
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if (wifiConnected){ //wifi connected
                Log.i("Connected with ", "Connected with Wifi");
                return true;
            }
            else if (mobileConnected){ //mobile data connected
                Log.i("Connected with ", "Connected with Mobile Data Connection");
                return true;
            }
        } else { //no internet connection
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return false;
        }

        return false;
    }
    private void CheckVehicles(String uniqueNumber, String intentVId) {
        Log.i("intentVId",intentVId);
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/NewScript/vehiclesDetails.php", //production
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("VehicleResponse",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("Vehicle");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    String type = object.getString("type");
                                    et_vehicle = type;
                                    Log.i("et_vehicle",et_vehicle);
                                }

                                if (intentVId.equalsIgnoreCase("0")) {
                                    Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
                                    intent.putExtra("TravelType", "Previous Transport");
                                    intent.putExtra("Msg", "CheckIn");
                                    intent.putExtra("prevFrom", from);
                                    intent.putExtra("prevTo", to);
                                    intent.putExtra("prevNote", note);
                                    intent.putExtra("prevVid", intentVId);
                                    intent.putExtra("prevDate", date);
                                    startActivity(intent);
                                    finish();
                                    Log.e("Previous Transport", intentVId);
                                } else if (!intentVId.equalsIgnoreCase("0")) {
                                    if (intentVId.equalsIgnoreCase("123456")) {
                                        Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
                                        intent.putExtra("TravelType", "Previous WFH");
                                        intent.putExtra("Msg", "CheckIn");
                                        intent.putExtra("prevFrom", from);
                                        intent.putExtra("prevTo", to);
                                        intent.putExtra("prevNote", note);
                                        intent.putExtra("prevVid", intentVId);
                                        intent.putExtra("prevDate", date);
                                        startActivity(intent);
                                        finish();
                                        Log.e("Previous WFH", intentVId);
                                    } else {
                                        Intent intent = new Intent(getApplicationContext(), CheckInActivity.class);
                                        intent.putExtra("TravelType", "Previous Vehicle");
                                        intent.putExtra("Msg", "CheckIn");
                                        intent.putExtra("prevFrom", from);
                                        intent.putExtra("prevTo", to);
                                        intent.putExtra("prevNote", note);
                                        intent.putExtra("prevVid", intentVId);
                                        intent.putExtra("prevDate", date);
                                        startActivity(intent);
                                        finish();
                                        Log.e("Previous Vehicle", intentVId);
                                    }
                                }
                                pd.dismiss();
                            }else if (jsonObject.optString("status").equals("false")){
                                pd.dismiss();
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                            pd.dismiss();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pd.dismiss();
                String message = null;
                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                    if (checkNetworkConnectionStatus()) {}
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found. Please try again later";
                    Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                map.put("uniquenumber", uniqueNumber);
                map.put("id", intentVId);
                return map;
            }

        };

        RequestQueue rQeue= Volley.newRequestQueue(AttendanceOptionActivity.this);
        rQeue.add(request);
    }

}