package org.blockchainbeasts.passbuddies;

/**
 * Created by dorian on 10-12-17.
 */

import android.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Represents an message containing a key share
 */
public class Message {
    private byte[] share;
    private int shareNumber;
    private String owner;

    public Message(byte[] share, int shareNumber, String owner) {
        this.share = share;
        this.shareNumber = shareNumber;
        this.owner = owner;
    }

    public Message(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        this.share = Base64.decode(obj.getString("share"), Base64.DEFAULT);
        this.shareNumber = obj.getInt("shareNumber");
        this.owner = obj.getString("owner");
    }

    public String toJSON() throws JSONException {
        JSONObject jsonobj = new JSONObject();
        jsonobj.put("share", new String(Base64.encode(getShare(), Base64.DEFAULT)));
        jsonobj.put("shareNumber", shareNumber);
        jsonobj.put("owner", owner);
        return jsonobj.toString();
    }

    public byte[] getShare() {
        return share;
    }

    public int getShareNumber() {
        return shareNumber;
    }

    public String getOwner() { return owner; }

}
