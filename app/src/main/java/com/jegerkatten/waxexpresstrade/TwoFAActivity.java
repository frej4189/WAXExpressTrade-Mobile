package com.jegerkatten.waxexpresstrade;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jegerkatten.waxexpresstrade.utils.FileUtils;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;
import com.jegerkatten.waxexpresstrade.utils.TwoFAUtils;

import java.security.GeneralSecurityException;

public class TwoFAActivity extends AppCompatActivity {

    ProgressBar progressBar;
    TextView codeView;
    DrawerLayout drawer;
    ActionBarDrawerToggle drawerToggle;
    NavigationView drawerItems;
    final Context ctx = this;

    Handler repeatHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_fa);

        Toolbar appbar = findViewById(R.id.trades_appbar);
        appbar.setTitle(R.string.title_activity_2fa);
        appbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);

        setSupportActionBar(appbar);

        drawer = findViewById(R.id.navigation_drawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawer, appbar, R.string.drawer_open_text, R.string.drawer_close_text)
        {
            public void onDrawerClosed(View view)
            {
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView)
            {
                drawerView.bringToFront();
                drawerView.requestLayout();
                supportInvalidateOptionsMenu();
            }
        };
        drawer.addDrawerListener(drawerToggle);
        drawerItems = findViewById(R.id.nav_view);
        drawerItems.bringToFront();
        RequestUtils.setDrawerInfo(this, drawerItems);
        drawerItems.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case android.R.id.home:
                        drawer.openDrawer(Gravity.START);
                        return true;
                    case R.id.select_trades:
                        Intent trades = new Intent(ctx, MainActivity.class);
                        startActivity(trades);
                        finish();
                        return true;
                    case R.id.select_trade_history:
                        Intent history = new Intent(ctx, TradeHistoryActivity.class);
                        startActivity(history);
                        finish();
                        return true;
                    case R.id.select_send_trade:
                        Intent sendTrade = new Intent(ctx, SendTradeActivity.class);
                        startActivity(sendTrade);
                        finish();
                        return true;
                    case R.id.select_inventory:
                        Intent inventory = new Intent(ctx, InventoryActivity.class);
                        startActivity(inventory);
                        finish();
                        return true;
                    case R.id.select_trade_url:
                        Intent tradeURL = new Intent(ctx, TradeURLActivity.class);
                        startActivity(tradeURL);
                        finish();
                        return true;
                    case R.id.select_2fa:
                        drawer.closeDrawer(Gravity.START);
                    case R.id.select_logout:
                        Intent logout = new Intent(ctx, LogoutActivity.class);
                        startActivity(logout);
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });
        Button disableBtn = findViewById(R.id.button_disable_2fa);
        disableBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder disableDialog = new AlertDialog.Builder(ctx);
                disableDialog.setTitle("Confirm");
                disableDialog.setMessage("It is recommended that you remove your 2FA from your OPSkins account page before removing it from this app, continue?");
                disableDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FileUtils.writeData(ctx, "twofa.txt", "", "2FA disabled.");
                    }
                });
                disableDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                disableDialog.show();
            }
        });

        repeatHandler = new Handler(Looper.myLooper());
        codeView = findViewById(R.id.twofa_current_code);
        progressBar = findViewById(R.id.twofa_current_code_progress);
        startRepeatingTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    public void updateCode() {
        double progress = 100.0 - (((System.currentTimeMillis() / 1000) % 30) / (30.0 / 100.0));
        progressBar.setProgress((int) Math.floor(progress));
        try {
            String code = TwoFAUtils.generateTwoFactorCode(FileUtils.get2FASecret(ctx));
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