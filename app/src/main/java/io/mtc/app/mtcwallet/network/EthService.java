package io.mtc.app.mtcwallet.network;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.mtc.app.mtcwallet.data.EthTransactionData;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by admin on 2018/1/8.
 */

public abstract class EthService {

    protected EthServiceListener listener;

    public EthService(EthServiceListener listener) {
        this.listener = listener;
    }

    public String getName() {
        return "";
    }

    public abstract void getEtherPrice();

    public abstract void getBalance(String walletAddress);

    public abstract void getTokenBalance(String contractAddress, String walletAddress);

    public abstract void getPriceConversionRates();

    public abstract void sendRawTransaction(EthTransactionData data);

    public abstract void getGasPrice();

    public abstract void getTransactionReceipt(String txhash);

    public abstract void getWalletCoinList(String walletAddress);

    public abstract void getTransactionList(String walletAddress, String contractAddress, long pageIndex, long pageSize);

    public void get(String url, Callback b) throws IOException {

        System.out.println(url);

        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(b);
    }

}
