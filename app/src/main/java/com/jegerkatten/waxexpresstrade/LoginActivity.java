package com.jegerkatten.waxexpresstrade;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jegerkatten.waxexpresstrade.utils.FileUtils;
import com.jegerkatten.waxexpresstrade.utils.RandomUtils;

import java.io.File;
import java.io.FileOutputStream;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
    }

    public void onLoginClick(View v) {
        String file = "secret.txt";
        String secret = RandomUtils.generateSecret();

        FileOutputStream out;

        try {
            out = openFileOutput(file, Context.MODE_PRIVATE);
            out.write(secret.getBytes());
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://oauth.opskins.com/v1/authorize?client_id=2f2856914a25&response_type=code&state=" + secret + "&duration=permanent&mobile=1&scope=identity_basic trades items"));
        startActivity(viewIntent);
    }
}
