package io.mtc.app.mtcwallet.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 2018/1/5.
 */

public class EthWalletInfo implements Parcelable {
    public int id;
    public String address;
    public String name;
    public int type;
    public long create_time;
    public String password_hint;
    public String note;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.address);
        dest.writeString(this.name);
        dest.writeInt(this.type);
        dest.writeLong(this.create_time);
        dest.writeString(this.password_hint);
        dest.writeString(this.note);
    }

    public EthWalletInfo() {
    }

    protected EthWalletInfo(Parcel in) {
        this.id = in.readInt();
        this.address = in.readString();
        this.name = in.readString();
        this.type = in.readInt();
        this.create_time = in.readLong();
        this.password_hint = in.readString();
        this.note = in.readString();
    }

    public static final Parcelable.Creator<EthWalletInfo> CREATOR = new Parcelable.Creator<EthWalletInfo>() {
        @Override
        public EthWalletInfo createFromParcel(Parcel source) {
            return new EthWalletInfo(source);
        }

        @Override
        public EthWalletInfo[] newArray(int size) {
            return new EthWalletInfo[size];
        }
    };
}
