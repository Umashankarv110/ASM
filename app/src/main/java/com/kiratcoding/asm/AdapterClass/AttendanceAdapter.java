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

public class AttendanceAdapter extends ArrayAdapter<Attendance> {
    Context context;
    List<Attendance> arrayListAttendance;

    public AttendanceAdapter(@NonNull Context context, List<Attendance> arrayListAttendance) {
        super(context, R.layout.layout_view_attendance, arrayListAttendance);
        this.context = context;
        this.arrayListAttendance = arrayListAttendance;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_view_attendance,null,true);
        TextView tv_a_id = view.findViewById(R.id.attendance_id);
        TextView tv_vehicle = view.findViewById(R.id.attendance_vehicle_type);
        TextView tv_status = view.findViewById(R.id.attendance_status);
        TextView tv_notes = view.findViewById(R.id.attendance_notes);
        TextView tv_checkin_from = view.findViewById(R.id.attendance_from);
        TextView tv_checkin_to = view.findViewById(R.id.attendance_to);
        TextView tv_checkin_time = view.findViewById(R.id.attendance_checkin_time);
        TextView tv_checkin_reading = view.findViewById(R.id.attendance_checkin_reading);
        TextView tv_checkout_time = view.findViewById(R.id.attendance_checkout_time);
        TextView tv_checkout_reading = view.findViewById(R.id.attendance_checkout_reading);
        TextView tvDistance = view.findViewById(R.id.attendance_traveled_distance);
        TextView tv_distance_amt = view.findViewById(R.id.tv_distance_amt);

        if(arrayListAttendance.get(position).getVehicleName().equalsIgnoreCase("Public Transport")) {
            tv_a_id.setText("" + arrayListAttendance.get(position).getId());
            tv_vehicle.setText("" + arrayListAttendance.get(position).getVehicleName());
            tv_status.setText("Status: " + arrayListAttendance.get(position).getStatus());
            tv_notes.setText("Note: " + arrayListAttendance.get(position).getNote());
            tv_checkin_time.setText("Check-In at " + arrayListAttendance.get(position).getStartTime());
            tv_checkin_reading.setText("From : " + arrayListAttendance.get(position).getFromLocation());
            tv_checkout_reading.setText("To : " + arrayListAttendance.get(position).getToLocation());
            tv_checkin_from.setVisibility(View.GONE);
            tv_checkin_to.setVisibility(View.GONE);
            tv_distance_amt.setText("Amount:");

            if(arrayListAttendance.get(position).getStatus().equalsIgnoreCase("CheckIn")){
                tv_checkout_time.setText("Not Check-Out yet ");
                tvDistance.setText("₹0.0/-");
            }else{
                tv_checkout_time.setText("Check-Out at " + arrayListAttendance.get(position).getCloseTime());
                tvDistance.setText("₹" +arrayListAttendance.get(position).getAmount()+ "/-");
            }

        }
        else {
            tv_a_id.setText("" + arrayListAttendance.get(position).getId());
            tv_vehicle.setText("" + arrayListAttendance.get(position).getVehicleName());
            tv_status.setText("Status: " + arrayListAttendance.get(position).getStatus());
            tv_notes.setText("Note: " + arrayListAttendance.get(position).getNote());
            tv_checkin_time.setText("Check-In at " + arrayListAttendance.get(position).getStartTime());
            tv_checkin_reading.setText("Reading: " + arrayListAttendance.get(position).getStartReading() + " km");
            tv_checkin_from.setText("From "+arrayListAttendance.get(position).getFromLocation());
            tv_checkin_to.setText("To "+arrayListAttendance.get(position).getToLocation());
            tv_distance_amt.setText("Distance Travelled :");

            if(arrayListAttendance.get(position).getStatus().equalsIgnoreCase("CheckIn")){
                tv_checkout_time.setText("Not Check-Out yet ");
                tv_checkout_reading.setText("Reading: 0.0 km");
                tvDistance.setText("0.0 km");
            }else{
                tv_checkout_time.setText("Check-Out at " + arrayListAttendance.get(position).getCloseTime());
                tv_checkout_reading.setText("Reading: " + arrayListAttendance.get(position).getCloseReading() + " km");
                tvDistance.setText("" +arrayListAttendance.get(position).getDistance() + " km");
            }

        }
        return view;
    }
}
