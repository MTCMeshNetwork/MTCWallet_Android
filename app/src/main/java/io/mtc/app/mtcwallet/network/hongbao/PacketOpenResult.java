package io.mtc.app.mtcwallet.network.hongbao;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2018/2/4.
 */

public class PacketOpenResult {

    public String id;
    public String packetId;
    public String address;
    public String amount;
    public long createdTime;

    public static PacketOpenResult from(JSONObject jsonObject) throws JSONException {
        PacketOpenResult result = new PacketOpenResult();
        result.id = jsonObject.getString("id");
        result.packetId = jsonObject.getString("packetId");
        result.address = jsonObject.getString("address");
        result.amount = jsonObject.getString("amount");
        result.createdTime = jsonObject.getLong("createdTime");
        return result;
    }
}
