package com.jegerkatten.waxexpresstrade;

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
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jegerkatten.waxexpresstrade.adapters.InventoryPagerAdapter;
import com.jegerkatten.waxexpresstrade.adapters.SelectItemsPagerAdapter;
import com.jegerkatten.waxexpresstrade.utils.FileUtils;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;

import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity {

    DrawerLayout drawer;
    ActionBarDrawerToggle drawerToggle;
    NavigationView drawerItems;
    private final Context ctx = this;

    private String uid = "-1";

    private InventoryPagerAdapter adapter;
    private SparseArray<InventoryActivity.RefreshListener> refreshers = null;
    private ArrayList<String> items;

    public InventoryActivity.RefreshListener getRefreshListener(int appid) {
        return refreshers.get(appid);
    }

    public void addRefreshListener(int appid, InventoryActivity.RefreshListener refreshListener) {
        if(refreshers == null) {
            refreshers = new SparseArray<InventoryActivity.RefreshListener>();
        }
        refreshers.put(appid, refreshListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        Toolbar appbar = findViewById(R.id.inventory_appbar);
        appbar.setTitle(R.string.title_activity_inventory);
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
                        Intent sendTrade = new Intent(ctx, SendTradeActivity.class);
                        startActivity(sendTrade);
                        finish();
                        return true;
                    case R.id.select_inventory:
                        drawer.closeDrawer(Gravity.START);
                        return true;
                    case R.id.select_trade_url:
                        Intent tradeURL = new Intent(ctx, TradeURLActivity.class);
                        startActivity(tradeURL);
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

        adapter = new InventoryPagerAdapter(getSupportFragmentManager());
        adapter.setCaller(this);
        RequestUtils.updateApps(this, adapter);
    }

    public void update() {
        ViewPager pager = findViewById(R.id.pager_inventory);
        pager.setAdapter(adapter);
        TabLayout tabs = findViewById(R.id.tabs_inventory);
        tabs.setupWithViewPager(pager);
    }

    public interface RefreshListener {
        void onRefresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                if(refreshers != null) {
                    for(int i = 0; i < refreshers.size(); i++) {
                        refreshers.get(refreshers.keyAt(i)).onRefresh();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ArrayList<String> getExclude() {
        return items;
    }

    public String getUID() {
        return uid;
    }

    public InventoryPagerAdapter getAdapter() {
        return adapter;
    }
}
