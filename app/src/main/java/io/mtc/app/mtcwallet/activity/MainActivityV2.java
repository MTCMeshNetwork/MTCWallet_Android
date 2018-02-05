package io.mtc.app.mtcwallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.activities.hongbao.HongBaoMainActivity;
import io.mtc.app.mtcwallet.adapter.AdapterOnClickListener;
import io.mtc.app.mtcwallet.adapter.WalletCoinAdapter;
import io.mtc.app.mtcwallet.adapter.WalletDisplayAdapter;
import io.mtc.app.mtcwallet.data.AppPref;
import io.mtc.app.mtcwallet.data.Constants;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;
import io.mtc.app.mtcwallet.data.WalletDisplayInfo;
import io.mtc.app.mtcwallet.database.EthWalletInfo;
import io.mtc.app.mtcwallet.database.EthWalletInfoDB;
import io.mtc.app.mtcwallet.dialog.DialogFragmentCallback;
import io.mtc.app.mtcwallet.dialog.EthWalletExportDialogFragment;
import io.mtc.app.mtcwallet.dialog.SimpleConfirmDialogFragment;
import io.mtc.app.mtcwallet.dialog.WalletManageDialogFragment;
import io.mtc.app.mtcwallet.dialog.WalletOperConfirmDialogFragmentV2;
import io.mtc.app.mtcwallet.network.DefaultEthServiceListener;
import io.mtc.app.mtcwallet.network.EthService;
import io.mtc.app.mtcwallet.network.EthServiceFactory;
import io.mtc.app.mtcwallet.utils.MTCWalletUtils;

public class MainActivityV2 extends AppCompatActivity implements View.OnClickListener, DialogFragmentCallback {

    public final static String REQUEST_TAG_WALLET_OPER_CONFIRM_DIALOG = "tag_wallet_oper_confirm_dialog";
    public final static String REQUEST_TAG_WALLET_MANAGE_CONFIRM_DIALOG = "tag_wallet_manage_confirm_dialog";
    public final static String REQUEST_TAG_EXPORT_DIALOG = "request_tag_export_dialog";
    public final static String TAG_EXIT_CONFIRM = "tag_exit_confirm";

    private Toolbar toolbar;
    private TextView tvTitle;
    private TextView tvTotal;
    private TextView tvWalletAddress;
    private SwipeRefreshLayout swipeRefreshLayout;

    private DrawerLayout drawerLayout;
    private View vWalletListEdit;
    private View vWalletListEditDone;

    private EthWalletInfo ethWalletInfo = null;
    private List<EthWalletInfo> walletInfoList = new LinkedList<>();

    private SwipeMenuRecyclerView walletCoinListView;
    private WalletCoinAdapter walletCoinAdapter;
    private List<WalletCoinInfo> walletCoinInfoList = new LinkedList<>();

    private SwipeMenuRecyclerView walletDisplayListView;
    private WalletDisplayAdapter walletDisplayAdapter;
    private List<WalletDisplayInfo> walletDisplayInfoList = new LinkedList<>();

    private AppPref appPref;
    private EthWalletInfoDB ethWalletInfoDB;
    private EthService ethService;
    private LinkedList<EthWalletInfo> walletPriceRequestList = new LinkedList<>();
    private Handler handler = new Handler();

    private String export_wallet_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //  NavigationView
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        vWalletListEdit =  findViewById(R.id.iv_edit);
        vWalletListEditDone = findViewById(R.id.iv_edit_done);


        findViewById(R.id.tv_wallet_new).setOnClickListener(this);
        findViewById(R.id.tv_wallet_import).setOnClickListener(this);

        walletDisplayListView = (SwipeMenuRecyclerView) findViewById(R.id.listView_Wallet);
        walletDisplayListView.setLayoutManager(new LinearLayoutManager(this));
        walletDisplayAdapter = new WalletDisplayAdapter(this, walletDisplayInfoList);
        walletDisplayAdapter.setClickListener(walletDisplayAdapterListener);
        walletDisplayListView.setAdapter(walletDisplayAdapter);

        walletDisplayListView.setOnItemMoveListener(walletDisplayItemMoveListener);

        vWalletListEdit.setOnClickListener(this);
        vWalletListEditDone.setOnClickListener(this);

        //  ContentView
        tvTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvTotal = (TextView) findViewById(R.id.tv_total);
        tvWalletAddress = (TextView) findViewById(R.id.tv_wallet_address);
        tvTitle.setOnClickListener(this);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(walletCoinRefreshListener);

        walletCoinListView = (SwipeMenuRecyclerView) findViewById(R.id.listView);
        walletCoinListView.setLayoutManager(new LinearLayoutManager(this));
        walletCoinAdapter = new WalletCoinAdapter(this, walletCoinInfoList);
        walletCoinAdapter.setClickListener(walletCoinAdapterListener);
        walletCoinListView.setAdapter(walletCoinAdapter);

        appPref = new AppPref(this);
        ethWalletInfoDB = new EthWalletInfoDB(this);
        ethService = EthServiceFactory.createDefaultService(this, defaultEthServiceListener);

        loadWalletList();

        if (!loadDefaultWallet()) {
            startEthWalletGenGuideActivity();
            return;
        }

        swipeRefreshLayout.setRefreshing(true);
        ethService.getWalletCoinList(ethWalletInfo.address);
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_WALLET_GEN_GUIDE_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                loadDefaultWallet();
            }
            loadWalletList();
            if (this.ethWalletInfo == null) {
                finish();
                return;
            }
        }
    }

    private void loadWalletList() {
        walletInfoList.clear();
        walletInfoList.addAll(ethWalletInfoDB.getWalletList());

        walletPriceRequestList.clear();
        walletPriceRequestList.addAll(walletInfoList);

        walletDisplayInfoList.clear();
        for (EthWalletInfo walletInfo: walletInfoList) {
            WalletDisplayInfo walletDisplayInfo = new WalletDisplayInfo();
            walletDisplayInfo.walletInfo = walletInfo;
            walletDisplayInfoList.add(walletDisplayInfo);
        }
        walletDisplayAdapter.notifyDataSetChanged();

    }

    private boolean loadDefaultWallet() {
        EthWalletInfo walletInfo = null;
        String walletAddress = appPref.getWalletAddress();
        walletInfo = ethWalletInfoDB.getWalletInfo(walletAddress);
        if (walletInfo == null) {
            List<EthWalletInfo> ethWalletInfoList = ethWalletInfoDB.getWalletList();
            if (!ethWalletInfoList.isEmpty()) {
                walletInfo = ethWalletInfoList.get(0);
                appPref.setWalletAddress(MTCWalletUtils.getNoPrefixAddress(walletInfo.address));
            }
        }

        if (walletInfo == null)
            return false;

        this.ethWalletInfo = walletInfo;
        tvTitle.setText(this.ethWalletInfo.name);

        return true;
    }

    private void clearWalletCointPriceView() {
        tvTotal.setText("0.00");
        for (WalletCoinInfo walletCoinInfo: walletCoinInfoList) {
            walletCoinInfo.setBalance("0");
            walletCoinInfo.update();
        }
        walletCoinAdapter.notifyDataSetChanged();
    }

    private void startEthWalletGenGuideActivity() {
        Intent intent = new Intent(this, EthWalletGenGuideActivity.class);
        startActivityForResult(intent, Constants.REQUEST_WALLET_GEN_GUIDE_ACTIVITY);
    }

    private DefaultEthServiceListener defaultEthServiceListener = new DefaultEthServiceListener() {

        @Override
        public void onGetWalletCointListSuccess(final String walletAddress, final List<WalletCoinInfo> resultList) {
            super.onGetWalletCointListSuccess(walletAddress, walletCoinInfoList);

            BigDecimal totalPrice = BigDecimal.ZERO;
            BigDecimal totalPriceCNY = BigDecimal.ZERO;
            for (WalletCoinInfo walletCoinInfo: resultList) {
                totalPrice = totalPrice.add(walletCoinInfo.getTotalPrice());
                totalPriceCNY = totalPriceCNY.add(walletCoinInfo.getTotalPriceCNY());
            }

            final BigDecimal finalTotalPriceCNY = totalPriceCNY;
            final BigDecimal finalTotalPrice = totalPrice;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ethWalletInfo != null && walletAddress.equalsIgnoreCase(ethWalletInfo.address)) {
                        walletCoinInfoList.clear();
                        walletCoinInfoList.addAll(resultList);
                        walletCoinAdapter.notifyDataSetChanged();
                        tvTotal.setText(finalTotalPriceCNY.setScale(2, RoundingMode.HALF_UP).toPlainString());
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    for (WalletDisplayInfo walletDisplayInfo: walletDisplayInfoList) {
                        if (walletDisplayInfo.walletInfo.address.equalsIgnoreCase(walletAddress)) {
                            walletDisplayInfo.setTotalPrice(finalTotalPrice);
                            walletDisplayInfo.setTotalPriceCNY(finalTotalPriceCNY);
                            walletDisplayAdapter.notifyDataSetChanged();
                            break;
                        }
                    }

                    if (!walletPriceRequestList.isEmpty()) {
                        EthWalletInfo nextWalletInfo = walletPriceRequestList.pop();
                        ethService.getWalletCoinList(nextWalletInfo.address);
                    }
                }
            });
        }

        @Override
        public void onGetWalletCointListError(String walletAddress, Exception e) {
            super.onGetWalletCointListError(walletAddress, e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    };

    private SwipeRefreshLayout.OnRefreshListener walletCoinRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (ethWalletInfo == null)
                swipeRefreshLayout.setRefreshing(false);

            ethService.getWalletCoinList(ethWalletInfo.address);
        }
    };

    private AdapterOnClickListener walletCoinAdapterListener = new AdapterOnClickListener() {
        @Override
        public void onClick(View view, int position) {
            WalletCoinInfo walletCoinInfo = walletCoinInfoList.get(position);
            String wallet_address = ethWalletInfo.address;
            String contract_address = MTCWalletUtils.getNoPrefixAddress(walletCoinInfo.address).trim();
            if (contract_address.equals("0")) {
                contract_address = "";
            }
            switch (view.getId()) {
                case R.id.content_view: {
                    EthTransactionListActivityV2.startActivity(MainActivityV2.this, wallet_address, walletCoinInfo);
                    break;
                }

                case R.id.btn_send: {
                    if (TextUtils.isEmpty(contract_address)) {
                        EthSendActivity.startActivity(MainActivityV2.this, contract_address, wallet_address, "");
                        //EthSendActivityV2.startActivity(MainActivityV2.this, wallet_address, ethWalletInfo.name, "", walletCoinInfo);
                    } else {
                        EthContractSendActivity.startActivity(MainActivityV2.this, contract_address, wallet_address, "");
                        //EthContractSendActivityV2.startActivity(MainActivityV2.this, wallet_address, ethWalletInfo.name, "", walletCoinInfo);
                    }
                    break;
                }

                case R.id.btn_receive: {
                    Intent intent = new Intent(MainActivityV2.this, EthReceiveActivity.class);
                    intent.putExtra(EthReceiveActivity.ADDRESS, MTCWalletUtils.getNoPrefixAddress(ethWalletInfo.address));
                    startActivity(intent);
                    break;
                }
            }
        }
    };

    private AdapterOnClickListener walletDisplayAdapterListener = new AdapterOnClickListener() {
        @Override
        public void onClick(View view, int position) {
            WalletDisplayInfo walletDisplayInfo = walletDisplayInfoList.get(position);
            switch (view.getId()) {

                case R.id.content_view: {
                    drawerLayout.closeDrawers();
                    appPref.setWalletAddress(walletDisplayInfo.walletInfo.address);
                    clearWalletCointPriceView();
                    loadDefaultWallet();
                    ethService.getWalletCoinList(ethWalletInfo.address);
                    swipeRefreshLayout.setRefreshing(true);
                    break;
                }

                case R.id.iv_remove: {
                    WalletOperConfirmDialogFragmentV2.newInstance(walletDisplayInfo.walletInfo.address, "delete").show(getSupportFragmentManager(), REQUEST_TAG_WALLET_OPER_CONFIRM_DIALOG);
                    break;
                }

                case R.id.btn_manage: {
                    WalletManageDialogFragment.newInstance(walletDisplayInfo.walletInfo.address).show(getSupportFragmentManager(), REQUEST_TAG_WALLET_MANAGE_CONFIRM_DIALOG);
                    break;
                }
            }

        }
    };

    private OnItemMoveListener walletDisplayItemMoveListener = new OnItemMoveListener() {
        @Override
        public boolean onItemMove(RecyclerView.ViewHolder srcHolder, RecyclerView.ViewHolder targetHolder) {
            int fromPosition = srcHolder.getAdapterPosition();
            int toPosition = targetHolder.getAdapterPosition();
            Collections.swap(walletDisplayInfoList, fromPosition, toPosition);
            walletDisplayAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(RecyclerView.ViewHolder srcHolder) {

        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_edit: {
                vWalletListEdit.setVisibility(View.GONE);
                vWalletListEditDone.setVisibility(View.VISIBLE);
                walletDisplayListView.setLongPressDragEnabled(true);
                walletDisplayAdapter.setEditState(1);
                break;
            }

            case R.id.iv_edit_done: {
                vWalletListEdit.setVisibility(View.VISIBLE);
                vWalletListEditDone.setVisibility(View.GONE);
                walletDisplayListView.setLongPressDragEnabled(false);
                walletDisplayAdapter.setEditState(0);
                break;
            }

            case R.id.tv_wallet_new: {
                Intent intent = new Intent(this, EthWalletGenNewActivity.class);
                startActivityForResult(intent, Constants.REQUEST_WALLET_GEN_GUIDE_ACTIVITY);
                break;
            }

            case R.id.tv_wallet_import: {
                Intent intent = new Intent(this, EthWalletGenImportActivity.class);
                startActivityForResult(intent, Constants.REQUEST_WALLET_GEN_GUIDE_ACTIVITY);
                break;
            }

            case R.id.tv_toolbar_title: {
                /*
                Intent intent = new Intent(this, HongBaoMainActivity.class);
                startActivity(intent);
                */
                HongBaoMainActivity.startActivity(this, ethWalletInfo.address, walletCoinInfoList.get(1));
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
        if (REQUEST_TAG_WALLET_MANAGE_CONFIRM_DIALOG.equals(dialogFragment.getTag())) {
            String wallet_address = dialogFragment.getArguments().getString(WalletManageDialogFragment.KEY_WALLET_ADDRESS);
            switch (view.getId()) {
                case R.id.btn_delete: {
                    WalletOperConfirmDialogFragmentV2.newInstance(wallet_address, "delete").show(getSupportFragmentManager(), REQUEST_TAG_WALLET_OPER_CONFIRM_DIALOG);
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
        } else if (TAG_EXIT_CONFIRM.equals(dialogFragment.getTag())) {
            if (view.getId() != R.id.btn_ok) {
                return;
            }
            finish();
        }
    }

    @Override
    public void onDialogFragmentResult(DialogFragment dialogFragment, Bundle bundle) {
        if (REQUEST_TAG_WALLET_OPER_CONFIRM_DIALOG.equals(dialogFragment.getTag())) {
            String wallet_address = dialogFragment.getArguments().getString(WalletOperConfirmDialogFragmentV2.KEY_WALLET_ADDRESS);
            String wallet_oper = dialogFragment.getArguments().getString(WalletOperConfirmDialogFragmentV2.KEY_WALLET_OPER);
            if ("delete".equals(wallet_oper)) {
                ethWalletInfoDB.removeWallet(wallet_address);
                walletDisplayAdapter.remove(wallet_address);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                drawerLayout.closeDrawers();
            } else {
                SimpleConfirmDialogFragment.newInstance("提醒", "是否确认退出?").show(getSupportFragmentManager(), TAG_EXIT_CONFIRM);
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


}
