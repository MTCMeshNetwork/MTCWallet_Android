package io.mtc.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import io.mtc.app.R;
import io.mtc.app.data.EthWalletDisplay;

import java.util.List;

/**
 * Created by admin on 2018/1/7.
 */

public class EthWalletDisplayAdapter extends ArrayAdapter<EthWalletDisplay> {

    public interface EthWalletDisplayAdapterListener {
        void onClick(int id, EthWalletDisplay ethWalletDisplay);
    }

    public final static int STATE_NORMAL = 0;
    public final static int STATE_EDIT = 1;
    private int viewState = STATE_NORMAL;

    private EthWalletDisplayAdapterListener listener;

    public EthWalletDisplayAdapter(@NonNull Context context, List<EthWalletDisplay> walletList, EthWalletDisplayAdapterListener listener) {
        super(context, R.layout.item_wallet_display, R.id.tv_name, walletList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rootView = super.getView(position, convertView, parent);
        View ivRemove = rootView.findViewById(R.id.iv_remove);
        TextView tvName = (TextView) rootView.findViewById(R.id.tv_name);
        TextView tvTotalPrice = (TextView) rootView.findViewById(R.id.tv_total_price);

        ivRemove.setVisibility(viewState == STATE_EDIT ? View.VISIBLE: View.GONE);
        final EthWalletDisplay walletDisplay = getItem(position);
        tvName.setText(walletDisplay.walletInfo.name);
        tvTotalPrice.setText("---");

        ivRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(R.id.iv_remove, walletDisplay);
                }
            }
        });

        return rootView;
    }

    public void setState(int newState) {
        viewState = newState;
        notifyDataSetChanged();
    }

    public void remove(String wallet_address) {
        EthWalletDisplay ethWalletDisplay = null;
        for (int index = 0; index < getCount(); ++index) {
            ethWalletDisplay = getItem(index);
            if (ethWalletDisplay.walletInfo.address.equals(wallet_address)) {
                break;
            }
        }
        remove(ethWalletDisplay);
    }



}
