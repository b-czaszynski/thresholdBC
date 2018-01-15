package org.blockchainbeasts.passbuddies;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.codahale.shamir.Scheme;

import org.json.JSONException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecoverSecretActivity extends AppCompatActivity {

    private ArrayList<Message> messagesReceivedArray = new ArrayList<>();
    private Map<Integer, Message> receivedShares = new HashMap<>();
    private int n = 2;
    private int k = 5;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private PendingIntent pendingIntent;

    //TODO list of all the user's shares

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_secret);

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[] {ndef, };
        mTechLists = new String[][] { new String[] { NfcF.class.getName() } };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void handleNfcIntent(Intent NfcIntent) {
        System.out.println("HANDLING NFC INTENT" + NfcIntent.getAction());
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(NfcIntent.getAction())) {
            System.out.println("HANDLING NFC INTENT2");
            Parcelable[] receivedArray =
                    NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(receivedArray != null) {
                System.out.println("HANDLING NFC INTENT3");
                messagesReceivedArray.clear();
                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();
                for (NdefRecord record:attachedRecords) {
                    System.out.println("HANDLING NFC INTENT5");
                    byte[] buddySecretBytes = record.getPayload();
                    if (new String(buddySecretBytes, StandardCharsets.UTF_8).equals(getPackageName())) {
                        continue;
                    }
                    Message buddySecretMessage;
                    try {
                        buddySecretMessage = new Message(new String(buddySecretBytes, StandardCharsets.UTF_8));
                        messagesReceivedArray.add(buddySecretMessage);
                        System.out.println(buddySecretMessage.toJSON());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    System.out.println("RECEIVED SHARE" + buddySecretMessage.getOwner());
                    receivedShares.put(buddySecretMessage.getShareNumber(), buddySecretMessage);
                    if(receivedShares.size() >= k){
                        recoverSecret();
                    }
                }
            }
            else {
                Toast.makeText(this, "Received Blank Parcel", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void recoverSecret() {
        Scheme scheme = new Scheme(n, k);
        Map<Integer, byte[]> map = new HashMap<>();
        for(Message m : receivedShares.values()) {
            map.put(m.getShareNumber(), m.getShare());
        }
        byte[] secret = scheme.join(map);
        Toast.makeText(this, "Recovered secret" + new String(secret, StandardCharsets.UTF_8), Toast.LENGTH_LONG);
//        txtViewSecret.setText("Recovered secret: "+ new String(secret, StandardCharsets.UTF_8));
        //TODO show secret
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleNfcIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcAdapter.getDefaultAdapter(this) != null)
            NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(
                    this, pendingIntent, mFilters,
                    mTechLists);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (NfcAdapter.getDefaultAdapter(this) != null) NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
    }
}
