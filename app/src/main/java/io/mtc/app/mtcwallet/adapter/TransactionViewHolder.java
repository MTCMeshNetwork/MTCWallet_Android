package io.mtc.app.mtcwallet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuLayout;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.database.TransactionInfoV2;
import io.mtc.app.mtcwallet.utils.MTCWalletUtils;

/**
 * Created by admin on 2018/1/26.
 */

public class TransactionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

    TextView tv_address;
    TextView tv_balance;
    TextView tv_unit_name;
    TextView tv_datetime;
    TextView tv_state;
    private AdapterOnClickListener mOnClickListener;

    public TransactionViewHolder(View itemView) {
        super(itemView);
        tv_address = (TextView) itemView.findViewById(R.id.tv_address);
        tv_balance = (TextView) itemView.findViewById(R.id.tv_balance);
        tv_unit_name = (TextView) itemView.findViewById(R.id.tv_unit_name);
        tv_datetime = (TextView) itemView.findViewById(R.id.tv_datetime);
        tv_state = (TextView) itemView.findViewById(R.id.tv_state);

        itemView.findViewById(R.id.content_view).setOnClickListener(this);
    }

    public void bind(Context context, TransactionInfoV2 info) {
        tv_address.setText(MTCWalletUtils.getPrefixAddress(info.to));
        tv_balance.setText(info.getTokenCount().toPlainString());
        tv_datetime.setText(String.valueOf(info.time));
        String status_str;
        switch (info.status) {
            case 0: {
                status_str = "处理中";
                break;
            }

            case 1: {
                status_str = "交易成功";
                break;
            }

            case 2: {
                status_str = "交易失败";
                break;
            }

            default:
                status_str = "code:" + info.status;
        }
        tv_state.setText(status_str);
    }


    @Override
    public void onClick(View v) {
        SwipeMenuLayout swipeRefreshLayout = (SwipeMenuLayout)itemView;
        swipeRefreshLayout.smoothCloseMenu();
        if (mOnClickListener != null) {
            mOnClickListener.onClick(v, getAdapterPosition());
        }
    }

    public void setOnClickListener(AdapterOnClickListener listener) {
        this.mOnClickListener = listener;
    }

}
