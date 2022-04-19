package com.kiratcoding.asm.AdapterClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kiratcoding.asm.ModelsClass.Attendance;
import com.kiratcoding.asm.R;

import java.util.List;

public class CalendarAdapter extends ArrayAdapter<Attendance> {
    Context context;
    List<Attendance> arrayListAttendance;

    public CalendarAdapter(@NonNull Context context, List<Attendance> arrayListAttendance) {
        super(context, R.layout.c_layout, arrayListAttendance);
        this.context = context;
        this.arrayListAttendance = arrayListAttendance;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.c_layout,null,true);
        TextView tv_day = view.findViewById(R.id.dayNum);
        TextView tv_dayName = view.findViewById(R.id.dayName);
        TextView tv_monthName = view.findViewById(R.id.monthName);
        TextView tv_year = view.findViewById(R.id.monthYr);
        TextView tv_status = view.findViewById(R.id.attendanceStatus);
        TextView tv_task = view.findViewById(R.id.attendanceTask);

        tv_day.setText("" + arrayListAttendance.get(position).getDay());
        tv_dayName.setText("" + arrayListAttendance.get(position).getDayname());
        tv_status.setText("Status: " + arrayListAttendance.get(position).getStatus());
        tv_task.setText("Note: " + arrayListAttendance.get(position).getNote());
        tv_monthName.setText("" + arrayListAttendance.get(position).getMonth());
        tv_year.setText("" + arrayListAttendance.get(position).getYear());


        return view;
    }
}
