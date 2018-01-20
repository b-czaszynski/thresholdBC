package org.blockchainbeasts.passbuddies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codahale.shamir.Scheme;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class CreateSharesActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback{

    private ArrayList<Secret> messagesToSendArray;
    private int succesfullSent = 0;
    private int n;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shares);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Disable nfc untill messages are available
        NfcAdapter.getDefaultAdapter(this).setNdefPushMessageCallback(this, this);
        NfcAdapter.getDefaultAdapter(this).setOnNdefPushCompleteCallback(this, this);
        messagesToSendArray = new ArrayList<>();
    }

    public void createShares(View view) {
        succesfullSent = 0;
        //TODO input validation
        byte[] secret =  ((EditText)findViewById(R.id.txtBoxSecret)).getText().toString().getBytes(StandardCharsets.UTF_8);
        String name = ((EditText)findViewById(R.id.txtBoxSecretName)).getText().toString();
        String userName = this.getSharedPreferences("username", Context.MODE_PRIVATE).getString("username", null);
        n = Integer.parseInt(((EditText)findViewById(R.id.numberInputN)).getText().toString());
        int k = Integer.parseInt(((EditText)findViewById(R.id.numberInputK)).getText().toString());
        Scheme scheme = new Scheme(n, k);
        Map<Integer, byte[]> sharesBytes = scheme.split(secret);
        ArrayList<Share> shares = new ArrayList<>();
        for(int key : sharesBytes.keySet()){
            shares.add(new Share(sharesBytes.get(key), key));
        }
        if(shares.get(0) != null){
            try {
                Secret s = new Secret(userName, name, n, k);
                s.addShare(shares.remove(0));
                succesfullSent++;
                SecretStorageHandler.storeSecret(this, s);
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        while(!shares.isEmpty()){
            Secret s = new Secret("", name);
            s.addShare(shares.remove(0));
            messagesToSendArray.add(s);
        }
        RelativeLayout relativeLayout = findViewById(R.id.relativeLayoutSecretInput);
        relativeLayout.setVisibility(View.GONE);
        TextView progressView = findViewById(R.id.txtViewProgress);
        progressView.setText("Touch your phone against phones of friends to share parts of your secret(shares) with them.");
        // Register callback for nfc (i.e. enable nfcf)
        NfcAdapter.getDefaultAdapter(this).setNdefPushMessageCallback(this, this);
        NfcAdapter.getDefaultAdapter(this).setOnNdefPushCompleteCallback(this, this);

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
            return new NdefMessage( createRecords());
        }else{
            finish();
            return null;
        }
    }


    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        succesfullSent++;
        //Make sure the text is set on the main thread
        runOnUiThread(() -> {
            if(succesfullSent<n) {
                TextView progressView = findViewById(R.id.txtViewProgress);
                progressView.setText("Successfully shared with " + succesfullSent + " friends, share it with " + (n - succesfullSent) + " more");
            }else{
                TextView progressView = findViewById(R.id.txtViewProgress);
                progressView.setText("");
                RelativeLayout relativeLayout = findViewById(R.id.relativeLayoutSecretInput);
                relativeLayout.setVisibility(View.VISIBLE);
                clearInputs();
            }
        });
    }

    private void clearInputs() {
        ((EditText)findViewById(R.id.txtBoxSecret)).setText("");
        ((EditText)findViewById(R.id.txtBoxSecretName)).setText("");
        ((EditText)findViewById(R.id.numberInputN)).setText("");
        ((EditText)findViewById(R.id.numberInputK)).setText("");
    }
}
