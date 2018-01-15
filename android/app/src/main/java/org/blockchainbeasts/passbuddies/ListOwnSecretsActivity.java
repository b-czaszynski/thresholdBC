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
    ArrayAdapter<Secret> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_own_shares);
        secrets = SecretStorageHandler.retrieveAllSecrets(this);
        //TODO sort by owner
        ArrayList<String > shareStrings = new ArrayList<>();
        for(Secret s : secrets ) {
                shareStrings.add(s.getOwner() + " " + s.getName()  + " n:" + s.getN() + ",k:" + s.getK());
        }
        ListView view = findViewById(R.id.share_list);
        adapter = new ArrayAdapter(this, R.layout.ownsharelistitem, R.id.ownerNameId, shareStrings);
        view.setAdapter(adapter);
    }
    public void recoverSecret(View view) {
        startActivity(new Intent(this, RecoverSecretActivity.class));
    }

    public void deleteSecret(View v){

        final int position = ((ListView)findViewById(R.id.share_list)).getPositionForView((View) v.getParent());
        Secret removedSecret = secrets.remove(position);
        try {
            SecretStorageHandler.deleteSecret(v.getContext(), removedSecret);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();

    }
}
