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
    Secret secret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        secret = intent.getParcelableExtra("Secret");
        setContentView(R.layout.activity_send_share);
        ((TextView)findViewById(R.id.txtViewShareView)).setText("To return " +  secret.getOwner());

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void cancelSending(View view) {
        finish();
    }

    public NdefRecord[] createRecords() {

        NdefRecord[] records = new NdefRecord[2];
        try {
            byte[] payload = secret.toJSON().getBytes(StandardCharsets.UTF_8);
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
    public void onResume() {
        super.onResume();
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        finish();
    }
}
