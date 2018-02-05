package io.mtc.app.mtcwallet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.database.TransactionInfoV2;

/**
 * Created by admin on 2018/1/26.
 */

public class TransactionAdapter extends RecyclerView.Adapter<TransactionViewHolder> {

    private Context mContext;
    private List<TransactionInfoV2> mObjects;
    private AdapterOnClickListener mOnClickListener;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Calendar calendar = Calendar.getInstance();
    private String unit_name = "";
    private String walletAddress = "";

    public TransactionAdapter(Context context, List<TransactionInfoV2> objects) {
        this.mContext = context;
        this.mObjects = objects;
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TransactionViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_transaction_display_menu, parent, false));
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {
        TransactionInfoV2 info = mObjects.get(position);
        holder.bind(mContext, info);
        if (walletAddress.equalsIgnoreCase(info.from)) {
            holder.tv_balance.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryRed));
            holder.tv_balance.setText("-" + holder.tv_balance.getText());
        } else {
            holder.tv_balance.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryGreen));
        }
        calendar.setTimeInMillis(info.time);
        holder.tv_unit_name.setText(unit_name);
        holder.tv_datetime.setText(formatter.format(calendar.getTime()));
        holder.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    public void setClickListener(AdapterOnClickListener listener) {
        this.mOnClickListener = listener;
        notifyDataSetChanged();
    }

    public void setUnitName(String name) {
        this.unit_name = name;
        notifyDataSetChanged();
    }

    public String getUnitName() {
        return this.unit_name;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
}
