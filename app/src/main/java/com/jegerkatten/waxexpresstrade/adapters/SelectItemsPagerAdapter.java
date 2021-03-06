package com.jegerkatten.waxexpresstrade.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jegerkatten.waxexpresstrade.SelectItemsActivity;
import com.jegerkatten.waxexpresstrade.fragments.MainFragment;
import com.jegerkatten.waxexpresstrade.fragments.SelectItemsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectItemsPagerAdapter extends FragmentPagerAdapter {

    private List<String> title = new ArrayList<String>();
    private JSONArray apps;
    private SelectItemsActivity caller;

    public SelectItemsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setCaller(SelectItemsActivity caller) {
        this.caller = caller;
    }

    public void setApps(JSONArray apps) throws JSONException {
        this.apps = apps;

        for(int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            title.add(app.getString("name"));
        }
        caller.update();
    }

    public int getAppID(int position) throws JSONException {
        return apps.getJSONObject(position).getInt("internal_app_id");
    }

    @Override
    public Fragment getItem(int position) {
        return SelectItemsFragment.getInstance(position);
    }

    @Override
    public int getCount() {
        return title.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title.get(position);
    }
}
