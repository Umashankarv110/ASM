package com.kiratcoding.asm.AdapterClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kiratcoding.asm.ModelsClass.TempOrder;
import com.kiratcoding.asm.R;

import java.util.List;

public class TempOrderAdapter extends ArrayAdapter<TempOrder> {
    Context context;
    List<TempOrder> arrayTempOrder;
    public TempOrderAdapter(@NonNull Context context, List<TempOrder> arrayTempOrder) {
        super(context, R.layout.layout_temp_order, arrayTempOrder);
        this.context = context;
        this.arrayTempOrder = arrayTempOrder;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_temp_order,null,true);
        TextView tvfeedCount = view.findViewById(R.id.tv_feed_oid);
        TextView tvfeedID = view.findViewById(R.id.tv_orderFeedId);
        TextView tvName = view.findViewById(R.id.tv_orderTitle);
        TextView tvPrice = view.findViewById(R.id.tv_order_price);
        TextView tvQty = view.findViewById(R.id.tv_feedQty);
        TextView tvTotal = view.findViewById(R.id.tv_feedTotalAmt);

        int serialNo = position+1;
        tvfeedCount.setText(""+serialNo);
        tvfeedID.setText("Feed Id: "+arrayTempOrder.get(position).getFeedId());
        tvPrice.setText("Price: ₹"+arrayTempOrder.get(position).getFeedprice());
        tvQty.setText("Qty: "+arrayTempOrder.get(position).getFeedqty());
        tvName.setText(arrayTempOrder.get(position).getFeed());
        tvTotal.setText("Amt: ₹"+arrayTempOrder.get(position).getFeedprice() * arrayTempOrder.get(position).getFeedqty());
        return view;
    }
}
