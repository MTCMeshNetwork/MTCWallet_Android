package io.mtc.app.mtcwallet.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.spongycastle.util.encoders.Hex;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.data.Constants;
import io.mtc.app.mtcwallet.data.EthTransactionData;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;
import io.mtc.app.mtcwallet.dialog.DialogFragmentCallback;
import io.mtc.app.mtcwallet.dialog.EthSendConfirmDialogFragment;
import io.mtc.app.mtcwallet.dialog.ProgressDialogFragment;
import io.mtc.app.mtcwallet.interfaces.DefaultTextWatcher;
import io.mtc.app.mtcwallet.network.DefaultEthServiceListener;
import io.mtc.app.mtcwallet.network.EthService;
import io.mtc.app.mtcwallet.network.EthServiceFactory;
import io.mtc.app.mtcwallet.utils.ExchangeCalculator;
import io.mtc.app.mtcwallet.utils.MTCWalletUtils;

public class EthContractSendActivityV2 extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, DialogFragmentCallback {

    private final static String NAME = "EthSendActivityV2HandlerThread";
    private final int DEFAULT_GAS_PRICE = 18;

    private final static String TAG_PROGRESS_DIALOG = "tag_progress_dialog";
    private final static String TAG_CONFIRM_DIALOG = "tag_confirm_dialog";

    private final static String KEY_WALLET_COIN_INFO = "key_wallet_coin_info";
    public final static String KEY_WALLET_NAME = "key_wallet_name";
    public final static String KEY_ADDRESS_SEND_FROM = "key_address_send_from";
    public final static String KEY_ADDRESS_SEND_TO = "key_address_send_to";

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
    private SeekBar seekBarGasPrice;
    private Switch btnSwitchAdvance;
    private View btnOK;


    private String walletName;
    private WalletCoinInfo walletCoinInfo;
    private String walletAddressFrom;
    private String walletAddressTo;

    private BigInteger gasPriceOnline = BigInteger.ZERO;
    private final BigInteger gasLimitMin = new BigInteger("65000");
    private BigInteger gasPrice = new BigInteger("18000000000");
    private BigInteger gasLimit = gasLimitMin;
    private BigDecimal curAvailable = BigDecimal.ZERO;
    private BigDecimal curTxCost = new BigDecimal("0.000252");
    private BigDecimal sendAmount = BigDecimal.ZERO;

    private double realGasPrice;    //gwei
    private DecimalFormat gasPriceFormat = new DecimalFormat("0.##");
    private EthService ethService;
    private EthTransactionData targetTransactionData;


    public static void startActivity(Context context, String address_send_from, String wallet_name, String address_send_to, WalletCoinInfo wallet_coin_info) {
        Intent intent = new Intent(context, EthContractSendActivityV2.class);
        intent.putExtra(KEY_WALLET_NAME, wallet_name);
        intent.putExtra(KEY_ADDRESS_SEND_FROM, address_send_from);
        intent.putExtra(KEY_ADDRESS_SEND_TO, address_send_to);
        intent.putExtra(KEY_WALLET_COIN_INFO, wallet_coin_info);
        context.startActivity(intent);
    }

    public EthContractSendActivityV2() {
        ethService = EthServiceFactory.createDefaultService(this, ethServiceListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_send_v2);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        walletName = getIntent().getStringExtra(KEY_WALLET_NAME);
        walletAddressFrom = getIntent().getStringExtra(KEY_ADDRESS_SEND_FROM);
        walletAddressTo = getIntent().getStringExtra(KEY_ADDRESS_SEND_TO);
        walletCoinInfo = getIntent().getParcelableExtra(KEY_WALLET_COIN_INFO);

        initView();
        getWalletCoinExtraInfo();
    }

    private void initView() {

        tvTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvBalanceAmount = (TextView) findViewById(R.id.tv_balance_amount);
        tvSendFrom = (TextView) findViewById(R.id.tv_send_from);
        editSendTo = (EditText) findViewById(R.id.edit_send_to);
        editSendAmount = (EditText) findViewById(R.id.edit_send_amount);
        tvSendTokenName = (TextView) findViewById(R.id.tv_send_token_name);

        findViewById(R.id.iv_scan_qrcode).setOnClickListener(this);

        editGasPrice = (EditText) findViewById(R.id.edit_gas_price);
        editGasLimit = (EditText) findViewById(R.id.edit_gas_limit);
        seekBarGasPrice = (SeekBar) findViewById(R.id.seekBar);
        seekBarGasPrice.setOnSeekBarChangeListener(gasPriceSeekBarChangeListener);

        tvSendTotal = (TextView) findViewById(R.id.tv_send_total);
        editData = (EditText) findViewById(R.id.edit_data);
        tvTxCostAmount = (TextView) findViewById(R.id.tv_tx_cost_amount);
        editPassword = (EditText) findViewById(R.id.edit_password);
        layoutGasConfigAdvance = findViewById(R.id.layout_gas_config_adv);
        layoutGasConfigSimple = findViewById(R.id.layout_gas_config_simple);
        btnSwitchAdvance = (Switch) findViewById(R.id.switch_advance);
        btnOK = findViewById(R.id.btn_ok);

        tvTitle.setText(walletCoinInfo.unit_name + "转账");
        tvSendFrom.setText(MTCWalletUtils.getPrefixAddress(walletAddressFrom));
        editSendTo.setText(MTCWalletUtils.getPrefixAddress(walletAddressTo));

        btnOK.setOnClickListener(this);
        editSendAmount.addTextChangedListener(sendAmountTextWatcher);
        btnSwitchAdvance.setOnCheckedChangeListener(this);
        seekBarGasPrice.setProgress(DEFAULT_GAS_PRICE);
    }

    private void getWalletCoinExtraInfo() {
        ethService.getBalance(MTCWalletUtils.getPrefixAddress(walletAddressFrom));
        showProgressDialog("正在获取数据,请等待...");
        ethService.getGasPrice();
    }

    private void calculateAmountAndCost() {
        curTxCost = (new BigDecimal(gasLimit).multiply(new BigDecimal(gasPrice))).divide(ExchangeCalculator.ONE_ETHER, 6, BigDecimal.ROUND_DOWN);
    }

    private void calculateRealGasPrice() {
        realGasPrice = new BigDecimal(gasPrice).divide(Constants.ONE_GWEI).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private void updateGasSeekBarView() {
        int progress = (int) realGasPrice;
        seekBarGasPrice.setOnSeekBarChangeListener(null);
        seekBarGasPrice.setProgress(progress);
        seekBarGasPrice.setOnSeekBarChangeListener(gasPriceSeekBarChangeListener);
    }

    private void updateGasEditView() {
        editGasPrice.removeTextChangedListener(editGasPriceTextWatcher);
        editGasPrice.setText(gasPriceFormat.format(realGasPrice));
        editGasPrice.addTextChangedListener(editGasPriceTextWatcher);
    }

    private void updateAmountAndCostView() {
        tvSendTotal.setText(sendAmount.add(curTxCost).toPlainString());
        tvTxCostAmount.setText(curTxCost.toPlainString() + " ETH");
    }

    private SeekBar.OnSeekBarChangeListener gasPriceSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            realGasPrice = progress;
            if (realGasPrice < 1.0) {
                realGasPrice = 1.0;
            }
            gasPrice = Constants.ONE_GWEI.multiply(new BigDecimal(realGasPrice)).toBigInteger();
            calculateAmountAndCost();
            updateAmountAndCostView();
            updateGasEditView();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private DefaultTextWatcher editGasPriceTextWatcher = new DefaultTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            try {
                BigDecimal price = new BigDecimal(s.toString());
                gasPrice = price.multiply(Constants.ONE_GWEI).toBigInteger();
            } catch (Exception e) {
                e.printStackTrace();
                gasPrice = BigInteger.ZERO;
            }
            calculateRealGasPrice();
            updateGasSeekBarView();

            calculateAmountAndCost();
            updateAmountAndCostView();
            System.out.println("editGasPriceTextWatcher: afterTextChanged");
            System.out.println("realGasPrice: " + realGasPrice);
            System.out.println("gasLimit: " + gasLimit);
        }
    };


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
            updateAmountAndCostView();
        }
    };

    private DefaultTextWatcher editGasLimitTextWatcher = new DefaultTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            try {
                gasLimit = new BigDecimal(s.toString()).toBigInteger();
            } catch (Exception e) {
                e.printStackTrace();
                gasLimit = BigInteger.ZERO;
            }

            calculateAmountAndCost();
            updateAmountAndCostView();

            //updateDisplays();
            System.out.println("editGasLimitTextWatcher: afterTextChanged");
            System.out.println("realGasPrice: " + realGasPrice);
            System.out.println("gasLimit: " + gasLimit);
        }
    };

    private EthTransactionData check() {
        EthTransactionData transactionData = new EthTransactionData();
        transactionData.address_from = MTCWalletUtils.getNoPrefixAddress(walletAddressFrom);

        if (transactionData.address_from.length() != 40) {
            showSnack("钱包地址长度无效,请输入40个16进制字符!(当前字符数:" + transactionData.address_from.length() + ")");
            return null;
        }

        try {
            Hex.decode(transactionData.address_from);
        } catch (Exception e) {
            showSnack("钱包地址无效,请输入有效的16进制字符!");
            return null;
        }

        //  Contract Address
        String contract_address = MTCWalletUtils.getNoPrefixAddress(walletCoinInfo.address);
        if (contract_address.length() != 40) {
            showSnack("合约地址长度无效,请输入40个16进制字符!(当前字符数:" + contract_address.length() + ")");
            return null;
        }
        try {
            Hex.decode(contract_address);
        } catch (Exception e) {
            showSnack("接收地址无效,请输入有效的16进制字符!");
            return null;
        }
        transactionData.address_to = contract_address;


        String address_to = MTCWalletUtils.getNoPrefixAddress(editSendTo.getText().toString().trim());
        if (address_to.length() != 40) {
            showSnack("接收地址长度无效,请输入40个16进制字符!(当前字符数:" + address_to.length() + ")");
            return null;
        }
        try {
            Hex.decode(address_to);
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

        if (gasPrice.compareTo(BigInteger.ZERO) <= 0) {
            showSnack("GasPrice值无效,必须大于0!");
            return null;
        }

        transactionData.gas_price = gasPrice.toString();

        if (gasLimit.compareTo(gasLimitMin) < 0) {
            showSnack("GasLimit值不能小于" + gasLimitMin);
            return null;
        }

        transactionData.gas_limit = gasLimit.toString();

        BigInteger biSendAmount = sendAmount.multiply(ExchangeCalculator.ONE_ETHER).toBigInteger();
        Function function = new Function(
                "transfer",
                Arrays.<Type>asList(new Address(MTCWalletUtils.getPrefixAddress(address_to)),
                        new Uint256(biSendAmount)),
                Collections.<TypeReference<?>>emptyList());
        String encodedFunction = FunctionEncoder.encode(function);

        transactionData.data = encodedFunction;

        transactionData.data = "";

        transactionData.password = editPassword.getText().toString();
        if (transactionData.password.length() == 0) {
            showSnack("请输入钱包密码!");
            return null;
        }

        if (gasPrice.compareTo(new BigInteger("100000000000")) >= 0) {
            showSnack("GasPrice值过大, 建议适当调低!");
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

                targetTransactionData = transactionData;
                String address_to = MTCWalletUtils.getPrefixAddress(editSendTo.getText().toString().trim());

                StringBuilder sb = new StringBuilder();
                sb.append("转入地址: " + MTCWalletUtils.getPrefixAddress(address_to));
                sb.append("\n\n");
                sb.append("合约地址: " + MTCWalletUtils.getPrefixAddress(walletCoinInfo.address));
                sb.append("\n\n");
                sb.append("付款钱包: " + MTCWalletUtils.getPrefixAddress(transactionData.address_from));
                if (!TextUtils.isEmpty(walletName)) {
                    sb.append("\n");
                    sb.append("[" + walletName + "]");
                }
                sb.append("\n\n");
                sb.append("矿工费用: " + curTxCost.toPlainString() + "ETH  ");
                sb.append("( ≈ " + new BigDecimal(gasPrice).divide(Constants.ONE_GWEI).setScale(1, RoundingMode.HALF_UP) + "*" + gasLimit + " gwei)");
                sb.append("\n\n");
                sb.append("转出数量: " + sendAmount.toPlainString() + " ETH");

                EthSendConfirmDialogFragment.newInstance(sb.toString()).show(getSupportFragmentManager(), TAG_CONFIRM_DIALOG);

                /*
                showProgressDialog("正在发送请求，请等待...");
                ethService.sendRawTransaction(transactionData);
                */


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

            case R.id.iv_scan_qrcode: {
                Intent qr = new Intent(this, QRScanActivity.class);
                startActivityForResult(qr, QRScanActivity.REQUEST_CODE);
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

                    System.out.println("onSendRawTransactionSuccess: " + txHash);
                    if (TextUtils.isEmpty(txHash)) {
                        showSnack("交易请求失败,请检查交易参数是否选择正确!");
                        return;
                    }

                    Calendar calendar = Calendar.getInstance();
                    /*
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
                    EthTransactionDetailsActivity.startActivity(EthSendActivityV2.this, transactionInfo);
                    */

                    setResult(Activity.RESULT_OK);
                    finish();
                    System.out.println("onSendRawTransactionSuccess: " + txHash);
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
                    gasPriceOnline = value;
                    gasPrice = gasPriceOnline;
                    calculateRealGasPrice();
                    updateGasSeekBarView();
                    updateGasEditView();
                    calculateAmountAndCost();
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

    private Toast mToast;
    private void showSnack(String s) {
        /*
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.layout_content), s, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
        */
        if (mToast == null) {
            mToast = Toast.makeText(this, s, Toast.LENGTH_LONG);
        } else {
            mToast.setText(s);
        }
        mToast.show();
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
                    calculateRealGasPrice();
                    calculateAmountAndCost();
                    updateAmountAndCostView();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (QRScanActivity.REQUEST_CODE == requestCode) {
                editSendTo.setText(data.getStringExtra("ADDRESS"));
            }
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
    public void onDialogFragmentDismiss(DialogFragment dialogFragment) {

    }

    @Override
    public void onDialogFragmentCancel(DialogFragment dialogFragment) {
        if (TAG_CONFIRM_DIALOG.equals(dialogFragment.getTag())) {
            btnOK.setEnabled(true);
        }
    }

    @Override
    public void onDialogFragmentClick(DialogFragment dialogFragment, View view) {
        if (TAG_CONFIRM_DIALOG.equals(dialogFragment.getTag())) {
            if (targetTransactionData != null) {
                showSnack("TODO... 提交数据!");
                printTransactionData();
                targetTransactionData = null;
            }
            btnOK.setEnabled(true);
        }
    }

    @Override
    public void onDialogFragmentResult(DialogFragment dialogFragment, Bundle bundle) {

    }

    private void printTransactionData() {
        if (targetTransactionData != null) {
            System.out.println(targetTransactionData.address_from);
            System.out.println(targetTransactionData.address_to);
            System.out.println(targetTransactionData.amount);
            System.out.println(targetTransactionData.data);
            System.out.println(targetTransactionData.gas_limit);
            System.out.println(targetTransactionData.gas_price);
        } else {
            System.out.println("TransactionData is NULL");
        }
    }
}
