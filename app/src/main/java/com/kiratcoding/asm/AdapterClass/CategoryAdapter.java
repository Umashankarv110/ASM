package com.kiratcoding.asm.AdapterClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kiratcoding.asm.ModelsClass.FeedCategories;
import com.kiratcoding.asm.R;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<FeedCategories> {
    Context context;
    List<FeedCategories> categoriesList;

    public CategoryAdapter(@NonNull Context context, List<FeedCategories> categoriesList) {
        super(context, R.layout.layout_vehicle, categoriesList);
        this.context = context;
        this.categoriesList = categoriesList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_vehicle,null,true);
        TextView tvID = view.findViewById(R.id.tv_vehicle_id);
        TextView tvName = view.findViewById(R.id.tv_vehicle_Title);

        tvID.setText(""+categoriesList.get(position).getId());
        tvName.setText(categoriesList.get(position).getName());
        return view;
    }
}
