package com.kiratcoding.asm;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendancePrivateActivity extends AppCompatActivity  implements
        SharedPreferences.OnSharedPreferenceChangeListener{

    private TextView ownSelectedVehicle, ownTopText, Latitude, Longitude;
    private TextInputLayout ownFrom, ownTo, ownTaskDetails, ownMeterReading;
    private ImageView ownMeterImage;
    private Button selectVehicleButton, submitButton;

    private VehicleDbHelper vehicleDbHelper;
    private ListView vehicleListView;
    private SimpleCursorAdapter adapter;
    private String[] fromStrings=new String[]{vehicleDbHelper.ID, vehicleDbHelper.VID, vehicleDbHelper.TYPE};
    private int[] toInts =new int[]{R.id.tv_offline_id,R.id.offline_vehicle_id,R.id.offline_vehicle_type};

    private String Msg="",uniqueNumber="", userName="", currentDate="", currentTime="", vehicleId="";
    private String SelectedVehicle="",From="", To="", TaskNote="", MeterReading="", b64_img="", mCurrentPhotoPath="", type="", mLatitude="", mLongitude="";
    private String intentFrom="", intentTo="", intentVId="", intentCheckInType= "", intentPrevDate="";

    private Bitmap bMap;
    private ProgressDialog pd;
    private LocationSettingsRequest.Builder builder; //for GPS Location Request Build
    private FusedLocationProviderClient fusedLocationProviderClient;    //For Current Location

    private TrackingDbHelper db;
    private CheckInDbHelper dbCheckIn;
    private Employee employee;

    // Request Codes
    private final int REQUEST_GPS_CODE = 800;   // GPS Request Code
    static final int REQUEST_IMAGE_CAPTURE = 1; // Camera Image Code
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 10;

    //Location Service
    private LocationUpdatesService mService = null;
    private boolean mBound = false;
    private MyReceiver myReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_private);

        vehicleDbHelper = new VehicleDbHelper(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);    //For Location

        Msg = getIntent().getStringExtra("Msg");
        intentCheckInType = getIntent().getStringExtra("TravelType");

        employee = SharedPrefLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());
        userName = employee.getName();

        ownTopText = findViewById(R.id.private_Top_text);
        ownSelectedVehicle = findViewById(R.id.selected_vehicle);
        selectVehicleButton = findViewById(R.id.btn_selectVehicle);
        ownFrom = findViewById(R.id.private_from);
        ownTo = findViewById(R.id.private_to);
        ownTaskDetails = findViewById(R.id.private_task);
        ownMeterReading = findViewById(R.id.private_meter_reading);
        ownMeterImage = findViewById(R.id.private_add_img);
        submitButton = findViewById(R.id.private_button);
        Latitude = findViewById(R.id.tv_latitude);
        Longitude = findViewById(R.id.tv_longitude);

        Latitude.setVisibility(View.GONE);
        Longitude.setVisibility(View.GONE);
        ownSelectedVehicle.setVisibility(View.GONE);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());
        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");
        currentTime = time.format(calendar.getTime());

        db = new TrackingDbHelper(this);
        dbCheckIn = new CheckInDbHelper(this);
        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait.. ");
        pd.setMessage("this might take a few minutes");
        pd.setCancelable(false);


        if (Msg.equalsIgnoreCase("CheckIn")) {
            submitButton.setText("Check In");
            ownTopText.setText("Check-In Meter Reading");
            ownMeterReading.setHint("Enter Start Reading");
            ownTaskDetails.setHint("What's your plan today");
            if (intentCheckInType.equalsIgnoreCase("Own vehicle")) {
                ownSelectedVehicle.setVisibility(View.GONE);
            }else if (intentCheckInType.equalsIgnoreCase("Previous Private Vehicle")){
                intentVId = getIntent().getStringExtra("VehicleId");
                intentPrevDate = getIntent().getStringExtra("prevDate");
                getVehicleDetails(uniqueNumber, intentVId);
                intentFrom = getIntent().getStringExtra("fromLocation");
                intentTo = getIntent().getStringExtra("toLocation");
                ownFrom.getEditText().setText(intentFrom);
                ownTo.getEditText().setText(intentTo);
                ownSelectedVehicle.setVisibility(View.VISIBLE);
                selectVehicleButton.setVisibility(View.GONE);
            }
        } else if (Msg.equalsIgnoreCase("CheckOut")) {
            intentVId = getIntent().getStringExtra("VehicleId");
            getVehicleDetails(uniqueNumber, intentVId);
            intentFrom = getIntent().getStringExtra("fromLocation");
            intentTo = getIntent().getStringExtra("toLocation");
            ownFrom.getEditText().setText(intentFrom);
            ownTo.getEditText().setText(intentTo);
            submitButton.setText("Check Out");
            ownTopText.setText("Check-Out Meter Reading");
            ownMeterReading.setHint("Enter Close Reading");
            ownTaskDetails.setHint("Today's Report");
            ownSelectedVehicle.setVisibility(View.VISIBLE);
            selectVehicleButton.setVisibility(View.GONE);
        }


        ownMeterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAndRequestPermissions()) {
                    MyCurrentLocation();
                    CaptureImage();
                }
            }
        });

        buildAlertMessageNoGps();
        if(checkAndRequestPermissions()){}
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedVehicle =ownSelectedVehicle.getText().toString().trim();
                From =ownFrom.getEditText().getText().toString().trim();
                To =ownTo.getEditText().getText().toString().trim();
                TaskNote =ownTaskDetails.getEditText().getText().toString().trim();
                MeterReading =ownMeterReading.getEditText().getText().toString().trim();
                mLatitude = Latitude.getText().toString().trim();
                mLongitude = Longitude.getText().toString().trim();

                if (Msg.equalsIgnoreCase("CheckIn")) {

                    if (intentCheckInType.equalsIgnoreCase("Own vehicle")) {
                        if (SelectedVehicle.equalsIgnoreCase("Selected Vehicle")){
                            Toast.makeText(AttendancePrivateActivity.this, "Select Vehicle", Toast.LENGTH_SHORT).show();
                        }else if (TextUtils.isEmpty(From)){
                            Toast.makeText(AttendancePrivateActivity.this, "Enter From Location", Toast.LENGTH_SHORT).show();
                        }else if (TextUtils.isEmpty(To)){
                            Toast.makeText(AttendancePrivateActivity.this, "Enter To Location", Toast.LENGTH_SHORT).show();
                        }else if (TextUtils.isEmpty(TaskNote)){
                            Toast.makeText(AttendancePrivateActivity.this, "Enter Today's Task", Toast.LENGTH_SHORT).show();
                        }else if (TextUtils.isEmpty(MeterReading)){
                            Toast.makeText(AttendancePrivateActivity.this, "Enter Reading", Toast.LENGTH_SHORT).show();
                        }else if (TextUtils.isEmpty(b64_img)){
                            Toast.makeText(AttendancePrivateActivity.this, "Capture Reading Before Proceeding!", Toast.LENGTH_SHORT).show();
                        }else if (vehicleId.equalsIgnoreCase("0")){
                            Toast.makeText(mService, "Vid is 0", Toast.LENGTH_SHORT).show();
                        }else {
                            submitButton.setEnabled(false);
                            submitButton.setClickable(false);
                            mService.requestLocationUpdates();
                            if (dbCheckIn.addCheckIn(Msg, vehicleId, uniqueNumber, userName, currentDate, mLatitude, mLongitude, MeterReading, b64_img, currentTime, TaskNote, From, To, 0)) {
                                if (db.addTrackingDetail(uniqueNumber, employee.getName(), "1", mLatitude, mLongitude, currentDate, currentTime, 0)) {
                                    Attendance attendance = new Attendance(Integer.parseInt(uniqueNumber), Integer.parseInt(vehicleId), Msg);
                                    SharedPrefCheckIn.getInstance(getApplicationContext()).userCheckIn(attendance);
                                    Toast.makeText(AttendancePrivateActivity.this, Msg, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), AttendanceOptionsActivity.class);
                                    intent.putExtra("successMsg", "CheckedIn");
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }
                    }else if (intentCheckInType.equalsIgnoreCase("Previous Private Vehicle")){
                        if (TextUtils.isEmpty(From)){
                            Toast.makeText(AttendancePrivateActivity.this, "Enter From Location", Toast.LENGTH_SHORT).show();
                        }else if (TextUtils.isEmpty(To)){
                            Toast.makeText(AttendancePrivateActivity.this, "Enter To Location", Toast.LENGTH_SHORT).show();
                        }else if (TextUtils.isEmpty(TaskNote)){
                            Toast.makeText(AttendancePrivateActivity.this, "Enter Today's Task", Toast.LENGTH_SHORT).show();
                        }else if (TextUtils.isEmpty(MeterReading)){
                            Toast.makeText(AttendancePrivateActivity.this, "Enter Reading", Toast.LENGTH_SHORT).show();
                        }else if (TextUtils.isEmpty(b64_img)){
                            Toast.makeText(AttendancePrivateActivity.this, "Capture Reading Before Proceeding!", Toast.LENGTH_SHORT).show();
                        }else {
                            pd.show();
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/NewScript/PreviousCheckout.php",
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            Log.i("CheckOutResponse",response);
                                            if (response.equalsIgnoreCase("Checked-Out")) {
                                                Toast.makeText(AttendancePrivateActivity.this, response, Toast.LENGTH_SHORT).show();
                                                mService.requestLocationUpdates();
                                                if (dbCheckIn.addCheckIn(Msg, vehicleId, uniqueNumber, userName, currentDate, mLatitude, mLongitude, MeterReading, b64_img, currentTime, TaskNote, From, To, 0)) {
                                                    if (db.addTrackingDetail(uniqueNumber, employee.getName(), type, Latitude.getText().toString().trim(), Longitude.getText().toString().trim(), currentDate, currentTime, 0)) {
                                                        Attendance attendance = new Attendance(Integer.parseInt(uniqueNumber), Integer.parseInt(vehicleId), Msg);
                                                        SharedPrefCheckIn.getInstance(getApplicationContext()).userCheckIn(attendance);
                                                        Toast.makeText(AttendancePrivateActivity.this, Msg, Toast.LENGTH_SHORT).show();
                                                        pd.dismiss();
                                                        Intent intent = new Intent(getApplicationContext(), AttendanceOptionsActivity.class);
                                                        intent.putExtra("successMsg", "CheckedIn");
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    pd.dismiss();
                                    String message = null;
                                    if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                                        Toast.makeText(AttendancePrivateActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
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
                                    map.put("status","CheckOut");
                                    map.put("checkOutNote", "Continue On Next Day");
                                    map.put("employeeName", employee.getName());
                                    map.put("closeLatitude", Latitude.getText().toString().trim());
                                    map.put("closeLongitude", Longitude.getText().toString().trim());
                                    map.put("closeReading", ownMeterReading.getEditText().getText().toString().trim());
                                    map.put("closeImage", b64_img);
                                    map.put("closeTime", currentTime);
                                    map.put("prevDate", intentPrevDate);
                                    return map;
                                }
                            };

                            RequestQueue rQeue = Volley.newRequestQueue(AttendancePrivateActivity.this);
                            rQeue.add(stringRequest);

                        }
                    }

                }

                if (Msg.equalsIgnoreCase("CheckOut")) {
                    if (TextUtils.isEmpty(From)) {
                        Toast.makeText(AttendancePrivateActivity.this, "Enter From Location", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(To)) {
                        Toast.makeText(AttendancePrivateActivity.this, "Enter To Location", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(TaskNote)) {
                        Toast.makeText(AttendancePrivateActivity.this, "Enter Today's Report", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(MeterReading)) {
                        Toast.makeText(AttendancePrivateActivity.this, "Enter Close Reading", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(b64_img)) {
                        Toast.makeText(AttendancePrivateActivity.this, "Capture Reading Before Proceeding!", Toast.LENGTH_SHORT).show();
                    } else {

                        submitButton.setEnabled(false);
                        submitButton.setClickable(false);
                        type = "3";
                        mService.removeLocationUpdates();
                        pd.show();
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/CheckOut.php",
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        pd.dismiss();
                                        if (db.addTrackingDetail(uniqueNumber, employee.getName(), "3", mLatitude, mLongitude, currentDate, currentTime, 0)) {
                                            SharedPrefCheckIn.getInstance(getApplicationContext()).checkout();
                                            Toast.makeText(AttendancePrivateActivity.this, Msg, Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), AttendanceOptionsActivity.class);
                                            intent.putExtra("successMsg", "CheckedOut");
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                pd.dismiss();
                                String message = null;
                                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                                    Toast.makeText(AttendancePrivateActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
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
                                map.put("status",Msg);
                                map.put("fromLocation",From);
                                map.put("toLocation",To);
                                map.put("checkOutNote", TaskNote);
                                map.put("employeeName", employee.getName());
                                map.put("closeLatitude", mLatitude);
                                map.put("closeLongitude", mLongitude);
                                map.put("closeReading", MeterReading);
                                map.put("closeImage", b64_img);
                                map.put("closeTime", currentTime);
                                return map;
                            }
                        };

                        RequestQueue rQeue = Volley.newRequestQueue(AttendancePrivateActivity.this);
                        rQeue.add(stringRequest);
                    }
                }
            }
        });

    }

    private void getVehicleDetails(String uniqueNumber, String intentVId) {
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
                                    int id = object.getInt("id");
                                    String type = object.getString("type");
                                    ownSelectedVehicle.setText(type);
                                    vehicleId = String.valueOf(object.getInt("id"));
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
                    Toast.makeText(AttendancePrivateActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
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

        RequestQueue rQeue= Volley.newRequestQueue(AttendancePrivateActivity.this);
        rQeue.add(request);

    }



    public void selectVehicle(View view) {
        Dialog dialog = new Dialog(AttendancePrivateActivity.this);
        dialog.setContentView(R.layout.activity_vehicle);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        Cursor cursor = vehicleDbHelper.getSyncVehicle();
        vehicleListView = dialog.findViewById(R.id.vehicleList);
        vehicleListView.setEmptyView(dialog.findViewById(R.id.no_vehicle));
        adapter = new SimpleCursorAdapter(AttendancePrivateActivity.this, R.layout.layout_offline_vehicle,cursor,fromStrings,toInts,0);
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
                Log.i("Selected Vehicle Id",vid);
                ownSelectedVehicle.setText(type);
                selectVehicleButton.setText("Change Vehicle");
                ownSelectedVehicle.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });
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
                    Toast.makeText(AttendancePrivateActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
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
                    ownMeterImage.setImageBitmap(bMapRotate);
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
    private boolean checkAndRequestPermissions() {
        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
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

    private void buildAlertMessageNoGps() {
        MyCurrentLocation();
        LocationRequest request = new LocationRequest()
                .setFastestInterval(500)
                .setInterval(1500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder().addLocationRequest(request);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(AttendancePrivateActivity.this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode()){
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(AttendancePrivateActivity.this,REQUEST_GPS_CODE);
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

    private void MyCurrentLocation() {
        Log.i("MyCurrentLocation"," Called");
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(Task<Location> task) {
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
            ActivityCompat.requestPermissions(AttendancePrivateActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("TAG", "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("TAG", "sms & location services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("TAG", "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                                ||ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    @Override
    protected void onStart() {
        super.onStart();
        LocationAction();
    }

    //Location Services
    private void LocationAction() {
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        setButtonsState(Utils.requestingLocationUpdates(this));
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }
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
            submitButton.setEnabled(true);
        }
    }

}