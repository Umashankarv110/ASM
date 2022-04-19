package com.kiratcoding.asm;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
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
import com.kiratcoding.asm.AdapterClass.AttendanceAdapter;
import com.kiratcoding.asm.AdapterClass.CalendarAdapter;
import com.kiratcoding.asm.ModelsClass.Attendance;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AttendanceCalendarActivity extends AppCompatActivity {
    ListView attendanceListView;
    public static ArrayList<Attendance> attendanceArrayList = new ArrayList<>();
    CalendarAdapter attendanceAdapter;
    Attendance attendance;
    ProgressDialog pd;
    Employee employee;
    String selectedDate,currentDate,uniqueNumber;
    int currentDay;

    Calendar calendar;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_calendar);

        attendanceListView  = findViewById(R.id.calender_ListView1);

        employee = SharedPrefLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());

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


        getMonthlyAttendance(""+firstDayOfMonth,""+lastDayOfMonth);

        attendanceAdapter = new CalendarAdapter(this, attendanceArrayList);
        attendanceListView.setAdapter(attendanceAdapter);
    }
    private void getMonthlyAttendance(String firstdate, String lastdate) {
        pd.show();
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
                                    String task = object.getString("note");
                                    String date = object.getString("currentDate");
                                    LocalDate cDate = LocalDate.parse(date);
                                    int day = cDate.getDayOfMonth();
                                    int month = cDate.getDayOfMonth();
                                    int year = cDate.getDayOfMonth();
//                                    int weekDay = cDate.getDayOfWeek();

                                    Log.i("day|month|year|week",cDate.getDayOfMonth()+"|"+cDate.getMonth()+"|"+cDate.getYear()+"|"+cDate.getDayOfWeek());

                                    if (status.equalsIgnoreCase("CheckOut")) {
                                        attendance = new Attendance("Present",task,""+day,String.valueOf(cDate.getDayOfWeek()).substring(0,3),String.valueOf(cDate.getMonth()).substring(0,3),""+cDate.getYear());
                                        attendanceArrayList.add(attendance);
                                        attendanceAdapter.notifyDataSetChanged();
                                        pd.dismiss();
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
                Toast.makeText(AttendanceCalendarActivity.this, ""+message, Toast.LENGTH_SHORT).show();
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