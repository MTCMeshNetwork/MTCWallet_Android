package io.mtc.app.mtcwallet.network;

import io.mtc.app.mtcwallet.data.EthTransactionData;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;
import io.mtc.app.mtcwallet.database.TransactionInfoV2Container;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2018/1/10.
 */

public class DefaultEthServiceListener implements EthServiceListener {

    @Override
    public void onGetWalletCointListSuccess(String walletAddress, List<WalletCoinInfo> walletCoinInfoList) {

    }

    @Override
    public void onGetWalletCointListError(String walletAddress, Exception e) {

    }

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

    @Override
    public void onGetTransactionReceiptSuccess(String txHash, EthTransactionReceiptResult result) {

    }

    @Override
    public void onGetTransactionReceiptError(String txHash, Exception e) {

    }

    @Override
    public void onGetTransactionListSuccess(String walletAddress, String contractAddress, long pageIndex, long pageSize, TransactionInfoV2Container container) {

    }

    @Override
    public void onGetTransactionListError(String walletAddress, String contractAddress, long pageIndex, long pageSize, Exception e) {

    }
}
