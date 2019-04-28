package com.zhou.mobilesafe.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zhou.mobilesafe.R;

public class FileProtectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
    }

    public void bt_encrypt(View view) {
        startActivity(new Intent(getApplicationContext(),FileEncryptionActivity.class));
    }

    public void bt_decrypt(View view) {
        startActivity(new Intent(getApplicationContext(),FileDecryptionActivity.class));
    }

}
