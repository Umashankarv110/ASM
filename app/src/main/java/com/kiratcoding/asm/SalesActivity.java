package com.kiratcoding.asm;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kiratcoding.asm.HelperClass.HttpsTrustManager;

public class SalesActivity extends AppCompatActivity {
    private Button Order_management_btn,Target_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);
        Toolbar toolbar = findViewById(R.id.sales_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sales");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        HttpsTrustManager.allowAllSSL();

        Order_management_btn = findViewById(R.id.order_manage_btn);
        Order_management_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog typeDialog = new Dialog(SalesActivity.this);
                typeDialog.setContentView(R.layout.layout_options);
                typeDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                typeDialog.setTitle("");
                typeDialog.setCancelable(true);
                typeDialog.show();

                Button b1 = typeDialog.findViewById(R.id.btnOption1);
                Button b2 = typeDialog.findViewById(R.id.btnOption2);
                Button b3 = typeDialog.findViewById(R.id.btnOption3);

                b1.setText("Add New Party");
                b2.setText("Feed Order");
                b3.setVisibility(View.GONE);

                b1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(), NewPartyActivity.class));
                        typeDialog.dismiss();
                    }
                });

                b2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(), AllPartiesActivity.class));
                        typeDialog.dismiss();
                    }
                });

            }
        });

        Target_btn = findViewById(R.id.target_btn);
        Target_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DailyTargetsActivity.class));
            }
        });

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