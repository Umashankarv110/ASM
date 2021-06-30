package com.kiratcoding.asm.AdapterClass;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.kiratcoding.asm.ModelsClass.Leaves;
import com.kiratcoding.asm.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LeavesAdapter extends ArrayAdapter<Leaves> {
    Context context;
    List<Leaves> arrayListLeaves;

    public LeavesAdapter(@NonNull Context context, List<Leaves> arrayListLeaves) {
        super(context, R.layout.layout_leave_status, arrayListLeaves);
        this.context = context;
        this.arrayListLeaves = arrayListLeaves;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_leave_status,null,true);
        TextView tv_l_id = view.findViewById(R.id.tv_leaveId);
        TextView tv_date = view.findViewById(R.id.tv_leaveDate);
        TextView tv_status = view.findViewById(R.id.tv_leaveStatus);
        TextView tv_reason = view.findViewById(R.id.tv_leave_reason);
        TextView tv_start_date = view.findViewById(R.id.tv_leave_from);
        TextView tv_close_date = view.findViewById(R.id.tv_leave_to);
        TextView tv_day_count = view.findViewById(R.id.tv_leave_total);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(arrayListLeaves.get(position).getFromDate(),formatter);
        LocalDate end = LocalDate.parse(arrayListLeaves.get(position).getToDate(),formatter);
        int days = (int) (ChronoUnit.DAYS.between(start, end)+1);

        if(arrayListLeaves.get(position).getStatus() == 0){
            tv_status.setText("Not Approved");
            tv_status.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
            tv_day_count.setText(": " + days+" Days Leave Not Approved Yet");
        }else if(arrayListLeaves.get(position).getStatus() == 1){
            tv_status.setText("Approved");
            tv_status.setTextColor(ContextCompat.getColor(context, R.color.successGreen));
            tv_day_count.setText(": " + days+ " Days Leave Approved");
        }

        tv_l_id.setText("ID: "+arrayListLeaves.get(position).getId());
        tv_date.setText("Applied Date : " + arrayListLeaves.get(position).getTimestamp());
        tv_reason.setText(": " + arrayListLeaves.get(position).getReason());
        tv_start_date.setText(": " + arrayListLeaves.get(position).getFromDate());
        tv_close_date.setText(": " + arrayListLeaves.get(position).getToDate());
        return view;
    }
}
