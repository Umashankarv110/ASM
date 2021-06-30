package com.kiratcoding.asm;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.kiratcoding.asm.ModelsClass.Attendance;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.ModelsClass.MonthlyAttendance;
import com.kiratcoding.asm.ModelsClass.MonthName;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.naishadhparmar.zcustomcalendar.OnNavigationButtonClickedListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlyCalenderActivity extends AppCompatActivity{

    TextView textDateRange;
    TextView tv1,tv2,tv3,tv4,tv5,tv6,tv7,tv8,tv9,tv10;
    TextView tv11,tv12,tv13,tv14,tv15,tv16,tv17,tv18,tv19,tv20;
    TextView tv21,tv22,tv23,tv24,tv25,tv26,tv27,tv28,tv29,tv30,tv31;
    Spinner spinnerMonth, spinnerYear;

    private int year, month, day;
    private Calendar calendar;

    String firstDate, lastDate;

    ProgressDialog pd;
    int currentDay;
    String currentDate,status="";
    List<MonthlyAttendance> detailsList;
    List<MonthName> monthList;

    private Employee employee;
    private String uniqueNumber;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_calender);
        Toolbar toolbar = findViewById(R.id.monthlyReport_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Monthly Attendance");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        HttpsTrustManager.allowAllSSL();

        employee = SharedPrefLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());

        spinnerMonth = findViewById(R.id.spinMonth);
        spinnerYear = findViewById(R.id.spinYear);
        textDateRange = findViewById(R.id.textDateRange);
        tv1 = findViewById(R.id.tv_1); tv2 = findViewById(R.id.tv_2);
        tv3 = findViewById(R.id.tv_3); tv4 = findViewById(R.id.tv_4);
        tv5 = findViewById(R.id.tv_5); tv6 = findViewById(R.id.tv_6);
        tv7 = findViewById(R.id.tv_7); tv8 = findViewById(R.id.tv_8);
        tv9 = findViewById(R.id.tv_9); tv10 = findViewById(R.id.tv_10);

        tv11 = findViewById(R.id.tv_11); tv12 = findViewById(R.id.tv_12);
        tv13 = findViewById(R.id.tv_13); tv14 = findViewById(R.id.tv_14);
        tv15 = findViewById(R.id.tv_15); tv16 = findViewById(R.id.tv_16);
        tv17 = findViewById(R.id.tv_17); tv18 = findViewById(R.id.tv_18);
        tv19 = findViewById(R.id.tv_19); tv20 = findViewById(R.id.tv_20);


        tv21 = findViewById(R.id.tv_21); tv22 = findViewById(R.id.tv_22);
        tv23 = findViewById(R.id.tv_23); tv24 = findViewById(R.id.tv_24);
        tv25 = findViewById(R.id.tv_25); tv26 = findViewById(R.id.tv_26);
        tv27 = findViewById(R.id.tv_27); tv28 = findViewById(R.id.tv_28);
        tv29 = findViewById(R.id.tv_29); tv30 = findViewById(R.id.tv_30);
        tv31 = findViewById(R.id.tv_31);

        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait.. ");
        pd.setCanceledOnTouchOutside(false);

        calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());

        LocalDate cDate = LocalDate.parse(currentDate);
        currentDay = cDate.getDayOfMonth();

        detailsList = new ArrayList<>();
        monthList = new ArrayList<>();

        MonthName monthName1 = new MonthName(01, "January");
        MonthName monthName2 = new MonthName(02, "February");
        MonthName monthName3 = new MonthName(03, "March");
        MonthName monthName4 = new MonthName(04, "April");
        MonthName monthName5 = new MonthName(05, "May");
        MonthName monthName6 = new MonthName(06, "June");
        MonthName monthName7 = new MonthName(07, "July");
        MonthName monthName8 = new MonthName(8, "August");
        MonthName monthName9 = new MonthName(9, "September");
        MonthName monthName10 = new MonthName(10, "October");
        MonthName monthName11 = new MonthName(11, "November");
        MonthName monthName12 = new MonthName(12, "December");
        monthList.add(monthName1); monthList.add(monthName2);
        monthList.add(monthName3); monthList.add(monthName4);
        monthList.add(monthName5); monthList.add(monthName6);
        monthList.add(monthName7); monthList.add(monthName8);
        monthList.add(monthName9); monthList.add(monthName10);
        monthList.add(monthName11);monthList.add(monthName12);

        ArrayAdapter<MonthName> adapter = new ArrayAdapter<MonthName>(this, android.R.layout.simple_spinner_item, monthList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MonthName item = (MonthName) parent.getSelectedItem();
                month = item.getPosition();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //set Current Month Name and Year
        month = Calendar.getInstance().get(Calendar.MONTH);
        spinnerMonth.setSelection(month);
        spinnerYear.setSelection(1);

        LocalDate lastDayOfMonth = LocalDate.parse(currentDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate firstDayOfMonth = LocalDate.parse(currentDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).with(TemporalAdjusters.firstDayOfMonth());

        textDateRange.setText("Date Range: "+ firstDayOfMonth +" To "+ lastDayOfMonth);
        getMonthlyAttendance(String.valueOf(firstDayOfMonth),String.valueOf(lastDayOfMonth));
    }

    public void GetCalender(View view) throws ParseException {
        tv1.setBackgroundResource(R.drawable.slider_bg);  tv2.setBackgroundResource(R.drawable.slider_bg);
        tv3.setBackgroundResource(R.drawable.slider_bg);  tv4.setBackgroundResource(R.drawable.slider_bg);
        tv5.setBackgroundResource(R.drawable.slider_bg);  tv6.setBackgroundResource(R.drawable.slider_bg);
        tv7.setBackgroundResource(R.drawable.slider_bg);  tv8.setBackgroundResource(R.drawable.slider_bg);
        tv9.setBackgroundResource(R.drawable.slider_bg);  tv10.setBackgroundResource(R.drawable.slider_bg);

        tv11.setBackgroundResource(R.drawable.slider_bg);  tv12.setBackgroundResource(R.drawable.slider_bg);
        tv13.setBackgroundResource(R.drawable.slider_bg);  tv14.setBackgroundResource(R.drawable.slider_bg);
        tv15.setBackgroundResource(R.drawable.slider_bg);  tv16.setBackgroundResource(R.drawable.slider_bg);
        tv17.setBackgroundResource(R.drawable.slider_bg);  tv18.setBackgroundResource(R.drawable.slider_bg);
        tv19.setBackgroundResource(R.drawable.slider_bg);  tv20.setBackgroundResource(R.drawable.slider_bg);

        tv21.setBackgroundResource(R.drawable.slider_bg);  tv22.setBackgroundResource(R.drawable.slider_bg);
        tv23.setBackgroundResource(R.drawable.slider_bg);  tv24.setBackgroundResource(R.drawable.slider_bg);
        tv25.setBackgroundResource(R.drawable.slider_bg);  tv26.setBackgroundResource(R.drawable.slider_bg);
        tv27.setBackgroundResource(R.drawable.slider_bg);  tv28.setBackgroundResource(R.drawable.slider_bg);
        tv29.setBackgroundResource(R.drawable.slider_bg);  tv30.setBackgroundResource(R.drawable.slider_bg);

        tv31.setBackgroundResource(R.drawable.slider_bg);

        String string = spinnerYear.getSelectedItem().toString()+"-"+month+"-"+01; //assuming input
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = sdf .parse(string);
        Calendar c = Calendar.getInstance();
        c.setTime(dt);

        //For First Date
        c.add(Calendar.MONTH, 0);
        firstDate = sdf.format(c.getTime());

        //For Last Date
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DAY_OF_MONTH, -1);
        lastDate = sdf.format(c.getTime());

        textDateRange.setText("Date Range: "+ firstDate +" To "+ lastDate);
        getMonthlyAttendance(firstDate,lastDate);

    }

    private void getMonthlyAttendance(String firstdate, String lastdate) {
        pd.show();
        if (lastdate.endsWith("28")){
            tv29.setVisibility(View.GONE);
            tv30.setVisibility(View.GONE);
            tv31.setVisibility(View.GONE);
        } if (lastdate.endsWith("29")){
            tv30.setVisibility(View.GONE);
            tv31.setVisibility(View.GONE);
        } else if (lastdate.endsWith("30")){
            tv31.setVisibility(View.GONE);
        } else if (lastdate.endsWith("31")){
            tv31.setVisibility(View.VISIBLE);
        }

        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/NewScript/AttendanceMonthlyDetails.php",
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        detailsList.clear();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.i("AttendancResponce",response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("MonthAttendance");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    status = object.getString("status");
                                    String date = object.getString("currentDate");

                                    LocalDate cDate = LocalDate.parse(date);
                                    day = cDate.getDayOfMonth();

                                    MonthlyAttendance details = new MonthlyAttendance(day, status);
                                    detailsList.add(details);
                                    ShowDetailsOnCalander(details);
                                    pd.dismiss();
                                }
                            }else if (jsonObject.optString("status").equals("false")){
                                pd.dismiss();
                                Toast.makeText(MonthlyCalenderActivity.this, "No Record Found..", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MonthlyCalenderActivity.this, ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getPostParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("uniquenumber", uniqueNumber);
                orderMap.put("fromDate", firstdate);
                orderMap.put("toDate", lastdate);
                return orderMap;
            }
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("uniquenumber", uniqueNumber);
                orderMap.put("fromDate", firstdate);
                orderMap.put("toDate", lastdate);
                return orderMap;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    private void ShowDetailsOnCalander(MonthlyAttendance details) {
        int day = details.getDayNumber();
        String status = details.getAttendanceStatus();
        String userData = "Status: " + status + "| Day: " + day +"\n";
        Log.e("dvfvdsfvdsfvs",userData);
        if (day == 1) { tv1.setBackgroundResource(R.drawable.present); }
        if (day == 2) { tv2.setBackgroundResource(R.drawable.present); }
        if (day == 3) { tv3.setBackgroundResource(R.drawable.present); }
        if (day == 4) { tv4.setBackgroundResource(R.drawable.present); }
        if (day == 5) { tv5.setBackgroundResource(R.drawable.present); }
        if (day == 6) { tv6.setBackgroundResource(R.drawable.present); }
        if (day == 7) { tv7.setBackgroundResource(R.drawable.present); }
        if (day == 8) { tv8.setBackgroundResource(R.drawable.present); }
        if (day == 9) { tv9.setBackgroundResource(R.drawable.present); }
        if (day == 10) { tv10.setBackgroundResource(R.drawable.present); }

        if (day == 11) { tv11.setBackgroundResource(R.drawable.present); }
        if (day == 12) { tv12.setBackgroundResource(R.drawable.present); }
        if (day == 13) { tv13.setBackgroundResource(R.drawable.present); }
        if (day == 14) { tv14.setBackgroundResource(R.drawable.present); }
        if (day == 15) { tv15.setBackgroundResource(R.drawable.present); }
        if (day == 16) { tv16.setBackgroundResource(R.drawable.present); }
        if (day == 17) { tv17.setBackgroundResource(R.drawable.present); }
        if (day == 18) { tv18.setBackgroundResource(R.drawable.present); }
        if (day == 19) { tv19.setBackgroundResource(R.drawable.present); }
        if (day == 20) { tv20.setBackgroundResource(R.drawable.present); }

        if (day == 21) { tv21.setBackgroundResource(R.drawable.present); }
        if (day == 22) { tv22.setBackgroundResource(R.drawable.present); }
        if (day == 23) { tv23.setBackgroundResource(R.drawable.present); }
        if (day == 24) { tv24.setBackgroundResource(R.drawable.present); }
        if (day == 25) { tv25.setBackgroundResource(R.drawable.present); }
        if (day == 26) { tv26.setBackgroundResource(R.drawable.present); }
        if (day == 27) { tv27.setBackgroundResource(R.drawable.present); }
        if (day == 28) { tv28.setBackgroundResource(R.drawable.present); }
        if (day == 29) { tv29.setBackgroundResource(R.drawable.present); }
        if (day == 30) { tv30.setBackgroundResource(R.drawable.present); }

        if (day == 31) { tv31.setBackgroundResource(R.drawable.present); }

    }
}