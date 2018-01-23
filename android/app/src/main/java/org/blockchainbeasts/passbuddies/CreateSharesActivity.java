package org.blockchainbeasts.passbuddies;

import android.content.Context;
import android.content.Intent;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class CreateSharesActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback{

    public static final String INTENT_INIT_SECRET = "org.blockchainbeasts.passbuddies.INIT_SECRET";

    private ArrayList<Secret> messagesToSendArray;
    private int successfulSent = 0;
    private int n;
    private byte[] secret = "".getBytes();

    /**
     * Called when this Activity is created
     * @param savedInstanceState state to be used to restore from a previous state
     */
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

        if(getIntent() != null
                && INTENT_INIT_SECRET.equals(getIntent().getAction()))
            handleSecretIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (INTENT_INIT_SECRET.equals(intent.getAction())) {
            handleSecretIntent(intent);
        }
    }

    /**
     * Creates shares according to the given inputs
     * @param view the view  that calls this method
     */
    public void createShares(View view) {
        //TODO add message that share with name already exists if it exits
        successfulSent = 0;
        // Get info from input fields
        if (((EditText)findViewById(R.id.txtBoxSecret)).getVisibility() == View.VISIBLE)
            secret =  ((EditText)findViewById(R.id.txtBoxSecret)).getText().toString().getBytes(StandardCharsets.UTF_8);

        String name = ((EditText)findViewById(R.id.txtBoxSecretName)).getText().toString();
        String username = this.getSharedPreferences("username", Context.MODE_PRIVATE).getString("username", null);
        n = Integer.parseInt(((EditText)findViewById(R.id.numberInputN)).getText().toString());
        int k = Integer.parseInt(((EditText)findViewById(R.id.numberInputK)).getText().toString());
        int amountToKeep = Integer.parseInt(((EditText)findViewById(R.id.numberInputAmountToKeep)).getText().toString());

        Scheme scheme = new Scheme(n, k);
        Map<Integer, byte[]> sharesBytes = scheme.split(secret);
        ArrayList<Share> shares = new ArrayList<>();
        for(int key : sharesBytes.keySet()){
            shares.add(new Share(sharesBytes.get(key), key));
        }
        // Keep shares self
        Secret s = new Secret(username, name, n, k);
        for(int i = 0; i < amountToKeep; i++) {
            if (shares.get(0) != null) {
                s.addShare(shares.remove(0));
                successfulSent++;
            }
        }
        try {
            SecretStorageHandler.storeSecret(this, s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        while(!shares.isEmpty()){
            s = new Secret(username, name);
            s.addShare(shares.remove(0));
            messagesToSendArray.add(s);
        }
        // Set progress
        RelativeLayout relativeLayout = findViewById(R.id.relativeLayoutSecretInput);
        relativeLayout.setVisibility(View.GONE);
        TextView progressView = findViewById(R.id.txtViewProgress);
        progressView.setText("Touch your phone against phones of friends to share parts of your secret(shares) with them.");
        // Register callback for nfc (i.e. enable nfcf)
        NfcAdapter.getDefaultAdapter(this).setNdefPushMessageCallback(this, this);
        NfcAdapter.getDefaultAdapter(this).setOnNdefPushCompleteCallback(this, this);
    }

    /**
     * Creates a list of NdefRecords to be sent
     * @return a list of messages to be sent
     */
    public NdefRecord[] createRecords() throws JSONException {
        NdefRecord[] records = new NdefRecord[2];
        byte[] payload = messagesToSendArray.remove(0).toJSON().getBytes(StandardCharsets.UTF_8);
        records[0] =  NdefRecord.createMime("text/plain", payload);
        records[1] = NdefRecord.createApplicationRecord(this.getPackageName());
        return records;
    }

    /**
     * Creates a single NDF message (called when phone gets close to another nfc enabled device)
     * @param event nfc event containing information about nfc
     * @return returns the next message to be sent
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        if(messagesToSendArray.size()>0) {
            try {
                return new NdefMessage( createRecords());
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            finish();
            return null;
        }
    }

    /**
     * Gets called when an nfc message is succesfully send to another device.
     * @param nfcEvent an nfcEvent object containing data about delivering a message it to the other device
     */
    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        successfulSent++;
        //Make sure the text is set on the main thread
        runOnUiThread(() -> {
            if(successfulSent <n) {
                TextView progressView = findViewById(R.id.txtViewProgress);
                progressView.setText("Successfully shared with " + successfulSent + " friends, share it with " + (n - successfulSent) + " more");
            }else{ // All messages are send
                TextView progressView = findViewById(R.id.txtViewProgress);
                progressView.setText("");
                RelativeLayout relativeLayout = findViewById(R.id.relativeLayoutSecretInput);
                relativeLayout.setVisibility(View.VISIBLE);
                clearInputs();
            }
        });
    }

    private void handleSecretIntent(Intent secretIntent) {
        String userName = "External";
        int k = 1;
        int n = 1;
        int sharesToKeep = 1;

        //Check which extras are available
        if (secretIntent.hasExtra("user_name")
                && secretIntent.getStringExtra("user_name") != null) {
            userName = secretIntent.getStringExtra("user_name");
        }
        if (secretIntent.hasExtra("number_of_shares")
                && secretIntent.getIntExtra("number_of_shares", 0) != 0) {
            n = secretIntent.getIntExtra("number_of_shares", n);
        }
        if (secretIntent.hasExtra("shares_to_recover")
                && secretIntent.getIntExtra("shares_to_recover", 0) != 0) {
            k = secretIntent.getIntExtra("shares_to_recover", k);
        }
        if (secretIntent.hasExtra("shares_to_keep")
                && secretIntent.getIntExtra("shares_to_keep", 0) != 0) {
            sharesToKeep = secretIntent.getIntExtra("shares_to_keep", sharesToKeep);
        }
        if (secretIntent.hasExtra("secret")
                && secretIntent.getStringExtra("user_name") != null) {
            secret = secretIntent.getByteArrayExtra("secret");
            //Set secret view to invisible, so the secret cannot be changed.
            findViewById(R.id.txtBoxSecret).setVisibility(View.INVISIBLE);
            ((TextView)findViewById(R.id.txtViewSecretLabel)).setText("Secret: invisible");
        }

        ((EditText) findViewById(R.id.txtBoxSecretName)).setText(userName);
        ((EditText) findViewById(R.id.numberInputN)).setText(n + "");
        ((EditText) findViewById(R.id.numberInputK)).setText(k + "");
        ((EditText)findViewById(R.id.numberInputAmountToKeep)).setText(sharesToKeep + "");
    }

    /**
     * Removes any values from inputs
     */
    private void clearInputs() {
        ((EditText)findViewById(R.id.txtBoxSecret)).setText("");
        ((EditText)findViewById(R.id.txtBoxSecretName)).setText("");
        ((EditText)findViewById(R.id.numberInputN)).setText("");
        ((EditText)findViewById(R.id.numberInputK)).setText("");
        ((EditText)findViewById(R.id.numberInputAmountToKeep)).setText("");
    }
}
