package com.kiratcoding.asm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.kiratcoding.asm.HelperClass.HttpsTrustManager;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.OfflineSync.NetworkCheckerClass.VolleySingleton;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefCheckIn;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefPayrollLogin;

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

public class PayrollAttendanceActivity extends AppCompatActivity {

    private String attendanceStatus="",currentDate="", currentTime="", uniqueNumber="", employeeName="" ,mCurrentPhotoPath="", b64_img="";
    TextView topText, dateTextView, idTextView, locationTextView,latitudeTextView,longitudeTextView;
    Button button;
    private FusedLocationProviderClient fusedLocationProviderClient;    //For Current Location
    Employee employee;
    ImageView payrollImageView;
    TextInputLayout payrollTaskText;

    Bitmap bMap;
    // Request Codes
    private final int REQUEST_GPS_CODE = 800;   // GPS Request Code
    static final int REQUEST_IMAGE_CAPTURE = 1; // Camera Image Code
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 10;

    String payrollTask="", latitude="", longitude="";

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payroll_attendance);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);    //For Location

        HttpsTrustManager.allowAllSSL();

        attendanceStatus = getIntent().getStringExtra("status");

        employee = SharedPrefPayrollLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());
        employeeName = employee.getName();

        pd = new ProgressDialog(PayrollAttendanceActivity.this);
        pd.setMessage("Please Wait.. ");
        pd.setMessage("this might take a few minutes");
        pd.setCancelable(false);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());
        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");
        currentTime = time.format(calendar.getTime());

        topText = findViewById(R.id.text);
        dateTextView = findViewById(R.id.payroll_date);
        idTextView = findViewById(R.id.textEmpId);
        locationTextView = findViewById(R.id.textLocation);
        button = findViewById(R.id.btnPayroll);
        latitudeTextView = findViewById(R.id.tv_latitude);
        longitudeTextView = findViewById(R.id.tv_longitude);
        payrollImageView = findViewById(R.id.payroll_img);
        payrollTaskText = findViewById(R.id.et_payroll_task);

        topText.setText(attendanceStatus+" Payroll");
        idTextView.setText("Emp-"+uniqueNumber);
        button.setText(attendanceStatus);
        dateTextView.setText(currentDate);

        MyCurrentLocation();

        payrollImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAndRequestPermissions()) {
                    MyCurrentLocation();
                    CaptureImage();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payrollTask = payrollTaskText.getEditText().getText().toString();
                latitude = latitudeTextView.getText().toString();
                longitude = longitudeTextView.getText().toString();
                if (payrollTask.equalsIgnoreCase("")){
                    Toast.makeText(PayrollAttendanceActivity.this, "Enter "+ attendanceStatus + " Task", Toast.LENGTH_SHORT).show();
                }else if (b64_img.equalsIgnoreCase("")){
                    Toast.makeText(PayrollAttendanceActivity.this, "Click selfie ", Toast.LENGTH_SHORT).show();
                }else {
                    if (attendanceStatus.equalsIgnoreCase("CheckIn")) {
                    pd.show();
                    StringRequest sr = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/Employees/android/AttendancePayroll.php",
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.i("attendanceResponce", response);
                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        if (!obj.getBoolean("error")) {
                                            finish();
                                            pd.dismiss();
                                            Toast.makeText(PayrollAttendanceActivity.this, ""+obj.getString("message"), Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(PayrollAttendanceActivity.this, ""+obj.getString("message"), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            String message = null;
                            if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                                message = "No Internet Connection";
                                Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                            } else if (volleyError instanceof ServerError) {
                                message = "The server could not be found. Please try again later";
                                Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                            }
                            pd.dismiss();
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String,String> map = new HashMap<>();
                            map.put("status", attendanceStatus);
                            map.put("uniquenumber", uniqueNumber);
                            map.put("employeeName", employeeName);
                            map.put("checkInNote", payrollTask);
                            map.put("currentDate", currentDate);
                            map.put("startLatitude", latitude);
                            map.put("startLongitude", longitude);
                            map.put("startImage", b64_img);
                            map.put("startTime", currentTime);
                            map.put("closeLatitude", "");
                            map.put("closeLongitude", "");
                            map.put("closeImage", "");
                            map.put("closeTime", "");
                            map.put("checkOutNote", "");
                            return map;
                        }
                    };

                    RequestQueue rQeue= Volley.newRequestQueue(PayrollAttendanceActivity.this);
                    rQeue.add(sr);

                }else if (attendanceStatus.equalsIgnoreCase("CheckOut")) {
                        pd.show();
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/Employees/android/AttendancePayroll.php",
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        pd.dismiss();
                                        Toast.makeText(PayrollAttendanceActivity.this, response, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                pd.dismiss();
                                String message = null;
                                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                                    if (checkNetworkConnectionStatus()) {
                                    }
                                } else if (volleyError instanceof ServerError) {
                                    message = "The server could not be found. Please try again later";
                                    Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> map = new HashMap<>();
                                map.put("uniquenumber", uniqueNumber);
                                map.put("status", attendanceStatus);
                                map.put("checkOutNote", payrollTask);
                                map.put("employeeName", employeeName);
                                map.put("closeLatitude", latitude);
                                map.put("closeLongitude", longitude);
                                map.put("closeImage", b64_img);
                                map.put("closeTime", currentTime);
                                return map;
                            }
                        };

                        RequestQueue rQeue = Volley.newRequestQueue(PayrollAttendanceActivity.this);
                        rQeue.add(stringRequest);

                    }
                }
            }
        });

    }


    //Check Requred Permission
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
                                    findViewById(R.id.layout_payroll_attendance),
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
                                    findViewById(R.id.layout_payroll_attendance),
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
                            latitudeTextView.setText(""+addresses.get(0).getLatitude());
                            longitudeTextView.setText(""+addresses.get(0).getLongitude());
                            locationTextView.setText(""+addresses.get(0).getAddressLine(0));
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
            ActivityCompat.requestPermissions(PayrollAttendanceActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
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
                    Toast.makeText(PayrollAttendanceActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
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
                    payrollImageView.setImageBitmap(bMapRotate);
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


    private boolean checkNetworkConnectionStatus() {
        final Dialog dialog = new Dialog(PayrollAttendanceActivity.this);
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
}