package io.mtc.app.mtcwallet.data;

import io.mtc.app.mtcwallet.database.EthTokenInfo;

import java.math.BigDecimal;

/**
 * Created by admin on 2018/1/5.
 */

public class EthTokenDisplay {
    public int icon_resid;
    public String str_ether_count;
    public String str_total_price;
    public EthTokenInfo tokenInfo;
    private BigDecimal balance;
    private BigDecimal price;
    private String price_unit_name = "¥";
    private BigDecimal totalPrice;

    public void setPrice(BigDecimal price, String price_unit_name) {
        this.price = price;
        this.price_unit_name = price_unit_name;
        update();
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
        update();
    }

    /*
    public void setBalance(BigDecimal balance, double price, String price_unit) {
        this.balance = balance;
        if (this.balance == null) {
            str_ether_count = "--- " + tokenInfo.unit_name;
            str_total_price = price_unit + " ---";
        } else {
            str_ether_count = this.balance.toPlainString() + " " + tokenInfo.unit_name;
            str_total_price = price_unit + " " + balance.multiply(new BigDecimal(price)).setScale(2, BigDecimal.ROUND_HALF_DOWN).toPlainString();
        }
    }
    */

    private void update() {

        if (balance == null) {
            str_ether_count = "---";
        } else {
            str_ether_count = this.balance.toPlainString();
        }

        if (balance == null || price == null) {
            totalPrice = BigDecimal.ZERO;
            str_total_price = price_unit_name + " ---";
        } else {
            totalPrice = balance.multiply(price).setScale(2, BigDecimal.ROUND_HALF_DOWN);
            str_total_price = price_unit_name + " " + totalPrice;
        }
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

}
