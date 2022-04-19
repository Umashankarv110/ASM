package com.kiratcoding.asm;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.kiratcoding.asm.AdapterClass.PartiesAdapter;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.ModelsClass.Order;
import com.kiratcoding.asm.ModelsClass.Parties;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefOrderStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AllPartiesActivity extends AppCompatActivity {

    ListView partiesListView;
    public static ArrayList<Parties> partiesArrayList = new ArrayList<>();
    PartiesAdapter partiesAdapter;
    Parties parties;

    String status,uniqueNumber,PId,PName;
    private String currentDate,currentTime;
    private ProgressDialog pd;

    Employee employee;
    String OrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_parties);
        Toolbar toolbar = findViewById(R.id.parties_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Select Party");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

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

        partiesListView  = findViewById(R.id.partyList);
        partiesAdapter = new PartiesAdapter(this, partiesArrayList);
        partiesListView.setAdapter(partiesAdapter);

        partiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PId = String.valueOf(partiesArrayList.get(position).getId());
                PName = partiesArrayList.get(position).getName();
                CheckPendingOrder(PName,currentDate);
            }
        });

    }

    private void CheckPendingOrder(final String pName, final String currentDate) {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/retrieveOrderStatus.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("CheckOrder", response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if(obj.optString("status").equals("true")) {
                                JSONArray dataArray = obj.getJSONArray("Orders");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject dataobj = dataArray.getJSONObject(i);
                                    Order order = new Order(dataobj.getInt("id"), dataobj.getString("partyName"), dataobj.getString("orderStatus"), dataobj.getString("date"));
                                    SharedPrefOrderStatus.getInstance(getApplicationContext()).orderPending(order);
                                    final String Status = order.getOrderStatus();
                                    OrderId = String.valueOf(dataobj.getInt("id"));
                                    pd.dismiss();

                                    if(Status.equalsIgnoreCase("Processing")){
                                        new AlertDialog.Builder(AllPartiesActivity.this)
                                                .setMessage("Are you proceeding with the previous order?")
                                                .setCancelable(true).setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(getApplicationContext(), OrderFeedsActivity.class);
                                                intent.putExtra("partyId", PId);
                                                intent.putExtra("partyName", PName);
                                                intent.putExtra("orderId", OrderId);
                                                startActivity(intent);
                                            }
                                        }).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                deletePreviousOrder(OrderId,PName);
                                            }
                                        }).show();
                                    }
                                }
                            }else{
                                status = "Processing";
                                uploadOrder(status);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        pd.dismiss();
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
                Toast.makeText(AllPartiesActivity.this, ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("partyName", pName);
                orderMap.put("orderStatus", "Processing");
                orderMap.put("uniquenumber", uniqueNumber);
                return orderMap;
            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(AllPartiesActivity.this);
        rQeue.add(request);

    }

    private void uploadOrder(final String status) {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/Orders.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("UploadOrderRequest", response);
                        if(response.equalsIgnoreCase("Inserted")){
                            getOrderId();
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
                Toast.makeText(AllPartiesActivity.this, ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("partyId",PId);
                orderMap.put("partyName", PName);
                orderMap.put("uniquenumber", uniqueNumber);
                orderMap.put("employeeName", employee.getName());
                orderMap.put("totalAmount", "");
                orderMap.put("orderStatus", status);
                orderMap.put("date", currentDate);
                orderMap.put("time", currentTime);
                return orderMap;

            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(AllPartiesActivity.this);
        rQeue.add(request);

    }

    private void getOrderId() {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/retrieveOrder.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("getOrderIdResponce", response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if(obj.optString("status").equals("true")) {
                                JSONArray dataArray = obj.getJSONArray("Orders");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject dataobj = dataArray.getJSONObject(i);
                                    Order order = new Order(dataobj.getInt("id"), dataobj.getString("partyName"), dataobj.getString("orderStatus"), dataobj.getString("date"));
                                    SharedPrefOrderStatus.getInstance(getApplicationContext()).orderPending(order);
                                    OrderId = String.valueOf(dataobj.getInt("id"));
                                    Intent intent = new Intent(getApplicationContext(), OrderFeedsActivity.class);
                                    intent.putExtra("partyId", PId);
                                    intent.putExtra("partyName", PName);
                                    intent.putExtra("orderId", OrderId);
                                    startActivity(intent);
                                    pd.dismiss();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        pd.dismiss();
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
                Toast.makeText(AllPartiesActivity.this, ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("partyName", PName);
                orderMap.put("date", currentDate);
                return orderMap;

            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(AllPartiesActivity.this);
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
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/retrievePartiesDetails.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        partiesArrayList.clear();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("Parties_Name");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int id = object.getInt("id");
                                    String name = object.getString("name");
                                    parties = new Parties(id,name);

                                    partiesArrayList.add(parties);
                                    partiesAdapter.notifyDataSetChanged();
                                    progressDialog.dismiss();

                                }
                            }else if (jsonObject.optString("status").equals("false")){
                                progressDialog.dismiss();
                                Toast.makeText(AllPartiesActivity.this, "Please add party", Toast.LENGTH_SHORT).show();
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
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
                Toast.makeText(AllPartiesActivity.this, ""+message, Toast.LENGTH_SHORT).show();
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

    private void deletePreviousOrder(final String orderId, final String PName) {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/deleteOrders.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Request", response);
                        Toast.makeText(AllPartiesActivity.this, "Previous Order Deleted", Toast.LENGTH_LONG).show();
                        pd.dismiss();
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
                Toast.makeText(AllPartiesActivity.this, ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("id",orderId);
                return orderMap;

            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(AllPartiesActivity.this);
        rQeue.add(request);

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

    public void Add_Parties(View view) {
        startActivity(new Intent(getApplicationContext(), NewPartyActivity.class));
    }
}