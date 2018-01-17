package io.mtc.app.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.mtc.app.R;

public class EthWalletExportDialogFragment extends DialogFragment implements View.OnClickListener {

    private DialogFragmentCallback callback;

    public EthWalletExportDialogFragment() {

    }

    public static EthWalletExportDialogFragment newInstance() {
        EthWalletExportDialogFragment fragment = new EthWalletExportDialogFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_eth_wallet_export_dialog, container, false);

        rootView.findViewById(R.id.tv_keystore).setOnClickListener(this);
        rootView.findViewById(R.id.tv_private_key).setOnClickListener(this);
        rootView.findViewById(R.id.btn_cancel).setOnClickListener(this);

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
        if (callback != null) {
            callback.onDialogFragmentClick(this, v);
        }
        dismiss();
    }
}
