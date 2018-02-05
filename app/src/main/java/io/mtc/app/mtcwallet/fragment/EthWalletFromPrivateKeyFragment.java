package io.mtc.app.mtcwallet.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.data.AppPref;
import io.mtc.app.mtcwallet.database.EthWalletInfo;
import io.mtc.app.mtcwallet.database.EthWalletInfoDB;
import io.mtc.app.mtcwallet.dialog.ProgressDialogFragment;
import io.mtc.app.mtcwallet.interfaces.QRCodeScanListener;
import io.mtc.app.mtcwallet.services.EthWalletTask;

import java.util.Calendar;

public class EthWalletFromPrivateKeyFragment extends Fragment implements View.OnClickListener, QRCodeScanListener {

    private final static String TAG_PROGRESS_DIALOG = "tag_progress_dialog";

    private EditText editPrivateKey;
    private EditText editName;
    private EditText editPassword;
    private EditText editPasswordConfirm;
    private View btnGenerate;

    private EthWalletTask ethWalletTask;
    private AppPref appPref;
    private EthWalletInfoDB ethWalletInfoDB;

    public EthWalletFromPrivateKeyFragment() {
        // Required empty public constructor
    }

    public static EthWalletFromPrivateKeyFragment newInstance() {
        EthWalletFromPrivateKeyFragment fragment = new EthWalletFromPrivateKeyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) { }
        ethWalletTask = new EthWalletTask(ethWalletTaskListener);
        appPref = new AppPref(getContext());
        ethWalletInfoDB = new EthWalletInfoDB(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eth_wallet_from_private_key, container, false);

        editPrivateKey = (EditText) rootView.findViewById(R.id.edit_private_key);
        editName = (EditText) rootView.findViewById(R.id.edit_name);
        editPassword = (EditText) rootView.findViewById(R.id.edit_password);
        editPasswordConfirm = (EditText) rootView.findViewById(R.id.edit_password_confirm);
        btnGenerate = rootView.findViewById(R.id.btn_generate);
        btnGenerate.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_generate: {
                btnGenerate.setEnabled(false);

                String privateKey = editPrivateKey.getText().toString();
                final String password = editPassword.getText().toString();
                String passwordConfirm = editPasswordConfirm.getText().toString();

                if (password.length() < 9) {
                    showSnack("密码长度不能小于9个字符!");
                    btnGenerate.setEnabled(true);
                    return;
                }

                if (!password.equals(passwordConfirm)) {
                    showSnack("两次输入密码不同!");
                    btnGenerate.setEnabled(true);
                    return;
                }

                showProgressDialog();
                ethWalletTask.createWalletFromPrivateKey(privateKey, password, getContext().getFilesDir());
            }
        }
    }

    private EthWalletTask.EthWalletTaskListener ethWalletTaskListener = new EthWalletTask.EthWalletTaskListener() {
        @Override
        public void onSuccess(int typeId, String walletAddress) {
            btnGenerate.setEnabled(true);
            showSnack("钱包创建成功:" + walletAddress);
            closeProgressDialog();
            EthWalletInfo ethWalletInfo = new EthWalletInfo();
            ethWalletInfo.address = walletAddress;
            ethWalletInfo.name = editName.getText().toString();
            ethWalletInfo.create_time = Calendar.getInstance().getTimeInMillis();
            if (ethWalletInfoDB.addWallet(ethWalletInfo)) {
                showSnack("钱包创建成功:" + walletAddress);
                appPref.setWalletAddress(walletAddress);
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            } else {
                showSnack("创建钱包记录失败!");
            }
        }

        @Override
        public void onError(int typeId, Exception e) {
            btnGenerate.setEnabled(true);
            showSnack("钱包创建失败: " + e.getMessage());
            closeProgressDialog();
        }
    };

    private void showProgressDialog() {
        closeProgressDialog();
        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance("正在创建钱包,请等待...");
        progressDialogFragment.setCancelable(false);
        progressDialogFragment.show(getFragmentManager(), TAG_PROGRESS_DIALOG);
    }

    private void closeProgressDialog() {
        ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment)getFragmentManager().findFragmentByTag(TAG_PROGRESS_DIALOG);
        if (progressDialogFragment != null)
            progressDialogFragment.dismiss();
    }

    private void showSnack(String s) {
        Snackbar mySnackbar = Snackbar.make(getView(), s, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    @Override
    public void onQRCodeScanResult(String text) {
        editPrivateKey.setText(text);
    }
}
