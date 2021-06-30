package com.kiratcoding.asm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.textfield.TextInputLayout;
import com.kiratcoding.asm.HelperClass.HttpsTrustManager;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefPayrollLogin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AuthActivity extends AppCompatActivity {

    private Button signin,login;
    private TextView email,password,forgotpawd;
    String Email,pass1;
    private ProgressDialog pd;
    int otpCode;

    private Dialog dialog;

    ImageView mCloseIv;
    TextInputLayout mEmail, mEmpId;
    Button mVerify,mSendAgain;
    LinearLayout mlinearOtp;
    TextView mSuccessText, mCounter;
    EditText mCode1, mCode2, mCode3, mCode4;

    String emailId="", uid="", name="", gender="";

    CountDownTimer countDownTimer;
    long timeLeftInMilliSecond = 300000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        HttpsTrustManager.allowAllSSL();

        pd = new ProgressDialog(this);
        pd.setMessage("Checking Credentials \nPlease Wait.... ");

        //if the user is already logged in we will directly start the profile activity
        if (SharedPrefLogin.getInstance(this).isLoggedIn()) {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        //if the user is already logged in we will directly start the profile activity
        if (SharedPrefPayrollLogin.getInstance(this).isLoggedIn()) {
            Intent intent = new Intent(getApplicationContext(), PayrollMainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPass);
        login = findViewById(R.id.btnLogin);
        forgotpawd = findViewById(R.id.forgotPassword);

        forgotpawd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkNetworkConnectionStatus()){
                    Email = email.getText().toString().trim();
                    pass1 = password.getText().toString().trim();

                    if(Email.isEmpty()){
                        email.setError("Enter Email");
                        email.requestFocus();
                        return;
                    } else if(pass1.isEmpty()){
                        password.setError("Enter Password");
                        password.requestFocus();
                        return;
                    }
                    else {
                        String  tag_string_req = "string_req";
                        StringRequest sr = new StringRequest(Request.Method.POST, "https://www.arunodyafeeds.com/sales/android/Authentication.php",
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.i("Response:  ",response);

                                        try {
                                            JSONObject obj = new JSONObject(response);
                                            Employee employee = new Employee(
                                                    obj.getInt("uniquenumber"),
                                                    obj.getString("name"),
                                                    obj.getString("email"),
                                                    obj.getString("gender")
                                            );
                                            SharedPrefLogin.getInstance(getApplicationContext()).userLogin(employee);

                                            Toast.makeText(AuthActivity.this, "Successfully Login...", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                            startActivity(intent);
                                            finish();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                String message = null;
                                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                                    message = "No Internet Connection";
                                    Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                                } else if (volleyError instanceof ServerError) {
                                    message = "The server could not be found. Please try again later";
                                    Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                                }
                                pd.dismiss();
                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> map = new HashMap<>();
                                map.put("email",Email);
                                map.put("password",pass1);
                                return map;
                            }

                            @Override
                            protected Map<String, String> getPostParams() throws AuthFailureError {
                                Map<String,String> map = new HashMap<>();
                                map.put("email",email.getText().toString().trim());
                                map.put("password",password.getText().toString().trim());
                                return map;
                            }
                        };

//                        AppCompatActivity.getInstance().addToRequestQueue(strReq, tag_string_req);

                        RequestQueue rQeue;
                        rQeue= Volley.newRequestQueue(AuthActivity.this);
                        rQeue.add(sr);

                    }

                }
            }
        });
    }


    private boolean checkNetworkConnectionStatus() {
        final Dialog dialog = new Dialog(AuthActivity.this);
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
    private void randomOtp(){
        Random random = new Random();
        otpCode = random.nextInt(8999)+1000;
    }

    public void OtpLogin(View view) {
        randomOtp();
        dialog = new Dialog(AuthActivity.this);
        dialog.setContentView(R.layout.layout_otp);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();

        mCloseIv = dialog.findViewById(R.id.otpClose);
        mEmail = dialog.findViewById(R.id.otpEmail);
        mEmpId = dialog.findViewById(R.id.tvEmpIdforOtp);
        mVerify = dialog.findViewById(R.id.btnVerify);
        mlinearOtp = dialog.findViewById(R.id.linearOtp);
        mSuccessText = dialog.findViewById(R.id.textOtpMsg);
        mCounter = dialog.findViewById(R.id.textCounter);
        mSendAgain = dialog.findViewById(R.id.btnSendAgain);

        mCode1 = dialog.findViewById(R.id.digit1);
        mCode2 = dialog.findViewById(R.id.digit2);
        mCode3 = dialog.findViewById(R.id.digit3);
        mCode4 = dialog.findViewById(R.id.digit4);

        mCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0){
                    mCode2.requestFocus();
                }
            }
        });
        mCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0){
                    mCode3.requestFocus();
                }
            }
        });
        mCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0){
                    mCode4.requestFocus();
                }
            }
        });

        mlinearOtp.setVisibility(View.GONE);
        mEmail.setVisibility(View.GONE);
        mSuccessText.setVisibility(View.GONE);
        mCounter.setVisibility(View.GONE);
        mSendAgain.setVisibility(View.GONE);
        mEmpId.setVisibility(View.VISIBLE);

        mCloseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        mVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String unum = mEmpId.getEditText().getText().toString();
                String digit1 = mCode1.getText().toString();
                String digit2 = mCode2.getText().toString();
                String digit3 = mCode3.getText().toString();
                String digit4 = mCode4.getText().toString();

                if (mVerify.getText().toString().equalsIgnoreCase("Login")) {
                    if (unum.equalsIgnoreCase("")) {
                        Toast.makeText(AuthActivity.this, "Enter Your Employee Id", Toast.LENGTH_SHORT).show();
                    } else {
                        pd.show();
                        StringRequest sr = new StringRequest(Request.Method.POST, "https://arunodyafeeds.com/Employees/Employees/checkEmployeeUniqueNumber",
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.i("Response:  ", response);

                                        try {
                                            JSONObject jsonObject = new JSONObject(response);
                                            Log.i("Email Id:  ", jsonObject.optString("email"));

                                            mEmail.getEditText().setText(jsonObject.optString("email"));
                                            emailId = mEmail.getEditText().getText().toString();
                                            uid = jsonObject.optString("uniquenumber");
                                            name = jsonObject.optString("name");
                                            gender = jsonObject.optString("gender");
                                            sendOtpToEmail(uid, name, emailId, gender, otpCode);

                                        } catch (JSONException e) {
                                            pd.dismiss();
                                            e.printStackTrace();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                String message = null;
                                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                                    message = "No Internet Connection";
                                    Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();
                                } else if (volleyError instanceof ServerError) {
                                    message = "The server could not be found. Please try again later";
                                    Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();
                                }
                                pd.dismiss();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> map = new HashMap<>();
                                map.put("unum", unum);
                                return map;
                            }

                            @Override
                            protected Map<String, String> getPostParams() throws AuthFailureError {
                                Map<String, String> map = new HashMap<>();
                                map.put("unum", unum);
                                return map;
                            }
                        };

                        RequestQueue rQeue = Volley.newRequestQueue(AuthActivity.this);
                        rQeue.add(sr);

                    }
                }
                else if (mVerify.getText().toString().equalsIgnoreCase("Verify")) {
                    String inputCode;
                    inputCode = digit1+digit2+digit3+digit4;
                    if (inputCode.equalsIgnoreCase("")){
                        Toast.makeText(AuthActivity.this, "Enter Otp", Toast.LENGTH_SHORT).show();
                    }else if (inputCode.equals(String.valueOf(otpCode))){
                        Employee employee = new Employee(Integer.parseInt(uid),""+emailId,""+name,""+gender);
                        SharedPrefPayrollLogin.getInstance(getApplicationContext()).userLogin(employee);
                        startActivity(new Intent(getApplicationContext(), PayrollMainActivity.class));
                        finish();
                        Toast.makeText(AuthActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(AuthActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();

                }
            }
        });
        mSendAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mEmail.getEditText().getText().toString();
                emailId = mEmail.getEditText().getText().toString();
                if(emailId.equalsIgnoreCase("")){
                    Toast.makeText(AuthActivity.this, "Enter Email id", Toast.LENGTH_SHORT).show();
                }else {
                    pd.show();
                    randomOtp();
                    sendOtpToEmail(uid, name, emailId, gender, otpCode);
                    mVerify.setEnabled(true);
                    mSendAgain.setVisibility(View.GONE);
                }
            }
        });


    }
    private void sendOtpToEmail(String uid, String name, String emailId, String gender, int otp) {
        StringRequest sr = new StringRequest(Request.Method.POST, "https://arunodyafeeds.com/Employees/Notify/sendOTPForLogin",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("sendOtpToEmail:  ", response);
                        pd.dismiss();
                        mlinearOtp.setVisibility(View.VISIBLE);
                        mEmail.setVisibility(View.VISIBLE);
                        mSuccessText.setVisibility(View.VISIBLE);
                        mCounter.setVisibility(View.VISIBLE);
                        mEmpId.setVisibility(View.GONE);
                        mCode1.requestFocus();
                        mVerify.setText("Verify");

                        countDownTimer = new CountDownTimer(timeLeftInMilliSecond,1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                timeLeftInMilliSecond = millisUntilFinished;

                                int minutes = (int) timeLeftInMilliSecond / 60000;
                                int seconds = (int) timeLeftInMilliSecond % 60000 / 1000;

                                String timeLeft;
                                timeLeft = ""+minutes;
                                timeLeft += ":";
                                if (seconds < 5) timeLeft += "0";
                                timeLeft += seconds;
                                mCounter.setText("Expired in : " + timeLeft);
                            }

                            @Override
                            public void onFinish() {
                                mCounter.setText("Expired!");
                                mVerify.setEnabled(false);
                                mSendAgain.setVisibility(View.VISIBLE);
                                otpCode = 0000;
                            }
                        }.start();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String message = null;
                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
                    message = "No Internet Connection";
                    Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found. Please try again later";
                    Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();
                }
                pd.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("emailTo", emailId);
                map.put("otp", "" + otp);
                return map;
            }

            @Override
            protected Map<String, String> getPostParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("emailTo", emailId);
                map.put("otp", "" + otp);
                return map;
            }
        };

        RequestQueue rQeue = Volley.newRequestQueue(AuthActivity.this);
        rQeue.add(sr);

    }
}
