package io.mtc.app.dialog;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.mtc.app.R;

public class WalletDeleteConfirmDialogFragment extends DialogFragment implements View.OnClickListener {

    public final static String KEY_WALLET_ADDRESS = "key_wallet_address";

    private DialogFragmentCallback callback;

    private View btnDelete;
    private View btnExport;

    public WalletDeleteConfirmDialogFragment() {

    }

    public static WalletDeleteConfirmDialogFragment newInstance(String wallet_address) {
        WalletDeleteConfirmDialogFragment fragment = new WalletDeleteConfirmDialogFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_wallet_delete_confirm_dialog, container, false);

        btnDelete = rootView.findViewById(R.id.btn_delete);
        btnExport = rootView.findViewById(R.id.btn_export);

        btnDelete.setOnClickListener(this);
        btnExport.setOnClickListener(this);

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
