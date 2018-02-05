package io.mtc.app.mtcwallet.database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by admin on 2018/1/25.
 */

public class TransactionInfoV2Container {
    public long pageNum;
    public long pageSize;
    public long size;
    public long startRow;
    public long endRow;
    public long total;
    public long pages;
    public ArrayList<TransactionInfoV2> list = new ArrayList<>();


    public static TransactionInfoV2Container fromJSON(JSONObject jsonObject) throws JSONException {
        TransactionInfoV2Container container = new TransactionInfoV2Container();
        container.pageNum = jsonObject.getLong("pageNum");
        container.pageSize = jsonObject.getLong("pageSize");
        container.size = jsonObject.getLong("size");
        container.startRow = jsonObject.getLong("startRow");
        container.endRow = jsonObject.getLong("endRow");
        container.total = jsonObject.getLong("total");
        container.pages = jsonObject.getLong("pages");
        JSONArray jsonListArray = jsonObject.getJSONArray("list");
        for (int index = 0; index < jsonListArray.length(); ++index) {
            container.list.add(TransactionInfoV2.fromJSON(jsonListArray.getJSONObject(index)));
        }
        return container;
    }

}
