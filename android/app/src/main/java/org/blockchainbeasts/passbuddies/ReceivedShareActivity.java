package org.blockchainbeasts.passbuddies;

import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.nio.charset.StandardCharsets;

public class ReceivedShareActivity extends AppCompatActivity {

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_share);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        username = this.getSharedPreferences("username", Context.MODE_PRIVATE).getString("username", null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleNfcIntent(getIntent());
    }

    public void handleNfcIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] receivedArray =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(receivedArray != null) {
                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();
                for (NdefRecord record:attachedRecords) {
                    if (!new String(record.getPayload(), StandardCharsets.UTF_8).equals(getPackageName())) {
                        try {
                            Secret receivedSecret = new Secret(new String(record.getPayload(), StandardCharsets.UTF_8));
                            if(!receivedSecret.getOwner().equals(username)) {
                                SecretStorageHandler.storeSecret(this, receivedSecret);
                            }
                            ((TextView)findViewById(R.id.txtViewReceivedShare)).setText((receivedSecret.toJSON()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else {
                Toast.makeText(this, "Received Blank Parcel", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        handleNfcIntent(intent);
    }

}
