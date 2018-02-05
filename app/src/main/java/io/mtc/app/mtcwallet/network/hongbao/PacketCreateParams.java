package io.mtc.app.mtcwallet.network.hongbao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 2018/2/4.
 */

public class PacketCreateParams implements Parcelable {
    public String accountId;
    public String address;
    public String total;
    public String currency;
    public int quantity;
    public String title;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.accountId);
        dest.writeString(this.address);
        dest.writeString(this.total);
        dest.writeString(this.currency);
        dest.writeInt(this.quantity);
        dest.writeString(this.title);
    }

    public PacketCreateParams() {
    }

    protected PacketCreateParams(Parcel in) {
        this.accountId = in.readString();
        this.address = in.readString();
        this.total = in.readString();
        this.currency = in.readString();
        this.quantity = in.readInt();
        this.title = in.readString();
    }

    public static final Parcelable.Creator<PacketCreateParams> CREATOR = new Parcelable.Creator<PacketCreateParams>() {
        @Override
        public PacketCreateParams createFromParcel(Parcel source) {
            return new PacketCreateParams(source);
        }

        @Override
        public PacketCreateParams[] newArray(int size) {
            return new PacketCreateParams[size];
        }
    };
}
