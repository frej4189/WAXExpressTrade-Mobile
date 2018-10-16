package com.jegerkatten.waxexpresstrade;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jegerkatten.waxexpresstrade.utils.ImageDownloader;

import org.json.JSONException;
import org.json.JSONObject;

public class ItemActivity extends AppCompatActivity {

    private final Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        try {
            final JSONObject item = new JSONObject(getIntent().getStringExtra("item"));
            LinearLayout layout = findViewById(R.id.item_layout);
            ImageView close = new ImageView(this);
            close.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_close_white_24dp, null));
            LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            closeParams.gravity = Gravity.END;
            close.setLayoutParams(closeParams);
            close.setOnClickListener(new ImageView.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            layout.addView(close);
            ImageView img = new ImageView(this);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            imgParams.leftMargin = 10;
            imgParams.rightMargin = 10;
            new ImageDownloader(img, (int)Math.floor((metrics.widthPixels - 20) / 300) * 300, (int)Math.floor((metrics.widthPixels - 20) / 300) * 300).execute(item.getJSONObject("image").getString("600px"));
            img.setLayoutParams(imgParams);
            layout.addView(img);
            if(item.has("in_offer") && item.getBoolean("in_offer")) {
                LinearLayout inOffer = new LinearLayout(this);
                inOffer.setOrientation(LinearLayout.HORIZONTAL);
                ImageView warning = new ImageView(this);
                warning.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_priority_high_white_24dp, null));
                inOffer.setBackgroundColor(Color.parseColor("#dc3545"));
                TextView inOfferText = new TextView(this);
                inOfferText.setText(R.string.in_offer);
                inOffer.setGravity(Gravity.CENTER_HORIZONTAL);
                inOffer.addView(warning);
                inOffer.addView(inOfferText);
                layout.addView(inOffer);
            }
            if(item.has("trade_hold_expires") && item.get("trade_hold_expires") != null && item.get("trade_hold_expires").getClass().getName() == "java.lang.Integer") {
                LinearLayout tradehold = new LinearLayout(this);
                tradehold.setOrientation(LinearLayout.HORIZONTAL);
                ImageView warning = new ImageView(this);
                warning.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_priority_high_white_24dp, null));
                tradehold.setBackgroundColor(Color.parseColor("#dc3545"));
                TextView tradeholdText = new TextView(this);
                tradeholdText.setText(R.string.on_trade_hold);
                tradeholdText.setTextSize(12);
                tradehold.setGravity(Gravity.CENTER_HORIZONTAL);
                tradehold.addView(warning);
                tradehold.addView(tradeholdText);
                layout.addView(tradehold);
            }
            TextView name = new TextView(this);
            name.setText(item.getString("name"));
            name.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(name);
            if(item.has("wear")) {
                TextView wear = new TextView(this);
                wear.setText(getResources().getString(R.string.wear, item.get("wear").toString()));
                wear.setGravity(Gravity.CENTER_HORIZONTAL);
                layout.addView(wear);
            }
            TextView price = new TextView(this);
            double usdprice = ((double)item.getInt("suggested_price")) / 100.0;
            price.setText(getResources().getString(R.string.price, Double.toString(usdprice)));
            price.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(price);
            Button add = new Button(this);
            add.setBackgroundColor(Color.parseColor("#007bff"));
            add.setText(R.string.add_item);
            add.setGravity(Gravity.CENTER_HORIZONTAL);
            add.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("item", item.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
            layout.addView(add);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
