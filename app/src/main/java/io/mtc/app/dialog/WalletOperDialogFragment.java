package io.mtc.app.dialog;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.mtc.app.R;

public class WalletOperDialogFragment extends DialogFragment {

    public WalletOperDialogFragment() {

    }

    public static WalletOperDialogFragment newInstance() {
        WalletOperDialogFragment fragment = new WalletOperDialogFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_wallet_oper_dialog, container, false);

        return rootView;
    }

}
