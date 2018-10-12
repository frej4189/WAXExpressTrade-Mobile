package com.jegerkatten.waxexpresstrade;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jegerkatten.waxexpresstrade.adapters.MainPagerAdapter;
import com.jegerkatten.waxexpresstrade.adapters.SelectItemsPagerAdapter;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SelectItemsActivity extends AppCompatActivity {

    private final Context ctx = this;

    private String uid = "-1";

    private SelectItemsPagerAdapter adapter;
    private SparseArray<RefreshListener> refreshers = null;
    private ArrayList<String> items;

    public RefreshListener getRefreshListener(int appid) {
        return refreshers.get(appid);
    }

    public void addRefreshListener(int appid, RefreshListener refreshListener) {
        if(refreshers == null) {
            refreshers = new SparseArray<RefreshListener>();
        }
        refreshers.put(appid, refreshListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_items);

        Toolbar appbar = findViewById(R.id.trades_appbar);
        appbar.setTitle(R.string.title_activity_select_items);
        appbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        setSupportActionBar(appbar);
        appbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            setResult(RESULT_CANCELED);
            finish();
            }
        });

        adapter = new SelectItemsPagerAdapter(getSupportFragmentManager());
        adapter.setCaller(this);
        RequestUtils.updateApps(this, adapter);

        items = new ArrayList<String>();
        if(getIntent().hasExtra("items")) {
            items = getIntent().getStringArrayListExtra("items");
        }

        if(getIntent().hasExtra("uid")) {
            uid = getIntent().getStringExtra("uid");
        } else {
            Toast.makeText(this, "Invalid user.", Toast.LENGTH_SHORT).show();
            final Intent sendTrade = new Intent(this, SendTradeActivity.class);

            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        startActivity(sendTrade);
                        finish();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            return;
        }

        if(Integer.parseInt(uid) < 0) {
            Toast.makeText(this, "Invalid user.", Toast.LENGTH_SHORT).show();
            final Intent sendTrade = new Intent(this, SendTradeActivity.class);

            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        startActivity(sendTrade);
                        finish();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            return;
        }
    }

    public void update() {
        ViewPager pager = findViewById(R.id.pager_select_items);
        pager.setAdapter(adapter);
        TabLayout tabs = findViewById(R.id.tabs_select_items);
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
                        refreshers.get(i).onRefresh();
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

    public SelectItemsPagerAdapter getAdapter() {
        return adapter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1) {
            if(resultCode == RESULT_OK) {
                if(data.hasExtra("item")) {
                    Intent intent = new Intent();
                    intent.putExtra("item", data.getStringExtra("item"));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    }
}
