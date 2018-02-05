package io.mtc.app.mtcwallet.utils;

import io.mtc.app.mtcwallet.data.CurrencyEntry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class ExchangeCalculator {

    public static final BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
    public static final BigDecimal ONE_GWEI = new BigDecimal("1000000000");

    private static ExchangeCalculator instance;
    private long lastUpdateTimestamp = 0;
    private double rateForChartDisplay = 1;
    private DecimalFormat formatterUsd = new DecimalFormat("#,###,###.##");
    private DecimalFormat formatterCrypt = new DecimalFormat("#,###,###.####");
    private DecimalFormat formatterCryptExact = new DecimalFormat("#,###,###.#######");

    private ExchangeCalculator() {
    }

    public static ExchangeCalculator getInstance() {
        if (instance == null)
            instance = new ExchangeCalculator();
        return instance;
    }

    private CurrencyEntry[] conversionNames = new CurrencyEntry[]{
            new CurrencyEntry("ETH", 1, "Ξ"),
            new CurrencyEntry("BTC", 0.07, "฿"),
            new CurrencyEntry("USD", 0, "$"),
            new CurrencyEntry("CNY", 0, "")
    };

    private int index = 0;

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public double getRateForChartDisplay() {
        return rateForChartDisplay;
    }

    public CurrencyEntry next() {
        index = (index + 1) % conversionNames.length;
        return conversionNames[index];
    }

    public CurrencyEntry getCurrent() {
        return conversionNames[index];
    }

    public CurrencyEntry previous() {
        index = index > 0 ? index - 1 : conversionNames.length - 1;
        return conversionNames[index];
    }

    public CurrencyEntry getMainCurreny() {
        return conversionNames[2];
    }

    public CurrencyEntry getEtherCurrency() {
        return conversionNames[0];
    }

    public String getCurrencyShort() {
        return conversionNames[index].getShorty();
    }

    public String displayBalanceNicely(double d) {
        if (index == 2)
            return displayUsdNicely(d);
        else
            return displayEthNicely(d);
    }

    public String displayUsdNicely(double d) {
        return formatterUsd.format(d);
    }

    public String displayEthNicely(double d) {
        return formatterCrypt.format(d);
    }

    public String displayEthNicelyExact(double d) {
        return formatterCryptExact.format(d);
    }

    /**
     * Converts given tokenbalance to ETH
     *
     * @param tokenbalance native token balance
     * @param tokenusd     price in USD for each token
     * @return Ether worth of given tokens
     */
    public double convertTokenToEther(double tokenbalance, double tokenusd) {
        return Math.floor((((tokenbalance * tokenusd) / conversionNames[2].getRate()) * 10000)) / 10000;
    }

    public double convertRate(double balance, double rate) {
        if (index == 2) {
            if (balance * rate >= 100000) // dont display cents if bigger than 100k
                return (int) Math.floor(balance * rate);
            return Math.floor(balance * rate * 100) / 100;
        } else {
            if (balance * rate >= 1000)
                return Math.floor(balance * rate * 10) / 10;
            if (balance * rate >= 100)
                return Math.floor(balance * rate * 100) / 100;
            return Math.floor(balance * rate * 1000) / 1000;
        }
    }

    public double weiToEther(long weis) {
        return new BigDecimal(weis).divide(ONE_ETHER, 8, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public String convertRateExact(BigDecimal balance, double rate) {
        if (index == 2) {
            return displayUsdNicely(Math.floor(balance.doubleValue() * rate * 100) / 100) + "";
        } else
            return displayEthNicelyExact(balance.multiply(new BigDecimal(rate)).setScale(7, RoundingMode.CEILING).doubleValue());
    }

    public double convertToUsd(double balance) {
        return Math.floor(balance * getUSDPrice() * 100) / 100;
    }


    public double getUSDPrice() {
        return Math.floor(conversionNames[2].getRate() * 100) / 100;
    }

    public double getBTCPrice() {
        return Math.floor(conversionNames[1].getRate() * 10000) / 10000;
    }

}
