package org.blockchainbeasts.passbuddies;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.codahale.shamir.Scheme;

import org.json.JSONException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class CreateSharesActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback{

    private ArrayList<Message> messagesToSendArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shares);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        //Disable nfc untill messages are available
        NfcAdapter.getDefaultAdapter(this).setNdefPushMessageCallback(this, this);
        NfcAdapter.getDefaultAdapter(this).setOnNdefPushCompleteCallback(this, this);
        setSupportActionBar(toolbar);
        messagesToSendArray = new ArrayList<>();
    }

    public void createShares(View view) {
        byte[] secret =  ((EditText)findViewById(R.id.txtBoxSecret)).getText().toString().getBytes(StandardCharsets.UTF_8);
        String name = ((EditText)findViewById(R.id.txtBoxUserName)).getText().toString();
        int n = Integer.parseInt(((EditText)findViewById(R.id.numberInputN)).getText().toString());
        int k = Integer.parseInt(((EditText)findViewById(R.id.numberInputK)).getText().toString());
        Scheme scheme = new Scheme(n, k);
        Map<Integer, byte[]> shares = scheme.split(secret);
        if(shares.get(1) != null){
            try {
                StorageHandler.storeMessage(this, new Message(shares.get(1), 1, name));
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        for(int i = 2; i<=n; i++) {
            messagesToSendArray.add(new Message(shares.get(i), i, name));
        }
        // Register callback for nfc (i.e. enable nfcf)
        System.out.println("ENABLING NFC" );
        NfcAdapter.getDefaultAdapter(this).setNdefPushMessageCallback(this, this);
        NfcAdapter.getDefaultAdapter(this).setOnNdefPushCompleteCallback(this, this);
        System.out.println("Created " + messagesToSendArray.size() + "messages");

//TODO give user feedback about the sending process
        //startActivity(new Intent(this, CreateSharesActivity.class));
    }

    public NdefRecord[] createRecords() {

        NdefRecord[] records = new NdefRecord[2];
        byte[] payload;
        try {
            payload = messagesToSendArray.remove(0).toJSON().getBytes(StandardCharsets.UTF_8);
        }catch(JSONException e) {
            e.printStackTrace();
            return new NdefRecord[0];
        }
        System.out.println("Sending: " + new String(payload, StandardCharsets.UTF_8));
        records[0] =  NdefRecord.createMime("text/plain",payload);
        records[1] = NdefRecord.createApplicationRecord(this.getPackageName());
        return records;
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        if(messagesToSendArray.size()>0) {
            System.out.println("Sending message");
            return new NdefMessage( createRecords());
        }else{
            System.out.println("No messages");
            return null;
        }
    }


    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        System.out.println("Succesfully send over nfc");
//        finish();
    }


    //TODO resume
}
