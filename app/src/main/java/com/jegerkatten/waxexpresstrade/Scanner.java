package com.jegerkatten.waxexpresstrade;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.Result;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Scanner extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    ZXingScannerView scannerView;
    final Context ctx = this;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        scannerView = new ZXingScannerView(this);
        //scannerView.setAspectTolerance(0.5f);
        setContentView(scannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        String scanResult = result.getText();
        Uri uri = (scanResult != null) ? Uri.parse(scanResult) : null;
        String authority = uri.getAuthority();
        String user = (authority.equals("totp")) ? uri.getPath() : null;
        String issuer = (user != null) ? uri.getQueryParameter("issuer") : null;
        String secret = (issuer != null) ? uri.getQueryParameter("secret") : null;
        if(secret != null) {
            RequestUtils.addTwoFactor(this, user, secret);
            finish();
        } else {
            Toast.makeText(this, R.string.action_failed, Toast.LENGTH_SHORT).show();
            new Thread() {
                public void run() {
                    try {
                        this.sleep(2500);
                        Intent intent = new Intent(ctx, MainActivity.class);
                        startActivity(intent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
}
