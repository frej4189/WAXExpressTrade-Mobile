package com.jegerkatten.waxexpresstrade;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeReader;
import com.jegerkatten.waxexpresstrade.adapters.MainPagerAdapter;
import com.jegerkatten.waxexpresstrade.utils.FileUtils;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;
import com.jegerkatten.waxexpresstrade.utils.TwoFAUtils;

import java.security.GeneralSecurityException;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Setup2FAActivity extends AppCompatActivity {

    DrawerLayout drawer;
    ActionBarDrawerToggle drawerToggle;
    NavigationView drawerItems;
    final Context ctx = this;
    final Setup2FAActivity instance = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup_2fa);

        Toolbar appbar = findViewById(R.id.trades_appbar);
        appbar.setTitle(R.string.title_activity_setup_2fa);
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

        Button scanQR = findViewById(R.id.setup_2fa_button);
        scanQR.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scanner = new Intent(ctx, Scanner.class);
                startActivity(scanner);
            }
        });

        Button enterCode = findViewById(R.id.setup_2fa_secret_next);
        enterCode.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder codeDialog = new AlertDialog.Builder(ctx);
                codeDialog.setTitle("Enter key code");
                final EditText input = new EditText(ctx);
                input.setHint(R.string.setup_2fa_secret_placeholder);
                codeDialog.setView(input);

                codeDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RequestUtils.addTwoFactor(ctx, "manual", input.getText().toString());
                    }
                });
                codeDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                codeDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar, menu);
        return true;
    }
}
