package org.blockchainbeasts.passbuddies;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Set;

public class ListOwnSecretsActivity extends Activity {

    ArrayList<Secret> secrets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_own_shares);
        secrets = SecretStorageHandler.retrieveAllSecrets(this);
        //TODO sort by owner
        //TODO support multiple owners
        ArrayList<String > shareStrings = new ArrayList<>();
        for(Secret s : secrets ) {
                shareStrings.add(s.getOwner() + " " + s.getName()  + " n:" + s.getN() + ",k:" + s.getK());
        }
        ListView view = findViewById(R.id.share_list);
        view.setAdapter(new ArrayAdapter(this, R.layout.ownsharelistitem, R.id.ownerNameId, shareStrings));
        //TODO not on view but on button
        view.setOnItemClickListener((parent, view1, position, arg3) -> {
            //add validation email is already selected
            Intent i = new Intent(view1.getContext(), RecoverSecretActivity.class);
            i.putExtra("Message", (Parcelable)secrets.toArray()[position]);
            startActivity(i);
        });
    }

    public void deleteShare(View view) {
        System.out.println("Trying to delete share");
    }

    public void recoverSecret(View view) {
        System.out.println("Trying to recover secret");
    }
}
