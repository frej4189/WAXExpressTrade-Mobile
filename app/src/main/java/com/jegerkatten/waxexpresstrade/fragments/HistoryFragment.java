package com.jegerkatten.waxexpresstrade.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jegerkatten.waxexpresstrade.R;
import com.jegerkatten.waxexpresstrade.TradeHistoryActivity;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;

public class HistoryFragment extends Fragment {

    int position;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        HistoryFragment fragment = new HistoryFragment();
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
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View v = view;

        if(position == 0) {
            RequestUtils.displayIncomingTradesHistory(v, v.getContext());

            ((TradeHistoryActivity)getActivity()).setIncomingRefreshListener(new TradeHistoryActivity.IncomingRefreshListener() {
                @Override
                public void onRefresh() {
                    RequestUtils.displayIncomingTradesHistory(v, v.getContext());
                }
            });
        } else if(position == 1) {
            RequestUtils.displaySentTradesHistory(v, v.getContext());

            ((TradeHistoryActivity)getActivity()).setSentRefreshListener(new TradeHistoryActivity.SentRefreshListener() {
                @Override
                public void onRefresh() {
                    RequestUtils.displaySentTradesHistory(v, v.getContext());
                }
            });
        }
    }
}
