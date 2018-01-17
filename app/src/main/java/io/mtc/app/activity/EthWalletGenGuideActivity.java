package io.mtc.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import io.mtc.app.R;
import io.mtc.app.data.Constants;

import java.util.Calendar;

public class EthWalletGenGuideActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_wallet_gen_guide);

        findViewById(R.id.layout_wallet_new).setOnClickListener(this);
        findViewById(R.id.layout_wallet_import).setOnClickListener(this);
    }

    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {

        long currentClickTime = Calendar.getInstance().getTimeInMillis();
        if (currentClickTime - lastClickTime < 1000) {
            return;
        }
        lastClickTime = currentClickTime;

        switch (v.getId()) {
            case R.id.layout_wallet_new: {
                Intent intent = new Intent(this, EthWalletGenNewActivity.class);
                startActivityForResult(intent, Constants.REQUEST_WALLET_NEW_ACTIVITY);
                break;
            }

            case R.id.layout_wallet_import: {
                Intent intent = new Intent(this, EthWalletGenImportActivity.class);
                startActivityForResult(intent, Constants.REQEUST_WALLET_IMPORT_ACTIVITY);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }
}
