package io.mtc.app.mtcwallet.network.hongbao;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2018/2/4.
 */

public class PacketCreateResult {

    public String id;
    public String accountId;
    public String address;
    public String total;
    public String currency;
    public int quantity;
    public String title;

    public static PacketCreateResult from(JSONObject jsonObject) throws JSONException {
        PacketCreateResult result = new PacketCreateResult();
        result.id = jsonObject.getString("id");
        result.accountId = jsonObject.getString("accountId");
        result.address = jsonObject.getString("address");
        result.total = jsonObject.getString("total");
        result.currency = jsonObject.getString("currency");
        result.quantity = jsonObject.getInt("quantity");
        result.title = jsonObject.getString("title");
        return result;
    }

}
