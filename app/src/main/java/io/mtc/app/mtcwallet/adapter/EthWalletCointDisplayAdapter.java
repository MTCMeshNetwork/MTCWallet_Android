package io.mtc.app.mtcwallet.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.List;

import io.mtc.app.mtcwallet.data.WalletCoinInfo;

/**
 * Created by admin on 2018/1/22.
 */

public class EthWalletCointDisplayAdapter extends ArrayAdapter<WalletCoinInfo> {

    public EthWalletCointDisplayAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<WalletCoinInfo> objects) {
        super(context, resource, textViewResourceId, objects);
    }

}
