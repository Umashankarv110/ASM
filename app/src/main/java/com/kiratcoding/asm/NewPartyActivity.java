package com.kiratcoding.asm;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;

import java.util.HashMap;
import java.util.Map;

public class NewPartyActivity extends AppCompatActivity {

    private TextView partyName, feedItem;
    private TextView address,country,state,city,pincode;
    private TextView contactPerson, mobile, emailid, website;
    private TextView bankName,accType,accNo,ifscCode, gstNo;

    String PartyName="",Address="",Country="",State,City="",Pincode="",ContactPerson="",Mobile="",EmailId="",Website="",BankName="",AccType="",AccNo="",Ifsc="",Gst="";

    private Employee employee;
    private String uniqueNumber;

    private ProgressDialog pd;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_party);
        Toolbar toolbar = findViewById(R.id.party_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Party");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        pd = new ProgressDialog(this);

        employee = SharedPrefLogin.getInstance(this).getUser();
        uniqueNumber = String.valueOf(employee.getUniquenumber());

        partyName = findViewById(R.id.tv_company_name);
        address = findViewById(R.id.tv_address);
        country = findViewById(R.id.tv_country);
        state = findViewById(R.id.tv_state);
        city = findViewById(R.id.tv_city);
        pincode = findViewById(R.id.tv_pincode);
        contactPerson = findViewById(R.id.tv_contact_person);
        mobile = findViewById(R.id.tv_contact_no);
        emailid = findViewById(R.id.tv_email);
        website = findViewById(R.id.tv_website);
        bankName = findViewById(R.id.tv_bank_name);
        accNo = findViewById(R.id.tv_acc_no);
        ifscCode = findViewById(R.id.tv_ifsc_code);
        gstNo = findViewById(R.id.tv_gst_no);


    }

    public void SubmitAction(View view) {
        if(checkNetworkConnectionStatus()) {
            PartyName = partyName.getText().toString().trim();
            Address = address.getText().toString().trim();
            Country = country.getText().toString().trim();
            State = state.getText().toString().trim();
            City = city.getText().toString().trim();
            Pincode = pincode.getText().toString().trim();
            ContactPerson = contactPerson.getText().toString().trim();
            Mobile = mobile.getText().toString().trim();
            EmailId = emailid.getText().toString().trim();
            Website = website.getText().toString().trim();
            BankName = bankName.getText().toString().trim();
            AccNo = accNo.getText().toString().trim();
            Ifsc = ifscCode.getText().toString().trim();
            Gst = gstNo.getText().toString().trim();

            if (PartyName.isEmpty()) {
                Toast.makeText(this, "Enter Party / Company Name", Toast.LENGTH_SHORT).show();
            } else if (Address.isEmpty()) {
                Toast.makeText(this, "Enter Address", Toast.LENGTH_SHORT).show();
            } else if (Country.isEmpty()) {
                Toast.makeText(this, "Enter Country", Toast.LENGTH_SHORT).show();
            } else if (State.isEmpty()) {
                Toast.makeText(this, "Enter State", Toast.LENGTH_SHORT).show();
            } else if (City.isEmpty()) {
                Toast.makeText(this, "Enter City", Toast.LENGTH_SHORT).show();
            } else if (Pincode.isEmpty()) {
                Toast.makeText(this, "Enter Pincode", Toast.LENGTH_SHORT).show();
            } else if (Pincode.length() != 6) {
                Toast.makeText(this, "Enter Valid Pincode", Toast.LENGTH_SHORT).show();
            } else if (ContactPerson.isEmpty()) {
                Toast.makeText(this, "Enter Person Name", Toast.LENGTH_SHORT).show();
            } else if (Mobile.isEmpty()) {
                Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_SHORT).show();
            } else if (Mobile.length() != 10) {
                Toast.makeText(this, "Enter Valid Number", Toast.LENGTH_SHORT).show();
            } else if (EmailId.isEmpty()) {
                Toast.makeText(this, "Enter EmailId", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(EmailId).matches()) {
                Toast.makeText(this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
            } else if (BankName.isEmpty()) {
                Toast.makeText(this, "Enter BankName", Toast.LENGTH_SHORT).show();
            } else if (AccNo.isEmpty()) {
                Toast.makeText(this, "Enter Account No", Toast.LENGTH_SHORT).show();
            } else if (Ifsc.isEmpty()) {
                Toast.makeText(this, "Enter IFSC code", Toast.LENGTH_SHORT).show();
            } else if (Gst.isEmpty()) {
                Toast.makeText(this, "Enter Pan/GST no ", Toast.LENGTH_SHORT).show();
            } else {

                pd.setMessage("Please Wait.. ");
                pd.setCanceledOnTouchOutside(false);
                pd.show();

                OrdersDetails();

            }
        }

}

    private void OrdersDetails() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/PartyDetails.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Responce", response);
                Toast.makeText(NewPartyActivity.this, "Record Inserted..", Toast.LENGTH_SHORT).show();
                partyName.setText("");
                address.setText("");
                country.setText("India");
                state.setText("");
                city.setText("");
                pincode.setText("");
                contactPerson.setText("");
                mobile.setText("");
                emailid.setText("");
                website.setText("");
                bankName.setText("");
                accNo.setText("");
                ifscCode.setText("");
                gstNo.setText("");
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
                orderMap.put("name", PartyName);
                orderMap.put("mobile", Mobile);
                orderMap.put("uniquenumber", uniqueNumber);
                orderMap.put("email", EmailId);
                orderMap.put("website", Website);
                orderMap.put("address", Address);
                orderMap.put("city", City);
                orderMap.put("state", State);
                orderMap.put("pincode", Pincode);
                orderMap.put("country", Country);
                orderMap.put("bank", BankName);
                orderMap.put("accountNumber", AccNo);
                orderMap.put("ifsc", Ifsc);
                orderMap.put("gst", Gst);
                orderMap.put("contact_person", ContactPerson);
                return orderMap;
            }
        };
        RequestQueue rQeue;
        rQeue= Volley.newRequestQueue(NewPartyActivity.this);
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


    private boolean checkNetworkConnectionStatus() {
        final Dialog dialog = new Dialog(NewPartyActivity.this);
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


}