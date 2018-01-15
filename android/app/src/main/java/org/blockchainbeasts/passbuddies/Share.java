package org.blockchainbeasts.passbuddies;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dorian on 15-1-18.
 */

public class Share {

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
}
