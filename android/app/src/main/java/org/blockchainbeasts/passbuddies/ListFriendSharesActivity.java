package org.blockchainbeasts.passbuddies;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.util.ArrayList;

public class ListFriendSharesActivity  extends AppCompatActivity {

    ArrayList<Secret> secrets;
    ArrayList<String> secretStrings;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friend_shares);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String userName = this.getSharedPreferences("username", Context.MODE_PRIVATE).getString("username", null);
        secrets = new ArrayList<>();
        for(Secret s :  SecretStorageHandler.retrieveAllSecrets(this)) {
            if(!s.getOwner().equals(userName)){
                secrets.add(s);
            }
        }
        secretStrings = new ArrayList<>();
        for(Secret s : secrets) {
            secretStrings.add(s.getName() + "\nOwner: " + s.getOwner() + "\nAmount:" + s.getShares().size());
        }
        ListView view =  (ListView)findViewById(R.id.share_list);
        adapter = new ArrayAdapter<>(this, R.layout.sharelistitem, R.id.ownerNameId, secretStrings);
        view.setAdapter(adapter);
    }

    public void deleteSecret(View v){
        final int position = ((ListView)findViewById(R.id.share_list)).getPositionForView((View) v.getParent());
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        Secret secret = secrets.get(position);
        dialog.setTitle("Are you sure?");
        dialog.setMessage("Do you really want "+secret.getOwner() + "'s secret "+ secret.getName() + "?\nIf you delete your share " + secret.getOwner() + " might not be able to recover his secret anymore!");
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
                    adapter.notifyDataSetChanged();

                });
        dialog.show();

    }

    public void sendBackShare(View view) {
        final int position = ((ListView)findViewById(R.id.share_list)).getPositionForView((View) view.getParent());
        Intent i = new Intent(this, SendShareActivity.class);
        i.putExtra("Secret", (Parcelable)secrets.toArray()[position]);
        startActivity(i);
    }


}
