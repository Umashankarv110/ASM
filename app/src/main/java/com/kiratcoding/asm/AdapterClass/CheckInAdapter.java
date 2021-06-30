package com.kiratcoding.asm.AdapterClass;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kiratcoding.asm.ModelsClass.Attendance;
import com.kiratcoding.asm.R;

import java.util.List;

public class CheckInAdapter extends ArrayAdapter<Attendance> {

    //storing all the names in the list
    private List<Attendance> checkIns;

    //context object
    private Context context;

    //constructor
    public CheckInAdapter(Context context, int resource, List<Attendance> checkIns) {
        super(context, resource, checkIns);
        this.context = context;
        this.checkIns = checkIns;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //getting the layoutinflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //getting listview itmes
        View listViewItem = inflater.inflate(R.layout.layout_checkin, null, true);
        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);
        ImageView imageViewStatus = (ImageView) listViewItem.findViewById(R.id.imageViewStatus);

        //getting the current name
        Attendance checkIn = checkIns.get(position);

        //setting the name to textview
        textViewName.setText(
                "status:"+checkIn.getStatus()+"\n" +
                "vehicleId:"+checkIn.getVehicleId()+"\n" +
                "uniquenumber:"+checkIn.getUniquenumber()+"\n" +
                "employeeName:"+checkIn.getEmployeeName()+"\n" +
                "Date | Time:"+checkIn.getCurrentDate()+"|"+checkIn.getStartTime()+"\n"+
                "Latitude|Longitude:"+checkIn.getStartLatitude()+"|"+checkIn.getStartLongitude()+"\n"+
                "startReading:"+checkIn.getStartReading()+"\n" +
                "note:"+checkIn.getNote()+"\n" +
                "fromLocation:"+checkIn.getFromLocation()+"\n" +
                "toLocation:"+checkIn.getToLocation()+"\n" +
                "Uploaded: "+checkIn.getUpload_status()
        );

        //if the synced status is 0 displaying
        //queued icon
        //else displaying synced icon
        if (checkIn.getUpload_status() == 0) {
            imageViewStatus.setBackgroundResource(R.drawable.wait);
        }else {
            imageViewStatus.setBackgroundResource(R.drawable.done);
        }
        return listViewItem;
    }
}