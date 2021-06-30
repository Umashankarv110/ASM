package com.kiratcoding.asm.ui.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
//import com.kiratcoding.asm.Graph.DayAxisValueFormatter;
//import com.kiratcoding.asm.Graph.MyAxisValueFormatter;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.R;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MonthlyTargetsFragment extends Fragment {
    ArrayList<BarEntry> entries;
    ArrayList<String> labels;
    BarChart barChart;
    BarData data;
    BarDataSet bardataset;

    String uniqueNumber;
    private String currentDate, currentTime;
    private ProgressDialog pd;
    Employee employee;

    Button okBtn;
    Spinner spinMonth, spinYear;
    ArrayAdapter<String> monthAdapter;
    ArrayList<String> spinnerMonthList;

    ArrayAdapter<String> yearAdapter;
    ArrayList<String> spinnerYearList;
    private String monthName,Month, Year;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_targets, container, false);

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Please Wait.. ");
        pd.setCanceledOnTouchOutside(false);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        currentTime = time.format(calendar.getTime());

        employee = SharedPrefLogin.getInstance(getActivity()).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());


        barChart = (BarChart) view.findViewById(R.id.barchart);
        entries = new ArrayList<>();

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinValue(0f);

        //Select Month
        spinMonth = view.findViewById(R.id.spinMonth);
        spinnerMonthList = new ArrayList<>();
        spinnerMonthList.add("Select Month");
        spinnerMonthList.add("January");
        spinnerMonthList.add("February");
        spinnerMonthList.add("March");
        spinnerMonthList.add("April");
        spinnerMonthList.add("May");
        spinnerMonthList.add("June");
        spinnerMonthList.add("July");
        spinnerMonthList.add("August");
        spinnerMonthList.add("September");
        spinnerMonthList.add("October");
        spinnerMonthList.add("November");
        spinnerMonthList.add("December");

        monthAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,spinnerMonthList);
        spinMonth.setAdapter(monthAdapter);
        spinMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                if(i == 0){
                    Month = "Select Month";
                }else {
                    Month = String.valueOf(i);
                    monthName = parent.getItemAtPosition(i).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        //Select Year
        spinYear = view.findViewById(R.id.spinYr);
        spinnerYearList = new ArrayList<>();
        spinnerYearList.add("Select Year");
        spinnerYearList.add("2020");
        spinnerYearList.add("2021");

        yearAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,spinnerYearList);
        spinYear.setAdapter(yearAdapter);
        spinYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                if(i == 0){
                    Year = "Select Year";
                }else {
                    Year = parent.getItemAtPosition(i).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });


        okBtn = view.findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (okBtn.getText().toString().equalsIgnoreCase("OK")) {
                    if (Month.equalsIgnoreCase("Select Month")) {
                        Toast.makeText(getActivity(), "Select Month", Toast.LENGTH_SHORT).show();
                    } else if (Year.equalsIgnoreCase("Select Year")) {
                        Toast.makeText(getActivity(), "Select Year", Toast.LENGTH_SHORT).show();
                    } else {
                        spinMonth.setEnabled(false);
                        spinYear.setEnabled(false);
                        okBtn.setText("Re-set");
                        Log.i("MonthYear",Month+"||"+Year);
                        getExpectedTarget(Month, Year);
                    }
                }else if (okBtn.getText().toString().equalsIgnoreCase("Re-set")) {
                    entries.clear();
                    barChart.clear();
                    barChart.invalidate();
                    spinMonth.setEnabled(true);
                    spinYear.setEnabled(true);
                    spinMonth.setSelection(0);
                    spinYear.setSelection(0);
                    okBtn.setText("Ok");
                }
            }
        });

        return view;
    }


    private void getExpectedTarget(String month, String year) {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/monthlyExpectedTarget.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("expectedTarget", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("expectedTarget");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int id = object.getInt("id");
                                    float expected = object.getLong("value");
                                    entries.add(new BarEntry(expected, 0));
                                    getAchievedTarget(expected,month,year);
                                    Log.i("expectedTargetvalue", String.valueOf(expected));
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
                String message = null;
                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                    message = "No Internet Connection";
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found. Please try again later";
                }
                pd.dismiss();
                Toast.makeText(getActivity(), ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("uniquenumber", uniqueNumber);
                orderMap.put("month", month);
                orderMap.put("year", year);
                return orderMap;

            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(request);

    }

    private void getAchievedTarget(float expected, String month, String year) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/monthlyAchievedTarget.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("achievedTarget", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("achievedTarget");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    float achieved = object.getLong("feedQty");
                                    Log.i("achievedTargetValue", String.valueOf(achieved));
                                    entries.add(new BarEntry(achieved, 1));
                                    ploatGraph(expected,achieved);
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
                String message = null;
                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                    message = "No Internet Connection";
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found. Please try again later";
                }
                pd.dismiss();
                Toast.makeText(getActivity(), ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("uniquenumber", uniqueNumber);
                orderMap.put("month", month);
                orderMap.put("year", year);
                return orderMap;

            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(request);

    }

    private void ploatGraph(float expected, float achieved) {
        bardataset = new BarDataSet(entries, monthName +" Month Graph");
        labels = new ArrayList<String>();
        labels.add("Expected");
        labels.add("Achieved");

        data = new BarData(labels, bardataset);
        barChart.setData(data); // set the data and list of labels into chart
        barChart.setDescription("");  // set the description
        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
        bardataset.setValueTextSize(16f);
        pd.dismiss();
        barChart.animateY(2000);
    }

}