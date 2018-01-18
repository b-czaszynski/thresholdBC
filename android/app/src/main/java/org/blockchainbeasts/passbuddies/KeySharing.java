package org.blockchainbeasts.passbuddies;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codahale.shamir.Scheme;

import org.json.JSONException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeySharing extends Activity
        implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback {
    public static final String TAG = "passbudies.KeySharing";
    public static final String INTENT_INIT_SECRET = "org.blockchainbeasts.passbuddies.INIT_SECRET";
    public static final String INTENT_NFC = "android.nfc.action.NDEF_DISCOVERED";
    public static final String secretPlaceholder = "SECRET";

    private ArrayList<Message> messagesToSendArray = new ArrayList<>();
    private ArrayList<Message> messagesReceivedArray = new ArrayList<>();
    private Map<Integer, Message> receivedShares = new HashMap<>();
    private ArrayList<Message> myShares = new ArrayList<>();

    private StorageHandler storageHandler;
    private EditText txtBoxAddMessage;
    private TextView txtViewUnsharedShares;
    private TextView txtViewReceivedShares;
    private TextView txtViewSecret;
    private int k = 2;
    private int n = 2;

    private byte[] secret = "".getBytes();

    private NfcAdapter mNfcAdapter;

    public void updateFields() {

    }

    public void createShares(View view) {
        if (txtBoxAddMessage.getVisibility() == View.VISIBLE) {
            secret = txtBoxAddMessage.getText().toString().getBytes(StandardCharsets.UTF_8);
        }
        String name = ((EditText) findViewById(R.id.txtBoxUserName)).getText().toString();
        n = Integer.parseInt(((EditText) findViewById(R.id.numberInputN)).getText().toString());
        k = Integer.parseInt(((EditText) findViewById(R.id.numberInputK)).getText().toString());
        Scheme scheme = new Scheme(n, k);
        Map<Integer, byte[]> shares = scheme.split(secret);
        if (shares.get(1) != null) {
            receivedShares.put(1, new Message(shares.get(1), 1, name));
        }
        for (int i = 2; i <= k; i++) {
            myShares.add(new Message(shares.get(i), i, name));
        }
        messagesToSendArray.add(myShares.remove(0));
        updateTextViews();

        Toast.makeText(this, "Added Message", Toast.LENGTH_LONG).show();
    }

    public void sendBackShare(View view) {
        for (Message m : messagesReceivedArray) {
            messagesToSendArray.add(m);
        }
        Toast.makeText(this, "Send back secret", Toast.LENGTH_LONG).show();
    }


    private void updateTextViews() {
        txtViewUnsharedShares.setText("Shares not sent yet: " + myShares.size() + "/" + k + "\n");
        txtViewReceivedShares.setText("Amount of received shares: " + receivedShares.size() + "/" + n + "\n Needed to recover secret: " + k);
    }

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

//        startService(new Intent(INTENT_INIT_SECRET));

        txtBoxAddMessage = findViewById(R.id.txtBoxSecret);
        txtViewReceivedShares = findViewById(R.id.txtViewReceivedShares);
        txtViewUnsharedShares = findViewById(R.id.txtViewUnsharedShares);
        txtViewSecret = findViewById(R.id.txtViewSecret);
        Button btnAddMessage = findViewById(R.id.buttonAddMessage);

        updateTextViews();

        int result = checkSelfPermission(Manifest.permission.NFC);

        if (result == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "NFC permission granted",
                    Toast.LENGTH_SHORT).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.NFC}, 0);
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessageCallback(this, this);
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        } else {
            Toast.makeText(this, "NFC not available on this device",
                    Toast.LENGTH_SHORT).show();
        }

        storageHandler = new StorageHandler(this);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        messagesToSendArray.clear();
        if (myShares.size() > 0) {
            messagesToSendArray.add(myShares.remove(0));
        }
        updateTextViews();
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        if (messagesToSendArray.size() == 0) {
            return null;
        }
        NdefRecord[] recordsToAttach = createRecords();
        return new NdefMessage(recordsToAttach);
    }

    public NdefRecord[] createRecords() {

        NdefRecord[] records = new NdefRecord[messagesToSendArray.size() + 1];

        for (int i = 0; i < messagesToSendArray.size(); i++) {
            try {
                byte[] payload = messagesToSendArray.get(i).toJSON().getBytes(StandardCharsets.UTF_8);
                NdefRecord record = NdefRecord.createMime("text/plain", payload);
                records[i] = record;
            } catch (JSONException e) {
                e.printStackTrace();
                return new NdefRecord[0];
            }
        }

        records[messagesToSendArray.size()] =
                NdefRecord.createApplicationRecord(this.getPackageName());

        return records;
    }

    private void handleNfcIntent(Intent NfcIntent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(NfcIntent.getAction())) {
            Parcelable[] receivedArray =
                    NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (receivedArray != null) {
                messagesReceivedArray.clear();
                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();
                for (NdefRecord record : attachedRecords) {
                    byte[] buddySecretBytes = record.getPayload();
                    if (new String(buddySecretBytes, StandardCharsets.UTF_8).equals(getPackageName())) {
                        continue;
                    }
                    Message buddySecretMessage;
                    try {
                        buddySecretMessage = new Message(new String(buddySecretBytes, StandardCharsets.UTF_8));
                        messagesReceivedArray.add(buddySecretMessage);
                        storeMessage(buddySecretMessage);
                        System.out.println(buddySecretMessage.toJSON());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    receivedShares.put(buddySecretMessage.getShareNumber(), buddySecretMessage);
                    if (receivedShares.size() >= k) {
                        recoverSecret();
                    }
                }
                updateTextViews();
            } else {
                Toast.makeText(this, "Received Blank Parcel", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void recoverSecret() {
        Scheme scheme = new Scheme(n, k);
        Map<Integer, byte[]> map = new HashMap<>();
        for (Message m : receivedShares.values()) {
            map.put(m.getShareNumber(), m.getShare());
        }
        byte[] secret = scheme.join(map);
        Toast.makeText(this, "Recovered secret" + new String(secret, StandardCharsets.UTF_8), Toast.LENGTH_LONG);
        txtViewSecret.setText("Recovered secret: " + new String(secret, StandardCharsets.UTF_8));
    }

    private void handleSecretIntent(Intent secretIntent) {

        String userName = "External";

        if (secretIntent.hasExtra("user_name")) {
            userName = secretIntent.getStringExtra("user_name");
        }
        if (secretIntent.hasExtra("number_of_shares")) {
            n = secretIntent.getIntExtra("number_of_shares", n);
        }
        if (secretIntent.hasExtra("shares_to_recover")) {
            k = secretIntent.getIntExtra("shares_to_recover", k);
        }
        if (secretIntent.hasExtra("secret")) {
            secret = secretIntent.getByteArrayExtra("secret");
            txtBoxAddMessage.setVisibility(View.INVISIBLE);
        }

        ((EditText) findViewById(R.id.txtBoxUserName)).setText(userName);
        ((EditText) findViewById(R.id.numberInputN)).setText(n + "");
        ((EditText) findViewById(R.id.numberInputK)).setText(k + "");
    }

    @Override
    public void onNewIntent(Intent intent) {
        Toast.makeText(this, "New intent received: " + intent.getAction(), Toast.LENGTH_LONG).show();
        switch (intent.getAction()) {
            case INTENT_INIT_SECRET:
                handleSecretIntent(intent);
            case INTENT_NFC:
                handleNfcIntent(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTextViews();

        Intent intent = getIntent();

        switch (intent.getAction()) {
//            case INTENT_INIT_SECRET:
//                handleSecretIntent(intent);
            case INTENT_NFC:
                handleNfcIntent(intent);
        }
    }

    /**
     * Returns a set of all messages (as JSON strings) of the given owner
     *
     * @param owner
     * @return Set<String> Set of messages
     */
    public Set<String> retrieveMessages(String owner) {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        return preferences.contains(owner) ? preferences.getStringSet(owner, new HashSet<>()) : new HashSet<>();
    }

    /**
     * Stores a Set<String> of all messages with the same owner under the key 'owner'.
     *
     * @param message
     */
    private void storeMessage(Message message) {
        if (message == null) return;
        try {
            SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);

            Set<String> messageSet;
            try {
                messageSet = preferences.getStringSet(message.getOwner(), new HashSet<>());
            } catch (ClassCastException e) {
                messageSet = new HashSet<>();
            }

            messageSet.add(message.toJSON());

            SharedPreferences.Editor edit = preferences.edit();
            edit.putStringSet(message.getOwner(), messageSet);
            edit.commit();

        } catch (JSONException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace().toString());
        }
    }
}