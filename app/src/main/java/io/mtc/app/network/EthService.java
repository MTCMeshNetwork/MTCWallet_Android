package io.mtc.app.network;

import io.mtc.app.data.EthTransactionData;

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

}
