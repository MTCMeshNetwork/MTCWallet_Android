package io.mtc.app.network;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import io.mtc.app.data.Constants;
import io.mtc.app.data.EthTransactionData;
import io.mtc.app.utils.EtherscanServiceResponseParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.core.methods.request.RawTransaction;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by admin on 2018/1/8.
 */

public class EtherscanService extends EthService {

    private final static String NAME = "EtherscanServiceHandlerThread";

    private final Context context;
    private final Handler backgroundHandler;
    private final HandlerThread backgroundHandlerThread;
    private final Handler mainHandler;

    public EtherscanService(Context context, EthServiceListener listener) {
        super(listener);
        this.context = context;
        mainHandler = new Handler();
        backgroundHandlerThread = new HandlerThread(NAME, Process.THREAD_PRIORITY_FOREGROUND);
        backgroundHandlerThread.start();
        backgroundHandler = new Handler(backgroundHandlerThread.getLooper());
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
        EtherscanAPI etherscanAPI = EtherscanAPI.getInstance();
        try {
            etherscanAPI.getBalance("0x" + walletAddress, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onGetBalanceError(walletAddress, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        BigDecimal ethbal = new BigDecimal(EtherscanServiceResponseParser.parseBalance(response.body().string()));
                        listener.onGetBalanceSuccess(walletAddress, ethbal);
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onGetBalanceError(walletAddress, e);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            listener.onGetBalanceError(walletAddress, e);
        }
    }

    @Override
    public void getTokenBalance(final String contractAddress, final String walletAddress) {
        EtherscanAPI etherscanAPI = EtherscanAPI.getInstance();
        try {
            etherscanAPI.getTokenBalance("0x" + contractAddress, "0x" + walletAddress, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onGetTokenBalanceError(contractAddress, walletAddress, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        BigDecimal ethbal = new BigDecimal(EtherscanServiceResponseParser.parseBalance(response.body().string()));
                        listener.onGetTokenBalanceSuccess(contractAddress, walletAddress, ethbal);
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onGetTokenBalanceError(contractAddress, walletAddress, e);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
        EtherscanAPI etherscanAPI = EtherscanAPI.getInstance();
        try {
            etherscanAPI.getGasPrice(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    listener.onGetGasPriceError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    listener.onGetGasPriceError(new Exception("Not Support!"));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            listener.onGetGasPriceError(e);
        }
    }


    @Override
    public void sendRawTransaction(final EthTransactionData transactionData) {
        final File srcDir = context.getFilesDir();

        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                String walletAddress = transactionData.address_from;
                String password = transactionData.password;
                try {
                    final Credentials keys = WalletUtils.loadCredentials(password, new File(srcDir, walletAddress));

                    EtherscanAPI.getInstance().getNonceForAddress("0x" + transactionData.address_from, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            listener.onSendRawTransactionError(transactionData, e, "无法访问网络,请确认网络是否连通!");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject o = new JSONObject(response.body().string());
                                BigInteger nonce = new BigInteger(o.getString("result").substring(2), 16);
                                RawTransaction tx = RawTransaction.createTransaction(
                                        nonce,
                                        new BigInteger(transactionData.gas_price),
                                        new BigInteger(transactionData.gas_limit),
                                        transactionData.address_to,
                                        new BigDecimal(transactionData.amount).multiply(Constants.ONE_ETHER).toBigInteger(),
                                        transactionData.data
                                );

                                Log.d("txx",
                                        "Nonce: " + tx.getNonce() + "\n" +
                                                "gasPrice: " + tx.getGasPrice() + "\n" +
                                                "gasLimit: " + tx.getGasLimit() + "\n" +
                                                "To: " + tx.getTo() + "\n" +
                                                "Amount: " + tx.getValue() + "\n" +
                                                "Data: " + tx.getData()
                                );

                                byte[] signed = TransactionEncoder.signMessage(tx, (byte) 1, keys);

                                forwardTX(transactionData, signed);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onSendRawTransactionError(transactionData, e, "无法访问指定的服务端!");
                } catch (CipherException e) {
                    e.printStackTrace();
                    listener.onSendRawTransactionError(transactionData, e, "交易密码错误,请重新输入!");
                }
            }
        });
    }

    private void forwardTX(final EthTransactionData transactionData, byte[] signed){
        try {
            EtherscanAPI.getInstance().forwardTransaction("0x" + Hex.toHexString(signed), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onSendRawTransactionError(transactionData, e, "");
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    String received = response.body().string();
                    System.out.println(received);
                    try {
                        listener.onSendRawTransactionSuccess(transactionData, new JSONObject(received).getString("result"));
                    } catch (Exception e) {
                        try {
                            JSONObject resultJson = new JSONObject(received);
                            /*
                            String errormsg = new JSONObject(received).getJSONObject("error").getString("message");
                            if (errormsg.indexOf(".") > 0)
                                errormsg = errormsg.substring(0, errormsg.indexOf("."));
                            */
                            String errormsg = new JSONObject(received).getString("error");
                            listener.onSendRawTransactionError(transactionData, e, errormsg); // f.E Insufficient funds
                        } catch (JSONException e1) {
                            listener.onSendRawTransactionError(transactionData, e1, "无法解析服务端返回结果!");
                        }
                    }
                }
            });
        } catch (Exception e) {
            listener.onSendRawTransactionError(transactionData, e, "无法访问指定的服务端!");
        }
    }



    @Override
    public String getName() {
        return "Etherscan";
    }



}
