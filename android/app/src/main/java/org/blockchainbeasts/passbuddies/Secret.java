package org.blockchainbeasts.passbuddies;

/**
 * Created by dorian on 10-12-17.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.codahale.shamir.Scheme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an message containing a key share
 */
public class Secret implements Parcelable {
    private ArrayList<Share> shares = new ArrayList<>();
    private String owner, name;
    private int n, k;

    public Secret(String owner, String secretName) {
        this.owner = owner;
        this.name = secretName;
    }

    public Secret(String owner, String secretName, int n, int k) {
        this(owner, secretName);
        this.n = n;
        this.k = k;
    }

    public Secret(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        this.shares = new ArrayList<>();
        JSONArray arr = obj.getJSONArray("shares");
        for(int i = 0; i <  arr.length(); i++){
            this.shares.add(new Share(arr.getString(i)));
        }
        this.owner = obj.getString("owner");
        this.name = obj.getString("name");
        this.k = obj.getInt("k");
        this.n = obj.getInt("n");
    }

    public String toJSON() throws JSONException {
        JSONObject jsonobj = new JSONObject();
        JSONArray sharesArray = new JSONArray();
        for(Share s : shares) {
            sharesArray.put(s.toJSON());
        }
        jsonobj.put("shares", sharesArray);
        jsonobj.put("owner", owner);
        jsonobj.put("name", name);
        jsonobj.put("n", n);
        jsonobj.put("k", k);
        return jsonobj.toString();
    }

    public ArrayList<Share> getShares() {
        return shares;
    }

    public String getOwner() { return owner; }

    public String getName() {return name;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(owner);
        parcel.writeString(name);
        parcel.writeInt(n);
        parcel.writeInt(k);
        //TODO write share
        parcel.writeArray(shares.toArray());
    }

    public Secret(Parcel parcel) {
        this.owner  = parcel.readString();
        this.name = parcel.readString();
        this.n = parcel.readInt();
        this.k = parcel.readInt();
        //TODO read shares
        this.shares = ((ArrayList<Share>) parcel.readArrayList(Share.class.getClassLoader()));
    }

    public static final Parcelable.Creator<Secret> CREATOR = new Parcelable.Creator<Secret>() {

        @Override
        public Secret createFromParcel(Parcel parcel) {
            return new Secret(parcel);
        }

        @Override
        public Secret[] newArray(int size) {
            return new Secret[0];
        }
    };

    public void addShare(Share share) {
        this.shares.add(share);
    }

    public int getK() {
        return k;
    }

    public int getN() {
        return n;
    }

    public String recoverSecret() {
        if(shares.size()>k){
            return null;
        }
        Scheme scheme = new Scheme(n, k);
        Map<Integer, byte[]> map = new HashMap<>();
        for(Share s : shares) {
            map.put(s.getShareNumber(), s.getBytes());
        }
        byte[] secret = scheme.join(map);
        return new String(secret, StandardCharsets.UTF_8);
    }
}
