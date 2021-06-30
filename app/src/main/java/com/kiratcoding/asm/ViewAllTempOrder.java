package com.kiratcoding.asm;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.kiratcoding.asm.AdapterClass.TempOrderAdapter;
import com.kiratcoding.asm.HelperClass.DoubleKeyHashMap;
import com.kiratcoding.asm.HelperClass.HttpsTrustManager;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.ModelsClass.TempOrder;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewAllTempOrder extends AppCompatActivity {

    ListView tOrderListView;
    TextView tv_TotalAmt;
    Button addButton;
    public static ArrayList<TempOrder> tOrderArrayList = new ArrayList<>();
    TempOrderAdapter tOrderAdapter;
    TempOrder tOrder;
    ConstraintLayout amtConstraintLayout;

    String orderId, uniqueNumber, tOrderId, tOrderName, tOrderPrice, tOrderQty, tOrderfeedId;
    private String currentDate, currentTime,pId,pName;
    private ProgressDialog pd;

    Employee employee;
    SwipeRefreshLayout pullToRefresh;

    DoubleKeyHashMap<String, String, String> doubleKeyHashMap2 = new DoubleKeyHashMap<String, String, String>();
    String[][] spaceProbes;
    String[] spaceProbHeaders;

    private float oneTypeProductPrice = 0, OverAllTotalPrice =0;
    private int OverAllTotalQty=0;
    String totalAmount;
    int cartId,itemId,itemQty,oId;
    float itemPrice;
    String itemName;

    FusedLocationProviderClient fusedLocationProviderClient;
    private TextView Latitude,Longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_temp_order);
        Toolbar toolbar = findViewById(R.id.TempOrder_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cart Orders");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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

        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderid");
        pId = intent.getStringExtra("partyId");
        pName = intent.getStringExtra("partyName");

        tOrderListView = findViewById(R.id.tempOrderList);
        tv_TotalAmt = findViewById(R.id.totalAmt);
        amtConstraintLayout = findViewById(R.id.constraintLayout3);
        pullToRefresh = findViewById(R.id.pullToRefresh);
        addButton = findViewById(R.id.addAmtBtn);
        Latitude = findViewById(R.id.tv_latitude);
        Longitude = findViewById(R.id.tv_longitude);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterTotalAmt();
            }
        });

        amtConstraintLayout.setVisibility(View.GONE);


        tOrderAdapter = new TempOrderAdapter(this, tOrderArrayList);
        tOrderListView.setAdapter(tOrderAdapter);
        tOrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tOrderId = String.valueOf(tOrderArrayList.get(position).getId());
                tOrderfeedId = String.valueOf(tOrderArrayList.get(position).getFeedId());
                tOrderName = tOrderArrayList.get(position).getFeed();
                tOrderPrice = String.valueOf(tOrderArrayList.get(position).getFeedprice());
                tOrderQty = String.valueOf(tOrderArrayList.get(position).getFeedqty());

                final Dialog qtyDialog = new Dialog(ViewAllTempOrder.this);
                qtyDialog.setContentView(R.layout.layout_more_options);
                qtyDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                qtyDialog.setCancelable(false);
                qtyDialog.show();

                ImageView imageView = qtyDialog.findViewById(R.id.tv_close);
                ConstraintLayout deleteBtn = qtyDialog.findViewById(R.id.orderdelete);
                ConstraintLayout editBtn = qtyDialog.findViewById(R.id.orderEdit);

                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(ViewAllTempOrder.this)
                                .setMessage("Are you sure you want to delete "+tOrderName+" ?")
                                .setCancelable(true).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                qtyDialog.dismiss();
                                if(deleteTempOrder(tOrderId,tOrderName)){
                                    tOrderArrayList.remove(position);
                                    tOrderAdapter.notifyDataSetChanged();
                                    if(tOrderArrayList.size() == 0) {
                                        Intent intent = new Intent(getApplicationContext(), OrderFeedsActivity.class);
                                        intent.putExtra("partyId", pId);
                                        intent.putExtra("partyName", pName);
                                        intent.putExtra("orderId",orderId);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        refresh();
                                        Toast.makeText(ViewAllTempOrder.this,  tOrderName+ " Deleted", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }).setNegativeButton("Cancel", null).show();
                    }
                });

                editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        qtyDialog.dismiss();
                        final Dialog dialog = new Dialog(ViewAllTempOrder.this);
                        dialog.setContentView(R.layout.layout_feed_edit);
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.setCancelable(false);

                        EditText name = dialog.findViewById(R.id.tv_edit_feedname);
                        EditText price = dialog.findViewById(R.id.tv_edit_feedprice);
                        EditText qty = dialog.findViewById(R.id.et_editQty);
                        Button update = dialog.findViewById(R.id.qtyUpdate);
                        ImageView close = dialog.findViewById(R.id.tv_edit_close);

                        name.setText(tOrderName);
                        price.setText(tOrderPrice);
                        qty.setText(tOrderQty);

                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        update.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(qty.getText().toString().isEmpty()){
                                    qty.setError("Enter Qty");
                                    qty.requestFocus();
                                    return;
                                }else {
                                    String feedQty = qty.getText().toString().trim();
                                    if(UpdateTempOrder(tOrderId, feedQty)){
                                        dialog.dismiss();
                                        refresh();
                                    }
                                }
                            }
                        });

                        dialog.show();
                    }
                });

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        qtyDialog.dismiss();
                    }
                });

            }
        });

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tOrderAdapter.notifyDataSetChanged();
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    private void EnterTotalAmt() {

        final Dialog qtyDialog = new Dialog(ViewAllTempOrder.this);
        qtyDialog.setContentView(R.layout.layout_total_amt);
        qtyDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qtyDialog.setCancelable(false);
        qtyDialog.show();

        EditText totalAmt = qtyDialog.findViewById(R.id.etAmt);
        ImageView imageView = qtyDialog.findViewById(R.id.tv_close);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qtyDialog.dismiss();
            }
        });

        Button create = qtyDialog.findViewById(R.id.btnAmt);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Amt = totalAmt.getText().toString().trim();
                totalAmount = Amt;
                amtConstraintLayout.setVisibility(View.VISIBLE);
                tv_TotalAmt.setText("Total: ₹"+Amt);
                addButton.setText("Change Total Amount");
                qtyDialog.dismiss();
            }
        });

    }

    private boolean UpdateTempOrder(String tOrderId, String qty) {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/updateTempOrder.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("UploadOrderRequest", response);
                        if(response.equalsIgnoreCase("Updated")){
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
                Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("id",tOrderId);
                orderMap.put("feedqty",qty);
                return orderMap;

            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(ViewAllTempOrder.this);
        rQeue.add(request);

        return true;
    }

    private boolean deleteTempOrder(String tOrderId, String tOrderName) {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/deleteSelectedOrders.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Request", response);
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
                Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("id",tOrderId);
                return orderMap;

            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(ViewAllTempOrder.this);
        rQeue.add(request);

        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();
        retriveTempOrderData();
        MyCurrentLocation();
    }

    private void MyCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(ViewAllTempOrder.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null){
                        try {
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());  //Initialize geoCode
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                            Latitude.setText(""+addresses.get(0).getLatitude());
                            Longitude.setText(""+addresses.get(0).getLongitude());
                            Log.i("Latitude",""+addresses.get(0).getLatitude());
                            Log.i("Longitude",""+addresses.get(0).getLongitude());
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }else{
            ActivityCompat.requestPermissions(ViewAllTempOrder.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
    }

    private void retriveTempOrderData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/retrieveTempOrderDetails.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Temp", response);
                        tOrderArrayList.clear();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("tempOrderList");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int id = object.getInt("id");
                                    int feedid = object.getInt("feedId");
                                    String feedname = object.getString("feed");
                                    int feedqty = object.getInt("feedqty");
                                    float price = object.getLong("feedprice");

//                                    oneTypeProductPrice = feedqty * price;
//                                    OverAllTotalPrice = OverAllTotalPrice + oneTypeProductPrice;
//                                    tv_TotalAmt.setText("Total: ₹"+OverAllTotalPrice);

                                    Log.i("oneTypeProductPrice",""+oneTypeProductPrice);
                                    Log.i("OverAllTotalPrice",""+OverAllTotalPrice);

                                    tOrder = new TempOrder(id,feedid,feedqty,feedname,price);
                                    tOrderArrayList.add(tOrder);
                                    doubleKeyHashMap2.put(""+feedid,feedname,""+feedqty);
                                    tOrderAdapter.notifyDataSetChanged();
                                    progressDialog.dismiss();
                                }
                            }else if (jsonObject.optString("status").equals("false")){
                                progressDialog.dismiss();
                                Toast.makeText(ViewAllTempOrder.this, "Opps!! \nNo feeds Available", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("orderid",orderId);
                orderMap.put("uniquenumber", uniqueNumber);
                return orderMap;

            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    public void PlaceMyOrderNow(View view) {
        Log.i("MapList",doubleKeyHashMap2.toString());
        new AlertDialog.Builder(ViewAllTempOrder.this)
                .setMessage("Are you sure you want to proceed")
                .setCancelable(true).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PlaceOrder();
            }
        }).setNegativeButton("Cancel", null).show();

    }

    private void PlaceOrder() {
        pd.show();
        Iterator<DoubleKeyHashMap<String, String, String>.Pair<String, String>> iterator = doubleKeyHashMap2.iterator();
        while(iterator.hasNext()) {
            DoubleKeyHashMap<String, String, String>.Pair<String, String> pair = iterator.next();
            System.out.println(pair.key1 + "," + pair.key2 + " = " +  doubleKeyHashMap2.get(pair.key1, pair.key2));

            StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/Orderdetails.php",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("Request", response);
                            UpdateOrderStatus();
                            Toast.makeText(ViewAllTempOrder.this, "Record Inserted..", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> orderMap = new HashMap<>();
                    orderMap.put("name", pName);
                    orderMap.put("feedId", pair.key1);
                    orderMap.put("feed", pair.key2);
                    orderMap.put("feedQty", doubleKeyHashMap2.get(pair.key1, pair.key2));
                    orderMap.put("uniquenumber", uniqueNumber);
                    orderMap.put("orderId", orderId);
                    orderMap.put("date", currentDate);
                    orderMap.put("time", currentTime);
                    return orderMap;

                }
            };
            RequestQueue rQeue;
            rQeue = Volley.newRequestQueue(ViewAllTempOrder.this);
            rQeue.add(request);

        }
    }

    private void UpdateOrderStatus() {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/updateOrder.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("UploadOrderRequest", response);
                        if(response.equalsIgnoreCase("Updated")){
                            deleteThisOrderIdFromCart(orderId);
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
                Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("id",orderId);
                orderMap.put("orderStatus", "Received");
                orderMap.put("totalAmount", totalAmount);
                orderMap.put("orderLatitude",Latitude.getText().toString().trim());
                orderMap.put("orderLongitude",Longitude.getText().toString().trim());
                return orderMap;

            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(ViewAllTempOrder.this);
        rQeue.add(request);

    }

    private void deleteThisOrderIdFromCart(String orderId) {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/deleteCartOrder.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Request", response);
                        pd.dismiss();
                        Log.i("Delete all from cart",""+response);
                        startActivity(new Intent(getApplicationContext(), AllPartiesActivity.class));
                        finish();
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
                Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("orderid",orderId);
                return orderMap;

            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(ViewAllTempOrder.this);
        rQeue.add(request);

    }

    private void refresh() {
        Intent intent = new Intent(getApplicationContext(), ViewAllTempOrder.class);
        intent.putExtra("partyId", pId);
        intent.putExtra("partyName", pName);
        intent.putExtra("orderid",orderId);
        startActivity(intent);
        Animatoo.animateFade(this);
        finish();
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