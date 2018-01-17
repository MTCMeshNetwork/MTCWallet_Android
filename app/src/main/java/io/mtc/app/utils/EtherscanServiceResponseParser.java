package io.mtc.app.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by admin on 2018/1/9.
 */

public class EtherscanServiceResponseParser {

    public static String parseBalance(String response) throws JSONException {
        return parseBalance(response, 7);
    }

    public static String parseBalance(String response, int comma) throws JSONException {
        String balance = new JSONObject(response).getString("result");
        if (balance.equals("0")) return "0";
        return new BigDecimal(balance).divide(new BigDecimal(1000000000000000000d), comma, BigDecimal.ROUND_UP).toPlainString();
    }


}
