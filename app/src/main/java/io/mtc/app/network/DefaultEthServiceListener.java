package io.mtc.app.network;

import io.mtc.app.data.EthTransactionData;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Created by admin on 2018/1/10.
 */

public class DefaultEthServiceListener implements EthServiceListener {

    @Override
    public void onGetEtherPriceSuccess(Map<String, Double> priceMap) {

    }

    @Override
    public void onGetEtherPriceError(Exception e) {

    }

    @Override
    public void onGetBalanceSuccess(String walletAddress, BigDecimal value) {

    }

    @Override
    public void onGetBalanceError(String walletAddress, Exception e) {

    }

    @Override
    public void onGetTokenBalanceSuccess(String contractAddress, String address, BigDecimal value) {

    }

    @Override
    public void onGetTokenBalanceError(String contractAddress, String address, Exception e) {

    }

    @Override
    public void onGetPriceConversionRates(Map<String, Double> ratesMap) {

    }

    @Override
    public void onGetPriceConversionRatesError(Exception e) {

    }

    @Override
    public void onGetGasPriceSuccess(BigInteger value) {

    }

    @Override
    public void onGetGasPriceError(Exception e) {

    }

    @Override
    public void onSendRawTransactionSuccess(EthTransactionData transactionData, String txHash) {

    }

    @Override
    public void onSendRawTransactionError(EthTransactionData transactionData, Exception e, String errorString) {

    }
}
