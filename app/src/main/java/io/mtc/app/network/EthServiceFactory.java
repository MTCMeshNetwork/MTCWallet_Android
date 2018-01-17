package io.mtc.app.network;

import android.content.Context;

/**
 * Created by admin on 2018/1/9.
 */

public class EthServiceFactory {

    public final int DEFAULT_SERVICE = 0;
    public final int ETHERSCAN_SERVICE = 1;

    public static EthService createDefaultService(Context context, EthServiceListener listener) {
        //return new EtherscanService(context, listener);
        return new MTCServiceRPC(context, listener);
    }

    public static EthService createEthService(Context context, int serviceType, EthServiceListener listener) {
        return new EtherscanService(context, listener);
    }


}
