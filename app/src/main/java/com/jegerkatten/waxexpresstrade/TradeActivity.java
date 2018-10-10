package com.jegerkatten.waxexpresstrade;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jegerkatten.waxexpresstrade.utils.ImageDownloader;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TradeActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView drawerItems;
    private final Context ctx = this;

    private boolean shouldReact = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        JSONObject offer;

        Toolbar appbar = findViewById(R.id.trades_appbar);
        appbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        try {
            offer = new JSONObject(getIntent().getStringExtra("EXTRA_OFFER_DATA"));
            appbar.setTitle(getResources().getString(R.string.title_activity_trade, Integer.toString(offer.getInt("id"))));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load trade.", Toast.LENGTH_SHORT).show();
            Intent trades = new Intent(this, MainActivity.class);
            startActivity(trades);
            finish();
            return;
        }

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

        try {
            LinearLayout layout = findViewById(R.id.trade_layout);
            TextView message = new TextView(this);
            LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            messageParams.weight = 1;
            message.setLayoutParams(messageParams);
            message.setText(getResources().getString(R.string.message, offer.getString("message")));
            TextView status = new TextView(this);
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            statusParams.weight = 1;
            status.setLayoutParams(statusParams);
            status.setGravity(Gravity.CENTER_HORIZONTAL);
            message.setGravity(Gravity.CENTER_HORIZONTAL);
            status.setText(getResources().getString(R.string.status, offer.getString("state_name")));
            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.HORIZONTAL);
            texts.addView(message);
            texts.addView(status);
            View divider = new View(this);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
            dividerParams.topMargin = 10;
            dividerParams.bottomMargin = 10;
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(Color.parseColor("#FFFFFF"));
            JSONObject sender = offer.getJSONObject("sender");
            JSONArray senderItems = sender.getJSONArray("items");
            JSONObject recipient = offer.getJSONObject("recipient");
            JSONArray recipientItems = recipient.getJSONArray("items");
            final boolean incoming = recipient.getInt("uid") == RequestUtils.getUserID(this);
            LinearLayout senderInfo = new LinearLayout(this);
            senderInfo.setOrientation(LinearLayout.HORIZONTAL);
            ImageView senderImg = new ImageView(this);
            new ImageDownloader(senderImg, 64, 64).execute(sender.getString("avatar").startsWith("http") ? sender.getString("avatar") : "https://opskins.com" + sender.getString("avatar"));
            View senderDivider = new View(this);
            LinearLayout.LayoutParams senderDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
            senderDividerParams.leftMargin = 10;
            senderDividerParams.rightMargin = 10;
            senderDivider.setLayoutParams(senderDividerParams);
            senderDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
            TextView senderName = new TextView(this);
            senderName.setText(getResources().getString(incoming ? R.string.them_name : R.string.you_name, sender.getString("display_name"), Integer.toString(senderItems.length())));
            senderInfo.setPadding(10, 10, 10,0);
            senderInfo.addView(senderImg);
            senderInfo.addView(senderDivider);
            senderInfo.addView(senderName);
            LinearLayout senderItemsLayout = new LinearLayout(this);
            senderItemsLayout.setOrientation(LinearLayout.VERTICAL);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int maxItems = ((int) Math.floor(metrics.widthPixels / 122));
            int items = 0;
            LinearLayout current = new LinearLayout(this);
            current.setOrientation(LinearLayout.HORIZONTAL);
            if(senderItems.length() > 0) {
                for (int i = 0; i < senderItems.length(); i++) {
                    View itemDivider = new View(this);
                    LinearLayout.LayoutParams itemDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                    itemDividerParams.leftMargin = 10;
                    itemDividerParams.rightMargin = 10;
                    itemDivider.setLayoutParams(itemDividerParams);
                    itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    final JSONObject item = senderItems.getJSONObject(i);

                    ImageView img = new ImageView(this);
                    new ImageDownloader(img, 100, 100).execute(item.getJSONObject("image").getString("300px"));
                    img.setOnClickListener(new ImageView.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent itemIntent = new Intent(ctx, PreviewItemActivity.class);
                            itemIntent.putExtra("item", item.toString());
                            ctx.startActivity(itemIntent);
                        }
                    });

                    items++;
                    if (items > maxItems) {
                        items--;
                        current.addView(itemDivider);
                        senderItemsLayout.addView(current);
                        current = new LinearLayout(this);
                        current.setOrientation(LinearLayout.HORIZONTAL);
                        itemDivider = new View(this);
                        itemDivider.setLayoutParams(itemDividerParams);
                        itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        current.addView(itemDivider);
                        current.addView(img);
                        View lineDivider = new View(this);
                        LinearLayout.LayoutParams lineDividerParams = new LinearLayout.LayoutParams(items * 122 + 12, 2);
                        lineDividerParams.topMargin = 10;
                        lineDividerParams.leftMargin = 5;
                        lineDividerParams.bottomMargin = 10;
                        lineDivider.setLayoutParams(lineDividerParams);
                        lineDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        senderItemsLayout.addView(lineDivider);
                        items = 1;
                    } else {
                        current.addView(itemDivider);
                        current.addView(img);
                    }
                }
                View itemDivider = new View(this);
                LinearLayout.LayoutParams itemDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                itemDividerParams.leftMargin = 10;
                itemDividerParams.rightMargin = 10;
                itemDivider.setLayoutParams(itemDividerParams);
                itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                current.addView(itemDivider);
                senderItemsLayout.addView(current);
            } else {
                TextView noItems = new TextView(this);
                noItems.setText(R.string.no_items);
                noItems.setGravity(Gravity.CENTER);
                senderItemsLayout.addView(noItems);
            }
            View personDivider = new View(this);
            LinearLayout.LayoutParams personDividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
            personDividerParams.topMargin = 20;
            personDividerParams.bottomMargin = 20;
            personDivider.setLayoutParams(personDividerParams);
            personDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
            LinearLayout recipientInfo = new LinearLayout(this);
            recipientInfo.setOrientation(LinearLayout.HORIZONTAL);
            ImageView recipientImg = new ImageView(this);
            new ImageDownloader(recipientImg, 64, 64).execute(recipient.getString("avatar").startsWith("https://") ? recipient.getString("avatar") : "https://opskins.com" + recipient.getString("avatar"));
            View recipientDivider = new View(this);
            LinearLayout.LayoutParams recipientDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
            recipientDividerParams.leftMargin = 10;
            recipientDividerParams.rightMargin = 10;
            recipientDivider.setLayoutParams(recipientDividerParams);
            recipientDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
            TextView recipientName = new TextView(this);
            recipientName.setText(getResources().getString(incoming ? R.string.you_name : R.string.them_name, recipient.getString("display_name"), Integer.toString(recipientItems.length())));
            recipientInfo.setPadding(10, 10, 10,0);
            recipientInfo.addView(recipientImg);
            recipientInfo.addView(recipientDivider);
            recipientInfo.addView(recipientName);
            LinearLayout recipientItemsLayout = new LinearLayout(this);
            recipientItemsLayout.setOrientation(LinearLayout.VERTICAL);
            current = new LinearLayout(this);
            current.setOrientation(LinearLayout.HORIZONTAL);
            items = 0;
            if(recipientItems.length() > 0) {
                for (int i = 0; i < recipientItems.length(); i++) {
                    View itemDivider = new View(this);
                    LinearLayout.LayoutParams itemDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                    itemDividerParams.leftMargin = 10;
                    itemDividerParams.rightMargin = 10;
                    itemDivider.setLayoutParams(itemDividerParams);
                    itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    final JSONObject item = recipientItems.getJSONObject(i);

                    ImageView img = new ImageView(this);
                    new ImageDownloader(img, 100, 100).execute(item.getJSONObject("image").getString("300px"));
                    img.setOnClickListener(new ImageView.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent itemIntent = new Intent(ctx, PreviewItemActivity.class);
                            itemIntent.putExtra("item", item.toString());
                            ctx.startActivity(itemIntent);
                        }
                    });

                    items++;
                    if (items > maxItems) {
                        items--;
                        current.addView(itemDivider);
                        recipientItemsLayout.addView(current);
                        current = new LinearLayout(this);
                        current.setOrientation(LinearLayout.HORIZONTAL);
                        itemDivider = new View(this);
                        itemDivider.setLayoutParams(itemDividerParams);
                        itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        current.addView(itemDivider);
                        current.addView(img);
                        View lineDivider = new View(this);
                        LinearLayout.LayoutParams lineDividerParams = new LinearLayout.LayoutParams(items * 122 + 12, 2);
                        lineDividerParams.topMargin = 10;
                        lineDividerParams.leftMargin = 5;
                        lineDividerParams.bottomMargin = 10;
                        lineDivider.setLayoutParams(lineDividerParams);
                        lineDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        recipientItemsLayout.addView(lineDivider);
                        items = 1;
                    } else {
                        current.addView(itemDivider);
                        current.addView(img);
                    }
                }
                View itemDivider = new View(this);
                LinearLayout.LayoutParams itemDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                itemDividerParams.leftMargin = 10;
                itemDividerParams.rightMargin = 10;
                itemDivider.setLayoutParams(itemDividerParams);
                itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                current.addView(itemDivider);
                recipientItemsLayout.addView(current);
            } else {
                TextView noItems = new TextView(this);
                noItems.setText(R.string.no_items);
                noItems.setGravity(Gravity.CENTER);
                recipientItemsLayout.addView(noItems);
            }

            layout.addView(texts);
            layout.addView(divider);
            layout.addView(senderInfo);
            layout.addView(senderItemsLayout);
            layout.addView(personDivider);
            layout.addView(recipientInfo);
            layout.addView(recipientItemsLayout);
            LinearLayout buttons = new LinearLayout(this);
            buttons.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            buttons.setOrientation(LinearLayout.HORIZONTAL);
            final int offerid = offer.getInt("id");
            if(offer.getInt("state") == 2) {
                if(sender.getInt("uid") == RequestUtils.getUserID(this)) {
                    Button cancel = new Button(this);
                    cancel.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(shouldReact())
                                return;
                            react(false);
                            RequestUtils.cancelOffer(ctx, offerid, true);
                        }
                    });
                    cancel.setText(R.string.cancel);
                    cancel.setBackgroundColor(Color.parseColor("#6c757d"));
                    buttons.addView(cancel);
                } else {
                    Button accept = new Button(this);
                    accept.setText(R.string.accept);
                    accept.setBackgroundColor(Color.parseColor("#007bff"));
                    accept.setGravity(Gravity.CENTER_HORIZONTAL);
                    accept.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(shouldReact())
                                return;
                            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                            builder.setTitle("2FA Code");

                            final EditText input = new EditText(ctx);
                            builder.setView(input);

                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    react(false);
                                    RequestUtils.acceptOffer(ctx, offerid, input.getText().toString());
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    react(true);
                                    dialog.cancel();
                                }
                            });

                            builder.show();
                        }
                    });
                    View buttonsDivider = new View(this);
                    LinearLayout.LayoutParams buttonsDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                    buttonsDividerParams.leftMargin = 10;
                    buttonsDividerParams.rightMargin = 10;
                    buttonsDivider.setLayoutParams(buttonsDividerParams);
                    buttonsDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    Button decline = new Button(this);
                    decline.setText(R.string.decline);
                    decline.setBackgroundColor(Color.parseColor("#6c757d"));
                    decline.setGravity(Gravity.CENTER_HORIZONTAL);
                    decline.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(shouldReact())
                                return;
                            react(false);
                            RequestUtils.cancelOffer(ctx, offerid, false);
                        }
                    });
                    buttons.addView(accept);
                    buttons.addView(buttonsDivider);
                    buttons.addView(decline);
                }
                View buttonDivider = new View(this);
                LinearLayout.LayoutParams buttonDividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
                buttonDividerParams.topMargin = 20;
                buttonDividerParams.bottomMargin = 20;
                buttonDivider.setLayoutParams(buttonDividerParams);
                buttonDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                layout.addView(buttonDivider);
                buttons.setGravity(Gravity.CENTER_HORIZONTAL);
                layout.addView(buttons);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load trade.", Toast.LENGTH_SHORT).show();
            final Intent trades = new Intent(this, MainActivity.class);

            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        startActivity(trades);
                        finish();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public boolean shouldReact() {
        return shouldReact;
    }

    public void react(boolean react) {
        shouldReact = react;
    }
}
