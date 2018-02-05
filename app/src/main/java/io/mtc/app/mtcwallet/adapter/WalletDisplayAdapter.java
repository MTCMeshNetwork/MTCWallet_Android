package io.mtc.app.mtcwallet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.data.EthWalletDisplay;
import io.mtc.app.mtcwallet.data.WalletDisplayInfo;

/**
 * Created by admin on 2018/1/24.
 */

public class WalletDisplayAdapter extends RecyclerView.Adapter<WalletDisplayViewHolder> {

    private Context mContext;
    private List<WalletDisplayInfo> mObjects;
    private AdapterOnClickListener mOnClickListener;

    private int mEditState = 0;

    public WalletDisplayAdapter(Context context, List<WalletDisplayInfo> objects) {
        this.mContext = context;
        this.mObjects = objects;
    }

    @Override
    public WalletDisplayViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new WalletDisplayViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_wallet_display_menu, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(WalletDisplayViewHolder walletDisplayViewHolder, int i) {
        WalletDisplayInfo info = mObjects.get(i);
        walletDisplayViewHolder.bind(mContext, info);
        if (mEditState == 0) {
            walletDisplayViewHolder.iv_delete.setVisibility(View.GONE);
            walletDisplayViewHolder.iv_move.setVisibility(View.GONE);
        } else {
            walletDisplayViewHolder.iv_delete.setVisibility(View.VISIBLE);
            walletDisplayViewHolder.iv_move.setVisibility(View.VISIBLE);
        }
        walletDisplayViewHolder.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    public void setClickListener(AdapterOnClickListener listener) {
        this.mOnClickListener = listener;
        notifyDataSetChanged();
    }

    public void setEditState(int state) {
        this.mEditState = state;
        notifyDataSetChanged();
    }

    public int getEditState() {
        return mEditState;
    }

    public void remove(String wallet_address) {
        WalletDisplayInfo walletDisplayInfo = null;
        for (int index = 0; index < mObjects.size(); ++index) {
            walletDisplayInfo = mObjects.get(index);
            if (walletDisplayInfo.walletInfo.address.equals(wallet_address)) {
                break;
            }
        }
        mObjects.remove(walletDisplayInfo);
        notifyDataSetChanged();
    }

}
