package com.jegerkatten.waxexpresstrade.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jegerkatten.waxexpresstrade.MainActivity;
import com.jegerkatten.waxexpresstrade.R;
import com.jegerkatten.waxexpresstrade.SelectItemsActivity;
import com.jegerkatten.waxexpresstrade.TradeHistoryActivity;
import com.jegerkatten.waxexpresstrade.adapters.SelectItemsPagerAdapter;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;

import org.json.JSONException;

public class SelectItemsFragment extends Fragment {

    int position;
    private SelectItemsActivity parent;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        SelectItemsFragment fragment = new SelectItemsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("position");
        parent = (SelectItemsActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_items, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            final View v = view;
            final LinearLayout layout = view.findViewById(R.id.linear_select_items);
            final int appid = parent.getAdapter().getAppID(position);
            RequestUtils.displayItemPick(layout, v.getContext(), Integer.toString(appid), parent.getUID(), parent.getExclude());
            ((SelectItemsActivity)getActivity()).addRefreshListener(appid, new SelectItemsActivity.RefreshListener() {
                @Override
                public void onRefresh() {
                    RequestUtils.displayItemPick(layout, v.getContext(), Integer.toString(appid), parent.getUID(), parent.getExclude());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
