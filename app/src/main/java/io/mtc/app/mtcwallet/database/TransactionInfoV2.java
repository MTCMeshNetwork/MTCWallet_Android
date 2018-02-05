package io.mtc.app.mtcwallet.database;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

import io.mtc.app.mtcwallet.data.Constants;

/**
 * Created by admin on 2018/1/15.
 */

public class TransactionInfoV2 implements Parcelable {
    public int id;
    public String tokenCounts;
    public int status;
    public int type;
    public String contractAddress;
    public String from;
    public String to;
    public String gasPrice;
    public String nonce;
    public String gas;
    public String hash;
    public long time;
    public String cumulativeGasUsed;
    public String blockNumber;
    public String blockHash;

    public TransactionInfoV2() {

    }

    public BigDecimal getGasPriceGWei() {
        try {
            return new BigDecimal(gasPrice).divide(Constants.ONE_GWEI).setScale(1, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO.setScale(0, RoundingMode.HALF_UP);
        }
    }

    public BigDecimal getTokenCount() {
        try {
            return new BigDecimal(tokenCounts).divide(Constants.ONE_ETHER).setScale(6, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO.setScale(0, BigDecimal.ROUND_DOWN);
        }
    }

    public static TransactionInfoV2 fromJSON(JSONObject jsonObject) throws JSONException {
        TransactionInfoV2 info = new TransactionInfoV2();
        info.id = jsonObject.getInt("id");
        info.tokenCounts = jsonObject.optString("tokenCounts", "");
        info.status = jsonObject.getInt("status");
        info.type = jsonObject.getInt("type");
        info.contractAddress = jsonObject.getString("contractAddress");
        info.from = jsonObject.getString("from");
        info.to = jsonObject.getString("to");
        info.gasPrice = jsonObject.getString("gasPrice");
        info.nonce = jsonObject.getString("nonce");
        info.gas = jsonObject.getString("gas");
        info.hash = jsonObject.getString("hash");
        info.time = jsonObject.getLong("time");
        info.cumulativeGasUsed = jsonObject.optString("cumulativeGasUsed", "");
        info.blockNumber = jsonObject.optString("blockNumber", "");
        info.blockHash = jsonObject.optString("blockHash", "");
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.tokenCounts);
        dest.writeInt(this.status);
        dest.writeInt(this.type);
        dest.writeString(this.contractAddress);
        dest.writeString(this.from);
        dest.writeString(this.to);
        dest.writeString(this.gasPrice);
        dest.writeString(this.nonce);
        dest.writeString(this.gas);
        dest.writeString(this.hash);
        dest.writeLong(this.time);
        dest.writeString(this.cumulativeGasUsed);
        dest.writeString(this.blockNumber);
        dest.writeString(this.blockHash);
    }

    protected TransactionInfoV2(Parcel in) {
        this.id = in.readInt();
        this.tokenCounts = in.readString();
        this.status = in.readInt();
        this.type = in.readInt();
        this.contractAddress = in.readString();
        this.from = in.readString();
        this.to = in.readString();
        this.gasPrice = in.readString();
        this.nonce = in.readString();
        this.gas = in.readString();
        this.hash = in.readString();
        this.time = in.readLong();
        this.cumulativeGasUsed = in.readString();
        this.blockNumber = in.readString();
        this.blockHash = in.readString();
    }

    public static final Parcelable.Creator<TransactionInfoV2> CREATOR = new Parcelable.Creator<TransactionInfoV2>() {
        @Override
        public TransactionInfoV2 createFromParcel(Parcel source) {
            return new TransactionInfoV2(source);
        }

        @Override
        public TransactionInfoV2[] newArray(int size) {
            return new TransactionInfoV2[size];
        }
    };
}
