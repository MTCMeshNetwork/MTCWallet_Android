package io.mtc.app.mtcwallet.dialog;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;

import io.mtc.app.mtcwallet.R;

public class EthSendConfirmDialogFragment extends DialogFragment implements View.OnClickListener {

    public final static String KEY_INFO = "key_info";

    private DialogFragmentCallback callback;

    private View btnOK;
    private String info;

    public EthSendConfirmDialogFragment() {

    }

    public static EthSendConfirmDialogFragment newInstance(String info) {
        EthSendConfirmDialogFragment fragment = new EthSendConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_INFO, info);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            info = getArguments().getString(KEY_INFO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eth_send_confirm_dialog, container, false);

        TextView tvInfo = (TextView) rootView.findViewById(R.id.tv_info);
        tvInfo.setText(info);

        btnOK = rootView.findViewById(R.id.btn_ok);
        btnOK.setOnClickListener(this);

        rootView.findViewById(R.id.iv_close).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() instanceof DialogFragmentCallback) {
            callback = (DialogFragmentCallback) getTargetFragment();
        } else if (getActivity() instanceof DialogFragmentCallback) {
            callback = (DialogFragmentCallback) getActivity();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_ok: {
                btnOK.setEnabled(false);
                break;
            }
        }

        dismiss();

        if (callback != null) {
            callback.onDialogFragmentClick(this, v);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (callback != null) {
            callback.onDialogFragmentCancel(this);
        }
    }
}
