package io.mtc.app.mtcwallet.activities.hongbao.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.activities.hongbao.HongBaoDistributeActivity;
import io.mtc.app.mtcwallet.data.WalletCoinInfo;
import io.mtc.app.mtcwallet.network.hongbao.PacketCreateParams;
import io.mtc.app.mtcwallet.utils.MTCWalletUtils;

public class HongBaoDistributeParamsFragment extends Fragment implements View.OnClickListener {

    public final static String ARG_WALLET_ADDRESS = "arg_wallet_address";
    public final static String ARG_WALLET_COIN_INFO = "arg_wallet_coin_info";

    private String walletAddress;
    private WalletCoinInfo walletCoinInfo;

    public HongBaoDistributeParamsFragment() {

    }

    public static HongBaoDistributeParamsFragment newInstance(String walletAddress, WalletCoinInfo walletCoinInfo) {
        HongBaoDistributeParamsFragment fragment = new HongBaoDistributeParamsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_WALLET_ADDRESS, walletAddress);
        args.putParcelable(ARG_WALLET_COIN_INFO, walletCoinInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            walletAddress = getArguments().getString(ARG_WALLET_ADDRESS);
            walletCoinInfo = getArguments().getParcelable(ARG_WALLET_COIN_INFO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hong_bao_distribute_params, container, false);

        rootView.findViewById(R.id.btn_ok).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok: {
                PacketCreateParams params = new PacketCreateParams();
                params.address = MTCWalletUtils.getPrefixAddress(walletAddress);
                params.quantity = 999;
                params.currency = walletCoinInfo.unit_name;
                params.total = "1000000000";
                params.title = "中华人民共和国万岁!";
                HongBaoDistributeActivity.startActivity(getContext(), walletAddress, walletCoinInfo, params);
                break;
            }
        }
    }
}
