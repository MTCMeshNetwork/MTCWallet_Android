package io.mtc.app.data;

import io.mtc.app.database.EthTokenInfo;

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

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
        if (this.balance == null) {
            str_ether_count = "--- " + tokenInfo.unit_name;
            str_total_price = "¥ ---";
        } else {
            str_ether_count = this.balance.toPlainString() + " " + tokenInfo.unit_name;
            str_total_price = "¥ ---";
        }
    }

}
