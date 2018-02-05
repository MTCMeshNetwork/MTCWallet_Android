package io.mtc.app.mtcwallet.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhangchi on 2018/1/14.
 */

public class CurrencyExchange {

    public static final BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
    public static final BigDecimal ONE_GWEI = new BigDecimal("1000000000");

    private DecimalFormat formatterUsd = new DecimalFormat("#,###,###.##");
    private DecimalFormat formatterCrypt = new DecimalFormat("#,###,###.####");
    private DecimalFormat formatterCryptExact = new DecimalFormat("#,###,###.#######");

    private double ethPriceUSD = 0.0;
    private Map<String, Double> priceRateMapUSD = new LinkedHashMap<>();

    public void updatePriceRateUSD(Map<String, Double> rateMap) {
        if (rateMap == null)
            return;
        priceRateMapUSD.clear();
        priceRateMapUSD.putAll(rateMap);
    }





}
