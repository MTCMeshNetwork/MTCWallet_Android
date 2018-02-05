package io.mtc.app.mtcwallet.activities.hongbao;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import java.util.LinkedList;
import java.util.List;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.activities.hongbao.fragment.HongBaoDistributeParamsFragment;
import io.mtc.app.mtcwallet.activities.hongbao.fragment.HongBaoNearbyListFragment;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;
import io.mtc.app.mtcwallet.network.hongbao.PacketCreateParams;

public class HongBaoMainActivity extends AppCompatActivity {

    public final static String KEY_WALLET_ADDRESS = "key_wallet_address";
    public final static String KEY_WALLET_COIN_INFO = "key_wallet_coin_info";

    public static void startActivity(Context context, String walletAddress, WalletCoinInfo walletCoinInfo) {
        Intent intent = new Intent(context, HongBaoMainActivity.class);
        intent.putExtra(KEY_WALLET_ADDRESS, walletAddress);
        intent.putExtra(KEY_WALLET_COIN_INFO, walletCoinInfo);
        context.startActivity(intent);
    }

    private String walletAddress;
    private WalletCoinInfo walletCoinInfo;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private List<Fragment> fragmentList = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hong_bao_main);

        walletAddress = getIntent().getStringExtra(KEY_WALLET_ADDRESS);
        walletCoinInfo = getIntent().getParcelableExtra(KEY_WALLET_COIN_INFO);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        fragmentList.add(HongBaoNearbyListFragment.newInstance());
        fragmentList.add(HongBaoDistributeParamsFragment.newInstance(walletAddress, walletCoinInfo));

        setFragment(fragmentList.get(0));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hongbao, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_view, fragment)
                .commit();
    }

    private TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            setFragment(fragmentList.get(tab.getPosition()));
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };


}
