package io.mtc.app.mtcwallet.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 2018/1/15.
 */

public class TransactionInfo implements Parcelable {
    public int id;
    public String address_from;
    public String address_to;
    public String contract_address;
    public String balance;
    public String data;
    public String txhash;
    public int oper_type;
    public int net_type;
    public int state;
    public long create_time;
    public long confirm_time;

    public TransactionInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.address_from);
        dest.writeString(this.address_to);
        dest.writeString(this.contract_address);
        dest.writeString(this.balance);
        dest.writeString(this.data);
        dest.writeString(this.txhash);
        dest.writeInt(this.oper_type);
        dest.writeInt(this.net_type);
        dest.writeInt(this.state);
        dest.writeLong(this.create_time);
        dest.writeLong(this.confirm_time);
    }

    protected TransactionInfo(Parcel in) {
        this.id = in.readInt();
        this.address_from = in.readString();
        this.address_to = in.readString();
        this.contract_address = in.readString();
        this.balance = in.readString();
        this.data = in.readString();
        this.txhash = in.readString();
        this.oper_type = in.readInt();
        this.net_type = in.readInt();
        this.state = in.readInt();
        this.create_time = in.readLong();
        this.confirm_time = in.readLong();
    }

    public static final Creator<TransactionInfo> CREATOR = new Creator<TransactionInfo>() {
        @Override
        public TransactionInfo createFromParcel(Parcel source) {
            return new TransactionInfo(source);
        }

        @Override
        public TransactionInfo[] newArray(int size) {
            return new TransactionInfo[size];
        }
    };
}
