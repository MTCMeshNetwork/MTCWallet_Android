package io.mtc.app.mtcwallet.dialog;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.mtc.app.mtcwallet.R;

public class WalletManageDialogFragment extends DialogFragment implements View.OnClickListener {

    public final static String KEY_WALLET_ADDRESS = "key_wallet_address";

    private DialogFragmentCallback callback;

    private TextView tvTip;

    private View btnDelete;
    private View btnExport;
    private TextView btnCancel;

    public WalletManageDialogFragment() {

    }

    public static WalletManageDialogFragment newInstance(String wallet_address) {
        WalletManageDialogFragment fragment = new WalletManageDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_WALLET_ADDRESS, wallet_address);
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
        View rootView = inflater.inflate(R.layout.fragment_wallet_manage_dialog, container, false);

        btnDelete = rootView.findViewById(R.id.btn_delete);
        btnExport = rootView.findViewById(R.id.btn_export);
        btnCancel = (TextView) rootView.findViewById(R.id.btn_cancel);
        tvTip = (TextView) rootView.findViewById(R.id.tv_tip);

        tvTip.setText(Html.fromHtml("钱包删除后，<font color='#FF6600'>将无法撤消操作</font>，请确保已备份钱包私钥！\n"));

        btnDelete.setOnClickListener(this);
        btnExport.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

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
        callback.onDialogFragmentClick(this, v);
        dismiss();
    }


}
