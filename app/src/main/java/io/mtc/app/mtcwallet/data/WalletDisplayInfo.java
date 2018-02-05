package io.mtc.app.mtcwallet.data;

import java.math.BigDecimal;

import io.mtc.app.mtcwallet.database.EthWalletInfo;

/**
 * Created by admin on 2018/1/24.
 */

public class WalletDisplayInfo {
    public EthWalletInfo walletInfo;
    BigDecimal bdTotalPrice = BigDecimal.ZERO;
    BigDecimal bdTotalPriceCNY = BigDecimal.ZERO;

    public void setTotalPrice(BigDecimal price) {
        bdTotalPrice = price;
    }

    public void setTotalPriceCNY(BigDecimal price) {
        bdTotalPriceCNY = price;
    }

    public BigDecimal getTotalPriceReadable() {
        return this.bdTotalPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getTotalPriceCNYReadable() {
        return this.bdTotalPriceCNY.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
