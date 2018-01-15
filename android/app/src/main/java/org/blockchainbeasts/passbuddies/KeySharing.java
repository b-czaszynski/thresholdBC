package org.blockchainbeasts.passbuddies;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class KeySharing extends Activity{

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_sharing);


        int result = checkSelfPermission(Manifest.permission.NFC);

        if (result == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "NFC permission granted",
                    Toast.LENGTH_SHORT).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.NFC}, 0);
        }
        NfcAdapter.getDefaultAdapter(this).setNdefPushMessage(null, this);
    }

    public void onClickViewOnSharesButton(View view){
        startActivity(new Intent(this, ListOwnSecretsActivity.class));
    }

    public void onClickCreateSharesButton(View view) {
        startActivity(new Intent(this, CreateSharesActivity.class));
    }

    public void onClickViewReceivedSharesButton(View view) {
        startActivity(new Intent(this, ListFriendSharesActivity.class));
    }

}