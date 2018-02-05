package io.mtc.app.mtcwallet.data;

import java.math.BigDecimal;

/**
 * Created by admin on 2018/1/5.
 */

public class Constants {

    public static final String MTC_CONTRACT_ADDRESS = "8feBf7551EeA6Ce499F96537Ae0e2075c5A7301a";
    //public static final String MTC_CONTRACT_ADDRESS = "4136C5a204Fe23140A7D23dd950434DC5490eBEc";

    public static final BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
    public static final BigDecimal ONE_GWEI = new BigDecimal("1000000000");

    public final static int REQUEST_MAIN_ACTIVITY = 1;
    public final static int REQUEST_WALLET_GEN_GUIDE_ACTIVITY = 2;
    public final static int REQUEST_WALLET_NEW_ACTIVITY = 3;
    public final static int REQEUST_WALLET_IMPORT_ACTIVITY = 4;
    public final static int REQUEST_WALLET_NEW_SUCCESS = 5;

}
