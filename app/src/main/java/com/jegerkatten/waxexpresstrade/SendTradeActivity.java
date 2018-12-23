package com.jegerkatten.waxexpresstrade;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jegerkatten.waxexpresstrade.adapters.MainPagerAdapter;
import com.jegerkatten.waxexpresstrade.utils.FileUtils;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;
import com.jegerkatten.waxexpresstrade.utils.StringUtils;

public class SendTradeActivity extends AppCompatActivity {

    DrawerLayout drawer;
    ActionBarDrawerToggle drawerToggle;
    NavigationView drawerItems;
    final Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_trade);

        if(getIntent() != null && getIntent().getAction() == "android.intent.action.VIEW") {
            final int uid = StringUtils.getUserID(getIntent().getData().toString());
            int user = RequestUtils.getUserID(this);

            if(uid >= 0 && uid != user) {
                Intent intent = new Intent(this, MakeTradeActivity.class);
                intent.putExtra("EXTRA_USER_ID", Integer.toString(uid));
                intent.putExtra("EXTRA_TRADE_URL", getIntent().getData().toString());
                startActivity(intent);
                finish();
                return;
            } else if(uid == user) {
                Toast.makeText(ctx, "You cannot trade with yourself.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        }

        Toolbar appbar = findViewById(R.id.trades_appbar);
        appbar.setTitle(R.string.title_activity_send_trade);
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
                    case R.id.update:
                        return true;
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
                        drawer.closeDrawer(Gravity.START);
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
                    case R.id.select_logout:
                        Intent logout = new Intent(ctx, LogoutActivity.class);
                        startActivity(logout);
                        finish();
                        return true;
                    case R.id.select_2fa:
                        if(FileUtils.get2FASecret(ctx) == null) {
                            Intent setup2FA = new Intent(ctx, Setup2FAActivity.class);
                            startActivity(setup2FA);
                            finish();
                        } else {
                            Intent twoFA = new Intent(ctx, TwoFAActivity.class);
                            startActivity(twoFA);
                            finish();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });

        android.content.ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();

        if(clipData != null && clipData.getItemCount() > 0) {
            ClipData.Item item = clipData.getItemAt(0);
            final String paste = String.valueOf(item.getText());
            final int uid = StringUtils.getUserID(paste);
            int user = RequestUtils.getUserID(this);

            if(uid >= 0 && uid != user) {
                Button pasteBtn = findViewById(R.id.paste_button);
                pasteBtn.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getBaseContext(), MakeTradeActivity.class);
                        intent.putExtra("EXTRA_USER_ID", Integer.toString(uid));
                        intent.putExtra("EXTRA_TRADE_URL", paste);
                        startActivity(intent);
                        finish();
                    }
                });
                pasteBtn.setVisibility(View.VISIBLE);
            }
        }

        Button nextBtn = findViewById(R.id.next_button);
        nextBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText urlField = findViewById(R.id.tradeURLField);
                String tradeurl = urlField.getText().toString();
                final int uid = StringUtils.getUserID(tradeurl);
                int user = RequestUtils.getUserID(ctx);

                if(uid < 0) {
                    Toast.makeText(ctx, "Invalid TradeURL.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(uid == user) {
                    Toast.makeText(ctx, "You cannot trade with yourself.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getBaseContext(), MakeTradeActivity.class);
                intent.putExtra("EXTRA_USER_ID", Integer.toString(uid));
                intent.putExtra("EXTRA_TRADE_URL", tradeurl);
                startActivity(intent);
                finish();
            }
        });
    }
}
