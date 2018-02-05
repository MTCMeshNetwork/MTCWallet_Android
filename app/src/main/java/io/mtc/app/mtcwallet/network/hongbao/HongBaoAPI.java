package io.mtc.app.mtcwallet.network.hongbao;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by admin on 2018/2/4.
 */

public class HongBaoAPI {

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public final static String HTTP_SERVER_URL = "http://192.168.1.189:8080";

    //  查询相应币种信息
    public void getCurrencyInfo(String address, String currency, Callback callback) throws IOException {
        post(HTTP_SERVER_URL + "/v1/account/getx?address=" + address + "&currency=" + currency, "", callback);
    }

    public void getPacketList(Callback callback) throws IOException {
        get(HTTP_SERVER_URL + "/v1/redpacket/list", callback);
    }

    public void createPacket(PacketCreateParams params, Callback callback) throws IOException, JSONException {
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("accountId", params.accountId);
        jsonParams.put("address", params.address);
        jsonParams.put("total", params.total);
        jsonParams.put("currency", params.currency);
        jsonParams.put("quantity", params.quantity);
        jsonParams.put("title", params.title);
        post(HTTP_SERVER_URL + "/v1/redpacket/create", jsonParams.toString(), callback);
    }

    public void getPacketStatus(String packetId, Callback callback) throws IOException {
        get(HTTP_SERVER_URL + "/v1/redpacket/status/" + packetId, callback);
    }

    public void openPacket(String packetId, String receiverAddress, Callback callback) throws IOException {
        post(HTTP_SERVER_URL + "/v1/redpacket/open?id=" + packetId + "&addr=" + receiverAddress, "", callback);
    }

    public void closePacket(String packetId, String receiverAddress, Callback callback) throws IOException {
        post(HTTP_SERVER_URL + "/v1/redpacket/close?id=" + packetId + "&addr=" + receiverAddress, "", callback);
    }


    public void get(String url, Callback b) throws IOException {

        System.out.println(url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(b);
    }

    public void post(String url, String content, Callback callback) throws IOException {

        System.out.println(url);
        System.out.println(content);

        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, content);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(callback);
    }

}
