package com.kiratcoding.asm.AdapterClass;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kiratcoding.asm.ModelsClass.Vehicles;
import com.kiratcoding.asm.R;

import java.util.List;

public class OfflineVehicleAdapter extends ArrayAdapter<Vehicles> {

    //storing all the names in the list
    private List<Vehicles> vehiclesList;

    //context object
    private Context context;

    //constructor
    public OfflineVehicleAdapter(Context context, int resource, List<Vehicles> vehiclesList) {
        super(context, resource, vehiclesList);
        this.context = context;
        this.vehiclesList = vehiclesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //getting the layoutinflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //getting listview itmes
        View listViewItem = inflater.inflate(R.layout.layout_vehicle, null, true);
        TextView tvID = listViewItem.findViewById(R.id.tv_vehicle_id);
        TextView tvName = listViewItem.findViewById(R.id.tv_vehicle_Title);
        TextView tvType = listViewItem.findViewById(R.id.tv_vehicle_type);

        //getting the current name
        Vehicles vehicles = vehiclesList.get(position);

        //setting the name to textview
        tvID.setText(""+vehicles.getId());
        tvName.setText(""+ vehicles.getType());

        return listViewItem;
    }
}