package io.mtc.app.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import io.mtc.app.R;

import io.mtc.app.database.TransactionInfo;
import io.mtc.app.database.TransactionInfoDB;
import io.mtc.app.dialog.DialogFragmentCallback;
import io.mtc.app.dialog.EthWalletExportDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class EthTransactionListActivity extends AppCompatActivity implements View.OnClickListener, DialogFragmentCallback, AdapterView.OnItemClickListener {

    public final static String REQUEST_TAG_EXPORT_DIALOG = "request_tag_export_dialog";

    public final static String KEY_WALLET_ADDRESS = "key_wallet_address";
    public final static String KEY_CONTRACT_ADDRESS = "key_contract_address";

    private Toolbar toolbar;
    private ListView listView;

    private String send_wallet_address;
    private String contract_address;

    private List<TransactionInfo> transactionInfoList = new LinkedList<>();
    private ArrayAdapter<TransactionInfo> transactionInfoAdapter;
    private TransactionInfoDB transactionInfoDB;

    public static void startActivity(Context context, String wallet_address, String contract_address) {
        Intent intent = new Intent(context, EthTransactionListActivity.class);
        intent.putExtra(KEY_WALLET_ADDRESS, wallet_address);
        intent.putExtra(KEY_CONTRACT_ADDRESS, contract_address);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_transaction_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        send_wallet_address = getIntent().getStringExtra(KEY_WALLET_ADDRESS);
        contract_address = getIntent().getStringExtra(KEY_CONTRACT_ADDRESS);

        transactionInfoDB = new TransactionInfoDB(this);

        initView();
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void initView() {
        transactionInfoList.clear();
        transactionInfoList.addAll(transactionInfoDB.getTransactionList(send_wallet_address, contract_address));

        listView = (ListView) findViewById(R.id.listView);
        transactionInfoAdapter = new ArrayAdapter<TransactionInfo>(this, R.layout.item_transaction_info, R.id.tv_from, transactionInfoList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View rootView = super.getView(position, convertView, parent);
                TextView tv_from = (TextView) rootView.findViewById(R.id.tv_from);
                TextView tv_to = (TextView) rootView.findViewById(R.id.tv_to);
                TextView tv_time = (TextView) rootView.findViewById(R.id.tv_time);
                TextView tv_balance = (TextView) rootView.findViewById(R.id.tv_balance);

                TransactionInfo transactionInfo = getItem(position);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(transactionInfo.create_time);
                tv_from.setText(transactionInfo.address_from);
                tv_to.setText(transactionInfo.address_to);
                tv_time.setText(formatter.format(calendar.getTime()));

                if (transactionInfo.oper_type == 0) {
                    tv_balance.setTextColor(getResources().getColor(R.color.colorPrimaryRed));
                    tv_balance.setText("-" + transactionInfo.balance + " ETH");
                } else {
                    tv_balance.setTextColor(getResources().getColor(R.color.colorPrimaryGreen));
                    tv_balance.setText("+" + transactionInfo.balance + " ETH");
                }

                return rootView;
            }
        };
        listView.setAdapter(transactionInfoAdapter);
        listView.setOnItemClickListener(this);


        findViewById(R.id.btn_send).setOnClickListener(this);
        findViewById(R.id.btn_recevice).setOnClickListener(this);
        findViewById(R.id.btn_export).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send: {
                EthSendActivity.startActivity(this, contract_address, send_wallet_address, "");
                break;
            }

            case R.id.btn_recevice: {
                EthReceiveActivity.startActivity(this, send_wallet_address);
                break;
            }

            case R.id.btn_export: {
                EthWalletExportDialogFragment.newInstance().show(getSupportFragmentManager(), REQUEST_TAG_EXPORT_DIALOG);
                break;
            }
        }
    }


    @Override
    public void onDialogFragmentDismiss(DialogFragment dialogFragment) {

    }

    @Override
    public void onDialogFragmentCancel(DialogFragment dialogFragment) {

    }

    @Override
    public void onDialogFragmentClick(DialogFragment dialogFragment, View view) {
        switch (view.getId()) {
            case R.id.tv_keystore: {

                EthWalletExportKeystoreActivity.startActivity(this, send_wallet_address);
                break;
            }

            case R.id.tv_private_key: {
                EthWalletExportPrivateKeyActivity.startActivity(this, send_wallet_address);
                break;
            }
        }
    }

    @Override
    public void onDialogFragmentResult(DialogFragment dialogFragment, Bundle bundle) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TransactionInfo transactionInfo = (TransactionInfo) parent.getItemAtPosition(position);
        EthTransactionDetailsActivity.startActivity(this, transactionInfo);
    }
}
