package com.jegerkatten.waxexpresstrade.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jegerkatten.waxexpresstrade.MainActivity;
import com.jegerkatten.waxexpresstrade.R;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;

public class MainFragment extends Fragment {

    int position;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        MainFragment fragment = new MainFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("position");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View v = view;

        if(position == 0) {
            RequestUtils.displayIncomingTrades(v, v.getContext());

            ((MainActivity)getActivity()).setIncomingRefreshListener(new MainActivity.IncomingRefreshListener() {
                @Override
                public void onRefresh() {
                    RequestUtils.displayIncomingTrades(v, v.getContext());
                }
            });
        } else if(position == 1) {
            RequestUtils.displaySentTrades(v, v.getContext());

            ((MainActivity)getActivity()).setSentRefreshListener(new MainActivity.SentRefreshListener() {
                @Override
                public void onRefresh() {
                    RequestUtils.displaySentTrades(v, v.getContext());
                }
            });
        }
    }
}
