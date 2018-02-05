package io.mtc.app.mtcwallet.network.hongbao;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2018/2/4.
 */

public class PacketStatusResult {

    public Result result;
    public Data data;

    public static PacketStatusResult from(JSONObject jsonObject) throws JSONException {
        PacketStatusResult result = new PacketStatusResult();
        result.result = Result.from(jsonObject.getJSONObject("result"));
        result.data = Data.from(jsonObject.getJSONObject("data"));
        return result;
    }

    public static class Result {
        public String id;
        public String accountId;
        public String address;
        public String total;
        public String currency;
        public String title;
        public int quantity;
        public int state;
        public long createdTime;


        public static Result from(JSONObject jsonObject) throws JSONException {
            Result result = new Result();
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

    public static class Data {
        public int winners;
        public String consumption;

        public static Data from(JSONObject jsonObject) throws JSONException {
            Data data = new Data();
            data.winners = jsonObject.getInt("winners");
            data.consumption = jsonObject.getString("consumption");
            return data;
        }

    }
}
