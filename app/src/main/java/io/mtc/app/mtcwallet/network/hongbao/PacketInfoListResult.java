package io.mtc.app.mtcwallet.network.hongbao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by admin on 2018/2/5.
 */

public class PacketInfoListResult {
    public List<PacketInfo> packetInfoList = new LinkedList<>();

    public static PacketInfoListResult from(JSONObject jsonObject) throws JSONException {

        JSONArray packetArray = jsonObject.getJSONArray("result");
        PacketInfoListResult result = new PacketInfoListResult();

        for (int i = 0; i < packetArray.length(); ++i) {
            result.packetInfoList.add(PacketInfo.from(packetArray.getJSONObject(i)));
        }

        return result;
    }
}
