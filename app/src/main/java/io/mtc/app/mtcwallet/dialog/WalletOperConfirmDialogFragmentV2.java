package io.mtc.app.mtcwallet.dialog;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.activity.EthWalletExportPrivateKeyActivity;

public class WalletOperConfirmDialogFragmentV2 extends DialogFragment implements View.OnClickListener {

    public final static String TAG_PROGRESS_DIALOG = "tag_progress_dialog";

    public final static String KEY_WALLET_ADDRESS = "key_wallet_address";
    public final static String KEY_WALLET_OPER = "key_wallet_oper";

    private DialogFragmentCallback callback;
    private String wallet_address;
    private String wallet_oper;

    private View btnOK;
    private View btnCancel;
    private EditText editPassword;

    private final Handler backgroundHandler;
    private final HandlerThread backgroundHandlerThread;
    private final Handler mainHandler;

    public WalletOperConfirmDialogFragmentV2() {
        mainHandler = new Handler();
        backgroundHandlerThread = new HandlerThread("WalletOperConfirmDialogFragmentV2HandlerThread", Process.THREAD_PRIORITY_FOREGROUND);
        backgroundHandlerThread.start();
        backgroundHandler = new Handler(backgroundHandlerThread.getLooper());
    }

    public static WalletOperConfirmDialogFragmentV2 newInstance(String wallet_address, String oper) {
        WalletOperConfirmDialogFragmentV2 fragment = new WalletOperConfirmDialogFragmentV2();
        Bundle args = new Bundle();
        args.putString(KEY_WALLET_ADDRESS, wallet_address);
        args.putString(KEY_WALLET_OPER, oper);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            wallet_address = getArguments().getString(KEY_WALLET_ADDRESS, "");
            wallet_oper = getArguments().getString(KEY_WALLET_OPER, "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet_oper_confirm_dialog_v2, container, false);

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
                showProgressDialog();
                decode(wallet_address, password);
                break;
            }

            case R.id.btn_cancel: {
                dismiss();
                break;
            }
        }
    }

    private void decode(final String address, final String password) {
        final File srcDir = getActivity().getFilesDir();
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                File srcFile = new File(srcDir, address);
                try {
                    Credentials credentials = WalletUtils.loadCredentials(password, srcFile);
                    onDecodeSuccess(credentials);
                } catch (IOException e) {
                    e.printStackTrace();
                    onDecodeError("读取钱包数据失败!");
                } catch (CipherException e) {
                    e.printStackTrace();
                    onDecodeError("解码钱包私钥数据失败!");
                }
            }
        });
    }

    private void onDecodeError(final String message) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                closeProgressDialog();
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDecodeSuccess(final Credentials credentials) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                closeProgressDialog();
                Toast.makeText(getActivity(), "密码验证成功!", Toast.LENGTH_SHORT).show();
                if (callback != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_WALLET_ADDRESS, wallet_address);
                    bundle.putString(KEY_WALLET_OPER, wallet_oper);
                    callback.onDialogFragmentResult(WalletOperConfirmDialogFragmentV2.this, bundle);
                }
                dismiss();
            }
        });
    }

    private void showProgressDialog() {
        closeProgressDialog();
        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance("正在验证密码,请等待...");
        progressDialogFragment.setCancelable(false);
        progressDialogFragment.show(getFragmentManager(), TAG_PROGRESS_DIALOG);
    }

    private void closeProgressDialog() {
        ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment)getFragmentManager().findFragmentByTag(TAG_PROGRESS_DIALOG);
        if (progressDialogFragment != null)
            progressDialogFragment.dismiss();
    }

}
