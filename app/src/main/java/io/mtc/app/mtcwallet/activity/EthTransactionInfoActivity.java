package io.mtc.app.mtcwallet.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;
import io.mtc.app.mtcwallet.database.TransactionInfo;
import io.mtc.app.mtcwallet.database.TransactionInfoV2;
import io.mtc.app.mtcwallet.network.DefaultEthServiceListener;
import io.mtc.app.mtcwallet.network.EthService;
import io.mtc.app.mtcwallet.network.EthServiceFactory;
import io.mtc.app.mtcwallet.network.EthTransactionReceiptResult;
import io.mtc.app.mtcwallet.utils.MTCWalletUtils;

public class EthTransactionInfoActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String KEY_WALLET_COIN_INFO = "key_wallet_coin_info";
    public final static String KEY_TRANSACTION_INFO = "key_transaction_info";

    private Toolbar toolbar;
    private TextView tv_amount;
    private TextView tv_send_from;
    private TextView tv_send_to;
    private TextView tv_create_time;

    private TextView tv_block_number;
    private TextView tv_block_time;
    private TextView tv_gas_used;

    private TextView tv_data;
    private TextView tv_txhash;

    private EthService ethService;
    private WalletCoinInfo walletCoinInfo;
    private TransactionInfoV2 transactionInfo;

    private Handler handler = new Handler();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void startActivity(Context context, WalletCoinInfo walletCoinInfo, TransactionInfoV2 transactionInfo) {
        Intent intent = new Intent(context, EthTransactionInfoActivity.class);
        intent.putExtra(KEY_WALLET_COIN_INFO, walletCoinInfo);
        intent.putExtra(KEY_TRANSACTION_INFO, transactionInfo);
        context.startActivity(intent);
    }

    public EthTransactionInfoActivity() {
        ethService = EthServiceFactory.createDefaultService(this, ethServiceListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_transaction_info);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        walletCoinInfo = getIntent().getParcelableExtra(KEY_WALLET_COIN_INFO);
        transactionInfo = getIntent().getParcelableExtra(KEY_TRANSACTION_INFO);

        String balance_unit_name = walletCoinInfo.unit_name;

        tv_amount = (TextView) findViewById(R.id.tv_amount);
        tv_send_from = (TextView) findViewById(R.id.tv_send_from);
        tv_send_to = (TextView) findViewById(R.id.tv_send_to);
        tv_create_time = (TextView) findViewById(R.id.tv_create_time);

        tv_block_number = (TextView) findViewById(R.id.tv_block_number);
        tv_block_time = (TextView) findViewById(R.id.tv_block_time);
        tv_gas_used = (TextView) findViewById(R.id.tv_gas_used);

        tv_data = (TextView) findViewById(R.id.tv_data);
        tv_txhash = (TextView) findViewById(R.id.tv_txhash);
        tv_txhash.setOnClickListener(this);
        findViewById(R.id.iv_txhash_copy).setOnClickListener(this);

        tv_amount.setTextColor(getResources().getColor(R.color.colorPrimaryRed));
        tv_amount.setText("-" + transactionInfo.getTokenCount() + " " + balance_unit_name);

        tv_send_from.setText(MTCWalletUtils.getPrefixAddress(transactionInfo.from));
        tv_send_to.setText(MTCWalletUtils.getPrefixAddress(transactionInfo.to));
        tv_txhash.setText(transactionInfo.hash);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(transactionInfo.time);
        tv_create_time.setText(formatter.format(calendar.getTime()));

        if (!TextUtils.isEmpty(transactionInfo.hash)) {
            ethService.getTransactionReceipt(transactionInfo.hash);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_txhash: {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://etherscan.io/tx/" + transactionInfo.hash);
                intent.setData(content_url);
                startActivity(intent);

                break;
            }

            case R.id.iv_txhash_copy: {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", transactionInfo.hash);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "复制TXHASH到剪贴板！", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private DefaultEthServiceListener ethServiceListener = new DefaultEthServiceListener() {

        @Override
        public void onGetTransactionReceiptError(String txHash, Exception e) {
            super.onGetTransactionReceiptError(txHash, e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ethService.getTransactionReceipt(transactionInfo.hash);
                        }
                    }, 1000);
                }
            });
        }

        @Override
        public void onGetTransactionReceiptSuccess(String txHash, final EthTransactionReceiptResult result) {
            super.onGetTransactionReceiptSuccess(txHash, result);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_block_number.setText(result.blockNumber);
                    tv_gas_used.setText(result.gasUsed);
                    if (result.blockTimeStamp > 0) {
                        Calendar calendar = Calendar.getInstance(Locale.CHINA);
                        calendar.setTimeInMillis(result.blockTimeStamp);
                        tv_block_time.setText(formatter.format(calendar.getTime()));
                    } else {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ethService.getTransactionReceipt(transactionInfo.hash);
                            }
                        }, 1000);
                    }
                }
            });
        }
    };
}
