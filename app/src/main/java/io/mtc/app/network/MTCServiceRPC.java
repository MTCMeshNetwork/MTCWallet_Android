package io.mtc.app.network;

import android.content.Context;

import io.mtc.app.data.EthTransactionData;
import io.mtc.app.utils.ExchangeCalculator;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.RawTransaction;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by admin on 2018/1/12.
 */

public class MTCServiceRPC extends EthService {


    //private final static String SERVER_RPC_BASE_URL = "http://192.168.1.223:8545/";
    //private final static String SERVER_RPC_BASE_URL = "http://192.168.1.84:8545/";
    private final static String SERVER_RPC_BASE_URL = "http://wallet.mtc.io/rpc/api";
    private final static String SERVER_CONTRACT_BASE_URL = "http://wallet.mtc.io/rpc";

    private Web3j web3;
    private Context context;

    public MTCServiceRPC(Context context, EthServiceListener listener) {
        super(listener);
        web3 = Web3jFactory.build(new HttpService(SERVER_RPC_BASE_URL));
        this.context = context;
    }

    @Override
    public void getEtherPrice() {
        EtherscanAPI etherscanAPI = EtherscanAPI.getInstance();
        try {
            etherscanAPI.getEtherPrice(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onGetEtherPriceError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject data = new JSONObject(response.body().string()).getJSONObject("result");
                        Map<String, Double> priceMap = new LinkedHashMap<>();
                        priceMap.put("BTC", data.getDouble("ethbtc"));
                        priceMap.put("USD", data.getDouble("ethusd"));
                        listener.onGetEtherPriceSuccess(priceMap);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onGetEtherPriceError(e);
                    }
                }
            });
        } catch (IOException e) {
            listener.onGetEtherPriceError(e);
        }
    }

    @Override
    public void getBalance(final String walletAddress) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EthGetBalance result = web3.ethGetBalance("0x" + walletAddress, DefaultBlockParameterName.LATEST).send();
                    BigDecimal humanReadResult = new BigDecimal(result.getBalance()).divide(new BigDecimal(1000000000000000000d), 7, BigDecimal.ROUND_UP);
                    listener.onGetBalanceSuccess(walletAddress, humanReadResult);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onGetBalanceError(walletAddress, e);
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onGetBalanceError(walletAddress, e);
                }
            }
        }).start();
    }

    @Override
    public void getTokenBalance(final String contractAddress, final String walletAddress) {
        //listener.onGetTokenBalanceError(contractAddress, walletAddress, new Exception("暂时未实现此方法!"));
        try {
            get(SERVER_CONTRACT_BASE_URL + "/contract/balanceOf?ca=0x" + contractAddress + "&owner=0x" + walletAddress, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onGetTokenBalanceError(contractAddress, walletAddress, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        listener.onGetTokenBalanceSuccess(contractAddress, walletAddress, new BigDecimal(response.body().string()));
                    } catch (Exception e) {
                        listener.onGetTokenBalanceError(contractAddress, walletAddress, e);
                    }
                }
            });
        } catch (IOException e) {

        }
    }

    @Override
    public void getPriceConversionRates() {
        EtherscanAPI etherscanAPI = EtherscanAPI.getInstance();
        try {
            etherscanAPI.getPriceConversionRates(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    listener.onGetPriceConversionRatesError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject ratesJson = new JSONObject(response.body().string()).getJSONObject("rates");
                        Map<String, Double> ratesMap = new LinkedHashMap<>();
                        Iterator<String> iterator = ratesJson.keys();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            ratesMap.put(key, ratesJson.optDouble(key, 0));
                        }
                        listener.onGetPriceConversionRates(ratesMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onGetPriceConversionRatesError(e);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            listener.onGetPriceConversionRatesError(e);
        }
    }

    @Override
    public void getGasPrice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EthGasPrice ethGasPrice = web3.ethGasPrice().send();
                    BigInteger gasPrice = ethGasPrice.getGasPrice();
                    listener.onGetGasPriceSuccess(gasPrice);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onGetGasPriceError(e);
                }
            }
        }).start();
    }

    @Override
    public void sendRawTransaction(final EthTransactionData data) {
        final File srcDir = context.getFilesDir();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String address_from = data.address_from;
                    if (!address_from.startsWith("0x")) {
                        address_from = "0x" + address_from;
                    }

                    String address_to = data.address_to;
                    if (!address_to.startsWith("0x")) {
                        address_to = "0x" + address_to;
                    }

                    BigDecimal sendAmount = new BigDecimal(data.amount);
                    BigInteger biSendAmount = sendAmount.multiply(ExchangeCalculator.ONE_ETHER).toBigInteger();

                    EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(address_from, DefaultBlockParameterName.LATEST).send();
                    BigInteger transCount = ethGetTransactionCount.getTransactionCount();
                    System.out.println("ethGetTransactionCount: " + transCount.toString());

                    final Credentials credentials = WalletUtils.loadCredentials(data.password, new File(srcDir, data.address_from));
                    RawTransaction rawTransaction = RawTransaction.createTransaction(transCount,
                            new BigInteger(data.gas_price),
                            new BigInteger(data.gas_limit),
                            address_to,
                            biSendAmount,
                            data.data);
                    byte[] signed = TransactionEncoder.signMessage(rawTransaction, credentials);

                    EthSendTransaction ethSendRawTransaction = web3.ethSendRawTransaction("0x" + Hex.toHexString(signed)).send();
                    String txHash = ethSendRawTransaction.getTransactionHash();
                    listener.onSendRawTransactionSuccess(data, txHash);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onSendRawTransactionError(data, e, "加载钱包数据出错！");
                } catch (CipherException e) {
                    e.printStackTrace();
                    listener.onSendRawTransactionError(data, e, "交易密码错误！");
                }

            }
        }).start();
    }


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
