package org.blockchainbeasts.passbuddies;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.util.ArrayList;

public class ListOwnSecretsActivity extends AppCompatActivity {

    ArrayList<Secret> secrets;
    ArrayAdapter<String> adapter;
    ArrayList<String> secretStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_own_shares);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String userName = this.getSharedPreferences("username", Context.MODE_PRIVATE).getString("username", null);
        secrets = new ArrayList<>();
        for(Secret s :  SecretStorageHandler.retrieveAllSecrets(this)) {
            if(
                    s.getOwner().equals(userName)){
                secrets.add(s);
            }
        }
        secretStrings = new ArrayList<>();
        for(Secret s : secrets) {
            secretStrings.add(s.getName() + "\nOwner: " + s.getOwner() + "\nAmount:" + s.getShares().size());
        }
        ListView view = findViewById(R.id.share_list);
        adapter = new ArrayAdapter<>(this, R.layout.ownsharelistitem, R.id.ownerNameId, secretStrings);
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
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        Secret secret = secrets.get(position);
        dialog.setTitle("Are you sure?");
        dialog.setMessage("Do you really want to delete "+ secret.getName() + "?");
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                (dialog1, which) -> dialog1.dismiss()
        );
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES I'm sure",
                (dialog1, which) -> {
                    Secret removedSecret = secrets.remove(position);
                    secretStrings.remove(position);
                    try {
                        SecretStorageHandler.deleteSecret(v.getContext(), removedSecret);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dialog1.dismiss();
                    adapter.notifyDataSetChanged();
                });
        dialog.show();

    }
}
