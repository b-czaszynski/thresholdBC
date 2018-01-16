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
import android.widget.TextView;
import android.widget.Toast;

import com.codahale.shamir.Scheme;

import org.json.JSONException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecoverSecretActivity extends AppCompatActivity {

    private Secret secretToRecover;
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

        secretToRecover = getIntent().getParcelableExtra("Secret");
        ((TextView)findViewById(R.id.txtViewProgress)).setText("Ask your friends for your secret named " + secretToRecover.getName());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    private void handleNfcIntent(Intent NfcIntent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(NfcIntent.getAction())) {
            Parcelable[] receivedArray =
                    NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(receivedArray != null) {
                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();
                for (NdefRecord record:attachedRecords) {
                    byte[] buddySecretBytes = record.getPayload();
                    System.out.println("Package" + new String(buddySecretBytes, StandardCharsets.UTF_8));
                    if (new String(buddySecretBytes, StandardCharsets.UTF_8).equals(getPackageName())) {
                        continue;
                    }
                    Secret buddySecret;
                    try {
                        buddySecret = new Secret(new String(buddySecretBytes, StandardCharsets.UTF_8));
//                        if(secretToRecover.getName().equals(buddySecret.getName())
//                                && secretToRecover.getOwner().equals(buddySecret.getOwner())){
                        //TODO add notification if you already have that share
                            for(Share s : buddySecret.getShares()) {
                                secretToRecover.addShare(s);
//                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    ((TextView)findViewById(R.id.txtViewProgress)).setText("Recovered: " + secretToRecover.getShares().size() + "out of " + secretToRecover.getK() + " needed to recover secret");
                    if(secretToRecover.getShares().size() >= secretToRecover.getK()){
                        ((TextView)findViewById(R.id.txtViewProgress)).setText("Recovered secret:" + secretToRecover.recoverSecret());
                    }
                }
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
        if (NfcAdapter.getDefaultAdapter(this) != null)
            NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(
                    this, pendingIntent, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (NfcAdapter.getDefaultAdapter(this) != null) NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
    }
}
