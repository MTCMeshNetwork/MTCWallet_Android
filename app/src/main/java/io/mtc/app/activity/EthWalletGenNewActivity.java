package io.mtc.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import io.mtc.app.R;
import io.mtc.app.data.AppPref;
import io.mtc.app.data.Constants;
import io.mtc.app.database.EthWalletInfo;
import io.mtc.app.database.EthWalletInfoDB;
import io.mtc.app.dialog.ProgressDialogFragment;
import io.mtc.app.services.EthWalletTask;

import java.util.Calendar;

public class EthWalletGenNewActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG_PROGRESS_DIALOG = "tag_progress_dialog";

    private Toolbar toolbar;
    private TextView tvTitle;
    private EditText editName;
    private EditText editPassword;
    private EditText editPasswordConfirm;
    private EditText editPasswordHit;
    private View btnGenerate;

    private EthWalletTask ethWalletTask;
    private AppPref appPref;
    private EthWalletInfoDB ethWalletInfoDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_wallet_gen_new);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        tvTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvTitle.setText(R.string.title_wallet_new);

        editName = (EditText) findViewById(R.id.edit_name);
        editPassword = (EditText) findViewById(R.id.edit_password);
        editPasswordConfirm = (EditText) findViewById(R.id.edit_password_confirm);
        editPasswordHit = (EditText) findViewById(R.id.edit_password_hit);
        btnGenerate = findViewById(R.id.btn_generate);

        btnGenerate.setOnClickListener(this);

        ethWalletTask = new EthWalletTask(ethWalletTaskListener);
        appPref = new AppPref(this);
        ethWalletInfoDB = new EthWalletInfoDB(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ethWalletInfoDB.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_generate: {
                btnGenerate.setEnabled(false);

                String name = editName.getText().toString();
                final String password = editPassword.getText().toString();
                String passwordConfirm = editPasswordConfirm.getText().toString();
                String passwordHit = editPasswordHit.getText().toString();

                if (password.length() < 6) {
                    showSnack("密码长度不能小于6个字符!");
                    btnGenerate.setEnabled(true);
                    return;
                }

                if (!password.equals(passwordConfirm)) {
                    showSnack("两次输入密码不同!");
                    btnGenerate.setEnabled(true);
                    return;
                }

                showProgressDialog();
                ethWalletTask.createWalletFromPassword(password, getFilesDir());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Constants.REQUEST_WALLET_NEW_SUCCESS == requestCode) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    private EthWalletTask.EthWalletTaskListener ethWalletTaskListener = new EthWalletTask.EthWalletTaskListener() {
        @Override
        public void onSuccess(int typeId, String walletAddress) {
            btnGenerate.setEnabled(true);
            closeProgressDialog();
            EthWalletInfo ethWalletInfo = new EthWalletInfo();
            ethWalletInfo.address = walletAddress;
            ethWalletInfo.name = editName.getText().toString();
            ethWalletInfo.password_hint = editPasswordHit.getText().toString();
            ethWalletInfo.create_time = Calendar.getInstance().getTimeInMillis();
            String password = editPassword.getText().toString();
            if (ethWalletInfoDB.addWallet(ethWalletInfo)) {
                showSnack("钱包创建成功:" + walletAddress);
                appPref.setWalletAddress(walletAddress);
                EthWalletGenSuccessActivity.startActivityForResult(EthWalletGenNewActivity.this, ethWalletInfo, password, Constants.REQUEST_WALLET_NEW_SUCCESS);
                //setResult(Activity.RESULT_OK);
                //finish();
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
        progressDialogFragment.show(getSupportFragmentManager(), TAG_PROGRESS_DIALOG);
    }

    private void closeProgressDialog() {
        ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(TAG_PROGRESS_DIALOG);
        if (progressDialogFragment != null)
            progressDialogFragment.dismiss();
    }

    private void showSnack(String s) {
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.layout_content), s, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

}
