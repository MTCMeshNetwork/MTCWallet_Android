package io.mtc.app.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import io.mtc.app.R;
import io.mtc.app.database.TransactionInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EthTransactionDetailsActivity extends AppCompatActivity {

    public final static String KEY_TRANSACTION_INFO = "key_transaction_info";

    private TextView tv_amount;
    private TextView tv_send_from;
    private TextView tv_send_to;
    private TextView tv_create_time;

    private TextView tv_data;
    private TextView tv_txhash;

    private TransactionInfo transactionInfo;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void startActivity(Context context, TransactionInfo transactionInfo) {
        Intent intent = new Intent(context, EthTransactionDetailsActivity.class);
        intent.putExtra(KEY_TRANSACTION_INFO, transactionInfo);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_transaction_details);
        transactionInfo = getIntent().getParcelableExtra(KEY_TRANSACTION_INFO);

        tv_amount = (TextView) findViewById(R.id.tv_amount);
        tv_send_from = (TextView) findViewById(R.id.tv_send_from);
        tv_send_to = (TextView) findViewById(R.id.tv_send_to);
        tv_create_time = (TextView) findViewById(R.id.tv_create_time);
        tv_data = (TextView) findViewById(R.id.tv_data);
        tv_txhash = (TextView) findViewById(R.id.tv_txhash);

        if (transactionInfo.oper_type == 0) {
            tv_amount.setTextColor(getResources().getColor(R.color.colorPrimaryRed));
            tv_amount.setText("-" + transactionInfo.balance + " ETH");
        } else {
            tv_amount.setTextColor(getResources().getColor(R.color.colorPrimaryGreen));
            tv_amount.setText("+" + transactionInfo.balance + " ETH");
        }

        tv_send_from.setText("0x" + transactionInfo.address_from);
        tv_send_to.setText("0x" + transactionInfo.address_to);
        tv_data.setText(transactionInfo.data);
        tv_txhash.setText(transactionInfo.txhash);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(transactionInfo.create_time);
        tv_create_time.setText(formatter.format(calendar.getTime()));

    }
}
