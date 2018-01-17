package io.mtc.app.network;

import io.mtc.app.data.EthTransactionData;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Created by admin on 2018/1/8.
 */

public interface EthServiceListener {
    void onGetEtherPriceSuccess(Map<String, Double> priceMap);
    void onGetEtherPriceError(Exception e);

    void onGetBalanceSuccess(String walletAddress, BigDecimal value);
    void onGetBalanceError(String walletAddress, Exception e);

    void onGetTokenBalanceSuccess(String contractAddress, String address, BigDecimal value);
    void onGetTokenBalanceError(String contractAddress, String address, Exception e);

    void onGetPriceConversionRates(Map<String, Double> ratesMap);
    void onGetPriceConversionRatesError(Exception e);

    void onGetGasPriceSuccess(BigInteger value);    //wei
    void onGetGasPriceError(Exception e);

    void onSendRawTransactionSuccess(EthTransactionData transactionData, String txHash);
    void onSendRawTransactionError(EthTransactionData transactionData, Exception e, String errorString);

}
