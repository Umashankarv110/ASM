package com.kiratcoding.asm;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.kiratcoding.asm.AdapterClass.TabAdapter;
import com.kiratcoding.asm.ui.fragment.DailyTargetsFragment;
import com.kiratcoding.asm.ui.fragment.MonthlyTargetsFragment;


public class DailyTargetsActivity extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_targets);
        Toolbar toolbar = findViewById(R.id.daily_target_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daily Targets");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

    }

    @Override
    protected void onStart() {
        super.onStart();
        getTabLayout();
    }

    private void getTabLayout() {
        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {}

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setUpViewPager(ViewPager viewPager) {
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new DailyTargetsFragment(),"DailyTargets");
        adapter.addFragment(new MonthlyTargetsFragment(),"MonthlyTargets");
        viewPager.setAdapter(adapter);
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
