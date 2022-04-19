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
import com.kiratcoding.asm.AdapterClass.LeavesAdapter;
import com.kiratcoding.asm.ModelsClass.Leaves;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LeaveStatusActivity extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener{

    String currentDate,uniqueNumber;
    Employee employee;
    private ProgressDialog pd;
    Calendar calendar;
    Button leaveBtn;
    Button btnApply,btnCancel;
    EditText etName,etContact,etStartDate,etEndDate,etreasons;
    ImageView ivStartDate,ivEndDate;

    TextView tvDate,tvEmpId,tvEmpName;
    ConstraintLayout noRecordLayout;
    ListView attendanceListView;

    private String Name="",Contact="",Start="",End="",Reasons="";

    private int flag;

    public static ArrayList<Leaves> attendanceArrayList = new ArrayList<>();
    LeavesAdapter attendanceAdapter;
    Leaves attendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_status);
        Toolbar toolbar = findViewById(R.id.leave_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Leaves Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait.. ");
        pd.setCanceledOnTouchOutside(false);

        calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());

        employee = SharedPrefLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());

        leaveBtn = findViewById(R.id.apply_leave_btn);
        attendanceListView  = findViewById(R.id.leave_ListView);
        noRecordLayout = findViewById(R.id.no_record_layout);
        tvDate=findViewById(R.id.tv_date_);
        tvEmpId=findViewById(R.id.tv_emp_id);
        tvEmpName=findViewById(R.id.tv_emp_name);

        attendanceListView.setVisibility(View.GONE);
        noRecordLayout.setVisibility(View.GONE);

        tvEmpId.setText("Employee ID: EMP-"+uniqueNumber);
        tvEmpName.setText("Name: "+employee.getName());
        tvDate.setText("Date: "+currentDate);

        attendanceAdapter = new LeavesAdapter(this, attendanceArrayList);
        attendanceListView.setAdapter(attendanceAdapter);

        retriveData();

        leaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog leaveDialog = new Dialog(LeaveStatusActivity.this);
                leaveDialog.setContentView(R.layout.layout_leave_form);
                leaveDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                leaveDialog.setCancelable(true);
                leaveDialog.show();

                etName = leaveDialog.findViewById(R.id.leaveName);
                etContact = leaveDialog.findViewById(R.id.leaveContact);
                etStartDate = leaveDialog.findViewById(R.id.leave_start_date);
                etEndDate = leaveDialog.findViewById(R.id.leave_exp_date);
                etreasons = leaveDialog.findViewById(R.id.leaveDetails);
                ivStartDate = leaveDialog.findViewById(R.id.showStartDate);
                ivEndDate = leaveDialog.findViewById(R.id.showEndDate);
                btnApply = leaveDialog.findViewById(R.id.leaveApply);
                btnCancel = leaveDialog.findViewById(R.id.leaveCancel);

                //-------------------------
                etName.setText(employee.getName());
                etName.setFocusable(false);
                etStartDate.setFocusable(false);
                etEndDate.setFocusable(false);


                ivStartDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        flag=0;
                        showDatePicker();
                    }
                });

                ivEndDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        flag=1;
                        showDatePicker();
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        leaveDialog.dismiss();
                    }
                });

                btnApply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Get Text and Store in variable
                        Name = etName.getText().toString().trim();
                        Contact = etContact.getText().toString().trim();
                        Start = etStartDate.getText().toString().trim();
                        End = etEndDate.getText().toString().trim();
                        Reasons = etreasons.getText().toString().trim();

                        String MobilePattern = "[0-9]{10}";
                        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                        if (Contact.isEmpty()) {
                            Toast.makeText(LeaveStatusActivity.this, "Enter Contact", Toast.LENGTH_SHORT).show();
                        } else if (Contact.length() != 10) {
                            Toast.makeText(LeaveStatusActivity.this, "Invalid Contact", Toast.LENGTH_SHORT).show();
                        } else if (Start.isEmpty()) {
                            Toast.makeText(LeaveStatusActivity.this, "Select Start Date", Toast.LENGTH_SHORT).show();
                        } else if (End.isEmpty()) {
                            Toast.makeText(LeaveStatusActivity.this, "Select End Date", Toast.LENGTH_SHORT).show();
                        } else if (Reasons.isEmpty()) {
                            Toast.makeText(LeaveStatusActivity.this, "Enter Reason", Toast.LENGTH_SHORT).show();
                        } else {
                            pd.show();
                            StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/Leaves.php",
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            pd.dismiss();
                                            leaveDialog.dismiss();
                                            Toast.makeText(LeaveStatusActivity.this, "Leave Request Submitted", Toast.LENGTH_SHORT).show();
                                            retriveData();
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
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    HashMap<String, String> leaveMap = new HashMap<>();
                                    leaveMap.put("reason", Reasons);
                                    leaveMap.put("emergencyContact", Contact);
                                    leaveMap.put("fromDate", Start);
                                    leaveMap.put("toDate", End);
                                    leaveMap.put("uniquenumber", uniqueNumber);
                                    leaveMap.put("timestamp", currentDate);
                                    return leaveMap;
                                }
                            };
                            RequestQueue rQeue;
                            rQeue = Volley.newRequestQueue(LeaveStatusActivity.this);
                            rQeue.add(request);
                        }
                    }
                });

            }
        });

    }

    private void retriveData() {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/GetLeavesDetails.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        attendanceArrayList.clear();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.i("AttendancResponce",response);
                            if (jsonObject.optString("status").equals("true")){
                                attendanceListView.setVisibility(View.VISIBLE);
                                noRecordLayout.setVisibility(View.GONE);
                                JSONArray jsonArray = jsonObject.getJSONArray("Leaves_Details");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int id = object.getInt("id");
                                    String reason = object.getString("reason");
                                    String fromDate = object.getString("fromDate");
                                    String toDate = object.getString("toDate");
                                    String timestamp = object.getString("timestamp");
                                    int status = object.getInt("status");
                                    int dayCount = 0;
//                                    List<Integer> list=new ArrayList<>();
//                                    list.add(id);
//                                    Collections.sort(list);

                                    attendance = new Leaves(id, reason, fromDate, toDate, timestamp, status, dayCount);
                                    attendanceArrayList.add(attendance);
                                    Collections.reverse(attendanceArrayList);
                                    attendanceAdapter.notifyDataSetChanged();
                                    pd.dismiss();
                                }
                            }else if (jsonObject.optString("status").equals("false")){
                                pd.dismiss();
                                attendanceListView.setVisibility(View.GONE);
                                noRecordLayout.setVisibility(View.VISIBLE);
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
                Toast.makeText(LeaveStatusActivity.this, ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("uniquenumber", uniqueNumber);
                return orderMap;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(LeaveStatusActivity.this,this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());  //disable past dates
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(calendar.getTime());
        if(flag==0){
            etStartDate.setText(date);
        }
        if(flag==1){
            etEndDate.setText(date);
        }

    }

    private boolean checkNetworkConnectionStatus() {
        final Dialog dialog = new Dialog(LeaveStatusActivity.this);
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