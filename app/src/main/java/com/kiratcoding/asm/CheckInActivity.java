package com.kiratcoding.asm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.kiratcoding.asm.ModelsClass.Attendance;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.OfflineSync.DbHelperClass.CheckInDbHelper;
import com.kiratcoding.asm.OfflineSync.DbHelperClass.TrackingDbHelper;
import com.kiratcoding.asm.OfflineSync.DbHelperClass.VehicleDbHelper;
import com.kiratcoding.asm.OfflineSync.NetworkCheckerClass.VolleySingleton;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefCheckIn;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckInActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener{
    private Button bt_submit,selectVehicleBtn;
    private ImageView iv;
    private TextView travelTypeText,topText,et_vehicle,Latitude,Longitude;
    private String TravelTypeIntent="", Msg="";
    private TextInputLayout et_tAmt, et_reding, et_task, et_from, et_to;

    String intentVId,attendanceId="",vehicleId="";
    private LocationManager manager;    //for GPS Location
    private LocationSettingsRequest.Builder builder; //for GPS Location Request Build

    // Request Codes
    private final int REQUEST_GPS_CODE = 800;   // GPS Request Code
    static final int REQUEST_IMAGE_CAPTURE = 1; // Camera Image Code
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 10;

    private FusedLocationProviderClient fusedLocationProviderClient;    //For Current Location

    private VehicleDbHelper vehicleDbHelper;

    private SimpleCursorAdapter adapter;
    private ListView vehicleListView;
    final String[] fromStrings=new String[]{vehicleDbHelper.ID, vehicleDbHelper.VID, vehicleDbHelper.TYPE};
    final int[] toInts =new int[]{R.id.tv_offline_id,R.id.offline_vehicle_id,R.id.offline_vehicle_type};

    private Bitmap bMap;
    String type="", b64_img="", mCurrentPhotoPath="",uniqueNumber="", userName="", currentDate="", currentTime="";
    String vehicleName="", vehicleType="";

    private String attendanceStatus="", fromLocation="", toLocation="", firstDate="", lastDate="";

    private CheckInDbHelper dbCheckIn;
    private Employee employee;
    private ProgressDialog pd;

    private TrackingDbHelper db;

    private LocationUpdatesService mService = null;
    private boolean mBound = false;
    private MyReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);    //For Location

        TravelTypeIntent = getIntent().getStringExtra("TravelType");
        Msg = getIntent().getStringExtra("Msg");

        employee = SharedPrefLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());
        userName = employee.getName();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());
        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");
        currentTime = time.format(calendar.getTime());

        pd = new ProgressDialog(CheckInActivity.this);
        pd.setMessage("Please Wait.. ");
        pd.setMessage("this might take a few minutes");
        pd.setCancelable(false);

        myReceiver = new MyReceiver();

        topText = findViewById(R.id.text);
        iv=findViewById(R.id.add_img);
        bt_submit = findViewById(R.id.attendance_button);
        travelTypeText = findViewById(R.id.et_vehicle_type);
        et_reding = findViewById(R.id.et_meter_reading);
        et_task = findViewById(R.id.et_task);
        et_vehicle = findViewById(R.id.et_vehicle);
        et_from = findViewById(R.id.et_from);
        et_to = findViewById(R.id.et_to);
        et_tAmt = findViewById(R.id.et_tAmt);
        selectVehicleBtn = findViewById(R.id.textView5);
        Latitude = findViewById(R.id.tv_latitude);
        Longitude = findViewById(R.id.tv_longitude);

        Latitude.setVisibility(View.GONE);
        Longitude.setVisibility(View.GONE);

        vehicleDbHelper = new VehicleDbHelper(this);
        Cursor cursor = vehicleDbHelper.getSyncVehicle();
        dbCheckIn = new CheckInDbHelper(this);
        db = new TrackingDbHelper(this);

        checkRequredExitsOrNot();
        selectVehicleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyCurrentLocation();
                final Dialog dialog = new Dialog(CheckInActivity.this);
                dialog.setContentView(R.layout.activity_vehicle);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(true);
                dialog.show();

                vehicleListView = dialog.findViewById(R.id.vehicleList);
                vehicleListView.setEmptyView(dialog.findViewById(R.id.no_vehicle));
                adapter = new SimpleCursorAdapter(CheckInActivity.this, R.layout.layout_offline_vehicle,cursor,fromStrings,toInts,0);
                adapter.notifyDataSetChanged();
                vehicleListView.setAdapter(adapter);

                vehicleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView idTextView=view.findViewById(R.id.tv_offline_id);
                        TextView vIdTextView=view.findViewById(R.id.offline_vehicle_id);
                        TextView typeTextView=view.findViewById(R.id.offline_vehicle_type);

                        String vid=vIdTextView.getText().toString();
                        String type=typeTextView.getText().toString();
                        vehicleId = vid;
                        et_vehicle.setText(type);
                        selectVehicleBtn.setText("Change Vehicle");
                        et_vehicle.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });

            }
        });

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAndRequestPermissions()) {
                    MyCurrentLocation();
                    CaptureImage();
                }
            }
        });

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String etFromLocation = et_from.getEditText().getText().toString();
                String etToLocation = et_to.getEditText().getText().toString();
                String etTask = et_task.getEditText().getText().toString();
                String etReading = et_reding.getEditText().getText().toString();
                String etAmt = et_tAmt.getEditText().getText().toString();

                String tvLatitude = Latitude.getText().toString().trim();
                String tvLongitude = Longitude.getText().toString().trim();

                if (TravelTypeIntent.equalsIgnoreCase("Own vehicle")) {
                    if (Msg.equalsIgnoreCase("CheckIn")) {
                        if (et_vehicle.getText().toString().equalsIgnoreCase("")) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            Toast.makeText(CheckInActivity.this, "Select Vehicle", Toast.LENGTH_SHORT).show();
                        } else if (etFromLocation.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_from.setError("Enter From Location");
                            et_from.requestFocus();
                            return;
                        } else if (etToLocation.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_to.setError("Enter To Location");
                            et_to.requestFocus();
                            return;
                        } else if (etTask.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_task.setError("Enter Today's Task");
                            et_task.requestFocus();
                            return;
                        } else if (etReading.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_reding.setError("Enter Reading");
                            et_reding.requestFocus();
                            return;
                        }
                        else if (b64_img == null) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            Toast.makeText(CheckInActivity.this, "Capture Reading Before Proceeding!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            buildAlertMessageNoGps();
                            bt_submit.setEnabled(false);
                            bt_submit.setClickable(false);
                            type = "1";
                            mService.requestLocationUpdates();
                            if (dbCheckIn.addCheckIn(Msg, vehicleId, uniqueNumber, userName, currentDate, tvLatitude, tvLongitude, etReading, b64_img, currentTime, etTask, etFromLocation, etToLocation, 0)) {
                                if (db.addTrackingDetail(uniqueNumber, employee.getName(), type, Latitude.getText().toString().trim(), Longitude.getText().toString().trim(), currentDate, currentTime, 0)) {
                                    Attendance attendance = new Attendance(Integer.parseInt(uniqueNumber), Integer.parseInt(vehicleId), Msg);
                                    SharedPrefCheckIn.getInstance(getApplicationContext()).userCheckIn(attendance);
                                    Toast.makeText(CheckInActivity.this, Msg, Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), AttendanceOptionActivity.class));
                                    finish();
                                }
                            }

                        }


                    }
                    else if (Msg.equalsIgnoreCase("CheckOut")) {
                        if (etTask.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_task.setError("Enter Today's Report");
                            et_task.requestFocus();
                            return;
                        }else if (etReading.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_reding.setError("Enter Reading");
                            et_reding.requestFocus();
                            return;
                        }else if (b64_img == null) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            Toast.makeText(CheckInActivity.this, "Capture Reading Before Proceeding!", Toast.LENGTH_SHORT).show();
                        } else {
                            bt_submit.setEnabled(false);
                            bt_submit.setClickable(false);
                            type = "3";
                            mService.removeLocationUpdates();
                            CheckOutNow(Msg);
                        }
                    }
                }
                if (TravelTypeIntent.equalsIgnoreCase("Public Transport")) {
                    if (Msg.equalsIgnoreCase("CheckIn")) {
                        if (etTask.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_task.setError("Enter Today's Task");
                            et_task.requestFocus();
                            return;
                        }
                        if (etFromLocation.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_from.setError("Enter Start Stop");
                            et_from.requestFocus();
                            return;
                        }
                        if (etToLocation.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_to.setError("Enter End Stop");
                            et_to.requestFocus();
                            return;
                        }
                        else {
                            type = "1";
                            buildAlertMessageNoGps();
                            bt_submit.setEnabled(false);
                            bt_submit.setClickable(false);
                            mService.requestLocationUpdates();//call service
                            if (dbCheckIn.addCheckIn(Msg, vehicleId, uniqueNumber, userName, currentDate, tvLatitude, tvLongitude, etReading, b64_img, currentTime, etTask, etFromLocation, etToLocation, 0)) {
                                if (db.addTrackingDetail(uniqueNumber, employee.getName(), type, Latitude.getText().toString().trim(), Longitude.getText().toString().trim(), currentDate, currentTime, 0)) {
                                    Attendance attendance = new Attendance(Integer.parseInt(uniqueNumber), Integer.parseInt(vehicleId), Msg);
                                    SharedPrefCheckIn.getInstance(getApplicationContext()).userCheckIn(attendance);
                                    Toast.makeText(CheckInActivity.this, Msg, Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), AttendanceOptionActivity.class));
                                    finish();
                                }
                            }
                        }
                    }
                    else if (Msg.equalsIgnoreCase("CheckOut")) {
                        if (etAmt.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_tAmt.setError("Enter Amount");
                            et_tAmt.requestFocus();
                            return;
                        }
                        if (etTask.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_task.setError("Enter Today's Report");
                            et_task.requestFocus();
                            return;
                        }
                        if (b64_img == null) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            Toast.makeText(CheckInActivity.this, "Capture Ticket Image Before Proceeding!", Toast.LENGTH_LONG).show();
                        } else {
                            type = "3";
                            bt_submit.setEnabled(false);
                            bt_submit.setClickable(false);
                            mService.removeLocationUpdates();
                            CheckOutNow(Msg);
                        }
                    }
                }
                if (TravelTypeIntent.equalsIgnoreCase("Work from Home")) {
                    if (Msg.equalsIgnoreCase("CheckIn")) {
                        if (etTask.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_task.setError("Enter Today's Task");
                            et_task.requestFocus();
                            return;
                        }if (b64_img == null) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            Toast.makeText(CheckInActivity.this, "Capture Image Before Proceeding!", Toast.LENGTH_LONG).show();
                        } else {
                            type = "1";
                            bt_submit.setEnabled(false);
                            bt_submit.setClickable(false);
                            if (dbCheckIn.addCheckIn(Msg, vehicleId, uniqueNumber, userName, currentDate, tvLatitude, tvLongitude, etReading, b64_img, currentTime, etTask, etFromLocation, etToLocation, 0)) {
                                if (db.addTrackingDetail(uniqueNumber, employee.getName(), type, Latitude.getText().toString().trim(), Longitude.getText().toString().trim(), currentDate, currentTime, 0)) {
                                    Attendance attendance = new Attendance(Integer.parseInt(uniqueNumber), Integer.parseInt(vehicleId), Msg);
                                    SharedPrefCheckIn.getInstance(getApplicationContext()).userCheckIn(attendance);
                                    Toast.makeText(CheckInActivity.this, Msg, Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), AttendanceOptionActivity.class));
                                    finish();
                                }
                            }
                        }
                    }
                    else if (Msg.equalsIgnoreCase("CheckOut")) {
                        if (etTask.isEmpty()) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            et_task.setError("Enter Today's Report");
                            et_task.requestFocus();
                            return;
                        }
                        if (b64_img == null) {
                            bt_submit.setEnabled(true);
                            bt_submit.setClickable(true);
                            Toast.makeText(CheckInActivity.this, "Capture Image Before Proceeding!", Toast.LENGTH_LONG).show();
                        } else {
                            type = "3";
                            bt_submit.setEnabled(false);
                            bt_submit.setClickable(false);
                            WFHCheckOutNow(Msg);
                        }
                    }
                }
            }
        });
//
//        registerReceiver(new CheckInNetworkStateus(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//        registerReceiver(broadcastReceiver, new IntentFilter(CheckInNetworkStateus.DATA_SAVED_BROADCAST));
    }

    private void WFHCheckOutNow(final String msg) {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/CheckOut.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pd.dismiss();
                SharedPrefCheckIn.getInstance(getApplicationContext()).checkout();
                Toast.makeText(CheckInActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), AttendanceOptionActivity.class));
                finish();
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
                Map<String, String> map = new HashMap<>();
                map.put("uniquenumber",uniqueNumber);
                map.put("status",msg);
                map.put("checkOutNote", et_task.getEditText().getText().toString().trim());
                map.put("employeeName", employee.getName());
                map.put("closeLatitude", Latitude.getText().toString().trim());
                map.put("closeLongitude", Longitude.getText().toString().trim());
                map.put("closeReading", "");
                map.put("closeImage", b64_img);
                map.put("closeTime", currentTime);
                return map;
            }
        };
        RequestQueue rQeue = Volley.newRequestQueue(CheckInActivity.this);
        rQeue.add(request);
    }

    private void CheckOutNow(final String msg) {
        pd.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/CheckOut.php",
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pd.dismiss();
                if (db.addTrackingDetail(uniqueNumber, employee.getName(), type, Latitude.getText().toString().trim(), Longitude.getText().toString().trim(), currentDate, currentTime, 0)) {
                    SharedPrefCheckIn.getInstance(getApplicationContext()).checkout();
                    Toast.makeText(CheckInActivity.this, Msg, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), AttendanceOptionActivity.class));
                    finish();
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
                String cloaseReading = et_reding.getEditText().getText().toString().trim();
                Map<String, String> map = new HashMap<>();
                map.put("uniquenumber",uniqueNumber);
                map.put("status",msg);
                map.put("checkOutNote", et_task.getEditText().getText().toString().trim());
                map.put("employeeName", employee.getName());
                map.put("closeLatitude", Latitude.getText().toString().trim());
                map.put("closeLongitude", Longitude.getText().toString().trim());
                map.put("closeReading", cloaseReading);
                map.put("closeImage", b64_img);
                map.put("closeTime", currentTime);
                return map;
            }
        };

        VolleySingleton.getInstance(CheckInActivity.this).addToRequestQueue(stringRequest);
    }

    private void checkRequredExitsOrNot() {
        buildAlertMessageNoGps();   //Message for No GPS
        checkViewsVisibility();         //Check View Visibilty
        if(checkAndRequestPermissions()){}
    }

    private void checkViewsVisibility() {
        if(TravelTypeIntent.equalsIgnoreCase("Own vehicle")){
            travelTypeText.setText("Personal | Company Vehicle");
            iv.setVisibility(View.VISIBLE);
            et_tAmt.setVisibility(View.GONE);
            et_reding.setVisibility(View.VISIBLE);
            if (Msg.equalsIgnoreCase("CheckIn")) {
                bt_submit.setText("Check In");
                topText.setText("Check-In Meter Reading");
                et_reding.setHint("Enter Start Reading");
                et_task.setHint("What's your plan today");
                et_vehicle.setVisibility(View.GONE);
            } else if (Msg.equalsIgnoreCase("CheckOut")) {
                GetAttendance();

                bt_submit.setText("Check Out");
                topText.setText("Check-Out Meter Reading");
                et_reding.setHint("Enter Close Reading");
                et_task.setHint("Today's Report");
                et_vehicle.setVisibility(View.VISIBLE);
                selectVehicleBtn.setVisibility(View.GONE);
            }
        }
        else if(TravelTypeIntent.equalsIgnoreCase("Public Transport")) {
            vehicleId="0";
            travelTypeText.setText("Public Transport");
            if (Msg.equalsIgnoreCase("CheckIn")) {
                bt_submit.setText("Check In");
                topText.setText("Check-In Details");
                et_task.setHint("What's your plan today");
                et_task.setVisibility(View.VISIBLE);
                et_from.setVisibility(View.VISIBLE);
                et_to.setVisibility(View.VISIBLE);
                et_tAmt.setVisibility(View.GONE);
                et_reding.setVisibility(View.GONE);
                et_vehicle.setVisibility(View.GONE);
                selectVehicleBtn.setVisibility(View.GONE);
                iv.setVisibility(View.GONE);
            }
            else if (Msg.equalsIgnoreCase("CheckOut")) {
                GetAttendance();

                bt_submit.setText("Check Out");
                topText.setText("Check-Out Ticket Details");
                et_task.setHint("Today's Report");
                et_tAmt.setHint("Ticket Amount");
                iv.setVisibility(View.VISIBLE);
                et_task.setVisibility(View.VISIBLE);
                et_tAmt.setVisibility(View.VISIBLE);
                et_from.setVisibility(View.VISIBLE);
                et_to.setVisibility(View.VISIBLE);
                et_from.setFocusable(false);
                et_to.setFocusable(false);
                et_reding.setVisibility(View.GONE);
                et_vehicle.setVisibility(View.GONE);
                selectVehicleBtn.setVisibility(View.GONE);
            }
        }
        else if(TravelTypeIntent.equalsIgnoreCase("Work from Home")) {
            vehicleId="123456";
            travelTypeText.setText("Work from Home");
            if (Msg.equalsIgnoreCase("CheckIn")) {
                bt_submit.setText("Check In");
                topText.setText("Check-In For WFH");
                et_task.setHint("What's your plan today");
                iv.setVisibility(View.VISIBLE);
                et_reding.setVisibility(View.GONE);
                et_vehicle.setVisibility(View.GONE);
                et_reding.setVisibility(View.GONE);
                et_tAmt.setVisibility(View.GONE);
                selectVehicleBtn.setVisibility(View.GONE);
                et_from.setVisibility(View.GONE);
                et_to.setVisibility(View.GONE);
                et_tAmt.setVisibility(View.GONE);
            } else if (Msg.equalsIgnoreCase("CheckOut")) {
                bt_submit.setText("Check Out");
                topText.setText("Check-Out For WFH");
                et_task.setHint("Today's Report");
                iv.setVisibility(View.VISIBLE);
                et_reding.setVisibility(View.GONE);
                et_vehicle.setVisibility(View.GONE);
                et_reding.setVisibility(View.GONE);
                et_tAmt.setVisibility(View.GONE);
                selectVehicleBtn.setVisibility(View.GONE);
                et_from.setVisibility(View.GONE);
                et_to.setVisibility(View.GONE);
                et_tAmt.setVisibility(View.GONE);
            }
        }
    }

    //CaptureImage
    private void CaptureImage() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                if(ex.getMessage().equalsIgnoreCase("Permission denied")){
                    Log.i("CaptureMsg", ex.getMessage());
                    Toast.makeText(CheckInActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
            if (photoFile != null) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Report" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory()+File.separator+"images");
        storageDir.mkdirs();
        File image = File.createTempFile(
                imageFileName,".jpg", storageDir
        );
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if(resultCode == RESULT_OK){
                try {
                    Matrix mat = new Matrix();
                    bMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                    mat.postRotate(0);
                    Bitmap bMapRotate = Bitmap.createBitmap(bMap, 0, 0,bMap.getWidth(),bMap.getHeight(), mat, true);
                    iv.setImageBitmap(bMapRotate);
                    double ratio;
                    int reqw= (int) ((int) bMapRotate.getWidth()* 0.3);
                    int reqh= (int) ((int) bMapRotate.getHeight()* 0.3);
                    Bitmap bMapReduced = Bitmap.createScaledBitmap(bMapRotate,reqw,reqh,true);

                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    bMapReduced.compress(Bitmap.CompressFormat.JPEG,10, baos);
                    byte [] b=baos.toByteArray();
                    b64_img= Base64.encodeToString(b, Base64.DEFAULT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Build Msg Fro No GPS
    private void buildAlertMessageNoGps() {
        MyCurrentLocation();
        LocationRequest request = new LocationRequest()
                .setFastestInterval(500)
                .setInterval(1500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder().addLocationRequest(request);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(CheckInActivity.this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode()){
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(CheckInActivity.this,REQUEST_GPS_CODE);
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            } catch (ClassCastException ex){}
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: {
                            break;
                        }
                    }
                }
            }
        });
    }

    private void GetAttendance() {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/NewScript/Attendance.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Response////  ",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                pd.dismiss();
                                JSONArray jsonArray = jsonObject.getJSONArray("Attendance");
                                JSONObject object;
                                List<Integer> intValues = null;
                                for (int i=0; i<jsonArray.length();i++){
                                    object = jsonArray.getJSONObject(i);
                                    fromLocation = object.getString("fromLocation");
                                    toLocation = object.getString("toLocation");
                                    //To Store Vehicle Id
                                    vehicleId = String.valueOf(object.getInt("vehicleId"));

                                    et_from.getEditText().setText(fromLocation);
                                    et_to.getEditText().setText(toLocation);


                                    CheckVehicles(uniqueNumber, vehicleId);

                                    int id = object.getInt("id");
                                    intValues = new ArrayList<>();
                                    intValues.add(id);
                                }

                                Integer max = Collections.max(intValues);
                                //To store Attendance Id
                                attendanceId = String.valueOf(max);

                                Log.i("fromLocation",fromLocation);
                                Log.i("toLocation",toLocation);
                                Log.i("vehicleId",vehicleId);
                                Log.i("attendanceId", attendanceId);

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
                    Toast.makeText(CheckInActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
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

        RequestQueue rQeue= Volley.newRequestQueue(CheckInActivity.this);
        rQeue.add(request);

    }
    private void CheckVehicles(String uniqueNumber, String intentVId) {
        Log.i("intentVId",intentVId);
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
                                    int id = object.getInt("id");
                                    String type = object.getString("type");
                                    et_vehicle.setText(type);
                                    pd.dismiss();

                                }
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

        RequestQueue rQeue= Volley.newRequestQueue(CheckInActivity.this);
        rQeue.add(request);

    }

    //Check Required Permission
    private boolean checkAndRequestPermissions() {
        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("TAG", "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("TAG", "sms & location services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("TAG", "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            Snackbar.make(
                                    findViewById(R.id.layout_attendance),
                                    R.string.permission_rationale,
                                    Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            checkAndRequestPermissions();
                                        }
                                    })
                                    .show();
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Snackbar.make(
                                    findViewById(R.id.layout_attendance),
                                    R.string.permission_denied_explanation,
                                    Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.settings, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            // Build intent that displays the App settings screen.
                                            Intent intent = new Intent();
                                            intent.setAction(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package",
                                                    getApplicationContext().getPackageName(), null);
                                            intent.setData(uri);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    })
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }
    private void MyCurrentLocation() {
        Log.i("MyCurrentLocation"," Called");
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null){
                        try {
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());  //Initialize geoCode
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                            Latitude.setText(""+addresses.get(0).getLatitude());
                            Longitude.setText(""+addresses.get(0).getLongitude());

                            Log.i("Location: ",
                                    "Latitude : "+addresses.get(0).getLatitude()+
                                            "\nLongitude : "+addresses.get(0).getLongitude()+
                                            "\nCountry : "+addresses.get(0).getCountryName()+
                                            "\nLocality : "+addresses.get(0).getLocality()+
                                            "\nAddress : "+addresses.get(0).getAddressLine(0)
                            );

                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    //https://stackoverflow.com/questions/5205650/geocoder-getfromlocation-throws-ioexception-on-android-emulator
                }
            });
        }else{
            ActivityCompat.requestPermissions(CheckInActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            startActivity(new Intent(getApplicationContext(), AttendanceOptionActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkNetworkConnectionStatus() {
        final Dialog dialog = new Dialog(CheckInActivity.this);
        dialog.setContentView(R.layout.layout_connectivity);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();

        ImageView mCloseIv = dialog.findViewById(R.id.conClose);
        mCloseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ImageView mConStatusIv = dialog.findViewById(R.id.conStatusIv);
        TextView mConStatusTv = dialog.findViewById(R.id.conStatusTv);
        TextView mConStatusCloseBtn = dialog.findViewById(R.id.close);
        TextView mConStatusDataBtn = dialog.findViewById(R.id.setting_btn);

        mConStatusCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
                dialog.show();
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

    //Location Service
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    private void LocationAction() {
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        setButtonsState(Utils.requestingLocationUpdates(this));
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationAction();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }
    @Override
    protected void onStop() {
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = true;
        }
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Log.i("Location Updated",Utils.getLocationText(location));
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES, false));
        }
    }
    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            bt_submit.setEnabled(true);
        }
    }

}