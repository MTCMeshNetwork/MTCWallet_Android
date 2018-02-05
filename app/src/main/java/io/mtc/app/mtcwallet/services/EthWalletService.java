package io.mtc.app.mtcwallet.services;

import android.app.IntentService;
import android.content.Intent;

public class EthWalletService extends IntentService {

    public EthWalletService() {
        super("EthWalletService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
        }
    }


}
