package org.blockchainbeasts.passbuddies;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Set;

public class ListOwnSharesActivity extends Activity {

    Set<Message> shares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_own_shares);
        shares = StorageHandler.retrieveMessages(this, "");
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
        view.setAdapter(new ArrayAdapter(this, R.layout.ownsharelistitem, R.id.ownerNameId, shareStrings));
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
