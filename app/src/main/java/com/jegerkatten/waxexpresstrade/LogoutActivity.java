package com.jegerkatten.waxexpresstrade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jegerkatten.waxexpresstrade.utils.RequestUtils;

public class LogoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        RequestUtils.logout(this, this);
    }
}
