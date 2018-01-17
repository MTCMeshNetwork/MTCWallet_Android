package io.mtc.app.services;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import io.mtc.app.utils.OwnWalletUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.File;

/**
 * Created by admin on 2018/1/3.
 */

public class EthWalletTask {

    public interface EthWalletTaskListener {
        void onSuccess(int typeId, String walletAddress);
        void onError(int typeId, Exception e);
    }

    private final static String NAME = "EthWalletHandlerThread";

    public final static int WALLET_NEW = 1;
    public final static int WALLET_FROM_PRIVATE_KEY = 2;
    public final static int WALLET_FROM_KEYSTORE = 3;

    private final Handler backgroundHandler;
    private final HandlerThread backgroundHandlerThread;
    private final Handler mainHandler;
    private EthWalletTaskListener listener;

    public EthWalletTask(EthWalletTaskListener listener) {
        this.listener = listener;
        mainHandler = new Handler();
        backgroundHandlerThread = new HandlerThread(NAME, Process.THREAD_PRIORITY_FOREGROUND);
        backgroundHandlerThread.start();
        backgroundHandler = new Handler(backgroundHandlerThread.getLooper());
    }

    public void close() {
        listener = null;
    }

    private void notifySuccess(final int typeId, final String walletAddress) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onSuccess(typeId, walletAddress);
                }
            }
        });
    }

    private void notifyError(final int typeId, final Exception e) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onError(typeId, e);
                }
            }
        });
    }

    public void createWalletFromPassword(final String password, final File destDir) {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String walletAddress = OwnWalletUtils.generateNewWalletFile(password, destDir, true);
                    notifySuccess(WALLET_NEW, walletAddress);
                } catch (Exception e) {
                    notifyError(WALLET_NEW, e);
                    e.printStackTrace();
                }
            }
        });
    }

    public void createWalletFromPrivateKey(final String privatekey, final String password, final File destDir) {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ECKeyPair keys = ECKeyPair.create(Hex.decode(privatekey));
                    String walletAddress = OwnWalletUtils.generateWalletFile(password, keys, destDir, true);
                    notifySuccess(WALLET_FROM_PRIVATE_KEY, walletAddress);
                } catch (Exception e) {
                    notifyError(WALLET_FROM_PRIVATE_KEY, e);
                    e.printStackTrace();
                }
            }
        });
    }

    public void createWalletFromKeyStore(final String keystore, final String password, final File destDir) {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                try {
                    WalletFile walletFile = objectMapper.readValue(keystore, WalletFile.class);
                    Wallet.decrypt(password, walletFile);
                    String fileName = walletFile.getAddress();
                    File destination = new File(destDir, fileName);
                    objectMapper.writeValue(destination, walletFile);
                    notifySuccess(WALLET_FROM_KEYSTORE, fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    notifyError(WALLET_FROM_KEYSTORE, e);
                }
            }
        });
    }
}
