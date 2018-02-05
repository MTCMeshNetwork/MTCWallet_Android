package io.mtc.app.mtcwallet.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuLayout;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;
import io.mtc.app.mtcwallet.effect.CircleTransform;
import io.mtc.app.mtcwallet.effect.RoundTransform;

/**
 * Created by admin on 2018/1/23.
 */

public class WalletCoinViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    ImageView iv_icon;
    TextView tv_name;
    TextView tv_ether;
    TextView tv_price;

    private AdapterOnClickListener mOnClickListener;

    public WalletCoinViewHolder(View itemView) {
        super(itemView);
        iv_icon = (ImageView) itemView.findViewById(R.id.iv_icon);
        tv_name = (TextView) itemView.findViewById(R.id.tv_name);
        tv_ether = (TextView) itemView.findViewById(R.id.tv_ether);
        tv_price = (TextView) itemView.findViewById(R.id.tv_price);

        itemView.findViewById(R.id.content_view).setOnClickListener(this);
        itemView.findViewById(R.id.btn_send).setOnClickListener(this);
        itemView.findViewById(R.id.btn_receive).setOnClickListener(this);
    }

    public void bind(Context context, WalletCoinInfo info) {
        try {
            Picasso.with(context).load(Uri.parse(info.imageURL))
                    .resize(128, 128)
                    .error(R.drawable.ic_eth)
                    .transform(new CircleTransform(context))
                    .into(iv_icon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        tv_name.setText(info.unit_name);
        tv_ether.setText("Ξ " + info.getBalanceReadable());
        tv_price.setText("≈￥ " + info.getTotalPriceCNYReadable());
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
