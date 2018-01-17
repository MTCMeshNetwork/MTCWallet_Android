package io.mtc.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by admin on 2018/1/4.
 */

public class AppPref {
    private final static String APP_PREF = "app_preferences";

    public final static String KEY_ETH_WALLET_ADDRESS = "key_eth_wallet_address";

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public AppPref(Context context) {
        sp = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public String getWalletAddress() {
        return sp.getString(KEY_ETH_WALLET_ADDRESS, null);
    }

    public void setWalletAddress(String walletAddress) {
        if (TextUtils.isEmpty(walletAddress)) {
            editor.remove(KEY_ETH_WALLET_ADDRESS);
        } else {
            editor.putString(KEY_ETH_WALLET_ADDRESS, walletAddress);
        }
        editor.commit();
    }

}
