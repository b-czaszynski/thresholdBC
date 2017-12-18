package org.blockchainbeasts.passbuddies;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

public class ListSharesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_shares);
        String[] from = {"something", "else"};
        ListView view =  (ListView)findViewById(R.id.share_list);
        view.setAdapter(new ArrayAdapter(this, R.layout.sharelistitem, R.id.ownerNameId, from));
    }

    public void deleteShare(View view) {
        System.out.println("Trying to delete share");
    }

    public void sendBackShare(View view) {
        System.out.println("Trying to send back share");
    }
}
