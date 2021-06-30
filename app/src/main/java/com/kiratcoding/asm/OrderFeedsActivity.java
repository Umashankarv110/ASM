package com.kiratcoding.asm;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kiratcoding.asm.AdapterClass.CategoryAdapter;
import com.kiratcoding.asm.HelperClass.DoubleKeyHashMap;
import com.kiratcoding.asm.AdapterClass.FeedAdapter;
import com.kiratcoding.asm.HelperClass.HttpsTrustManager;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.ModelsClass.FeedCategories;
import com.kiratcoding.asm.ModelsClass.Feeds;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class OrderFeedsActivity extends AppCompatActivity {

    ListView feedListView;
    FloatingActionButton floating_layout;
    ImageView no_product_img;
    private TextView selectCategory, etPartyName, order_feed_count,selected_item,choose_title;

    public static ArrayList<Feeds> feedArrayList = new ArrayList<>();
    FeedAdapter feedAdapter;
    Feeds feed;


    String status,uniqueNumber,feedId,feedName,feedPrice;
    private String currentDate,currentTime;
    private ProgressDialog pd;

    Employee employee;
    String FeedValue,pId,pName,oId,PartyName,orderId;

    DoubleKeyHashMap<String, String, String> doubleKeyHashMap2 = new DoubleKeyHashMap<String, String, String>();

    ListView categoryListView;
    public static ArrayList<FeedCategories> categoryArrayList = new ArrayList<>();
    CategoryAdapter categoryAdapter;
    String categoryId,categoryName;

    int oCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_feeds);
        Toolbar toolbar = findViewById(R.id.feed_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Order Feeds");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

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

        feedListView  = findViewById(R.id.feedList);
        etPartyName = findViewById(R.id.et_party_name);
        order_feed_count = findViewById(R.id.order_feed_count);
        floating_layout = findViewById(R.id.floatCartOrder);
        selectCategory = findViewById(R.id.tv_category);
        selected_item = findViewById(R.id.tv_category_title);
        choose_title = findViewById(R.id.choose_title);
        no_product_img = findViewById(R.id.no_product_img);

        no_product_img.setVisibility(View.GONE);
        floating_layout.setVisibility(View.GONE);
        order_feed_count.setVisibility(View.GONE);
        feedListView.setVisibility(View.GONE);


        Intent intent = getIntent();
        pId = intent.getStringExtra("partyId");
        pName = intent.getStringExtra("partyName");
        orderId = intent.getStringExtra("orderId");

        etPartyName.setText(pName);

        feedAdapter = new FeedAdapter(this, feedArrayList);
        feedListView.setAdapter(feedAdapter);
        categoryAdapter = new CategoryAdapter(this, categoryArrayList);

        selectCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(OrderFeedsActivity.this);
                dialog.setContentView(R.layout.activity_vehicle);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(true);
                dialog.show();

                TextView topText = dialog.findViewById(R.id.text);
                categoryListView  = dialog.findViewById(R.id.vehicleList);
                topText.setText("Select Category");

                categoryListView.setAdapter(categoryAdapter);
                categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        categoryId = String.valueOf(categoryArrayList.get(position).getId());
                        categoryName = categoryArrayList.get(position).getName();
                        selected_item.setVisibility(View.VISIBLE);
                        feedListView.setVisibility(View.VISIBLE);
                        choose_title.setVisibility(View.GONE);
                        selected_item.setText(categoryName+" Selected");
                        retriveFeedData(categoryId);
                        dialog.dismiss();
                    }
                });

            }
        });


        feedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                feedId = String.valueOf(feedArrayList.get(position).getId());
                feedName = feedArrayList.get(position).getName();
                feedPrice = String.valueOf(feedArrayList.get(position).getSellprice());

                final Dialog qtyDialog = new Dialog(OrderFeedsActivity.this);
                qtyDialog.setContentView(R.layout.layout_bag_value);
                qtyDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                qtyDialog.setCancelable(false);
                qtyDialog.show();

                TextView head = qtyDialog.findViewById(R.id.textView42);
                ImageView imageView = qtyDialog.findViewById(R.id.tv_close);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        qtyDialog.dismiss();
                    }
                });

                Button create = qtyDialog.findViewById(R.id.btnOk);
                create.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText BagQty = qtyDialog.findViewById(R.id.etBag);
                        String Qty = BagQty.getText().toString().trim();
                        PlaceTempOrder(feedId,feedName,feedPrice,Qty,orderId,uniqueNumber,currentDate);
                        qtyDialog.dismiss();

                    }
                });



            }
        });

    }

    private void PlaceTempOrder(String feedId, String feedName, String feedPrice, String qty, String orderId, String uniqueNumber, String currentDate) {
        pd.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/uploadTempOrder.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("UploadOrderRequest", response);
                        if(response.equalsIgnoreCase("Uploaded")){
                            doubleKeyHashMap2.put(feedId,feedName,feedPrice);
                            Log.i("FeedList",doubleKeyHashMap2.toString());
                            refresh();
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
                orderMap.put("feedId",feedId);
                orderMap.put("feed", feedName);
                orderMap.put("feedqty",qty);
                orderMap.put("feedprice", feedPrice);
                orderMap.put("orderid",orderId);
                orderMap.put("uniquenumber", uniqueNumber);
                orderMap.put("currentDate",currentDate);
                return orderMap;

            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(OrderFeedsActivity.this);
        rQeue.add(request);
    }

    @Override
    protected void onStart() {
        super.onStart();
        retriveCategoryData();
        retriveTempOrderData();
    }

    //Retrieve Data
    private void retriveCategoryData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/retrieveCategoryDetails.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Categories_array", response);
                        categoryArrayList.clear();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("Categories_array");
                                FeedCategories categories0 = new FeedCategories(0,"Uncategorized","Uncategorized");
                                categoryArrayList.add(categories0);
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int id = object.getInt("id");
                                    String name = object.getString("name");
                                    String description = object.getString("description");
                                    FeedCategories categories = new FeedCategories(id,name,description);
                                    categoryArrayList.add(categories);
                                    categoryAdapter.notifyDataSetChanged();
                                    progressDialog.dismiss();

                                }
                            }else if (jsonObject.optString("status").equals("false")){
                                progressDialog.dismiss();
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
        });


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    private void retriveFeedData(String cId) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/retrieveFeedDetails.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("innnnnn",response);
                        feedArrayList.clear();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("Feed_Name");
                                for (int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int id = object.getInt("id");
                                    String name = object.getString("name");
                                    float sellprice = object.getLong("sellprice");
                                    feed = new Feeds(id,name,sellprice);
                                    feedArrayList.add(feed);
                                    feedAdapter.notifyDataSetChanged();
                                    progressDialog.dismiss();
                                    Log.i("if","response");
                                }
                                no_product_img.setVisibility(View.GONE);
                            }else if (jsonObject.optString("status").equals("false")){
                                no_product_img.setVisibility(View.VISIBLE);
                                Log.i("else if","response"+no_product_img.getVisibility());

                                progressDialog.dismiss();
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
                orderMap.put("categoryId", cId);
                return orderMap;

            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    private void retriveTempOrderData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/retrieveTempOrderDetails.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("status").equals("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("tempOrderList");
                                oCount = jsonArray.length();
                                order_feed_count.setText(""+oCount);
                                floating_layout.setVisibility(View.VISIBLE);
                                order_feed_count.setVisibility(View.VISIBLE);
                                progressDialog.dismiss();
                            }else if (jsonObject.optString("status").equals("false")){
                                progressDialog.dismiss();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void ViewOrder(View view) {
        Intent intent = new Intent(getApplicationContext(),ViewAllTempOrder.class);
        intent.putExtra("partyId", pId);
        intent.putExtra("partyName", pName);
        intent.putExtra("orderid",orderId);
        startActivity(intent);
    }

    private void refresh() {
        Intent intent = new Intent(getApplicationContext(), OrderFeedsActivity.class);
        intent.putExtra("partyId", pId);
        intent.putExtra("partyName", pName);
        intent.putExtra("orderId",orderId);
        startActivity(intent);
        Animatoo.animateFade(this);
        finish();
    }
}