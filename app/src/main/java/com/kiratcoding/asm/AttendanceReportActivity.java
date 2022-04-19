package com.kiratcoding.asm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.kiratcoding.asm.AdapterClass.AttendanceAdapter;
import com.kiratcoding.asm.ModelsClass.Attendance;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AttendanceReportActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    ListView attendanceListView;
    public static ArrayList<Attendance> attendanceArrayList = new ArrayList<>();
    AttendanceAdapter attendanceAdapter;
    Attendance attendance;

    TextView no_recordTextView, gotoAttendanceTextView;

    ConstraintLayout noRecordLayout;
    Button btnSelectDate;
    TextView tvDate,tvEmpId,tvEmpName,tvDistance;

    String selectedDate,currentDate,uniqueNumber;
    Employee employee;
    ProgressDialog pd;
    Calendar calendar;

    int id,vehicleId;
    float startReading, closeReading;
    String status ="",note ="", startTime ="", closeTime ="", timestamp ="", vehicleType ="";
    String fromLocation ="",toLocation ="",amount ="",distance ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);
        Toolbar toolbar = findViewById(R.id.attendanceReport_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Attendance Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait.. ");
        pd.setCanceledOnTouchOutside(false);

        calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());

        employee = SharedPrefLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());

        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvDate=findViewById(R.id.tv_date_);
        tvEmpId=findViewById(R.id.tv_emp_id);
        tvEmpName=findViewById(R.id.tv_emp_name);
        attendanceListView  = findViewById(R.id.attendance_ListView);
        noRecordLayout = findViewById(R.id.no_record_layout);
        no_recordTextView = findViewById(R.id.textView27);
        gotoAttendanceTextView = findViewById(R.id.goto_attendance);

        noRecordLayout.setVisibility(View.GONE);
        attendanceListView.setVisibility(View.GONE);
        gotoAttendanceTextView.setVisibility(View.GONE);

        gotoAttendanceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AttendanceOptionsActivity.class);
                intent.putExtra("successMsg", "attendance");
                startActivity(intent);
            }
        });

        tvEmpId.setText("Employee ID: EMP-"+uniqueNumber);
        tvEmpName.setText("Name: "+employee.getName());
        tvDate.setText(currentDate);

        retriveData(currentDate);
        getPublicTransport(currentDate);

        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        attendanceAdapter = new AttendanceAdapter(this, attendanceArrayList);
        attendanceListView.setAdapter(attendanceAdapter);

        attendanceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ID = String.valueOf(attendanceArrayList.get(position).getId());
                String Status = String.valueOf(attendanceArrayList.get(position).getStatus());
                String VType = String.valueOf(attendanceArrayList.get(position).getVehicleName());
                String SNote = String.valueOf(attendanceArrayList.get(position).getNote());
                String FromLoc = String.valueOf(attendanceArrayList.get(position).getFromLocation());
                String ToLoc = String.valueOf(attendanceArrayList.get(position).getToLocation());

                if(VType.equalsIgnoreCase("Public Transport")) {
                    Toast.makeText(AttendanceReportActivity.this, "You Can Not Edit Public Transport Details", Toast.LENGTH_LONG).show();
                }else if (Status.equalsIgnoreCase("CheckOut")){
                    Toast.makeText(AttendanceReportActivity.this, "Status: Checked-out \nNow You Can Not Edit Any Details", Toast.LENGTH_LONG).show();
                }else {
                    final Dialog dialog = new Dialog(AttendanceReportActivity.this);
                    dialog.setContentView(R.layout.layout_edit_attendance);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(true);
                    dialog.show();

                    TextView status = dialog.findViewById(R.id.et_edit_status);
                    EditText vtype = dialog.findViewById(R.id.et_edit_vtype);
                    EditText start_note = dialog.findViewById(R.id.et_edit_start_note);
                    EditText from = dialog.findViewById(R.id.et_edit_fromLocation);
                    EditText to = dialog.findViewById(R.id.et_edit_toLocation);
                    Button btn = dialog.findViewById(R.id.attendanceUpdateBtn);

                    Toast.makeText(AttendanceReportActivity.this, ""+Status, Toast.LENGTH_SHORT).show();
                    status.setText(Status);
                    vtype.setText(VType);
                    start_note.setText(SNote);
                    from.setText(FromLoc);
                    to.setText(ToLoc);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Status.equalsIgnoreCase("CheckIn")) {
                                pd.show();
                                StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/UpdateAttendance.php",
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                Log.i("UpdateAttendance", response);
                                                if (response.equalsIgnoreCase("Updated")) {
                                                    Toast.makeText(AttendanceReportActivity.this, "Record Updated", Toast.LENGTH_SHORT).show();
                                                    pd.dismiss();
                                                    dialog.dismiss();
                                                    refresh();
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {
                                        String message = null;
                                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                                            message = "No Internet Connection";
                                        } else if (volleyError instanceof ServerError) {
                                            message = "The server could not be found. Please try again later";
                                        }
                                        pd.dismiss();
                                        Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();
                                    }
                                }) {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        HashMap<String, String> orderMap = new HashMap<>();
                                        orderMap.put("uniquenumber", uniqueNumber);
                                        orderMap.put("note", start_note.getText().toString().trim());
                                        orderMap.put("fromLocation", from.getText().toString().trim());
                                        orderMap.put("toLocation", to.getText().toString().trim());
                                        return orderMap;

                                    }
                                };
                                RequestQueue rQeue;
                                rQeue = Volley.newRequestQueue(AttendanceReportActivity.this);
                                rQeue.add(request);
                            } else {
                                Toast.makeText(AttendanceReportActivity.this, "Status: Checked-out \nNow You Can Not Edit Any Details", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }


            }
        });
    }

    private void refresh() {
        Intent intent = new Intent(getApplicationContext(),AttendanceReportActivity.class);
        startActivity(intent);
        Animatoo.animateFade(this);
        finish();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(AttendanceReportActivity.this,this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        tvDate.setText(""+dateFormat.format(calendar.getTime()));
        selectedDate = dateFormat.format(calendar.getTime());
        retriveData(selectedDate);
        getPublicTransport(selectedDate);
    }

    private void retriveData(String date) {
        attendanceArrayList.clear();
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/AttendanceDetails.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.i("AttendancResponce",response);
                            if (jsonObject.optString("status").equals("true")){
                                attendanceListView.setVisibility(View.VISIBLE);
                                noRecordLayout.setVisibility(View.GONE);
                                JSONArray jsonArray = jsonObject.getJSONArray("Attendance_Details");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    id = object.getInt("id");
                                    vehicleId = object.getInt("vehicleId");
                                    status = object.getString("status");
                                    note = object.getString("note");
                                    startTime = object.getString("startTime");
                                    closeTime = object.getString("closeTime");
                                    fromLocation = object.getString("fromLocation");
                                    toLocation = object.getString("toLocation");
                                    startReading = object.getLong("startReading");
                                    closeReading = object.getLong("closeReading");
                                    float distance = closeReading - startReading;
                                    getVehicleType(uniqueNumber, id, vehicleId, startReading, closeReading, status, note, startTime, closeTime,String.valueOf(distance),fromLocation,toLocation);
                                }
                            }else if (jsonObject.optString("status").equals("false")){
                                pd.dismiss();
                                attendanceListView.setVisibility(View.GONE);
                                noRecordLayout.setVisibility(View.VISIBLE);
                                if(tvDate.getText().toString().equalsIgnoreCase(currentDate)){
                                    gotoAttendanceTextView.setVisibility(View.VISIBLE);
                                }else{
                                    gotoAttendanceTextView.setVisibility(View.GONE);
                                }
                                Log.i("status","No Record Found..");
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                            pd.dismiss();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String message = null;
                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                    message = "No Internet Connection";
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found. Please try again later";
                }
                pd.dismiss();
                Toast.makeText(AttendanceReportActivity.this, ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("uniquenumber", uniqueNumber);
                orderMap.put("currentDate", date);
                return orderMap;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    private void getVehicleType(String uniqueNumber, int aid, int vehicleId, float startReading, float closeReading, String status, String note, String startTime, String closeTime, String distance, String fromLocation, String toLocation) {
        Log.i("vehicleId",""+vehicleId);
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/retrieveVehicleType.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.i("VehicleResponce", response);
                            if (jsonObject.optString("status").equals("true")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("Vehicle_Type");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    vehicleType = object.getString("type");
                                    Log.i("vehicleType", vehicleType);
                                    attendance = new Attendance(id,vehicleType,startReading,closeReading, fromLocation, toLocation, status, note, startTime, closeTime,distance,amount);
                                    attendanceArrayList.add(attendance);
                                    attendanceAdapter.notifyDataSetChanged();
                                    pd.dismiss();
                                }
                            } else if (jsonObject.optString("status").equals("false")) {
                                pd.dismiss();
                            }
                        } catch (JSONException e) {
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
                map.put("id", String.valueOf(vehicleId));
                return map;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    private void getPublicTransport(String date) {
        pd.show();
        Log.i("vehicleId:",""+vehicleId);
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/AttendancePublicT.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("AttendancePublicT", response);

                        try {
                            JSONObject obj = new JSONObject(response);
                            if(obj.optString("status").equals("true")) {
                                JSONArray dataArray = obj.getJSONArray("Attendance_Details");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject dataobj = dataArray.getJSONObject(i);
                                    id = dataobj.getInt("id");
                                    vehicleType = "Public Transport";
                                    fromLocation = dataobj.getString("fromLocation");
                                    toLocation = dataobj.getString("toLocation");
                                    amount = dataobj.getString("amount");
                                    status = dataobj.getString("status");
                                    note = dataobj.getString("note");
                                    startTime = dataobj.getString("startTime");
                                    closeTime = dataobj.getString("closeTime");
                                    Log.i("vehicleType", vehicleType);
                                    attendance = new Attendance(id,vehicleType,startReading,closeReading, fromLocation, toLocation, status, note, startTime, closeTime,distance,amount);
                                    attendanceArrayList.add(attendance);
                                    attendanceAdapter.notifyDataSetChanged();
                                    pd.dismiss();
                                }
                            } else if (obj.optString("status").equals("false")) {
                                pd.dismiss();
                                if(tvDate.getText().toString().equalsIgnoreCase(currentDate)){
                                    gotoAttendanceTextView.setVisibility(View.VISIBLE);
                                }else{
                                    gotoAttendanceTextView.setVisibility(View.GONE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
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
                Map<String, String> map = new HashMap<>();
                map.put("uniquenumber", uniqueNumber);
                map.put("currentDate", date);
                return map;
            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(AttendanceReportActivity.this);
        rQeue.add(request);

    }

    private boolean checkNetworkConnectionStatus() {
        final Dialog dialog = new Dialog(AttendanceReportActivity.this);
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
                dialog.dismiss();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
