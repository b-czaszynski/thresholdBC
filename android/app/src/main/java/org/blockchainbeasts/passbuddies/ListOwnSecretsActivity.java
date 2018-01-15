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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;

public class ListOwnSecretsActivity extends Activity {

    ArrayList<Secret> secrets;
    ArrayAdapter<Secret> adapter;
    ArrayList<String> secretStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_own_shares);
        secrets = SecretStorageHandler.retrieveAllSecrets(this);
        //TODO sort by owner
        secretStrings = new ArrayList<>();
        for(Secret s : secrets ) {
                secretStrings.add(s.getOwner() + " " + s.getName()  + " n:" + s.getN() + ",k:" + s.getK());
        }
        ListView view = findViewById(R.id.share_list);
        adapter = new ArrayAdapter(this, R.layout.ownsharelistitem, R.id.ownerNameId, secretStrings);
        view.setAdapter(adapter);
    }
    public void recoverSecret(View view) {
        Intent intent = new Intent(this, RecoverSecretActivity.class);
        final int position = ((ListView)findViewById(R.id.share_list)).getPositionForView((View) view.getParent());
        intent.putExtra("Secret",  secrets.get(position));
        startActivity(intent);
    }

    public void deleteSecret(View v){

        final int position = ((ListView)findViewById(R.id.share_list)).getPositionForView((View) v.getParent());
        Secret removedSecret = secrets.remove(position);
        secretStrings.remove(position);
        try {
            SecretStorageHandler.deleteSecret(v.getContext(), removedSecret);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();

    }
}
