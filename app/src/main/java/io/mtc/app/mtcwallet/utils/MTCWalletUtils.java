package io.mtc.app.mtcwallet.utils;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.List;

import io.mtc.app.mtcwallet.data.WalletCoinInfo;

/**
 * Created by admin on 2018/1/22.
 */

public class MTCWalletUtils {

    public static String getPrefixAddress(String address) {
        if (address == null)
            return null;

        address = address.trim();

        if (TextUtils.isEmpty(address))
            return address;

        if (address.startsWith("0x") || address.startsWith("0X")) {
            return address;
        } else {
            return "0x" + address;
        }
    }

    public static String getNoPrefixAddress(String address) {
        if (address == null)
            return null;

        address = address.trim();

        if (address.startsWith("0x") || address.startsWith("0X")) {
            return address.substring(2, address.length());
        } else {
            return address;
        }
    }

    public static BigDecimal getSumOfCoinTotalPrice(List<WalletCoinInfo> walletCoinInfoList, String currencyType) {
        BigDecimal bdSum = BigDecimal.ZERO;
        if ("CNY".equalsIgnoreCase(currencyType)) {
            for (WalletCoinInfo walletCoinInfo : walletCoinInfoList) {
                bdSum.add(walletCoinInfo.getTotalPriceCNY());
            }
        } else {
            for (WalletCoinInfo walletCoinInfo : walletCoinInfoList) {
                bdSum.add(walletCoinInfo.getTotalPrice());
            }
        }
        return bdSum.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

}
