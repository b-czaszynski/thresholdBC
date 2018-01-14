package org.blockchainbeasts.passbuddies;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

public class SendShareActivity extends AppCompatActivity  implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback{
    NfcAdapter mNfcAdapter;
    Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        message = intent.getParcelableExtra("Message");
        setContentView(R.layout.activity_send_share);
        ((TextView)findViewById(R.id.txtViewShareView)).setText("To return " +  message.getOwner() + "'s share with number: " + message.getShareNumber());

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);
    }

    public void cancelSending(View view) {
        finish();
    }

    public NdefRecord[] createRecords() {

        NdefRecord[] records = new NdefRecord[2];
        try {
            byte[] payload = message.toJSON().getBytes(StandardCharsets.UTF_8);
            records[0] =  NdefRecord.createMime("text/plain",payload);
        }catch(JSONException e) {
            e.printStackTrace();
            return new NdefRecord[0];
        }
        records[1] = NdefRecord.createApplicationRecord(this.getPackageName());
        return records;
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        NdefRecord[] recordsToAttach = createRecords();
        return new NdefMessage(recordsToAttach);
    }


    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        finish();
    }
}
