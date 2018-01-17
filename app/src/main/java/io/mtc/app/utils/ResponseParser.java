package io.mtc.app.utils;

import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.BigInteger;


public class ResponseParser {

    public static String parseBalance(String response) throws JSONException {
        return parseBalance(response, 7);
    }

    public static String parseBalance(String response, int comma) throws JSONException {
        String balance = new JSONObject(response).getString("result");
        if (balance.equals("0")) return "0";
        return new BigDecimal(balance).divide(new BigDecimal(1000000000000000000d), comma, BigDecimal.ROUND_UP).toPlainString();
    }

    public static BigInteger parseGasPrice(String response) throws Exception {
        String gasprice = new JSONObject(response).getString("result");
        return new BigInteger(gasprice.substring(2), 16);
    }

    public static double parsePriceConversionRate(String response) {
        try {
            JSONObject jo = new JSONObject(response).getJSONObject("rates");
            String key = jo.keys().next();
            return jo.getDouble(key);
        } catch (Exception e) {
            return 1;
        }
    }

}
