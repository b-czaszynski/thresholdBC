package org.blockchainbeasts.passbuddies;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by dorian on 15-1-18.
 */

public class Share  implements Parcelable {

    private byte[] bytes;
    private int shareNumber;

    public Share(byte[] bytes, int shareNumber){
        this.bytes = bytes;
        this.shareNumber = shareNumber;
    }

    public Share(String json) throws JSONException{
        JSONObject obj = new JSONObject(json);
        this.bytes = Base64.decode(obj.getString("share"), Base64.DEFAULT);
        this.shareNumber = obj.getInt("shareNumber");
    }

    public byte[] getBytes(){
        return  bytes;
    }

    public int getShareNumber(){
        return shareNumber;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonobj = new JSONObject();
        jsonobj.put("share", new String(Base64.encode(bytes, Base64.DEFAULT)));
        jsonobj.put("shareNumber", shareNumber);
        return jsonobj;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Base64.encodeToString(bytes, Base64.DEFAULT));
        dest.writeInt(shareNumber);
    }

    public Share(Parcel parcel) {
        this.bytes = Base64.decode(parcel.readString(), Base64.DEFAULT);
        this.shareNumber = parcel.readInt();
    }

    public static final Parcelable.Creator<Share> CREATOR = new Parcelable.Creator<Share>() {

        @Override
        public Share createFromParcel(Parcel parcel) {
            return new Share(parcel);
        }

        @Override
        public Share[] newArray(int size) {
            return new Share[0];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Share share = (Share) o;

        if (getShareNumber() != share.getShareNumber()) return false;
        return Arrays.equals(getBytes(), share.getBytes());
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(getBytes());
        result = 31 * result + getShareNumber();
        return result;
    }
}

