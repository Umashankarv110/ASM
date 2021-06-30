package com.kiratcoding.asm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuItemCompat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.kiratcoding.asm.HelperClass.HttpsTrustManager;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefPayrollLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PayrollMainActivity extends AppCompatActivity {

    String attendanceId="",attendanceStatus="";
    private ProgressDialog pd;
    Employee employee;
    private String currentDate="", currentTime="", uniqueNumber="", employeeName="" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_payroll);
        Toolbar toolbar = findViewById(R.id.payroll_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Payroll Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        HttpsTrustManager.allowAllSSL();

        employee = SharedPrefPayrollLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());
        employeeName = employee.getName();

        pd = new ProgressDialog(PayrollMainActivity.this);
        pd.setMessage("Please Wait.. ");
        pd.setMessage("this might take a few minutes");
        pd.setCancelable(false);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());
        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");
        currentTime = time.format(calendar.getTime());

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkInAttendanceDetails();
    }

    private void checkInAttendanceDetails() {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/Employees/android/CheckInDetails.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Response////  ",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                pd.dismiss();
                                JSONArray jsonArray = jsonObject.getJSONArray("Attendance_Details");
                                JSONObject object;
                                for (int i=0; i<jsonArray.length();i++){
                                    object = jsonArray.getJSONObject(i);
                                    attendanceId = String.valueOf(object.getInt("id"));
                                    attendanceStatus = object.getString("status");
                                    Log.i("CheckedIn",attendanceId+"|"+attendanceStatus);
                                }
                            }else if (jsonObject.optString("status").equals("false")){
                                attendanceStatus="";
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
                    Toast.makeText(PayrollMainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
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

        RequestQueue rQeue= Volley.newRequestQueue(PayrollMainActivity.this);
        rQeue.add(request);

    }


    public void payrollAttendance(View view) {
        final Dialog typeDialog = new Dialog(PayrollMainActivity.this);
        typeDialog.setContentView(R.layout.layout_options);
        typeDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        typeDialog.setTitle("");
        typeDialog.setCancelable(true);
        typeDialog.show();

        TextView t1 = typeDialog.findViewById(R.id.Toptext);
        Button b1 = typeDialog.findViewById(R.id.btnOption1);
        Button b2 = typeDialog.findViewById(R.id.btnOption2);
        Button b3 = typeDialog.findViewById(R.id.btnOption3);

        t1.setText("Attendance Option");
        b1.setText("CheckIn");
        b2.setText("CheckOut");

        b3.setVisibility(View.GONE);
        if (attendanceStatus.equalsIgnoreCase("CheckIn")){
            b1.setEnabled(false);
            b2.setEnabled(true);
        }else if (attendanceStatus.equalsIgnoreCase("")){
            b1.setEnabled(true);
            b2.setEnabled(false);
        }

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PayrollAttendanceActivity.class);
                intent.putExtra("status","CheckIn");
                startActivity(intent);
                typeDialog.dismiss();
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PayrollAttendanceActivity.class);
                intent.putExtra("status","CheckOut");
                startActivity(intent);
                typeDialog.dismiss();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.payroll_menu, menu);
        return true;
    }


    public void Logout(MenuItem item){
        item.setEnabled(true);
        item.getIcon().setAlpha(255);
        new AlertDialog.Builder(PayrollMainActivity.this)
                .setMessage("Are you sure you want to Logout?")
                .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPrefPayrollLogin.getInstance(getApplicationContext()).logout();
                Intent intent = new Intent(PayrollMainActivity.this, AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("LOGOUT", true);
                startActivity(intent);
                finish();
            }
        }).setNegativeButton("No",null).show();
    }
}