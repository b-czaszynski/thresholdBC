package org.blockchainbeasts.passbuddies;

/**
 * Created by dorian on 10-12-17.
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Represents an message containing a key share
 */
public class Message implements Parcelable {
    private byte[] share;
    private int shareNumber;
    private String owner, shareName;

    public Message(byte[] share, int shareNumber, String owner, String shareName) {
        this.share = share;
        this.shareNumber = shareNumber;
        this.owner = owner;
        this.shareName = shareName;
    }

    public Message(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        this.share = Base64.decode(obj.getString("share"), Base64.DEFAULT);
        this.shareNumber = obj.getInt("shareNumber");
        this.owner = obj.getString("owner");
        this.shareName = obj.getString("shareName");
    }

    public String toJSON() throws JSONException {
        JSONObject jsonobj = new JSONObject();
        jsonobj.put("share", new String(Base64.encode(getShare(), Base64.DEFAULT)));
        jsonobj.put("shareNumber", shareNumber);
        jsonobj.put("owner", owner);
        jsonobj.put("shareName", shareName);
        return jsonobj.toString();
    }

    public byte[] getShare() {
        return share;
    }

    public int getShareNumber() {
        return shareNumber;
    }

    public String getOwner() { return owner; }

    public String getShareName() {return shareName;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(owner);
        parcel.writeString(shareName);
        parcel.writeInt(shareNumber);
        parcel.writeString(Base64.encodeToString(share, Base64.DEFAULT));
    }

    public Message(Parcel parcel) {
        this.owner  = parcel.readString();
        this.shareName = parcel.readString();
        this.shareNumber = parcel.readInt();
        this.share = Base64.decode(parcel.readString(), Base64.DEFAULT);
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {

        @Override
        public Message createFromParcel(Parcel parcel) {
            return new Message(parcel);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[0];
        }
    };
}
