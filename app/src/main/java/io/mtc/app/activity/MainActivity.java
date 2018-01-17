package io.mtc.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import io.mtc.app.R;
import io.mtc.app.adapter.EthWalletDisplayAdapter;
import io.mtc.app.data.AppPref;
import io.mtc.app.data.Constants;
import io.mtc.app.data.EthTokenDisplay;
import io.mtc.app.data.EthTransactionData;
import io.mtc.app.data.EthWalletDisplay;
import io.mtc.app.database.EthTokenInfo;
import io.mtc.app.database.EthWalletInfo;
import io.mtc.app.database.EthWalletInfoDB;
import io.mtc.app.dialog.DialogFragmentCallback;
import io.mtc.app.dialog.EthWalletExportDialogFragment;
import io.mtc.app.dialog.WalletDeleteConfirmDialogFragment;
import io.mtc.app.network.EthService;
import io.mtc.app.network.EthServiceFactory;
import io.mtc.app.network.EthServiceListener;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, DialogFragmentCallback{

    public final static String REQUEST_TAG_WALLET_DELETE_CONFIRM_DIALOG = "tag_wallet_delete_confirm_dialog";
    public final static String REQUEST_TAG_EXPORT_DIALOG = "request_tag_export_dialog";

    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView tvTitle;
    private TextView tvWalletAddress;
    private ListView listView;
    private Handler handler = new Handler();
    private Toolbar toolbar;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvWalletListEdit;
    private TextView tvWalletListEditDone;
    private ListView listViewWallet;
    private View tvWalletNew;
    private View tvWalletImport;

    private AppPref appPref;
    private EthWalletInfoDB ethWalletInfoDB;

    private EthWalletInfo ethWalletInfo;
    private LinkedList<EthTokenInfo> ethTokenInfoList = new LinkedList<>();
    private List<EthTokenDisplay> ethTokenDisplayList = new LinkedList<>();
    private ArrayAdapter<EthTokenDisplay> ethTokenDisplayAdapter = null;

    private List<EthWalletDisplay> ethWalletDisplayList = new LinkedList<>();
    private EthWalletDisplayAdapter ethWalletDisplayAdapter = null;

    private EthService ethService;

    private Map<String, Double> priceRateMap = new LinkedHashMap<>();
    private String export_wallet_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        tvTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvWalletAddress = (TextView) findViewById(R.id.tv_wallet_address);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        listView = (ListView)findViewById(R.id.listView);
        ethTokenDisplayAdapter = new ArrayAdapter<EthTokenDisplay>(this, R.layout.item_token_display, R.id.tv_name, ethTokenDisplayList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View rootView = super.getView(position, convertView, parent);
                ImageView iv_icon = (ImageView) rootView.findViewById(R.id.iv_icon);
                TextView tv_name = (TextView) rootView.findViewById(R.id.tv_name);
                TextView tv_ether = (TextView) rootView.findViewById(R.id.tv_ether);
                TextView tv_price = (TextView) rootView.findViewById(R.id.tv_price);

                EthTokenDisplay ethTokenDisplay = getItem(position);
                EthTokenInfo ethTokenInfo = ethTokenDisplay.tokenInfo;
                iv_icon.setImageResource(ethTokenDisplay.icon_resid);
                tv_name.setText(ethTokenInfo.name);
                tv_ether.setText(ethTokenDisplay.str_ether_count);
                tv_price.setText(ethTokenDisplay.str_total_price);

                return rootView;
            }
        };

        listView.setAdapter(ethTokenDisplayAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        appPref = new AppPref(this);
        ethWalletInfoDB = new EthWalletInfoDB(this);

        if (!loadDefaultWallet()) {
            startEthWalletGenGuideActivity();
        } else {
            initWalletView();
        }

        tvTitle.setOnClickListener(this);

        initNavigationView();

        ethService = EthServiceFactory.createDefaultService(this, ethServiceListener);
        refreshWalletInfo();
    }

    private void initNavigationView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        tvWalletListEdit = (TextView) findViewById(R.id.tv_edit);
        tvWalletListEditDone = (TextView) findViewById(R.id.tv_edit_done);
        listViewWallet = (ListView) findViewById(R.id.listview_wallet);
        tvWalletNew = findViewById(R.id.tv_wallet_new);
        tvWalletImport = findViewById(R.id.tv_wallet_import);

        tvWalletListEdit.setOnClickListener(this);
        tvWalletListEditDone.setOnClickListener(this);
        tvWalletNew.setOnClickListener(this);
        tvWalletImport.setOnClickListener(this);

        ethWalletDisplayAdapter = new EthWalletDisplayAdapter(this, ethWalletDisplayList, ethWalletDisplayAdapterListener);
        listViewWallet.setAdapter(ethWalletDisplayAdapter);
        listViewWallet.setOnItemClickListener(this);
        reloadWalletList();
    }

    private void reloadWalletList() {
        List<EthWalletInfo> ethWalletInfoList = ethWalletInfoDB.getWalletList();
        ethWalletDisplayList.clear();
        for (EthWalletInfo walletInfo: ethWalletInfoList) {
            EthWalletDisplay walletDisplay = new EthWalletDisplay();
            walletDisplay.walletInfo = walletInfo;
            walletDisplay.str_total_price = "---";
            ethWalletDisplayList.add(walletDisplay);
        }
        ethWalletDisplayAdapter.notifyDataSetChanged();
    }

    private EthWalletDisplayAdapter.EthWalletDisplayAdapterListener ethWalletDisplayAdapterListener = new EthWalletDisplayAdapter.EthWalletDisplayAdapterListener() {
        @Override
        public void onClick(int id, EthWalletDisplay ethWalletDisplay) {
            WalletDeleteConfirmDialogFragment.newInstance(ethWalletDisplay.walletInfo.address).show(getSupportFragmentManager(), REQUEST_TAG_WALLET_DELETE_CONFIRM_DIALOG);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_setting: {
                drawerLayout.openDrawer(Gravity.END);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void startEthWalletGenGuideActivity() {
        Intent intent = new Intent(this, EthWalletGenGuideActivity.class);
        startActivityForResult(intent, Constants.REQUEST_WALLET_GEN_GUIDE_ACTIVITY);
    }

    @Override
    public void onRefresh() {
        refreshWalletInfo();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 500);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_WALLET_GEN_GUIDE_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                loadDefaultWallet();
            }
            if (this.ethWalletInfo == null) {
                finish();
                return;
            }
        }
    }

    private boolean loadDefaultWallet() {
        EthWalletInfo walletInfo = null;
        String walletAddress = appPref.getWalletAddress();
        walletInfo = ethWalletInfoDB.getWalletInfo(walletAddress);
        if (walletInfo == null) {
            List<EthWalletInfo> ethWalletInfoList = ethWalletInfoDB.getWalletList();
            if (!ethWalletInfoList.isEmpty()) {
                walletInfo = ethWalletInfoList.get(0);
            }
        }

        if (walletInfo == null)
            return false;

        List<EthTokenInfo> tokenInfoList = ethWalletInfoDB.getTokenList(walletInfo.address);

        this.ethWalletInfo = walletInfo;
        this.ethTokenInfoList.clear();
        this.ethTokenInfoList.addAll(tokenInfoList);

        return true;
    }

    private void initWalletView() {
        if (ethWalletInfo == null)
            return;

        tvTitle.setText(ethWalletInfo.name);
        tvWalletAddress.setText("0x" + ethWalletInfo.address);

        ethTokenDisplayList.clear();
        EthTokenInfo ethTokenInfo = null;
        EthTokenDisplay ethTokenDisplay = null;

        {
            ethTokenInfo = new EthTokenInfo();
            ethTokenInfo.wallet_address = ethWalletInfo.address;
            ethTokenInfo.name = "ETH";
            ethTokenInfo.unit_name = "ether";
            ethTokenInfo.contract_address = ethWalletInfo.address;

            ethTokenDisplay = new EthTokenDisplay();
            ethTokenDisplay.tokenInfo = ethTokenInfo;
            ethTokenDisplay.icon_resid = R.drawable.ic_eth;
            ethTokenDisplay.setBalance(null);
            //ethTokenDisplay.str_ether_count = "--- ether";
            //ethTokenDisplay.str_total_price = "---";

            ethTokenDisplayList.add(ethTokenDisplay);
        }

        {
            ethTokenInfo = new EthTokenInfo();
            ethTokenInfo.wallet_address = ethWalletInfo.address;
            ethTokenInfo.name = "MTC";
            ethTokenInfo.unit_name = "mtc";
            ethTokenInfo.contract_address = "8febf7551eea6ce499f96537ae0e2075c5a7301a";

            ethTokenDisplay = new EthTokenDisplay();
            ethTokenDisplay.tokenInfo = ethTokenInfo;
            ethTokenDisplay.icon_resid = R.drawable.ic_eth;
            //ethTokenDisplay.str_ether_count = "--- mtc";
            //ethTokenDisplay.str_total_price = "---";
            ethTokenDisplay.setBalance(null);

            ethTokenDisplayList.add(ethTokenDisplay);
        }

        ethTokenDisplayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.tv_toolbar_title: {
                EthWalletDetailsActivity.startActivity(this, ethWalletInfo);
                break;
            }

            case R.id.tv_edit: {
                tvWalletListEdit.setVisibility(View.GONE);
                tvWalletListEditDone.setVisibility(View.VISIBLE);
                ethWalletDisplayAdapter.setState(EthWalletDisplayAdapter.STATE_EDIT);
                break;
            }

            case R.id.tv_edit_done: {
                tvWalletListEdit.setVisibility(View.VISIBLE);
                tvWalletListEditDone.setVisibility(View.GONE);
                ethWalletDisplayAdapter.setState(EthWalletDisplayAdapter.STATE_NORMAL);
                break;
            }

            case R.id.tv_wallet_new: {
                Intent intent = new Intent(this, EthWalletGenNewActivity.class);
                startActivityForResult(intent, 0);
                break;
            }

            case R.id.tv_wallet_import: {
                Intent intent = new Intent(this, EthWalletGenImportActivity.class);
                startActivityForResult(intent, 0);
                break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.listView) {
            EthTokenDisplay ethTokenDisplay = (EthTokenDisplay) parent.getItemAtPosition(position);
            EthTokenInfo ethTokenInfo = ethTokenDisplay.tokenInfo;
            EthTransactionListActivity.startActivity(this, ethTokenInfo.wallet_address, ethTokenInfo.contract_address);
            //showTokenPopMenu(view, ethTokenDisplay);
        } else if (parent.getId() == R.id.listview_wallet) {
            drawerLayout.closeDrawers();
            EthWalletDisplay ethWalletDisplay = (EthWalletDisplay) parent.getItemAtPosition(position);
            appPref.setWalletAddress(ethWalletDisplay.walletInfo.address);
            loadDefaultWallet();
            initWalletView();
            refreshWalletInfo();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.listView) {
            EthTokenDisplay ethTokenDisplay = (EthTokenDisplay) parent.getItemAtPosition(position);
            showTokenPopMenu(view, ethTokenDisplay);
        }
        return true;
    }

    private void showTokenPopMenu(final View view, final EthTokenDisplay ethTokenDisplay) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_token_operation);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_action_send: {
                        /*
                        Intent intent = new Intent(MainActivity.this, EthSendActivity.class);
                        startActivity(intent);
                        */
                        EthTokenInfo tokenInfo = ethTokenDisplay.tokenInfo;
                        EthSendActivity.startActivity(MainActivity.this, tokenInfo.contract_address, tokenInfo.wallet_address, "");
                        break;
                    }

                    case R.id.menu_action_receive: {
                        Intent intent = new Intent(MainActivity.this, EthReceiveActivity.class);
                        intent.putExtra(EthReceiveActivity.ADDRESS, "0x" + ethTokenDisplay.tokenInfo.wallet_address);
                        startActivity(intent);
                        break;
                    }
                }
                return true;
            }
        });
        popupMenu.setGravity(Gravity.END | Gravity.TOP);
        popupMenu.show();
    }

    private void refreshWalletInfo() {
        requestQueue.clear();

        for (EthTokenDisplay tokenDisplay: ethTokenDisplayList) {
            tokenDisplay.setBalance(null);
        }
        ethTokenDisplayAdapter.notifyDataSetChanged();

        if (ethWalletInfo != null) {
            ethService.getPriceConversionRates();
            ethService.getEtherPrice();
        }
    }

    private LinkedList<Runnable> requestQueue = new LinkedList<>();

    private void doNextRequest() {
        if (requestQueue.isEmpty())
            return;
        Runnable runnable = requestQueue.removeFirst();
        runnable.run();
    }

    private EthServiceListener ethServiceListener = new EthServiceListener() {

        @Override
        public void onGetEtherPriceSuccess(Map<String, Double> priceMap) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ethWalletInfo != null) {
                        for (final EthTokenDisplay tokenDisplay : ethTokenDisplayList) {
                            final EthTokenInfo tokenInfo = tokenDisplay.tokenInfo;
                            if (tokenInfo.wallet_address.equals(tokenInfo.contract_address)) {
                                requestQueue.add(new Runnable() {
                                    @Override
                                    public void run() {
                                        ethService.getBalance(tokenInfo.wallet_address);
                                    }
                                });
                            } else {
                                requestQueue.add(new Runnable() {
                                    @Override
                                    public void run() {
                                        ethService.getTokenBalance(tokenInfo.contract_address, tokenInfo.wallet_address);
                                    }
                                });
                            }
                        }
                    }
                    doNextRequest();
                }
            });

        }

        @Override
        public void onGetEtherPriceError(Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doNextRequest();
                }
            });
        }

        @Override
        public void onGetBalanceSuccess(final String walletAddress, final BigDecimal value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ethWalletInfo != null) {
                        for (EthTokenDisplay tokenDisplay : ethTokenDisplayList) {
                            EthTokenInfo tokenInfo = tokenDisplay.tokenInfo;
                            if (tokenInfo.wallet_address.equals(walletAddress) && tokenInfo.wallet_address.equals(tokenInfo.contract_address)) {
                                //tokenDisplay.str_total_price = "1232456789";
                                //tokenDisplay.str_ether_count = "987654321";
                                tokenDisplay.setBalance(value);
                                ethTokenDisplayAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }

                    doNextRequest();
                }
            });

        }

        @Override
        public void onGetBalanceError(String walletAddress, Exception e) {
            System.out.println(e.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doNextRequest();
                }
            });

        }

        @Override
        public void onGetTokenBalanceSuccess(final String contractAddress, final String address, final BigDecimal value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ethWalletInfo != null) {
                        for (EthTokenDisplay tokenDisplay : ethTokenDisplayList) {
                            EthTokenInfo tokenInfo = tokenDisplay.tokenInfo;
                            if (tokenInfo.wallet_address.equals(address) && tokenInfo.contract_address.equals(contractAddress)) {
                                //tokenDisplay.str_total_price = "$ 0";
                                //tokenDisplay.str_ether_count = "0 ether";
                                tokenDisplay.setBalance(value);
                                ethTokenDisplayAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                    doNextRequest();
                }
            });
        }

        @Override
        public void onGetTokenBalanceError(String contractAddress, String address, Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doNextRequest();
                }
            });

        }

        @Override
        public void onGetPriceConversionRates(final Map<String, Double> ratesMap) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    priceRateMap.clear();
                    priceRateMap.putAll(ratesMap);
                    doNextRequest();
                }
            });
        }

        @Override
        public void onGetPriceConversionRatesError(Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doNextRequest();
                }
            });
        }

        @Override
        public void onGetGasPriceSuccess(BigInteger value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doNextRequest();
                }
            });
        }

        @Override
        public void onGetGasPriceError(Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doNextRequest();
                }
            });
        }

        @Override
        public void onSendRawTransactionSuccess(EthTransactionData transactionData, String txHash) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doNextRequest();
                }
            });
        }

        @Override
        public void onSendRawTransactionError(EthTransactionData transactionData, Exception e, String errorString) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doNextRequest();
                }
            });
        }

    };

    @Override
    public void onDialogFragmentDismiss(DialogFragment dialogFragment) {

    }

    @Override
    public void onDialogFragmentCancel(DialogFragment dialogFragment) {

    }

    @Override
    public void onDialogFragmentClick(DialogFragment dialogFragment, View view) {
        if (REQUEST_TAG_WALLET_DELETE_CONFIRM_DIALOG.equals(dialogFragment.getTag())) {
            String wallet_address = dialogFragment.getArguments().getString(WalletDeleteConfirmDialogFragment.KEY_WALLET_ADDRESS);
            switch (view.getId()) {
                case R.id.btn_delete: {
                    ethWalletInfoDB.removeWallet(wallet_address);
                    ethWalletDisplayAdapter.remove(wallet_address);
                    break;
                }

                case R.id.btn_export: {
                    export_wallet_address = wallet_address;
                    EthWalletExportDialogFragment.newInstance().show(getSupportFragmentManager(), REQUEST_TAG_EXPORT_DIALOG);
                    break;
                }
            }
        } else if (REQUEST_TAG_EXPORT_DIALOG.equals(dialogFragment.getTag())) {
            switch (view.getId()) {
                case R.id.tv_keystore: {
                    EthWalletExportKeystoreActivity.startActivity(this, export_wallet_address);
                    break;
                }

                case R.id.tv_private_key: {
                    EthWalletExportPrivateKeyActivity.startActivity(this, export_wallet_address);
                    break;
                }
            }
        }
    }

    @Override
    public void onDialogFragmentResult(DialogFragment dialogFragment, Bundle bundle) {

    }
}
