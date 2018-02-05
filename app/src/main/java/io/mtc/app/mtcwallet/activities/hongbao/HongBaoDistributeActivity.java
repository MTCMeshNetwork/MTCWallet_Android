package io.mtc.app.mtcwallet.activities.hongbao;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;
import io.mtc.app.mtcwallet.dialog.ProgressDialogFragment;
import io.mtc.app.mtcwallet.network.hongbao.CurrencyInfo;
import io.mtc.app.mtcwallet.network.hongbao.PacketCreateParams;
import io.mtc.app.mtcwallet.network.hongbao.PacketCreateResult;
import io.mtc.app.mtcwallet.network.hongbao.HongBaoAPI;
import io.mtc.app.mtcwallet.network.hongbao.PacketStatusResult;
import io.mtc.app.mtcwallet.utils.MTCWalletUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HongBaoDistributeActivity extends AppCompatActivity {

    public final static String KEY_WALLET_ADDRESS = "key_wallet_address";
    public final static String KEY_WALLET_COIN_INFO = "key_wallet_coin_info";
    public final static String KEY_PACKET_PARAMS = "key_packet_params";

    private final static String TAG_PROGRESS_DIALOG = "tag_progress_dialog";

    public static void startActivity(Context context, String walletAddress, WalletCoinInfo walletCoinInfo, PacketCreateParams params) {
        Intent intent = new Intent(context, HongBaoDistributeActivity.class);
        intent.putExtra(KEY_WALLET_ADDRESS, walletAddress);
        intent.putExtra(KEY_WALLET_COIN_INFO, walletCoinInfo);
        intent.putExtra(KEY_PACKET_PARAMS, params);
        context.startActivity(intent);
    }

    private TextView tvProgress;

    private CurrencyInfo currencyInfo;
    private PacketCreateParams createParams;
    private PacketCreateResult createResult;
    private PacketStatusResult statusResult;
    private String walletAddress;
    private WalletCoinInfo walletCoinInfo;
    private HongBaoAPI hongBaoAPI = new HongBaoAPI();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hong_bao_distribute);

        createParams = getIntent().getParcelableExtra(KEY_PACKET_PARAMS);
        walletAddress = getIntent().getParcelableExtra(KEY_WALLET_ADDRESS);
        walletCoinInfo = getIntent().getParcelableExtra(KEY_WALLET_COIN_INFO);

        tvProgress = (TextView) findViewById(R.id.tv_progress);

        showProgressDialog("正在初始化,请等待...");
        getCurrencyInfo();
    }

    private void showProgressDialog(String msg) {
        closeProgressDialog();
        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(msg);
        progressDialogFragment.setCancelable(false);
        progressDialogFragment.show(getSupportFragmentManager(), TAG_PROGRESS_DIALOG);
    }

    private void closeProgressDialog() {
        ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(TAG_PROGRESS_DIALOG);
        if (progressDialogFragment != null)
            progressDialogFragment.dismiss();
    }

    private void getCurrencyInfo() {
        try {
            hongBaoAPI.getCurrencyInfo(MTCWalletUtils.getPrefixAddress(walletAddress), walletCoinInfo.unit_name, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onGetCurrencyInfoFail("获取红包数据失败! ");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String content = response.body().string();
                            JSONObject jsonObject = new JSONObject(content);
                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                CurrencyInfo result = CurrencyInfo.from(jsonObject.getJSONObject("result"));
                                onGetCurrencyInfoSuccess(result);
                            } else {
                                onGetCurrencyInfoFail("获取红包数据失败! " + jsonObject.optString("message"));
                            }
                        } catch (Exception e) {
                            onGetCurrencyInfoFail("获取红包数据失败! " + e.getMessage());
                        }
                    } else {
                        onGetCurrencyInfoFail("获取红包数据失败! " + response.code());
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            onGetCurrencyInfoFail("获取红包数据失败! " + e.getMessage());
        }
    }

    private void onGetCurrencyInfoSuccess(final CurrencyInfo result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO...
                currencyInfo = result;
                createParams.accountId = currencyInfo.id;
                createPacket();
            }
        });
    }

    private void onGetCurrencyInfoFail(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeProgressDialog();
                showToast(msg);
            }
        });
    }

    private void createPacket() {
        try {
            hongBaoAPI.createPacket(createParams, new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onCreatePacketFail("创建红包失败! ");
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Response response) {

                    if (response.isSuccessful()) {
                        try {
                            String content = response.body().string();
                            JSONObject jsonObject = new JSONObject(content);
                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                PacketCreateResult result = PacketCreateResult.from(jsonObject.getJSONObject("result"));
                                onCreatePacketSuccess(result);
                            } else {
                                onCreatePacketFail("创建红包失败! " + jsonObject.optString("message"));
                            }
                        } catch (Exception e) {
                            onCreatePacketFail("创建红包失败! " + e.getMessage());
                        }
                    } else {
                        onCreatePacketFail("创建红包失败! " + response.code());
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            onCreatePacketFail("创建红包失败! " + e.getMessage());
        }
    }

    private void onCreatePacketSuccess(final PacketCreateResult result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeProgressDialog();
                showToast("红包创建成功,开始广播红包!");
                createResult = result;
                updatePacketView();
                getPacketStatus();
            }
        });
    }

    private void onCreatePacketFail(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeProgressDialog();
                showToast(msg);
            }
        });
    }

    private void updatePacketView() {
        tvProgress.setText(0 + "/" + createResult.quantity);
    }

    private void getPacketStatus() {
        if (createResult == null)
            return;

        try {
            hongBaoAPI.getPacketStatus(createResult.id, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onGetPacketStatusFail("获取红包状态失败! ");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String content = response.body().string();
                            JSONObject jsonObject = new JSONObject(content);
                            int code = jsonObject.getInt("code");

                            if (code == 0) {
                                PacketStatusResult result = PacketStatusResult.from(jsonObject);
                                onGetPacketStatusSuccess(result);
                            } else {
                                onGetPacketStatusFail("获取红包状态失败! " + jsonObject.optString("message"));
                            }
                        } catch (Exception e) {
                            onGetPacketStatusFail("获取红包状态失败! " + e.getMessage());
                        }
                    } else {
                        onGetPacketStatusFail("获取红包状态失败! " + response.code());
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            onGetPacketStatusFail("获取红包状态失败! " + e.getMessage());
        }
    }

    private void onGetPacketStatusSuccess(final PacketStatusResult result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO...更新红包状态;
                if (createResult == null)
                    return;

                statusResult = result;

                updatePacketStatusView();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getPacketStatus();
                    }
                }, 1000);

            }
        });
    }

    private void onGetPacketStatusFail(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(msg);

                if (createResult == null)
                    return;

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getPacketStatus();
                    }
                }, 1000);
            }
        });
    }

    private void updatePacketStatusView() {
        if (statusResult == null)
            return;
        tvProgress.setText(statusResult.data.winners + "/" + statusResult.result.quantity);
    }

    private Toast mToast;
    private void showToast(String s) {
        if (mToast == null) {
            mToast = Toast.makeText(this, s, Toast.LENGTH_LONG);
        } else {
            mToast.setText(s);
        }
        mToast.show();
    }

}
