package com.kiratcoding.asm.AdapterClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kiratcoding.asm.ModelsClass.Feeds;
import com.kiratcoding.asm.R;

import java.util.List;

public class FeedAdapter extends ArrayAdapter<Feeds> {
    Context context;
    List<Feeds> arrayListFeeds;
    public FeedAdapter(@NonNull Context context, List<Feeds> arrayListFeeds) {
        super(context, R.layout.layout_feeds, arrayListFeeds);
        this.context = context;
        this.arrayListFeeds = arrayListFeeds;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_feeds,null,true);
        TextView tvfeedCount = view.findViewById(R.id.tv_feed_count);
        TextView tvfeedID = view.findViewById(R.id.tv_feed_id);
        TextView tvName = view.findViewById(R.id.tv_feedTitle);
        TextView tvPrice = view.findViewById(R.id.tv_feed_price);

        int serialNo = position+1;
        tvfeedCount.setText(""+serialNo);
        tvfeedID.setText(""+arrayListFeeds.get(position).getId());
        tvPrice.setText("Price: "+arrayListFeeds.get(position).getSellprice());
        tvName.setText(arrayListFeeds.get(position).getName());
        return view;
    }
}
