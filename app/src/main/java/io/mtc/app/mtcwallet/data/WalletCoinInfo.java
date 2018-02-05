package io.mtc.app.mtcwallet.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

/**
 * Created by admin on 2018/1/22.
 */

public class WalletCoinInfo implements Parcelable {

    public String address;
    public String imageURL;
    public String name;
    public String unit_name;
    public String balance;
    public Double price;
    public Double priceCNY;

    private BigDecimal bdBalance = BigDecimal.ZERO;
    private BigDecimal bdTotalPrice = BigDecimal.ZERO;
    private BigDecimal bdTotalPriceCNY = BigDecimal.ZERO;

    public void setBalance(String balance) {
        this.balance = balance;
        try {
            this.bdBalance = new BigDecimal(this.balance);
        } catch (Exception e) {
            this.bdBalance = BigDecimal.ZERO;
        }
    }

    public void update() {
        bdTotalPrice = this.bdBalance.multiply(new BigDecimal(price)).divide(Constants.ONE_ETHER);
        bdTotalPriceCNY = this.bdBalance.multiply(new BigDecimal(priceCNY)).divide(Constants.ONE_ETHER);
    }

    public BigDecimal getTotalPrice() {
        return this.bdTotalPrice;
    }

    public BigDecimal getTotalPriceCNY() {
        return this.bdTotalPriceCNY;
    }

    public BigDecimal getBalanceReadable() {
        return this.bdBalance.divide(Constants.ONE_ETHER, 6, BigDecimal.ROUND_DOWN);
    }

    public BigDecimal getTotalPriceReadable() {
        return this.bdTotalPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    public BigDecimal getTotalPriceCNYReadable() {
        return this.bdTotalPriceCNY.setScale(2, BigDecimal.ROUND_HALF_UP);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.address);
        dest.writeString(this.imageURL);
        dest.writeString(this.name);
        dest.writeString(this.unit_name);
        dest.writeString(this.balance);
        dest.writeValue(this.price);
        dest.writeValue(this.priceCNY);
        dest.writeSerializable(this.bdBalance);
        dest.writeSerializable(this.bdTotalPrice);
        dest.writeSerializable(this.bdTotalPriceCNY);
    }

    public WalletCoinInfo() {
    }

    protected WalletCoinInfo(Parcel in) {
        this.address = in.readString();
        this.imageURL = in.readString();
        this.name = in.readString();
        this.unit_name = in.readString();
        this.balance = in.readString();
        this.price = (Double) in.readValue(Double.class.getClassLoader());
        this.priceCNY = (Double) in.readValue(Double.class.getClassLoader());
        this.bdBalance = (BigDecimal) in.readSerializable();
        this.bdTotalPrice = (BigDecimal) in.readSerializable();
        this.bdTotalPriceCNY = (BigDecimal) in.readSerializable();
    }

    public static final Parcelable.Creator<WalletCoinInfo> CREATOR = new Parcelable.Creator<WalletCoinInfo>() {
        @Override
        public WalletCoinInfo createFromParcel(Parcel source) {
            return new WalletCoinInfo(source);
        }

        @Override
        public WalletCoinInfo[] newArray(int size) {
            return new WalletCoinInfo[size];
        }
    };
}
