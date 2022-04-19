package com.kiratcoding.asm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
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
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefCheckIn;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

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

public class AttendanceWFHActivity extends AppCompatActivity {

    TextView Latitude,Longitude, pTopTextView;
    TextInputLayout pTaskDetails;
    ImageView pTicketImage;
    Button submit;

    private Employee employee;

    private Bitmap bMap;
    private ProgressDialog pd;
    private LocationManager manager;    //for GPS Location
    private LocationSettingsRequest.Builder builder; //for GPS Location Request Build
    private FusedLocationProviderClient fusedLocationProviderClient;    //For Current Location

    private String Msg="",uniqueNumber="", currentDate="", currentTime="", vehicleId="123456";
    private String TaskNote="", b64_img="", mCurrentPhotoPath="",  mLatitude="", mLongitude="";

    // Request Codes
    private final int REQUEST_GPS_CODE = 800;   // GPS Request Code
    static final int REQUEST_IMAGE_CAPTURE = 1; // Camera Image Code
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_wfh);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);    //For Location

        Msg = getIntent().getStringExtra("Msg");

        employee = SharedPrefLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());

        pTopTextView = findViewById(R.id.whf_top_text);
        pTaskDetails = findViewById(R.id.whf_report);
        pTicketImage = findViewById(R.id.whf_img);
        submit = findViewById(R.id.wfh_button);
        Latitude = findViewById(R.id.tv_latitude);
        Longitude = findViewById(R.id.tv_longitude);

        Latitude.setVisibility(View.GONE);
        Longitude.setVisibility(View.GONE);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());
        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");
        currentTime = time.format(calendar.getTime());

        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait.. ");
        pd.setMessage("this might take a few minutes");
        pd.setCancelable(false);

        pTicketImage.setOnClickListener(new View.OnClickListener() {
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
        if (Msg.equalsIgnoreCase("CheckIn")) {
            submit.setText("Check In");
            pTopTextView.setText("Check-In For WFH");
            pTaskDetails.setHint("What's your plan today");
        }
        else if (Msg.equalsIgnoreCase("CheckOut")) {
            submit.setText("Check Out");
            pTopTextView.setText("Check-Out For WFH");
            pTaskDetails.setHint("Today's Report");
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TaskNote = pTaskDetails.getEditText().getText().toString().trim();
                mLatitude = Latitude.getText().toString().trim();
                mLongitude = Longitude.getText().toString().trim();


                if (Msg.equalsIgnoreCase("CheckIn")) {
                    if (TextUtils.isEmpty(TaskNote)){
                        Toast.makeText(AttendanceWFHActivity.this, "Enter Today's Task", Toast.LENGTH_SHORT).show();
                    }else if (TextUtils.isEmpty(b64_img)){
                        Toast.makeText(AttendanceWFHActivity.this, "Capture Image Before Proceeding!", Toast.LENGTH_SHORT).show();
                    }else {
                        pd.show();
                        submit.setEnabled(false);
                        submit.setClickable(false);
                        Log.i("ImagePath",b64_img);
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/CheckIn.php",
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Attendance attendance = new Attendance(Integer.parseInt(uniqueNumber), Integer.parseInt(vehicleId), Msg);
                                        SharedPrefCheckIn.getInstance(getApplicationContext()).userCheckIn(attendance);
                                        pd.dismiss();
                                        Toast.makeText(AttendanceWFHActivity.this, Msg, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), AttendanceOptionsActivity.class);
                                        intent.putExtra("successMsg", "CheckedIn");
                                        startActivity(intent);
                                        finish();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                pd.dismiss();
                                String message = null;
                                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                                    Toast.makeText(AttendanceWFHActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                                } else if (volleyError instanceof ServerError) {
                                    message = "The server could not be found. Please try again later";
                                    Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> map = new HashMap<>();

                                map.put("status",Msg);
                                map.put("vehicleId", vehicleId);
                                map.put("uniquenumber", uniqueNumber);
                                map.put("employeeName", employee.getName());
                                map.put("currentDate", currentDate);
                                map.put("startLatitude", mLongitude);
                                map.put("startLongitude", mLongitude);
                                map.put("startReading", "0");
                                map.put("startImage", b64_img);
                                map.put("startTime", currentTime);
                                map.put("note", TaskNote);
                                map.put("fromLocation", "Work From Home");
                                map.put("toLocation", "Work From Home");
                                map.put("closeLatitude", "");
                                map.put("closeLongitude", "");
                                map.put("closeReading", "");
                                map.put("closeImage", "");
                                map.put("closeTime", "");
                                map.put("timestamp", "");
                                return map;
                            }
                        };

                        RequestQueue rQeue = Volley.newRequestQueue(AttendanceWFHActivity.this);
                        rQeue.add(stringRequest);

                    }

                }
                else if (Msg.equalsIgnoreCase("CheckOut")) {
                    if (TextUtils.isEmpty(TaskNote)){
                        Toast.makeText(AttendanceWFHActivity.this, "Enter Today's Report", Toast.LENGTH_SHORT).show();
                    }else if (TextUtils.isEmpty(b64_img)){
                        Toast.makeText(AttendanceWFHActivity.this, "Click Ticket Image", Toast.LENGTH_SHORT).show();
                    }else {
                        pd.show();
                        submit.setEnabled(false);
                        submit.setClickable(false);
                        Log.i("ImagePath",b64_img);
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/PublicTransportCheckout.php",
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.i("Response", response);
                                        pd.dismiss();
                                        SharedPrefCheckIn.getInstance(getApplicationContext()).checkout();
                                        Toast.makeText(AttendanceWFHActivity.this, Msg, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), AttendanceOptionsActivity.class);
                                        intent.putExtra("successMsg", "CheckedOut");
                                        startActivity(intent);
                                        finish();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                pd.dismiss();
                                String message = null;
                                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                                    Toast.makeText(AttendanceWFHActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
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
                                map.put("fromLocation","Work From Home");
                                map.put("toLocation","Work From Home");
                                map.put("checkOutNote", TaskNote);
                                map.put("employeeName", employee.getName());
                                map.put("closeLatitude", mLatitude);
                                map.put("closeLongitude", mLongitude);
                                map.put("closeReading", "0");
                                map.put("closeImage", b64_img);
                                map.put("closeTime", currentTime);
                                return map;
                            }
                        };

                        RequestQueue rQeue = Volley.newRequestQueue(AttendanceWFHActivity.this);
                        rQeue.add(stringRequest);

                    }
                }
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
                    Toast.makeText(AttendanceWFHActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
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
                    pTicketImage.setImageBitmap(bMapRotate);
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
    private void buildAlertMessageNoGps() {
        MyCurrentLocation();
        LocationRequest request = new LocationRequest()
                .setFastestInterval(500)
                .setInterval(1500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder().addLocationRequest(request);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(AttendanceWFHActivity.this).checkLocationSettings(builder.build());
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
                                resolvableApiException.startResolutionForResult(AttendanceWFHActivity.this,REQUEST_GPS_CODE);
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
            ActivityCompat.requestPermissions(AttendanceWFHActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
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

}