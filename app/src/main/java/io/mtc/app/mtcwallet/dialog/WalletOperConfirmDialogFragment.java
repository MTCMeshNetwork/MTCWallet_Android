package io.mtc.app.mtcwallet.dialog;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import io.mtc.app.mtcwallet.R;

public class WalletOperConfirmDialogFragment extends DialogFragment implements View.OnClickListener {

    public final static String KEY_PASSWORD = "key_password";

    private DialogFragmentCallback callback;

    private View btnOK;
    private View btnCancel;
    private EditText editPassword;

    public WalletOperConfirmDialogFragment() {

    }

    public static WalletOperConfirmDialogFragment newInstance() {
        WalletOperConfirmDialogFragment fragment = new WalletOperConfirmDialogFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_wallet_oper_confirm_dialog, container, false);

        editPassword = (EditText) rootView.findViewById(R.id.edit_password);

        btnOK = rootView.findViewById(R.id.btn_ok);
        btnCancel = rootView.findViewById(R.id.btn_cancel);

        btnOK.setOnClickListener(this);
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
        switch (v.getId()) {

            case R.id.btn_ok: {
                String password = editPassword.getText().toString();
                if (password.length() < 9) {
                    Toast.makeText(getContext(), "密码长度不能小于9个字符!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (callback != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_PASSWORD, password);
                    callback.onDialogFragmentResult(this, bundle);
                }
                dismiss();
                break;
            }

            case R.id.btn_cancel: {

                dismiss();
                break;
            }
        }
    }


}
