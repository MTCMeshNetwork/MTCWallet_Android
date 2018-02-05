package io.mtc.app.mtcwallet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuLayout;

import java.math.BigDecimal;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.data.WalletDisplayInfo;

/**
 * Created by admin on 2018/1/24.
 */

public class WalletDisplayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    ImageView iv_delete;
    TextView tv_focus;
    TextView tv_name;
    TextView tv_total_price;
    ImageView iv_move;

    private AdapterOnClickListener mOnClickListener;

    public WalletDisplayViewHolder(View itemView) {
        super(itemView);
        iv_delete = (ImageView) itemView.findViewById(R.id.iv_remove);
        tv_focus = (TextView) itemView.findViewById(R.id.tv_focus);
        tv_name = (TextView) itemView.findViewById(R.id.tv_name);
        tv_total_price = (TextView) itemView.findViewById(R.id.tv_total_price);
        iv_move = (ImageView) itemView.findViewById(R.id.iv_move);

        itemView.findViewById(R.id.content_view).setOnClickListener(this);
        iv_delete.setOnClickListener(this);
        itemView.findViewById(R.id.btn_manage).setOnClickListener(this);
    }

    public void bind(Context context, WalletDisplayInfo displayInfo) {
        tv_name.setText(displayInfo.walletInfo.name);

        BigDecimal totalPrice = displayInfo.getTotalPriceCNYReadable();
        if (totalPrice.compareTo(BigDecimal.ZERO) != 0) {
            tv_total_price.setText("≈￥ " + displayInfo.getTotalPriceCNYReadable());
        } else {
            tv_total_price.setText("");
        }
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
