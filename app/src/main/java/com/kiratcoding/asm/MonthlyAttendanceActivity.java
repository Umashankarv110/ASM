package com.kiratcoding.asm;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.material.snackbar.Snackbar;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.naishadhparmar.zcustomcalendar.CustomCalendar;
import org.naishadhparmar.zcustomcalendar.OnDateSelectedListener;
import org.naishadhparmar.zcustomcalendar.OnNavigationButtonClickedListener;
import org.naishadhparmar.zcustomcalendar.Property;

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

public class MonthlyAttendanceActivity extends AppCompatActivity implements OnNavigationButtonClickedListener{
    CustomCalendar customCalendar;
    String selectedDate,currentDate,uniqueNumber;
    Employee employee;
    ProgressDialog pd;
    Calendar calendar;
    int currentDay;

    String firstdate, lastdate;

    HashMap<Object, Property> descHashMap;
    HashMap<Integer,Object> dateHashMap;
    List<Integer> daySList;
    Property defaultProperty,currentProperty,presentProperty,absentProperty,futureProperty,sundayProperty;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_attendance);
        Toolbar toolbar = findViewById(R.id.monthlyReport_Toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Monthly Attendance");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait.. ");
        pd.setCanceledOnTouchOutside(false);

        calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());

        LocalDate cDate = LocalDate.parse(currentDate);
        currentDay = cDate.getDayOfMonth();

        LocalDate lastDayOfMonth = LocalDate.parse(currentDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate firstDayOfMonth = LocalDate.parse(currentDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).with(TemporalAdjusters.firstDayOfMonth());

        employee = SharedPrefLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());

        customCalendar = findViewById(R.id.custom_calender);

        descHashMap = new HashMap<>();
        //For Default Property
        defaultProperty = new Property();
        defaultProperty.layoutResource = R.layout.default_view;
        defaultProperty.dateTextViewResource = R.id.textView;
        descHashMap.put("default",defaultProperty);

        futureProperty = new Property();
        futureProperty.layoutResource = R.layout.default_view;
        futureProperty.dateTextViewResource = R.id.textView;
        descHashMap.put("future",futureProperty);

        //For Current Property
        currentProperty = new Property();
        currentProperty.layoutResource = R.layout.current_view;
        currentProperty.dateTextViewResource = R.id.textView1;
        descHashMap.put("current",currentProperty);

        //For present Property
        presentProperty = new Property();
        presentProperty.layoutResource = R.layout.present_view;
        presentProperty.dateTextViewResource = R.id.textView2;
        descHashMap.put("present",presentProperty);

        //For absent Property
        absentProperty = new Property();
        absentProperty.layoutResource = R.layout.absent_view;
        absentProperty.dateTextViewResource = R.id.textView3;
        descHashMap.put("absent",absentProperty);
        customCalendar.setMapDescToProp(descHashMap);

        //For sunday Property
        sundayProperty = new Property();
        sundayProperty.layoutResource = R.layout.sunday_view;
        sundayProperty.dateTextViewResource = R.id.textView3;
        descHashMap.put("sunday",sundayProperty);
        customCalendar.setMapDescToProp(descHashMap);

        dateHashMap = new HashMap<>();
        dateHashMap.put(calendar.get(Calendar.DAY_OF_MONTH),"current");
        customCalendar.setDate(calendar, dateHashMap);

        customCalendar.setOnNavigationButtonClickedListener(CustomCalendar.PREVIOUS, this);//OnNavigationButtonClickedListener(CustomCalendar.PREVIOUS, this);
        customCalendar.setOnNavigationButtonClickedListener(CustomCalendar.NEXT, this);

        currentDay = cDate.getDayOfMonth();

        getMonthlyAttendance(String.valueOf(firstDayOfMonth), String.valueOf(lastDayOfMonth), currentDay);

        daySList = new ArrayList<>();
        //Mark Sunday As Holiday
        List<Date> disableList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int month = cal.get(Calendar.MONTH);
        do {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SUNDAY)
                disableList.add(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } while (cal.get(Calendar.MONTH) == month);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        for (Date date1: disableList) {
            String cDate1 = fmt.format(date1);
            LocalDate mLocalDate = LocalDate.parse(cDate1);
            int day = mLocalDate.getDayOfMonth();
            dateHashMap.put(day, "sunday");
            customCalendar.setDate(calendar,dateHashMap);
        }

    }




    @Override
    public Map<Integer, Object>[] onNavigationButtonClicked(int whichButton, Calendar newMonth) {

        int year = newMonth.get(Calendar.YEAR);
        int month = newMonth.get(Calendar.MONTH)+1;
        currentDay = newMonth.get(Calendar.DAY_OF_MONTH);

        if (String.valueOf(month).length()==1){
            firstdate = year+"-0"+month+"-01";
            lastdate = year+"-0"+month+"-31";
        } else {
            firstdate = year+"-"+month+"-01";
            lastdate = year+"-"+month+"-31";
        }

        Map<Integer, Object>[] arr = new Map[2];

//        getMonthlyAttendance(firstdate, lastdate, currentDay);
        switch(newMonth.get(Calendar.MONTH)) {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
            case Calendar.APRIL:
            case Calendar.MAY:
            case Calendar.JUNE:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                arr[0] = new HashMap<>();
                break;
        }
//        Toast.makeText(MonthlyAttendanceActivity.this, "lksdklsdflksfmkdmsf", Toast.LENGTH_SHORT).show();
//        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/AttendanceMonthlyDetails.php",
//                new Response.Listener<String>() {
//                    @RequiresApi(api = Build.VERSION_CODES.O)
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(response);
//                            Log.i("InAttendancResponce",response);
//                            if (jsonObject.optString("status").equals("true")){
//                                JSONArray jsonArray = jsonObject.getJSONArray("MonthAttendance");
//
//                                for (int i=0; i<jsonArray.length();i++){
//                                    JSONObject object = jsonArray.getJSONObject(i);
//                                    int id = object.getInt("id");
//                                    String status = object.getString("status");
//                                    String date = object.getString("currentDate");
//                                    LocalDate cDate = LocalDate.parse(date);
//                                    int day = cDate.getDayOfMonth();
//
//                                    if (status.equalsIgnoreCase("CheckOut")) {
//                                        switch(newMonth.get(Calendar.MONTH)) {
//                                            case Calendar.NOVEMBER:
//                                                Log.i("DATTTTTTTE",newMonth.get(Calendar.MONTH)+""+cDate.getMonthValue());
//                                                arr[0] = new HashMap<>();
//                                                arr[0].put(3, "present");
//                                                arr[0].put(5, "present");
//                                                arr[0].put(6, "present");
//                                                arr[0].put(10, "present");
//                                                arr[0].put(19, "present");
//                                                arr[0].put(21, "present");
//                                                arr[0].put(24, "present");
//                                                Log.i("DATE",""+arr.length);
//                                                break;
//                                        }
////                                        switch(newMonth.get(Calendar.MONTH)) {
////                                            case Calendar.JANUARY:
////                                            case Calendar.FEBRUARY:
////                                            case Calendar.MARCH:
////                                            case Calendar.APRIL:
////                                            case Calendar.MAY:
////                                            case Calendar.JUNE:
////                                            case Calendar.JULY:
////                                            case Calendar.AUGUST:
////                                            case Calendar.SEPTEMBER:
////                                            case Calendar.OCTOBER:
////                                            case Calendar.NOVEMBER:
////                                            case Calendar.DECEMBER:
////                                                arr[0] = new HashMap<>();
////                                                arr[0].put(day, "present");
////                                                break;
////                                        }
//
//                                    }
//                                    pd.dismiss();
//
//                                }
//                            }else if (jsonObject.optString("status").equals("false")){
//                                pd.dismiss();
//                                Log.i("status","No Record Found..");
//                            }
//                        }catch (JSONException e){
//                            e.printStackTrace();
//                            pd.dismiss();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                String message = null;
//                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
//                    message = "No Internet Connection";
//                } else if (volleyError instanceof ServerError) {
//                    message = "The server could not be found. Please try again later";
//                }
//                pd.dismiss();
//                Toast.makeText(MonthlyAttendanceActivity.this, ""+message, Toast.LENGTH_SHORT).show();
//            }
//        }){
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//
//                Log.i("uniquenumber", uniqueNumber);
//                Log.i("firstdate",""+firstdate);
//                Log.i("lastdate",""+lastdate);
//
//                HashMap<String, String> orderMap = new HashMap<>();
//                orderMap.put("uniquenumber", uniqueNumber);
//                orderMap.put("fromDate", firstdate);
//                orderMap.put("toDate", lastdate);
//                return orderMap;
//            }
//        };
//
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(request);
        return arr;
    }

    private void getMonthlyAttendance(String firstdate, String lastdate, int currentDay) {
        pd.show();
        for (int i=1; i<currentDay;i++){
            dateHashMap.put(i, "absent");
            customCalendar.setDate(calendar, dateHashMap);
            Log.i("Executed-",""+i);
        }
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/AttendanceMonthlyDetails.php",
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.i("AttendancResponce",response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("MonthAttendance");

                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int id = object.getInt("id");
                                    String status = object.getString("status");
                                    String date = object.getString("currentDate");
                                    LocalDate cDate = LocalDate.parse(date);
                                    int day = cDate.getDayOfMonth();

                                    if (status.equalsIgnoreCase("CheckOut")) {
//                                        dateHashMap.put(day, "present");
//                                        customCalendar.setDate(calendar, dateHashMap);
//                                        Log.i("day", status + "|" + day);
//                                        customCalendar.setOnDateSelectedListener(new OnDateSelectedListener() {
//                                            @Override
//                                            public void onDateSelected(View view, Calendar selectedDate, Object desc) {
//                                                String sDate = selectedDate.get(Calendar.DAY_OF_MONTH)
//                                                        +"/"+ (selectedDate.get(Calendar.MONTH)+1)
//                                                        +"/"+  selectedDate.get(Calendar.YEAR);
//                                                Snackbar.make(customCalendar, sDate + " selected", Snackbar.LENGTH_LONG).show();
//                                            }
//                                        });
                                    }

                                    pd.dismiss();
                                }
                            }else if (jsonObject.optString("status").equals("false")){
                                pd.dismiss();
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
                Toast.makeText(MonthlyAttendanceActivity.this, ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Log.i("uniquenumber", uniqueNumber);
                Log.i("firstdate",""+firstdate);
                Log.i("lastdate",""+lastdate);

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
}