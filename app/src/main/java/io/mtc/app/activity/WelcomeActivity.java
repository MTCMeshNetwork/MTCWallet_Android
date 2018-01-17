package io.mtc.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import io.mtc.app.R;
import io.mtc.app.data.AppPref;

public class WelcomeActivity extends AppCompatActivity {

    private final static int SHOW_WALLET_GEN_ACTIVITY = 1;

    public final static long TIME_MILLIS = 2 * 1000;
    private Handler handler = new Handler();
    private AppPref appPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        appPref = new AppPref(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gotoActivity();
            }
        }, TIME_MILLIS);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            gotoActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void gotoActivity() {
        String walletAddress = appPref.getWalletAddress();
        if (TextUtils.isEmpty(walletAddress)) {
            showWalletGenGuideActivity();
        } else {
            showMainActivity();
            finish();
        }
    }

    private void showMainActivity() {
        handler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void showWalletGenGuideActivity() {
        handler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(this, EthWalletGenGuideActivity.class);
        startActivityForResult(intent, SHOW_WALLET_GEN_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_WALLET_GEN_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                showMainActivity();
                return;
            }
        }
        finish();
    }

}
