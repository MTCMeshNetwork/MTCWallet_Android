package io.mtc.app.mtcwallet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;

/**
 * Created by admin on 2018/1/23.
 */

public class WalletCoinAdapter extends RecyclerView.Adapter<WalletCoinViewHolder> {

    private Context mContext;
    private List<WalletCoinInfo> mObjects;
    private AdapterOnClickListener mOnClickListener;

    public WalletCoinAdapter(Context context, List<WalletCoinInfo> objects) {
        this.mContext = context;
        this.mObjects = objects;
    }

    @Override
    public WalletCoinViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new WalletCoinViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_wallet_coin_menu, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(WalletCoinViewHolder walletCoinViewHolder, int i) {
        WalletCoinInfo walletCoinInfo = mObjects.get(i);
        walletCoinViewHolder.bind(mContext, walletCoinInfo);
        walletCoinViewHolder.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    public void setClickListener(AdapterOnClickListener listener) {
        this.mOnClickListener = listener;
        notifyDataSetChanged();
    }

}
