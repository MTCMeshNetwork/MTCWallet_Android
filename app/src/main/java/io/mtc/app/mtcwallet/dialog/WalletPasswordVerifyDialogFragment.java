package io.mtc.app.mtcwallet.dialog;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.mtc.app.mtcwallet.R;

public class WalletPasswordVerifyDialogFragment extends Fragment {

    public final static String KEY_WALLET_ADDRESS = "key_wallet_address";

    public WalletPasswordVerifyDialogFragment() {

    }

    public static WalletPasswordVerifyDialogFragment newInstance(String param1, String param2) {
        WalletPasswordVerifyDialogFragment fragment = new WalletPasswordVerifyDialogFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet_password_verify_dialog, container, false);
    }

}
