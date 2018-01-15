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

public class ListFriendSharesActivity extends Activity {

    ArrayList<Secret> secrets;
    ArrayList<String> secretStrings;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friend_shares);
        secrets = SecretStorageHandler.retrieveAllSecrets(this);
        //TODO sort by owner
        //TODO support multiple owners
        secretStrings = new ArrayList<>();
        for(Secret s : secrets) {
            secretStrings.add(s.getOwner() + " " + s.getName() + " amount of shares I have share with id " + s.getShares().get(0).getShareNumber());
        }
        ListView view =  (ListView)findViewById(R.id.share_list);
        adapter = new ArrayAdapter(this, R.layout.sharelistitem, R.id.ownerNameId, secretStrings);
        view.setAdapter(adapter);
    }

    public void deleteSecret(View v){

        final int position = ((ListView)findViewById(R.id.share_list)).getPositionForView((View) v.getParent());
        System.out.println("Hello" + secrets.size());
        Secret removedSecret = secrets.remove(position);
        secretStrings.remove(position);
        System.out.println("Hello" + secrets.size());

        try {
            SecretStorageHandler.deleteSecret(v.getContext(), removedSecret);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();

    }

    public void sendBackShare(View view) {
        Intent i = new Intent(this, SendShareActivity.class);
        i.putExtra("Secret", (Parcelable)secrets.toArray()[0]);
        startActivity(i);
    }


}
