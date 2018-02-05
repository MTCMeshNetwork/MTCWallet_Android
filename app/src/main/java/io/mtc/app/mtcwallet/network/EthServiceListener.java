package io.mtc.app.mtcwallet.network;

import io.mtc.app.mtcwallet.data.EthTransactionData;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;
import io.mtc.app.mtcwallet.database.TransactionInfoV2Container;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2018/1/8.
 */

public interface EthServiceListener {

    void onGetWalletCointListSuccess(String walletAddress, List<WalletCoinInfo> walletCoinInfoList);
    void onGetWalletCointListError(String walletAddress, Exception e);

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

    void onGetTransactionReceiptSuccess(String txHash, EthTransactionReceiptResult result);
    void onGetTransactionReceiptError(String txHash, Exception e);

    void onGetTransactionListSuccess(String walletAddress, String contractAddress, long pageIndex, long pageSize, TransactionInfoV2Container container);
    void onGetTransactionListError(String walletAddress, String contractAddress, long pageIndex, long pageSize, Exception e);

}
