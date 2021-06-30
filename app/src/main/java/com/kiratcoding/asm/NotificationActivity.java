package com.kiratcoding.asm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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
import com.kiratcoding.asm.AdapterClass.NotificationAdapter;
import com.kiratcoding.asm.HelperClass.HttpsTrustManager;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.ModelsClass.Notification;
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

public class NotificationActivity extends AppCompatActivity {

    ListView partiesListView;
    public static ArrayList<Notification> partiesArrayList = new ArrayList<>();
    NotificationAdapter partiesAdapter;
    Notification parties;

    ConstraintLayout noMsgLayout;
    LinearLayout msgLayout;

    private String currentDate, currentTime,pId,pName;
    private ProgressDialog pd;

    Employee employee;
    String uniqueNumber;
    SwipeRefreshLayout pullToRefresh;

    String msgId,msgTitle, msgDetail, msgDate, msgTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = findViewById(R.id.notify_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Notifications");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        HttpsTrustManager.allowAllSSL();

        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait.. ");
        pd.setCanceledOnTouchOutside(false);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = date.format(calendar.getTime());
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        currentTime = time.format(calendar.getTime());

        employee = SharedPrefLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());

        noMsgLayout = findViewById(R.id.noMsgLayout);
        msgLayout = findViewById(R.id.msgLayout);
        pullToRefresh = findViewById(R.id.notificationRefresh);
        partiesListView  = findViewById(R.id.msgListView);

        partiesAdapter = new NotificationAdapter(this, partiesArrayList);
        partiesListView.setAdapter(partiesAdapter);

        partiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                msgId = String.valueOf(partiesArrayList.get(position).getId());
                makeAsRead(msgId);
            }
        });

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                retriveData();
                partiesAdapter.notifyDataSetChanged();
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    private void makeAsRead(String msgId) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/updateNotificationStatus.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Upload", response);
                        if(response.equalsIgnoreCase("Updated")){
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
                Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("id",msgId);
                return orderMap;

            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(NotificationActivity.this);
        rQeue.add(request);

    }

    @Override
    protected void onStart() {
        super.onStart();
        retriveData();
    }

    private void retriveData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/Notification.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        partiesArrayList.clear();
                        Log.i("NotificationResponce", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("Notification");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int id = object.getInt("id");
                                    String title = object.getString("title");
                                    String detail = object.getString("message");
                                    String date = object.getString("date");
                                    String time = object.getString("time");
                                    parties = new Notification(id,title,detail,date,time);

                                    partiesArrayList.add(parties);
                                    Collections.reverse(partiesArrayList);
                                    partiesAdapter.notifyDataSetChanged();
                                    progressDialog.dismiss();

                                    noMsgLayout.setVisibility(View.GONE);
                                    msgLayout.setVisibility(View.VISIBLE);
                                }
                            }else if (jsonObject.optString("status").equals("false")){
                                noMsgLayout.setVisibility(View.VISIBLE);
                                msgLayout.setVisibility(View.GONE);
                                progressDialog.dismiss();
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                            noMsgLayout.setVisibility(View.VISIBLE);
                            msgLayout.setVisibility(View.GONE);
                            progressDialog.dismiss();
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
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
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