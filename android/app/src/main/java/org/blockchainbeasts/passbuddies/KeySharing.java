package org.blockchainbeasts.passbuddies;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codahale.shamir.Scheme;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KeySharing extends Activity
        implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback {
    private ArrayList<byte[]> messagesToSendArray = new ArrayList<>();
    private ArrayList<byte[]> messagesReceivedArray = new ArrayList<>();
    private byte[] myShare;

    private EditText txtBoxAddMessage;
    private TextView txtReceivedMessages;
    private TextView txtMessagesToSend;

    private NfcAdapter mNfcAdapter;

    public void addMessage(View view) {
        byte[] newMessage = txtBoxAddMessage.getText().toString().getBytes();
        Scheme scheme = new Scheme(2, 2);
        Map<Integer, byte[]> shares = scheme.split(newMessage);
        if(shares.get(1) != null) {
            messagesToSendArray.add(shares.get(1));
        }
        if(shares.get(2) != null){
            myShare = shares.get(2);
        }
        txtBoxAddMessage.setText(null);
        updateTextViews();

        Toast.makeText(this, "Added Message", Toast.LENGTH_LONG).show();
    }

    public void sendBackReceivedMessages(View view) {
        for(byte[] bytes : messagesReceivedArray){
            messagesToSendArray.add(bytes);
        }
        Toast.makeText(this, "Send back secret", Toast.LENGTH_LONG).show();
    }


    private  void updateTextViews() {
        txtMessagesToSend.setText("Messages To Send:\n");
        if(messagesToSendArray.size() > 0) {
            for (int i = 0; i < messagesToSendArray.size(); i++) {
                txtMessagesToSend.append(new String(messagesToSendArray.get(i)));
                txtMessagesToSend.append("\n");
            }
        }

        txtReceivedMessages.setText("Messages Received:\n");
        if (messagesReceivedArray.size() > 0) {
            for (int i = 0; i < messagesReceivedArray.size(); i++) {
                txtReceivedMessages.append(new String(messagesReceivedArray.get(i)));
                txtReceivedMessages.append("\n");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        super.onSaveInstanceState(savedInstanceState);
//        savedInstanceState.putByteArrayArrayList("messagesToSend", messagesToSendArray);
//        savedInstanceState.putStringArrayList("lastMessagesReceived",messagesReceivedArray);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        super.onRestoreInstanceState(savedInstanceState);
//        messagesToSendArray = savedInstanceState.getStringArrayList("messagesToSend");
//        messagesReceivedArray = savedInstanceState.getStringArrayList("lastMessagesReceived");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_sharing);

        txtBoxAddMessage = findViewById(R.id.txtBoxAddMessage);
        txtMessagesToSend = findViewById(R.id.txtMessageToSend);
        txtReceivedMessages = findViewById(R.id.txtMessagesReceived);
        Button btnAddMessage = findViewById(R.id.buttonAddMessage);

        btnAddMessage.setText("Add Message");
        updateTextViews();

        int result = checkSelfPermission(Manifest.permission.NFC);

        if (result == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "NFC permission granted",
                    Toast.LENGTH_SHORT).show();
        } else {
            requestPermissions(new String[] {Manifest.permission.NFC}, 0);
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessageCallback(this, this);
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
        else {
            Toast.makeText(this, "NFC not available on this device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        messagesToSendArray.clear();
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

        for (int i = 0; i < messagesToSendArray.size(); i++){
            byte[] payload = messagesToSendArray.get(i);

            NdefRecord record = NdefRecord.createMime("text/plain",payload);
            records[i] = record;
        }

        records[messagesToSendArray.size()] =
                NdefRecord.createApplicationRecord(this.getPackageName());

        return records;
    }

    private void handleNfcIntent(Intent NfcIntent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(NfcIntent.getAction())) {
            Parcelable[] receivedArray =
                    NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(receivedArray != null) {
                messagesReceivedArray.clear();
                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();

                for (NdefRecord record:attachedRecords) {
                    byte[] buddySecret = record.getPayload();
                    if (new String(buddySecret).equals(getPackageName())) {
                        continue;
                    }
                    System.out.println(new String(myShare) +  " Is my share");
                    if(myShare!=null) {
                        Scheme scheme = new Scheme(2, 2);
                        Map<Integer, byte[]> map = new HashMap<>();
                        map.put(1, buddySecret);
                        map.put(2, myShare);
                        byte[] secret = scheme.join(map);
                        Toast.makeText(this, "Recovered secret" + new String(secret), Toast.LENGTH_LONG);
                        System.out.println("Recovered secret" + new String(secret));
                    }else{
                        messagesReceivedArray.add(buddySecret);
                    }
                }
                updateTextViews();
            }
            else {
                Toast.makeText(this, "Received Blank Parcel", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleNfcIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTextViews();
        handleNfcIntent(getIntent());
    }
}