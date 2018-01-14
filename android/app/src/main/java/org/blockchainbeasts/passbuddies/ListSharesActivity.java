package org.blockchainbeasts.passbuddies;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import org.json.JSONException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ListSharesActivity extends Activity {

    Set<Message> shares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_shares);
        shares = StorageHandler.retrieveMessages(this, "fff");
        //TODO sort by owner
        //TODO support multiple owners
        ArrayList<String > shareStrings = new ArrayList<>();
        for(Message m : shares ) {
            try {
                shareStrings.add(m.getShare() + " " + m.getOwner() + " " + m.toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ListView view =  (ListView)findViewById(R.id.share_list);
        view.setAdapter(new ArrayAdapter(this, R.layout.sharelistitem, R.id.ownerNameId, shareStrings));
    }

    public void deleteShare(View view) {
        System.out.println("Trying to delete share");
    }

    public void sendBackShare(View view) {
        Intent i = new Intent(this, SendShareActivity.class);
        i.putExtra("Message", (Parcelable)shares.toArray()[0]);
        startActivity(i);
    }
}
