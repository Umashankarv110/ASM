package com.kiratcoding.asm.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.kiratcoding.asm.AttendanceOptionActivity;
import com.kiratcoding.asm.AttendanceReportActivity;
import com.kiratcoding.asm.LeaveStatusActivity;
import com.kiratcoding.asm.MonthlyCalenderActivity;
import com.kiratcoding.asm.SalesActivity;
import com.kiratcoding.asm.SharedPreferencesClass.SharedPrefLogin;
import com.kiratcoding.asm.ModelsClass.Employee;
import com.kiratcoding.asm.R;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

public class HomeFragment extends Fragment {

    private TextView TitleWelcome;
    ConstraintLayout attendanceLayout, salesLayout, viewAttendanceLayout, leaveLayout,viewMonthAttendanceLayout;

    CarouselView carouselView;
    int[] simpleImages = {R.drawable.feeds2};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        //getting the current user
        Employee employee = SharedPrefLogin.getInstance(getActivity()).getUser();

        TitleWelcome = root.findViewById(R.id.text_home);
        salesLayout = root.findViewById(R.id.sales);
        attendanceLayout = root.findViewById(R.id.attendance);
        viewAttendanceLayout = root.findViewById(R.id.viewAttendance);
        viewMonthAttendanceLayout = root.findViewById(R.id.viewMonthAttendance);
        leaveLayout = root.findViewById(R.id.viewLeaves);

        attendanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AttendanceOptionActivity.class);
                intent.putExtra("successMsg", "attendance");
                startActivity(intent);
            }
        });

        viewMonthAttendanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MonthlyCalenderActivity.class);
                startActivity(intent);
            }
        });

        viewAttendanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AttendanceReportActivity.class);
                startActivity(intent);
            }
        });

        salesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SalesActivity.class);
                startActivity(intent);
            }
        });

        leaveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LeaveStatusActivity.class);
                startActivity(intent);
            }
        });

        TitleWelcome.setText("Welcome "+String.valueOf(employee.getName()));

        carouselView = root.findViewById(R.id.carouselview);
        carouselView.setPageCount(simpleImages.length);
        carouselView.setImageListener(imageListener);

        return root;
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(simpleImages[position]);
        }
    };

}