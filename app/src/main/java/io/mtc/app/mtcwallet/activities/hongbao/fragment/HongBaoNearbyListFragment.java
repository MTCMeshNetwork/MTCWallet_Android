package io.mtc.app.mtcwallet.activities.hongbao.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.activities.hongbao.adapter.HongBaoPacketAdapter;
import io.mtc.app.mtcwallet.activities.hongbao.data.HongBaoPacketItem;
import io.mtc.app.mtcwallet.network.hongbao.HongBaoAPI;
import io.mtc.app.mtcwallet.network.hongbao.PacketInfo;
import io.mtc.app.mtcwallet.network.hongbao.PacketInfoListResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class HongBaoNearbyListFragment extends Fragment {

    private SwipeMenuRecyclerView swipeMenuRecyclerView;

    private HongBaoPacketAdapter packetListAdapter;
    private List<PacketInfo> packetItemList = new LinkedList<>();
    private HongBaoAPI hongBaoAPI = new HongBaoAPI();
    private Handler handler = new Handler();

    public HongBaoNearbyListFragment() {

    }

    public static HongBaoNearbyListFragment newInstance() {
        HongBaoNearbyListFragment fragment = new HongBaoNearbyListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        getHongBaoList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hong_bao_nearby_list, container, false);
        swipeMenuRecyclerView = (SwipeMenuRecyclerView) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        packetListAdapter = new HongBaoPacketAdapter(getContext(), packetItemList);
        swipeMenuRecyclerView.setAdapter(packetListAdapter);
        return rootView;
    }

    private void getHongBaoList() {
        try {
            hongBaoAPI.getPacketList(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onGetHongBaoListFail("获取周边红包信息失败!");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String content = response.body().string();
                            JSONObject jsonObject = new JSONObject(content);
                            int code = jsonObject.getInt("code");

                            if (code == 0) {
                                onGetHongBaoListSuccess(PacketInfoListResult.from(new JSONObject(content)).packetInfoList);
                            } else {
                                onGetHongBaoListFail("获取周边红包信息失败! " + jsonObject.optString("message"));
                            }
                        } catch (Exception e) {
                            onGetHongBaoListFail("获取周边红包信息失败! " + e.getMessage());
                        }
                    } else {
                        onGetHongBaoListFail("获取周边红包信息失败! " + response.code());
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onGetHongBaoListSuccess(final List<PacketInfo> packetInfoList) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                packetItemList.clear();
                packetItemList.addAll(packetInfoList);
                packetListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void onGetHongBaoListFail(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                showToast(msg);
            }
        });
    }

    private void openHongBao() {

    }

    private Toast mToast;
    private void showToast(String s) {
        if (mToast == null) {
            mToast = Toast.makeText(getContext(), s, Toast.LENGTH_LONG);
        } else {
            mToast.setText(s);
        }
        mToast.show();
    }

}
