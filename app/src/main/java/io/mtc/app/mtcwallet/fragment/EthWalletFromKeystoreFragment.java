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

public class EthWalletFromKeystoreFragment extends Fragment implements View.OnClickListener, QRCodeScanListener {

    private final static String TAG_PROGRESS_DIALOG = "tag_progress_dialog";

    private EditText editKeystore;
    private EditText editName;
    private EditText editPassword;
    private View btnGenerate;

    private EthWalletTask ethWalletTask;
    private AppPref appPref;
    private EthWalletInfoDB ethWalletInfoDB;
    private String keyStore;

    public EthWalletFromKeystoreFragment() {
        // Required empty public constructor
    }

    public static EthWalletFromKeystoreFragment newInstance() {
        EthWalletFromKeystoreFragment fragment = new EthWalletFromKeystoreFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        ethWalletTask = new EthWalletTask(ethWalletTaskListener);
        appPref = new AppPref(getContext());
        ethWalletInfoDB = new EthWalletInfoDB(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eth_wallet_from_keystore, container, false);

        editKeystore = (EditText) rootView.findViewById(R.id.edit_keystore);
        editName = (EditText) rootView.findViewById(R.id.edit_name);
        editPassword = (EditText) rootView.findViewById(R.id.edit_password);
        btnGenerate = rootView.findViewById(R.id.btn_generate);
        btnGenerate.setOnClickListener(this);
        if (keyStore != null) {
            editKeystore.setText(keyStore);
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_generate: {
                btnGenerate.setEnabled(false);

                String keystore = editKeystore.getText().toString();
                final String password = editPassword.getText().toString();

                showProgressDialog();
                ethWalletTask.createWalletFromKeyStore(keystore, password, getContext().getFilesDir());
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
        editKeystore.setText(text);
    }
}
