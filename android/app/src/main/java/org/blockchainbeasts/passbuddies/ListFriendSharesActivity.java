package org.blockchainbeasts.passbuddies;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListFriendSharesActivity extends Activity {

    ArrayList<Secret> secrets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friend_shares);
        secrets = SecretStorageHandler.retrieveAllSecrets(this);
        //TODO sort by owner
        //TODO support multiple owners
        ArrayList<String > secretStrings = new ArrayList<>();
        for(Secret s : secrets) {
            secretStrings.add(s.getOwner() + " " + s.getName() + " amount of shares I have: " + s.getShares().size());
        }
        ListView view =  (ListView)findViewById(R.id.share_list);
        view.setAdapter(new ArrayAdapter(this, R.layout.sharelistitem, R.id.ownerNameId, secretStrings));
    }

    public void deleteShare(View view) {
        System.out.println("Trying to delete share");
    }

    public void sendBackShare(View view) {
        Intent i = new Intent(this, SendShareActivity.class);
        i.putExtra("Message", (Parcelable)secrets.toArray()[0]);
        startActivity(i);
    }
}
