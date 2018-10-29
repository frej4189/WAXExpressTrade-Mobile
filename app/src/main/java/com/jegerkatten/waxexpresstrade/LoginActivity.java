package com.jegerkatten.waxexpresstrade;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jegerkatten.waxexpresstrade.utils.FileUtils;
import com.jegerkatten.waxexpresstrade.utils.RandomUtils;
import com.jegerkatten.waxexpresstrade.utils.TwoFAUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.security.GeneralSecurityException;

public class LoginActivity extends AppCompatActivity {

    ProgressBar progressBar;
    TextView codeView;

    Handler repeatHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        if(FileUtils.get2FASecret(this) != null) {
            if(FileUtils.get2FAUser(this) != null) {
                TextView userView = findViewById(R.id.login_user_view);
                userView.setText(FileUtils.get2FAUser(this));
                userView.setGravity(Gravity.CENTER_HORIZONTAL);
                userView.setVisibility(View.VISIBLE);
            }
            repeatHandler = new Handler(Looper.myLooper());
            codeView = findViewById(R.id.login_2fa_display);
            codeView.setGravity(Gravity.CENTER_HORIZONTAL);
            codeView.setVisibility(View.VISIBLE);
            progressBar = findViewById(R.id.login_2fa_progress);
            progressBar.setVisibility(View.VISIBLE);
            startRepeatingTask();
        }
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

    public void updateCode() {
        ProgressBar progressBar = findViewById(R.id.login_2fa_progress);
        TextView codeView = findViewById(R.id.login_2fa_display);
        double progress = 100.0 - (((System.currentTimeMillis() / 1000) % 30) / (30.0 / 100.0));
        progressBar.setProgress((int) Math.floor(progress));
        try {
            String code = TwoFAUtils.generateTwoFactorCode(FileUtils.get2FASecret(this));
            codeView.setText(code);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    Runnable codeUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                updateCode();
            } finally {
                repeatHandler.postDelayed(codeUpdater, 1000);
            }
        }
    };

    private void startRepeatingTask() {
        codeUpdater.run();
    }

    private void stopRepeatingTask() {
        repeatHandler.removeCallbacks(codeUpdater);
    }
}
