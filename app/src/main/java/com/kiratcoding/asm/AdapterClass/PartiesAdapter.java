package com.kiratcoding.asm.AdapterClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kiratcoding.asm.ModelsClass.Parties;
import com.kiratcoding.asm.R;

import java.util.List;

public class PartiesAdapter extends ArrayAdapter<Parties> {
    Context context;
    List<Parties> arrayListParties;

    public PartiesAdapter(@NonNull Context context, List<Parties> arrayListParties) {
        super(context, R.layout.layout_parties, arrayListParties);
        this.context = context;
        this.arrayListParties = arrayListParties;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_parties,null,true);
        TextView tvID = view.findViewById(R.id.tv_party_id);
        TextView tvStortname = view.findViewById(R.id.tv_short_name);
        TextView tvName = view.findViewById(R.id.tv_party_name);

        tvID.setText(""+arrayListParties.get(position).getId());
        tvStortname.setText(arrayListParties.get(position).getName().substring(0,1));
        tvName.setText(arrayListParties.get(position).getName());
        return view;
    }
}
