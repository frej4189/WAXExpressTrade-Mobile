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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jegerkatten.waxexpresstrade.adapters.MainPagerAdapter;
import com.jegerkatten.waxexpresstrade.utils.FileUtils;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView drawerItems;
    private final Context ctx = this;

    public IncomingRefreshListener getIncomingRefreshListener() {
        return incomingRefreshListener;
    }

    public void setIncomingRefreshListener(IncomingRefreshListener incomingRefreshListener) {
        this.incomingRefreshListener = incomingRefreshListener;
    }

    private IncomingRefreshListener incomingRefreshListener;

    public SentRefreshListener getSentRefreshListener() {
        return sentRefreshListener;
    }

    public void setSentRefreshListener(SentRefreshListener sentRefreshListener) {
        this.sentRefreshListener = sentRefreshListener;
    }

    private SentRefreshListener sentRefreshListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RequestUtils.updateBearer(this, FileUtils.getRefresh(this));
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar appbar = findViewById(R.id.trades_appbar);
        appbar.setTitle(R.string.title_activity_trades);
        appbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);

        setSupportActionBar(appbar);

        ViewPager pager = findViewById(R.id.pager_main);
        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        TabLayout tabs = findViewById(R.id.tabs_main);
        tabs.setupWithViewPager(pager);

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
                        drawer.closeDrawer(Gravity.START);
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
                if(getIncomingRefreshListener() != null && getSentRefreshListener() != null) {
                    getIncomingRefreshListener().onRefresh();
                    getSentRefreshListener().onRefresh();
                }
                return true;
            case android.R.id.home:
                drawer.openDrawer(Gravity.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface IncomingRefreshListener {
        void onRefresh();
    }

    public interface SentRefreshListener {
        void onRefresh();
    }
}
