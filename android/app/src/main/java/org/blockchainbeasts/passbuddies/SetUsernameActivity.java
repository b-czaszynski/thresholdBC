package org.blockchainbeasts.passbuddies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class SetUsernameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_username);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void onSetUsername(View view) {
        SharedPreferences.Editor preferences = this.getSharedPreferences("username", Context.MODE_PRIVATE).edit();
        String userName = ((EditText)findViewById(R.id.txtBoxUserName)).getText().toString();
        preferences.putString("username", userName);
        preferences.apply();
        this.finish();
    }
}
