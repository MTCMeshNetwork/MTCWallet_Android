package io.mtc.app.mtcwallet.network.hongbao;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2018/2/4.
 */

public class CurrencyInfo {
    public String id;
    public String address;
    public String currency;
    public String balance;

    public static CurrencyInfo from(JSONObject jsonObject) throws JSONException {
        CurrencyInfo result = new CurrencyInfo();
        result.id = jsonObject.getString("id");
        result.address = jsonObject.getString("address");
        result.currency = jsonObject.getString("currency");
        result.balance = jsonObject.getString("balance");
        return result;
    }
}
