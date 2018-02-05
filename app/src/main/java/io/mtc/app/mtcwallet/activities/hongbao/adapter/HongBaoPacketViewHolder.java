package io.mtc.app.mtcwallet.activities.hongbao.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.adapter.AdapterOnClickListener;

/**
 * Created by admin on 2018/2/3.
 */

public class HongBaoPacketViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView tvName;
    TextView tvDateTime;
    TextView tvInfo;
    Button btnGet;

    AdapterOnClickListener mOnClickListener;

    public HongBaoPacketViewHolder(View itemView) {
        super(itemView);
        tvName = (TextView)itemView.findViewById(R.id.tv_name);
        tvDateTime = (TextView)itemView.findViewById(R.id.tv_datetime);
        tvInfo = (TextView)itemView.findViewById(R.id.tv_info);
        btnGet = (Button)itemView.findViewById(R.id.btn_get);
        btnGet.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(v, getAdapterPosition());
        }
    }
}
