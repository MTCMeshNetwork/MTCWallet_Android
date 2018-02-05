package io.mtc.app.mtcwallet.network.hongbao;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2018/2/4.
 */

public class PacketInfo {
    public String id;
    public String accountId;
    public String address;
    public String total;
    public String currency;
    public String title;
    public int quantity;
    public int state;
    public long createdTime;

    public static PacketInfo from(JSONObject jsonObject) throws JSONException {
        PacketInfo result = new PacketInfo();
        result.id = jsonObject.getString("id");
        result.accountId = jsonObject.getString("accountId");
        result.address = jsonObject.getString("address");
        result.total = jsonObject.getString("total");
        result.currency = jsonObject.getString("currency");
        result.title = jsonObject.getString("title");
        result.quantity = jsonObject.getInt("quantity");
        result.state = jsonObject.getInt("state");
        result.createdTime = jsonObject.getLong("createdTime");
        return result;
    }
}
