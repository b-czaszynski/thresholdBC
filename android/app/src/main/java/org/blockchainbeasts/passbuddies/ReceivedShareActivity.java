package org.blockchainbeasts.passbuddies;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.nio.charset.StandardCharsets;

public class ReceivedShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_share);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("RECEIVED A SHARE");
        handleNfcIntent(getIntent());
    }

    public void handleNfcIntent(Intent intent) {
        System.out.println("HANDLING NFC INTENT");
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            System.out.println("HANDLING NFC INTENT2");
            Parcelable[] receivedArray =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(receivedArray != null) {
                System.out.println("HANDLING NFC INTENT3");
                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();
                for (NdefRecord record:attachedRecords) {
                    if (!new String(record.getPayload(), StandardCharsets.UTF_8).equals(getPackageName())) {
                        System.out.println("STORING MESSAGE"  + new String(record.getPayload(), StandardCharsets.UTF_8));
                        try {
                            System.out.println("STORING MESSAGEFF"  + new Message(new String(record.getPayload(), StandardCharsets.UTF_8)).toJSON());
                            StorageHandler.storeMessage(this, new Message(new String(record.getPayload(), StandardCharsets.UTF_8)));
                            ((TextView)findViewById(R.id.txtViewReceivedShare)).setText(((Message)StorageHandler.retrieveMessages(this, "fff").toArray()[0]).toJSON());
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
