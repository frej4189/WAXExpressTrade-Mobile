package com.jegerkatten.waxexpresstrade.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jegerkatten.waxexpresstrade.InventoryActivity;
import com.jegerkatten.waxexpresstrade.R;
import com.jegerkatten.waxexpresstrade.SelectItemsActivity;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;

import org.json.JSONException;

public class InventoryFragment extends Fragment {

    int position;
    private InventoryActivity parent;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        InventoryFragment fragment = new InventoryFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("position");
        parent = (InventoryActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            final View v = view;
            final LinearLayout layout = view.findViewById(R.id.linear_inventory);
            final int appid = parent.getAdapter().getAppID(position);
            RequestUtils.displayItems(layout, v.getContext(), Integer.toString(appid));
            ((InventoryActivity)getActivity()).addRefreshListener(appid, new InventoryActivity.RefreshListener() {
                @Override
                public void onRefresh() {
                    RequestUtils.displayItems(layout, v.getContext(), Integer.toString(appid));
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}