package com.kiratcoding.asm.AdapterClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kiratcoding.asm.ModelsClass.Notification;
import com.kiratcoding.asm.R;

import java.util.List;

public class NotificationAdapter extends ArrayAdapter<Notification> {
    Context context;
    List<Notification> arrayListNotification;

    public NotificationAdapter(@NonNull Context context, List<Notification> arrayListNotification) {
        super(context, R.layout.layout_notification, arrayListNotification);
        this.context = context;
        this.arrayListNotification = arrayListNotification;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification,null,true);

        TextView tvStortname = view.findViewById(R.id.Notifytitle);
        TextView tvName = view.findViewById(R.id.NotifyDetails);
        TextView tvDate = view.findViewById(R.id.NotifyTime);

        tvStortname.setText(arrayListNotification.get(position).getTitle());
        tvName.setText(arrayListNotification.get(position).getMessage());
        tvDate.setText(arrayListNotification.get(position).getDate()+" at "+arrayListNotification.get(position).getTime());
        return view;
    }
}
