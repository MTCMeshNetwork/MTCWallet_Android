package io.mtc.app.mtcwallet.activities.hongbao.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.adapter.AdapterOnClickListener;
import io.mtc.app.mtcwallet.network.hongbao.PacketInfo;

/**
 * Created by admin on 2018/2/3.
 */

public class HongBaoPacketAdapter extends RecyclerView.Adapter<HongBaoPacketViewHolder> {

    protected Context mContext;
    protected List<PacketInfo> mObjects;
    private AdapterOnClickListener mOnClickListener;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public HongBaoPacketAdapter(Context context, List<PacketInfo> objects) {
        mContext = context;
        mObjects = objects;
    }

    @Override
    public HongBaoPacketViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HongBaoPacketViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_hongbao_nearby_menu, parent, false));
    }

    @Override
    public void onBindViewHolder(HongBaoPacketViewHolder holder, int position) {
        holder.mOnClickListener = mOnClickListener;
        PacketInfo packetInfo = mObjects.get(position);
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTimeInMillis(packetInfo.createdTime);
        holder.tvName.setText(packetInfo.title);
        holder.tvDateTime.setText(sdf.format(calendar.getTime()));
        holder.tvInfo.setText(String.valueOf(packetInfo.quantity));
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

}
