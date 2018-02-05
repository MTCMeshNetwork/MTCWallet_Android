package io.mtc.app.mtcwallet.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.adapter.AdapterOnClickListener;
import io.mtc.app.mtcwallet.adapter.TransactionAdapter;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;
import io.mtc.app.mtcwallet.database.TransactionInfo;
import io.mtc.app.mtcwallet.database.TransactionInfoDB;
import io.mtc.app.mtcwallet.database.TransactionInfoV2;
import io.mtc.app.mtcwallet.database.TransactionInfoV2Container;
import io.mtc.app.mtcwallet.dialog.DialogFragmentCallback;
import io.mtc.app.mtcwallet.dialog.EthWalletExportDialogFragment;
import io.mtc.app.mtcwallet.effect.CircleTransform;
import io.mtc.app.mtcwallet.network.DefaultEthServiceListener;
import io.mtc.app.mtcwallet.network.EthService;
import io.mtc.app.mtcwallet.network.EthServiceFactory;
import io.mtc.app.mtcwallet.utils.MTCWalletUtils;

public class EthTransactionListActivityV2 extends AppCompatActivity implements View.OnClickListener {

    public final static String REQUEST_TAG_EXPORT_DIALOG = "request_tag_export_dialog";
    public final static String KEY_WALLET_COIN_INFO = "key_wallet_coin_info";
    public final static String KEY_WALLET_ADDRESS = "key_wallet_address";

    private Toolbar toolbar;
    private ImageView ivToolbarIcon;
    private TextView tvToolbarTitle;
    private TextView tvBalance;
    private TextView tvWalletAddress;

    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeMenuRecyclerView mRecyclerView;

    private WalletCoinInfo walletCoinInfo;
    private String walletAddress;
    private String contractAddress;

    private EthService ethService;

    private TransactionInfoV2Container transactionContainer;
    private List<TransactionInfoV2> transactionInfoList = new LinkedList<>();
    private TransactionAdapter transactionAdapter;

    //private List<TransactionInfo> transactionInfoList = new LinkedList<>();
    //private ArrayAdapter<TransactionInfo> transactionInfoAdapter;
    private Handler handler = new Handler();

    public static void startActivity(Context context, String wallet_address, WalletCoinInfo wallet_coin_info) {
        Intent intent = new Intent(context, EthTransactionListActivityV2.class);
        intent.putExtra(KEY_WALLET_ADDRESS, wallet_address);
        intent.putExtra(KEY_WALLET_COIN_INFO, wallet_coin_info);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_transaction_list_v2);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        walletAddress = getIntent().getStringExtra(KEY_WALLET_ADDRESS);
        walletCoinInfo = getIntent().getParcelableExtra(KEY_WALLET_COIN_INFO);
        contractAddress = MTCWalletUtils.getNoPrefixAddress(walletCoinInfo.address).trim();
        if (contractAddress.equals("0")) {
            contractAddress = "";
        }

        initView();

        ethService = EthServiceFactory.createDefaultService(this, ethServiceListener);
        swipeRefreshLayout.setRefreshing(true);
        getContent(1);
    }

    private void getContent(long pageIndex) {
        if (TextUtils.isEmpty(contractAddress)) {
            ethService.getBalance(walletAddress);
        } else {
            ethService.getTokenBalance(contractAddress, walletAddress);
        }
        ethService.getTransactionList(walletAddress, contractAddress, pageIndex, 10);
    }

    private void initView() {

        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        ivToolbarIcon = (ImageView) findViewById(R.id.iv_toolbar_icon);
        tvBalance = (TextView) findViewById(R.id.tv_balance);
        tvWalletAddress = (TextView) findViewById(R.id.tv_wallet_address);

        mRecyclerView = (SwipeMenuRecyclerView) findViewById(R.id.listView);
        mRecyclerView.useDefaultLoadMore(); // 使用默认的加载更多的View。
        mRecyclerView.setLoadMoreListener(mLoadMoreListener); // 加载更多的监听。
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        transactionAdapter = new TransactionAdapter(this, transactionInfoList);
        transactionAdapter.setWalletAddress(MTCWalletUtils.getPrefixAddress(walletAddress));
        transactionAdapter.setUnitName(walletCoinInfo.unit_name);
        transactionAdapter.setClickListener(transactionOnClickListener);
        mRecyclerView.setAdapter(transactionAdapter);

        transactionInfoList.clear();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(mRefreshListener);
        findViewById(R.id.btn_send).setOnClickListener(this);
        findViewById(R.id.btn_recevice).setOnClickListener(this);

        try {
            Picasso.with(this).load(Uri.parse(walletCoinInfo.imageURL))
                    .resize(64, 64)
                    .transform(new CircleTransform(this))
                    .into(ivToolbarIcon);
        } catch (Exception e) {}

        tvToolbarTitle.setText(walletCoinInfo.unit_name + "余额");
        tvWalletAddress.setText(MTCWalletUtils.getPrefixAddress(walletAddress));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send: {
                if (TextUtils.isEmpty(contractAddress)) {
                    EthSendActivity.startActivity(this, contractAddress, walletAddress, "");
                } else {
                    EthContractSendActivity.startActivity(this, contractAddress, walletAddress, "");
                }
                break;
            }

            case R.id.btn_recevice: {
                EthReceiveActivity.startActivity(this, walletAddress);
                break;
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

    /**
     * 刷新。
     */
    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    transactionInfoList.clear();
                    transactionContainer = null;
                    getContent(1);
                }
            });
        }
    };

    /**
     * 加载更多。
     */
    private SwipeMenuRecyclerView.LoadMoreListener mLoadMoreListener = new SwipeMenuRecyclerView.LoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (transactionContainer == null) {
                getContent(1);
            } else if (transactionContainer.pageNum < transactionContainer.pageSize) {
                getContent(transactionContainer.pageNum+1);
            } else {
                mRecyclerView.loadMoreFinish(true, false);
            }
        }
    };

    private AdapterOnClickListener transactionOnClickListener = new AdapterOnClickListener() {
        @Override
        public void onClick(View view, int position) {
            TransactionInfoV2 transactionInfo = transactionInfoList.get(position);
            //TransactionInfo transactionInfo = new TransactionInfo();
            //transactionInfo.address_from = info.from;
            //transactionInfo.address_to = info.to;
            //transactionInfo.contract_address = contractAddress;
            //transactionInfo.txhash = info.hash;
            //EthTransactionDetailsActivity.startActivity(EthTransactionListActivityV2.this, transactionInfo);
            EthTransactionInfoActivity.startActivity(EthTransactionListActivityV2.this, walletCoinInfo, transactionInfo);
        }
    };

    private DefaultEthServiceListener ethServiceListener = new DefaultEthServiceListener() {

        @Override
        public void onGetBalanceSuccess(String walletAddress, final BigDecimal value) {
            super.onGetBalanceSuccess(walletAddress, value);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvBalance.setText(value.setScale(6, RoundingMode.DOWN).toPlainString());
                }
            });
        }

        @Override
        public void onGetBalanceError(String walletAddress, Exception e) {
            super.onGetBalanceError(walletAddress, e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void onGetTokenBalanceSuccess(String contractAddress, String address, final BigDecimal value) {
            super.onGetTokenBalanceSuccess(contractAddress, address, value);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvBalance.setText(value.setScale(6, RoundingMode.DOWN).toPlainString());
                }
            });
        }

        @Override
        public void onGetTokenBalanceError(String contractAddress, String address, Exception e) {
            super.onGetTokenBalanceError(contractAddress, address, e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void onGetTransactionListSuccess(String walletAddress, String contractAddress, long pageIndex, long pageSize, final TransactionInfoV2Container container) {
            super.onGetTransactionListSuccess(walletAddress, contractAddress, pageIndex, pageSize, container);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    long pageNumCmp = transactionContainer == null ? 1 : container.pageNum - transactionContainer.pageNum;
                    boolean hasMore = container.pageNum < container.pages;

                    if (pageNumCmp == 1) {
                        if (container.pageNum == 1) {
                            transactionInfoList.clear();
                        }
                        transactionContainer = container;
                        transactionInfoList.addAll(container.list);
                        mRecyclerView.loadMoreFinish(false, hasMore);
                        transactionAdapter.notifyDataSetChanged();
                    } else if (pageNumCmp == 0){
                        mRecyclerView.loadMoreFinish(true, false);
                    } else {
                        mRecyclerView.loadMoreFinish(true, true);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }

        @Override
        public void onGetTransactionListError(String walletAddress, String contractAddress, long pageIndex, long pageSize, Exception e) {
            super.onGetTransactionListError(walletAddress, contractAddress, pageIndex, pageSize, e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                    mRecyclerView.loadMoreError(0, "访问网络出错!");
                }
            });
        }
    };

}
