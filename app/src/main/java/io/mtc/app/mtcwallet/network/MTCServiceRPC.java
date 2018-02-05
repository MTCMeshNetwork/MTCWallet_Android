package io.mtc.app.mtcwallet.network;

import android.content.Context;
import android.text.TextUtils;

import io.mtc.app.mtcwallet.data.EthTransactionData;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;
import io.mtc.app.mtcwallet.database.TransactionInfoV2Container;
import io.mtc.app.mtcwallet.utils.ExchangeCalculator;

import org.json.JSONArray;
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
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.mtc.app.mtcwallet.utils.MTCWalletUtils;
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

    public final static String SERVER_RPC_BASE_URL = "http://p.mtc.io/rpc/api";
    public final static String SERVER_HTTP_BASE_URL = "http://p.mtc.io/rpc";
    public final static String CONTRACT_BALANCE_URL = SERVER_HTTP_BASE_URL + "/contract/balanceOf";
    public final static String TRANSACTION_LIST_URL = SERVER_HTTP_BASE_URL + "/tl";

    /*
    private final static String SERVER_RPC_BASE_URL = "http://192.168.1.84:8080/tx/t1";
    private final static String SERVER_HTTP_BASE_URL = "http://192.168.1.84:8080";
    private final static String CONTRACT_BALANCE_URL = SERVER_HTTP_BASE_URL + "/tx/t3";
    */

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
                    EthGetBalance result = web3.ethGetBalance(MTCWalletUtils.getPrefixAddress(walletAddress), DefaultBlockParameterName.LATEST).send();
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
            get(CONTRACT_BALANCE_URL + "?ca=0x" + contractAddress + "&owner=0x" + walletAddress, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onGetTokenBalanceError(contractAddress, walletAddress, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        BigDecimal humanReadResult = new BigDecimal(response.body().string()).divide(new BigDecimal(1000000000000000000d), 7, BigDecimal.ROUND_UP);
                        listener.onGetTokenBalanceSuccess(contractAddress, walletAddress, humanReadResult);
                        //listener.onGetTokenBalanceSuccess(contractAddress, walletAddress, new BigDecimal(response.body().string()));
                    } catch (Exception e) {
                        listener.onGetTokenBalanceError(contractAddress, walletAddress, e);
                    }
                }
            });
        } catch (IOException e) {
            listener.onGetTokenBalanceError(contractAddress, walletAddress, e);
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

                    BigInteger gasPrice = new BigInteger(data.gas_price);
                    BigInteger gasLimit = new BigInteger(data.gas_limit);

                    if (!TextUtils.isEmpty(data.data)) {
                        Transaction estimateGasTransaction = new Transaction(address_from,
                                transCount,
                                gasPrice,
                                gasLimit,
                                address_to,
                                biSendAmount,
                                data.data);
                        EthEstimateGas ethEstimateGas = web3.ethEstimateGas(estimateGasTransaction).send();
                        if(TextUtils.isEmpty(ethEstimateGas.getResult())) {
                            listener.onSendRawTransactionError(data, new Exception("GASLIMIT_ERROR"), "无法校验GasLimit,请重试!");
                            return;
                        }
                        System.out.println(ethEstimateGas.getResult());
                        BigInteger amountUsed = ethEstimateGas.getAmountUsed();
                        if (amountUsed.compareTo(gasLimit) == 1) {
                            BigInteger newGasLimit = BigDecimal.valueOf(1.2).multiply(new BigDecimal(amountUsed)).toBigInteger();
                            listener.onSendRawTransactionError(data, new Exception("GASLIMIT"), "Gas Limit 建议为: " + newGasLimit);
                            return;
                        }
                    }


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
                    listener.onSendRawTransactionError(data, e, "钱包密码错误！");
                }

            }
        }).start();
    }

    @Override
    public void getTransactionReceipt(final String txhash) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EthGetTransactionReceipt ethGetTransactionReceipt = web3.ethGetTransactionReceipt(txhash).send();
                    TransactionReceipt transactionReceipt = ethGetTransactionReceipt.getTransactionReceipt();
                    EthTransactionReceiptResult receiptResult = new EthTransactionReceiptResult();

                    if (transactionReceipt != null) {

                        receiptResult.originResult = transactionReceipt;
                        receiptResult.gasUsed = transactionReceipt.getGasUsed().toString();
                        receiptResult.blockHash = transactionReceipt.getBlockHash();
                        receiptResult.blockNumber = transactionReceipt.getBlockNumber().toString();

                        EthBlock ethBlock = web3.ethGetBlockByHash(transactionReceipt.getBlockHash(), true).send();
                        EthBlock.Block block = ethBlock.getBlock();
                        receiptResult.blockTimeStamp = block.getTimestamp().multiply(BigInteger.valueOf(1000L)).longValue();
                    }

                    listener.onGetTransactionReceiptSuccess(txhash, receiptResult);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onGetTransactionReceiptError(txhash, e);
                }
            }
        }).start();
    }

    @Override
    public void getWalletCoinList(final String walletAddress) {
        final String prefixWalletAddress = MTCWalletUtils.getPrefixAddress(walletAddress);
        try {
            get(SERVER_HTTP_BASE_URL + "/cl?address=" + prefixWalletAddress, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onGetWalletCointListError(walletAddress, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("obj");
                        List<WalletCoinInfo> resultList = new LinkedList<>();
                        for (int index = 0; index < jsonArray.length(); ++index) {
                            JSONObject jsonCointInfo = jsonArray.getJSONObject(index);
                            WalletCoinInfo cointInfo = new WalletCoinInfo();
                            cointInfo.address = jsonCointInfo.getString("address");
                            cointInfo.imageURL = jsonCointInfo.getString("images");
                            cointInfo.name = jsonCointInfo.getString("name");
                            cointInfo.unit_name = jsonCointInfo.getString("units");
                            //cointInfo.balance = jsonCointInfo.getString("balance");
                            cointInfo.setBalance(jsonCointInfo.getString("balance"));
                            cointInfo.price = jsonCointInfo.getDouble("price");
                            cointInfo.priceCNY = jsonCointInfo.getDouble("cnyPrice");
                            cointInfo.update();
                            resultList.add(cointInfo);
                        }
                        listener.onGetWalletCointListSuccess(walletAddress, resultList);
                    } catch (Exception e) {
                        listener.onGetWalletCointListError(walletAddress, e);
                    }
                }
            });
        } catch (IOException e) {
            listener.onGetWalletCointListError(walletAddress, e);
        }
    }

    @Override
    public void getTransactionList(final String walletAddress, final String contractAddress, final long pageIndex, final long pageSize) {
        final String prefixWalletAddress = MTCWalletUtils.getPrefixAddress(walletAddress);
        final String prefixContractAddress = MTCWalletUtils.getPrefixAddress(contractAddress);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("/tl?a=" + prefixWalletAddress);
            sb.append("&i=" + pageIndex);
            sb.append("&c=" + pageSize);
            if (!TextUtils.isEmpty(prefixContractAddress)) {
                sb.append("&ca=" + prefixContractAddress);
            }

            get(SERVER_HTTP_BASE_URL + sb.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onGetTransactionListError(walletAddress, contractAddress, pageIndex, pageSize, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonResult = new JSONObject(response.body().string());
                        TransactionInfoV2Container container = TransactionInfoV2Container.fromJSON(jsonResult.getJSONObject("obj"));
                        listener.onGetTransactionListSuccess(walletAddress, contractAddress, pageIndex, pageSize, container);
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onGetTransactionListError(walletAddress, contractAddress, pageIndex, pageSize, e);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            listener.onGetTransactionListError(walletAddress, contractAddress, pageIndex, pageSize, e);
        }
    }


}
