package io.mtc.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import io.mtc.app.R;
import io.mtc.app.data.EthTransactionData;
import io.mtc.app.database.TransactionInfo;
import io.mtc.app.database.TransactionInfoDB;
import io.mtc.app.dialog.ProgressDialogFragment;
import io.mtc.app.interfaces.DefaultTextWatcher;
import io.mtc.app.network.DefaultEthServiceListener;
import io.mtc.app.network.EthService;
import io.mtc.app.network.EthServiceFactory;
import io.mtc.app.utils.ExchangeCalculator;

import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Map;

public class EthSendActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private final static String NAME = "EtherscanSendHandlerThread";
    private final int DEFAULT_GAS_PRICE = 18;

    public final static String KEY_CONTRACT_ADDRESS = "key_contract_address";
    public final static String KEY_ADDRESS_SEND_FROM = "key_address_send_from";
    public final static String KEY_ADDRESS_SEND_TO = "key_address_send_to";

    private final static String TAG_PROGRESS_DIALOG = "tag_progress_dialog";

    private Toolbar toolbar;
    private TextView tvTitle;
    private TextView tvSendFrom;
    private EditText editSendTo;
    private EditText editSendAmount;
    private TextView tvSendTokenName;
    private TextView tvSendTotal;
    private EditText editData;
    private EditText editPassword;

    private View layoutGasConfigAdvance;
    private View layoutGasConfigSimple;

    private TextView tvBalanceAmount;
    private TextView tvSendAmount;
    private TextView tvTxCostAmount;
    private TextView tvTotalAmount;

    private EditText editGasPrice;
    private EditText editGasLimit;
    private SeekBar seekBar;
    private Switch btnSwitchAdvance;

    private View btnOK;

    private String contract_address;
    private String address_send_from;
    private String address_send_to;

    private BigInteger gasPrice = new BigInteger("18000000000");
    private BigInteger gaslimit = new BigInteger("21000");
    private BigDecimal curAvailable = BigDecimal.ZERO;
    private BigDecimal curTxCost = new BigDecimal("0.000252");
    private BigDecimal sendAmount = BigDecimal.ZERO;

    private Double ethPrice = 0.0;
    private double realGas;

    private EthService ethService;
    private TransactionInfoDB transactionInfoDB;

    public static void startActivity(Context context, String contract_address, String address_send_from, String address_send_to) {
        Intent intent = new Intent(context, EthSendActivity.class);
        intent.putExtra(KEY_CONTRACT_ADDRESS, contract_address);
        intent.putExtra(KEY_ADDRESS_SEND_FROM, address_send_from);
        intent.putExtra(KEY_ADDRESS_SEND_TO, address_send_to);
        context.startActivity(intent);
    }

    public EthSendActivity() {
        ethService = EthServiceFactory.createDefaultService(this, ethServiceListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_send);

        transactionInfoDB = new TransactionInfoDB(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        tvTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvTitle.setText("ETH 转账");

        contract_address = getIntent().getStringExtra(KEY_CONTRACT_ADDRESS);
        address_send_from = getIntent().getStringExtra(KEY_ADDRESS_SEND_FROM);
        address_send_to = getIntent().getStringExtra(KEY_ADDRESS_SEND_TO);

        tvBalanceAmount = (TextView) findViewById(R.id.tv_balance_amount);

        ethService.getBalance(address_send_from);
        showProgressDialog("正在获取GasPrice,请等待...");
        ethService.getGasPrice();

        initView();
    }

    private void initView() {

        tvSendFrom = (TextView) findViewById(R.id.tv_send_from);
        tvSendFrom.setText("0x" + address_send_from);

        editSendTo = (EditText) findViewById(R.id.edit_send_to);
        //editSendTo.setText("0x" + address_send_to);

        editSendAmount = (EditText) findViewById(R.id.edit_send_amount);
        tvSendTokenName = (TextView) findViewById(R.id.tv_send_token_name);

        editGasPrice = (EditText) findViewById(R.id.edit_gas_price);
        editGasLimit = (EditText) findViewById(R.id.edit_gas_limit);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                realGas = progress;
                if (progress < 10)
                    realGas = (double) (progress + 1) / 10d;

                editGasPrice.setText((realGas + ""));
                curTxCost = (new BigDecimal(gaslimit).multiply(new BigDecimal(realGas + ""))).divide(new BigDecimal("1000000000"), 6, BigDecimal.ROUND_DOWN);

                System.out.println(realGas);
                System.out.println(gaslimit);

                updateDisplays();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        tvSendTotal = (TextView) findViewById(R.id.tv_send_total);

        editData = (EditText) findViewById(R.id.edit_data);

        tvTxCostAmount = (TextView) findViewById(R.id.tv_tx_cost_amount);

        editPassword = (EditText) findViewById(R.id.edit_password);

        layoutGasConfigAdvance = findViewById(R.id.layout_gas_config_adv);
        layoutGasConfigSimple = findViewById(R.id.layout_gas_config_simple);

        btnSwitchAdvance = (Switch) findViewById(R.id.switch_advance);

        btnOK = findViewById(R.id.btn_ok);
        btnOK.setOnClickListener(this);

        editSendAmount.addTextChangedListener(sendAmountTextWatcher);
        btnSwitchAdvance.setOnCheckedChangeListener(this);
        seekBar.setProgress(DEFAULT_GAS_PRICE);
    }

    private void updateDisplays() {
        updateTxCostDisplay();
        updateTotalCostDisplay();
    }

    private void updateTxCostDisplay() {
        tvTxCostAmount.setText(curTxCost.toPlainString() + " ETH");
    }

    private void updateTotalCostDisplay() {
        tvSendTotal.setText(sendAmount.add(curTxCost).toPlainString());
    }

    private TextWatcher sendAmountTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                sendAmount = new BigDecimal(s.toString());
            } catch (Exception e) {
                e.printStackTrace();
                sendAmount = BigDecimal.ZERO;
            }
            updateDisplays();
        }
    };

    private DefaultTextWatcher editGasPriceTextWatcher = new DefaultTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            try {
                realGas = Double.valueOf(s.toString());
            } catch (Exception e) {
                e.printStackTrace();
                realGas = 0.0;
            }

            curTxCost = (new BigDecimal(gaslimit).multiply(new BigDecimal(realGas + ""))).divide(ExchangeCalculator.ONE_GWEI, 6, BigDecimal.ROUND_DOWN);
            updateDisplays();
            System.out.println("editGasPriceTextWatcher: afterTextChanged");
            System.out.println("realGas: " + realGas);
            System.out.println("gaslimit: " + gaslimit);
        }
    };

    private DefaultTextWatcher editGasLimitTextWatcher = new DefaultTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            try {
                gaslimit = new BigDecimal(s.toString()).toBigInteger();
            } catch (Exception e) {
                e.printStackTrace();
                gaslimit = BigInteger.ZERO;
            }

            curTxCost = (new BigDecimal(gaslimit).multiply(new BigDecimal(realGas + ""))).divide(ExchangeCalculator.ONE_GWEI, 6, BigDecimal.ROUND_DOWN);

            updateDisplays();
            System.out.println("editGasLimitTextWatcher: afterTextChanged");
            System.out.println("realGas: " + realGas);
            System.out.println("gaslimit: " + gaslimit);
        }
    };

    private EthTransactionData check() {
        EthTransactionData transactionData = new EthTransactionData();

        String address_from = address_send_from.trim();
        if (address_from.startsWith("0x")) {
            address_from = address_from.substring (2, address_from.length());
        }
        transactionData.address_from = address_from;

        /*
        if (transactionData.address_from.length() != 64) {
            showSnack("钱包地址长度无效,请输入64个16进制字符!(当前字符数:" + transactionData.address_from.length() + ")");
            return null;
        }
        */
        try {
            Hex.decode(transactionData.address_from);
        } catch (Exception e) {
            showSnack("钱包地址无效,请输入有效的16进制字符!");
            return null;
        }

        String address_to = editSendTo.getText().toString().trim();
        if (address_to.startsWith("0x")) {
            address_to = address_to.substring (2, address_to.length());
        }
        transactionData.address_to = address_to;
        /*
        if (transactionData.address_to.length() != 64) {
            showSnack("接收地址长度无效,请输入64个16进制字符!(当前字符数:" + transactionData.address_to.length() + ")");
            return null;
        }
        */
        try {
            Hex.decode(transactionData.address_to);
        } catch (Exception e) {
            showSnack("接收地址无效,请输入有效的16进制字符!");
            return null;
        }

        try {
            sendAmount = new BigDecimal(editSendAmount.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
            showSnack("转账数量无效, 请输入有效的数字！");
            return null;
        }

        if (sendAmount.compareTo(BigDecimal.ZERO) <= 0) {
            showSnack("转账数量需大于0!");
            return null;
        }

        transactionData.amount = sendAmount.toString();

        transactionData.gas_price = gasPrice.toString();

        transactionData.gas_limit = gaslimit.toString();

        transactionData.data = "";

        transactionData.password = editPassword.getText().toString();
        if (transactionData.password.length() == 0) {
            showSnack("请输入钱包密码!");
            return null;
        }

        return transactionData;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok: {

                btnOK.setEnabled(false);
                final EthTransactionData transactionData = check();
                if (transactionData == null) {
                    btnOK.setEnabled(true);
                    return;
                }

                showProgressDialog("正在发送请求，请等待...");
                ethService.sendRawTransaction(transactionData);

                /*

                //EthTransactionData transactionData = new EthTransactionData();
                //transactionData.address_from = "0xff92fbd6e292e3e00001c7cd0df0e690165ad2ed";
                transactionData.address_to = "0xff92fbd6e292e3e00001c7cd0df0e690165ad2ed";
                transactionData.password = "123456789";
                transactionData.amount = "100000000000000000000000";
                transactionData.gas_limit = "21000";
                transactionData.gas_price = "210000";
                transactionData.data = "";
                ethService.sendRawTransaction(transactionData);
                */

                break;
            }
        }
    }


    private DefaultEthServiceListener ethServiceListener = new DefaultEthServiceListener() {

        @Override
        public void onSendRawTransactionError(EthTransactionData transactionData, Exception e, final String errorString) {
            super.onSendRawTransactionError(transactionData, e, errorString);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeProgressDialog();
                    btnOK.setEnabled(true);
                    showSnack(errorString);
                    System.out.println("onSendRawTransactionError: " + errorString);
                }
            });

        }

        @Override
        public void onSendRawTransactionSuccess(final EthTransactionData transactionData, final String txHash) {
            super.onSendRawTransactionSuccess(transactionData, txHash);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeProgressDialog();
                    btnOK.setEnabled(true);

                    Calendar calendar = Calendar.getInstance();

                    TransactionInfo transactionInfo = new TransactionInfo();
                    transactionInfo.address_from = transactionData.address_from;
                    transactionInfo.address_to = transactionData.address_to;
                    transactionInfo.contract_address = contract_address;
                    transactionInfo.net_type = 0;
                    transactionInfo.oper_type = 0;
                    transactionInfo.balance = transactionData.amount;
                    transactionInfo.data = transactionData.data;
                    transactionInfo.txhash = txHash;
                    transactionInfo.create_time = calendar.getTimeInMillis();
                    transactionInfoDB.addTransactionInfo(transactionInfo);

                    EthTransactionDetailsActivity.startActivity(EthSendActivity.this, transactionInfo);

                    setResult(Activity.RESULT_OK);
                    finish();
                    System.out.println("onSendRawTransactionSuccess: " + txHash);
                }
            });
        }

        @Override
        public void onGetEtherPriceSuccess(final Map<String, Double> priceMap) {
            super.onGetEtherPriceSuccess(priceMap);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ethPrice = priceMap.get("USD");
                    if (ethPrice == null)
                        ethPrice = 0.0;
                }
            });
        }

        @Override
        public void onGetBalanceError(String walletAddress, Exception e) {
            super.onGetBalanceError(walletAddress, e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showSnack("查询钱包余额失败!");
                }
            });
        }

        @Override
        public void onGetBalanceSuccess(String walletAddress, final BigDecimal value) {
            super.onGetBalanceSuccess(walletAddress, value);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    curAvailable = value;
                    //TODO...更新显示；
                    tvBalanceAmount.setText(value.toString() + " ETH");
                }
            });
        }

        @Override
        public void onGetGasPriceSuccess(final BigInteger value) {
            super.onGetGasPriceSuccess(value);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeProgressDialog();
                    gasPrice = value;
                    editGasPrice.setText(new BigDecimal(gasPrice).divide(ExchangeCalculator.ONE_GWEI).toPlainString());
                }
            });

        }

        @Override
        public void onGetGasPriceError(Exception e) {
            super.onGetGasPriceError(e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeProgressDialog();
                }
            });

        }
    };

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

    private void updateActivity() {

    }

    private void updateAvaliableBalanceView() {

    }

    private void showSnack(String s) {
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.layout_content), s, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_advance: {
                if (isChecked) {
                    layoutGasConfigAdvance.setVisibility(View.VISIBLE);
                    layoutGasConfigSimple.setVisibility(View.GONE);
                    editGasPrice.addTextChangedListener(editGasPriceTextWatcher);
                    editGasLimit.addTextChangedListener(editGasLimitTextWatcher);
                } else {
                    layoutGasConfigAdvance.setVisibility(View.GONE);
                    layoutGasConfigSimple.setVisibility(View.VISIBLE);
                    editGasPrice.removeTextChangedListener(editGasPriceTextWatcher);
                    editGasLimit.removeTextChangedListener(editGasLimitTextWatcher);
                }
                break;
            }
        }
    }
}
