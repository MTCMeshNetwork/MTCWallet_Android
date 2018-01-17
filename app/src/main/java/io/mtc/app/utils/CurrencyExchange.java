package io.mtc.app.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhangchi on 2018/1/14.
 */

public class CurrencyExchange {
    public Map<String, Double> priceMap = new LinkedHashMap<>();

    public void setEtherPrice(Map<String, Double> priceMap) {
        if (priceMap == null)
            return;
        this.priceMap.clear();
        this.priceMap.putAll(priceMap);
    }



}
