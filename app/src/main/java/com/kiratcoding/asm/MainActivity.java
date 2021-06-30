package com.kiratcoding.asm;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.kiratcoding.asm.HelperClass.HttpsTrustManager;
import com.kiratcoding.asm.ModelsClass.Attendance;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.OfflineSync.DbHelperClass.VehicleDbHelper;
import com.kiratcoding.asm.OfflineSync.NetworkCheckerClass.TrackingNetworkStatus;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private TextView navUsername, navEmailid;
    private Employee employee;
    private ConstraintLayout notifCount;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;    //For Current Location
    
    private String dt,currentDate,currentTime,userId;
    private String status="", appVersion="", version;
    private Attendance attendance;

    private ProgressDialog pd;
    private VehicleDbHelper vehicleDbHelper;
    private BroadcastReceiver trackBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);    //For Location

        HttpsTrustManager.allowAllSSL();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());
        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss");
        currentTime = time.format(calendar.getTime());

        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait.. ");
        pd.setCanceledOnTouchOutside(false);

        version = getResources().getString(R.string.app_version);

        vehicleDbHelper = new VehicleDbHelper(this);

        employee = SharedPrefLogin.getInstance(this).getUser();
        userId = String.valueOf(employee.getUniquenumber());

        if (!SharedPrefLogin.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, AuthActivity.class));
        }else{
            if(checkAndRequestPermissions()) {}
            retrieveVehicleFromServer();
            checkAppVersion();
            uploadEmployeeAppVersion(version);
        }


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://arunodyafeeds.com/");
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_about_us, R.id.nav_help, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View hView = navigationView.getHeaderView(0);
        navUsername = (TextView) hView.findViewById(R.id.tv_username);
        navEmailid = (TextView) hView.findViewById(R.id.tv_emailid);

//        setting the values to the textviews
        navUsername.setText(String.valueOf(employee.getName()));
        navEmailid.setText(String.valueOf(employee.getEmail()));

        MyCurrentLocation();

        registerReceiver(new TrackingNetworkStatus(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(trackBroadcastReceiver, new IntentFilter(TrackingNetworkStatus.TRACK_DATA_BROADCAST));
    }

    private void retrieveVehicleFromServer() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/retrieveVehicleDetails.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("VehicleResponse",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("Vehicle_Name");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int id = object.getInt("id");
                                    String type = object.getString("type");
                                    if (vehicleDbHelper.columnExists(String.valueOf(id))){
                                        Log.i("Vehicle exist","Yes");
                                    }else{
                                        Log.i("Vehicle exist","No");
                                        vehicleDbHelper.insertVehicle(String.valueOf(id),type);
                                    }
                                    progressDialog.dismiss();

                                }
                            }else if (jsonObject.optString("status").equals("false")){
                                progressDialog.dismiss();
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                String message = null;
                if (volleyError instanceof NetworkError || volleyError instanceof TimeoutError) {
                    Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found. Please try again later";
                    Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                }
            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                map.put("uniquenumber", String.valueOf(employee.getUniquenumber()));
                return map;
            }

            @Override
            protected Map<String, String> getPostParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                map.put("uniquenumber", String.valueOf(employee.getUniquenumber()));
                return map;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
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
                                    findViewById(R.id.layout_main),
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
                                    findViewById(R.id.layout_main),
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
    
    private void checkAppVersion() {
        pd.show();
        StringRequest sr = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/checkAppVersion.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("AppVersion", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("AppVersion");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    appVersion = object.getString("version");
                                    Log.i("Your version: ", version);
                                    Log.i("UpdateVersion: ", appVersion);
                                    if (!version.equalsIgnoreCase(appVersion)){
                                        Log.i("isValid: ", "False");
                                        final Dialog dialog = new Dialog(MainActivity.this);
                                        dialog.setContentView(R.layout.layout_app_version);
                                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        dialog.setCancelable(false);
                                        dialog.show();

                                        TextView server = dialog.findViewById(R.id.server_version);
                                        TextView your = dialog.findViewById(R.id.ur_version);

                                        server.setText("Server Version  : "+appVersion);
                                        your.setText("Your Version     : "+version);
                                        Button update = dialog.findViewById(R.id.updateVersionBtn);
                                        Button cancel = dialog.findViewById(R.id.cancelVersionBtn);
                                        update.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Uri uri = Uri.parse("https://arunodyafeeds.com/sales/download/");
                                                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                                startActivity(intent);
                                                dialog.dismiss();
                                            }
                                        });
                                        cancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                dialog.dismiss();
                                            }
                                        });
                                        pd.dismiss();

                                    }else {
                                        Log.i("isValid: ", "True");
                                        updateEmployeeAppVersion(version);
                                        pd.dismiss();
                                    }
                                }
                            }else if (jsonObject.optString("status").equals("false")){
                                pd.dismiss();
                            }
                        } catch (JSONException e) {
                            pd.dismiss();
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
        });

        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(MainActivity.this);
        rQeue.add(sr);

    }

    private void updateEmployeeAppVersion(String version) {
        pd.show();
        StringRequest sr = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/updateEmployeeAppVersion.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pd.dismiss();
                        Log.i("EmployeeAppVUpdate", response);
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
                map.put("uniquenumber",String.valueOf(employee.getUniquenumber()));
                map.put("employeeName",String.valueOf(employee.getName()));
                map.put("version",version);
                return map;
            }
        };

        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(MainActivity.this);
        rQeue.add(sr);

    }

    private void uploadEmployeeAppVersion(String version) {
        pd.show();
        StringRequest sr = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/uploadEmployeeAppVersion.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pd.dismiss();
                        Log.i("EmployeeAppV", response);
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
                map.put("uniquenumber",String.valueOf(employee.getUniquenumber()));
                map.put("employeeName",String.valueOf(employee.getName()));
                map.put("version",version);
                return map;
            }
        };

        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(MainActivity.this);
        rQeue.add(sr);

    }

    public void Logout(MenuItem item){
        item.setEnabled(true);
        item.getIcon().setAlpha(255);
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Are you sure you want to Logout?")
                .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPrefLogin.getInstance(getApplicationContext()).logout();
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("LOGOUT", true);
                startActivity(intent);
                finish();
            }
        }).setNegativeButton("No",null).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.notify_menu);
        MenuItemCompat.setActionView(item, R.layout.badge_layout);
        notifCount = (ConstraintLayout)   MenuItemCompat.getActionView(item);

        notifCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),NotificationActivity.class);
                intent.putExtra("cartCount",String.valueOf("10"));
                startActivity(intent);
            }
        });
        return true;
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    protected void onStart() {
        super.onStart();
        MyCurrentLocation();
        if (!checkNetwork()){
        }
    }

    private void MyCurrentLocation() {
        Log.i("MyCurrentLocation"," Called");
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                @Override
                public void onComplete(@NonNull Task<android.location.Location> task) {
                    android.location.Location location = task.getResult();
                    if (location != null){
                        try {
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());  //Initialize geoCode
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
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
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
    }

    private boolean checkNetwork() {
        boolean wifiConnected;
        boolean mobileConnected;
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()){ //connected with either mobile or wifi
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if (wifiConnected){ //wifi connected
                return true;
            }
            else if (mobileConnected){ //mobile data connected
                return true;
            }
        } else { //no internet connection
            return false;
        }
        return false;
    }
}
