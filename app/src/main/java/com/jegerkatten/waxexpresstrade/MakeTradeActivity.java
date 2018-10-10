package com.jegerkatten.waxexpresstrade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MakeTradeActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView drawerItems;
    private final Context ctx = this;

    private LinearLayout layout;
    private LinearLayout send;

    private int uid;
    private int uidMy;
    private String tradeURL;

    private ArrayList<String> myItems;
    private ArrayList<String> theirItems;
    private LinearLayout myItemsLayout;
    private LinearLayout theirItemsLayout;

    private int maxItems = 0;
    private int itemsMe = 0;
    private LinearLayout currentMe = null;
    private int itemsThem = 0;
    private LinearLayout currentThem = null;

    private LinearLayout infoMe;
    private LinearLayout infoThem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_trade);

        Toolbar appbar = findViewById(R.id.trades_appbar);
        appbar.setTitle(R.string.title_activity_send_trade);
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

        if(getIntent().hasExtra("EXTRA_USER_ID") && getIntent().hasExtra("EXTRA_TRADE_URL")) {
            uid = Integer.parseInt(getIntent().getStringExtra("EXTRA_USER_ID"));
            tradeURL = getIntent().getStringExtra("EXTRA_TRADE_URL");
        } else if(savedInstanceState != null && savedInstanceState.containsKey("partner") && savedInstanceState.containsKey("tradeurl")) {
            uid = savedInstanceState.getInt("partner");
            tradeURL = savedInstanceState.getString("tradeurl");
        } else {
            Toast.makeText(this, "No trade partner.", Toast.LENGTH_SHORT).show();
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
            return;
        }

        myItems = new ArrayList<String>();
        theirItems = new ArrayList<String>();

        if(!(savedInstanceState == null)) {
            if (savedInstanceState.containsKey("items_me")) {
                myItems = savedInstanceState.getStringArrayList("items_me");
            }
            if (savedInstanceState.containsKey("items_them")) {
                theirItems = savedInstanceState.getStringArrayList("items_them");
            }
        }

        if(getIntent().hasExtra("item") && getIntent().hasExtra("items_holder")) {
            if(getIntent().getIntExtra("items_holder", -1) == uid) {
                theirItems.add(getIntent().getStringExtra("item"));
            } else {
                myItems.add(getIntent().getStringExtra("item"));
            }
        }

        final int uidMe = RequestUtils.getUserID(this);
        uidMy = uidMe;
        final int uidThem = uid;
        if(uidMe < 0) {
            Toast.makeText(this, "Failed to load information.", Toast.LENGTH_SHORT).show();
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
            return;
        }

        LinearLayout.LayoutParams horizontalDividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        horizontalDividerParams.topMargin = 10;
        horizontalDividerParams.bottomMargin = 10;
        LinearLayout.LayoutParams verticalDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
        verticalDividerParams.leftMargin = 10;
        verticalDividerParams.rightMargin = 10;

        layout = findViewById(R.id.make_trade_layout);
        LinearLayout me = new LinearLayout(this);
        me.setOrientation(LinearLayout.VERTICAL);
        infoMe = new LinearLayout(this);
        infoMe.setOrientation(LinearLayout.HORIZONTAL);
        RequestUtils.displayInformation(infoMe, this, uidMe, R.string.you_name, myItems.size());
        me.addView(infoMe);
        myItemsLayout = new LinearLayout(this);
        myItemsLayout.setOrientation(LinearLayout.VERTICAL);
        itemsMe = 0;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        maxItems = ((int) Math.floor(metrics.widthPixels / 122));
        LinearLayout current = new LinearLayout(this);
        current.setOrientation(LinearLayout.HORIZONTAL);
        if(myItems.size() > 0) {
            try {
                for(int i = 0; i < myItems.size(); i++) {
                    final JSONObject item = new JSONObject(myItems.get(i));
                    View itemDivider = new View(this);
                    LinearLayout.LayoutParams itemDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                    itemDividerParams.leftMargin = 10;
                    itemDividerParams.rightMargin = 10;
                    itemDivider.setLayoutParams(itemDividerParams);
                    itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    ImageView img = new ImageView(this);
                    new ImageDownloader(img, 100, 100).execute(item.getJSONObject("image").getString("300px"));
                    img.setOnClickListener(new ImageView.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent itemIntent = new Intent(ctx, RemoveItemActivity.class);
                            itemIntent.putExtra("item", item.toString());
                            ((Activity) ctx).startActivityForResult(itemIntent, 3);
                        }
                    });

                    itemsMe++;
                    if (itemsMe > maxItems) {
                        itemsMe--;
                        current.addView(itemDivider);
                        myItemsLayout.addView(current);
                        current = new LinearLayout(this);
                        current.setOrientation(LinearLayout.HORIZONTAL);
                        itemDivider = new View(this);
                        itemDivider.setLayoutParams(itemDividerParams);
                        itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        current.addView(itemDivider);
                        current.addView(img);
                        View lineDivider = new View(this);
                        LinearLayout.LayoutParams lineDividerParams = new LinearLayout.LayoutParams(itemsMe * 122 + 12, 2);
                        lineDividerParams.topMargin = 10;
                        lineDividerParams.leftMargin = 5;
                        lineDividerParams.bottomMargin = 10;
                        lineDivider.setLayoutParams(lineDividerParams);
                        lineDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        myItemsLayout.addView(lineDivider);
                        itemsMe = 1;
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
                myItemsLayout.addView(current);
                currentMe = current;
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load information.", Toast.LENGTH_SHORT).show();
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
                return;
            }
        }
        me.addView(myItemsLayout);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = 10;
        buttonParams.leftMargin = 10;
        buttonParams.bottomMargin = 10;
        buttonParams.rightMargin = 10;
        Button addMyItems = new Button(this);
        addMyItems.setBackgroundColor(Color.parseColor("#007bff"));
        addMyItems.setText(R.string.add_items);
        addMyItems.setLayoutParams(buttonParams);
        addMyItems.setGravity(Gravity.CENTER_HORIZONTAL);
        addMyItems.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectItems = new Intent(ctx, SelectItemsActivity.class);
                selectItems.putExtra("uid", Integer.toString(uidMe));
                selectItems.putExtra("items", myItems);
                startActivityForResult(selectItems, 1);
            }
        });
        me.addView(addMyItems);
        View personDivider = new View(this);
        personDivider.setLayoutParams(horizontalDividerParams);
        personDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
        LinearLayout them = new LinearLayout(this);
        them.setOrientation(LinearLayout.VERTICAL);
        infoThem = new LinearLayout(this);
        infoThem.setOrientation(LinearLayout.HORIZONTAL);
        RequestUtils.displayInformation(infoThem, this, uid, R.string.them_name, theirItems.size());
        them.addView(infoThem);
        theirItemsLayout = new LinearLayout(this);
        theirItemsLayout.setOrientation(LinearLayout.VERTICAL);
        itemsThem = 0;
        current = new LinearLayout(this);
        current.setOrientation(LinearLayout.HORIZONTAL);
        if(theirItems.size() > 0) {
            try {
                for(int i = 0; i < theirItems.size(); i++) {
                    final JSONObject item = new JSONObject(theirItems.get(i));
                    View itemDivider = new View(this);
                    LinearLayout.LayoutParams itemDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                    itemDividerParams.leftMargin = 10;
                    itemDividerParams.rightMargin = 10;
                    itemDivider.setLayoutParams(itemDividerParams);
                    itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    ImageView img = new ImageView(this);
                    new ImageDownloader(img, 100, 100).execute(item.getJSONObject("image").getString("300px"));
                    img.setOnClickListener(new ImageView.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent itemIntent = new Intent(ctx, RemoveItemActivity.class);
                            itemIntent.putExtra("item", item.toString());
                            ((Activity) ctx).startActivityForResult(itemIntent, 4);
                        }
                    });

                    itemsThem++;
                    if (itemsThem > maxItems) {
                        itemsThem--;
                        current.addView(itemDivider);
                        theirItemsLayout.addView(current);
                        current = new LinearLayout(this);
                        current.setOrientation(LinearLayout.HORIZONTAL);
                        itemDivider = new View(this);
                        itemDivider.setLayoutParams(itemDividerParams);
                        itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        current.addView(itemDivider);
                        current.addView(img);
                        View lineDivider = new View(this);
                        LinearLayout.LayoutParams lineDividerParams = new LinearLayout.LayoutParams(itemsThem * 122 + 12, 2);
                        lineDividerParams.topMargin = 10;
                        lineDividerParams.leftMargin = 5;
                        lineDividerParams.bottomMargin = 10;
                        lineDivider.setLayoutParams(lineDividerParams);
                        lineDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        theirItemsLayout.addView(lineDivider);
                        itemsThem = 1;
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
                theirItemsLayout.addView(current);
                currentThem = current;
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load information.", Toast.LENGTH_SHORT).show();
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
                return;
            }
        }
        them.addView(theirItemsLayout);
        Button addTheirItems = new Button(this);
        addTheirItems.setBackgroundColor(Color.parseColor("#007bff"));
        addTheirItems.setText(R.string.add_items);
        addTheirItems.setGravity(Gravity.CENTER_HORIZONTAL);
        addTheirItems.setLayoutParams(buttonParams);
        addTheirItems.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent selectItems = new Intent(ctx, SelectItemsActivity.class);
            selectItems.putExtra("uid", Integer.toString(uidThem));
            if(theirItems != null) {
                selectItems.putExtra("items", theirItems);
            }
            startActivityForResult(selectItems, 2);
            }
        });
        them.addView(addTheirItems);
        layout.addView(me);
        layout.addView(personDivider);
        layout.addView(them);

        if(myItems.size() + theirItems.size() > 0) {
            LinearLayout send = new LinearLayout(this);
            send.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            send.setOrientation(LinearLayout.VERTICAL);
            LinearLayout message = new LinearLayout(this);
            message.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            message.setOrientation(LinearLayout.VERTICAL);
            message.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView messageGuide = new TextView(this);
            messageGuide.setText(R.string.message_guide);
            messageGuide.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams weightParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            weightParams.weight = 1;
            messageGuide.setLayoutParams(weightParams);
            final EditText messageField = new EditText(this);
            messageField.setHint(R.string.message_placeholder);
            messageField.setLayoutParams(weightParams);
            messageField.setFilters(new InputFilter[] {new InputFilter.LengthFilter(190)});
            message.addView(messageGuide);
            message.addView(messageField);
            LinearLayout twofa = new LinearLayout(this);
            twofa.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            twofa.setOrientation(LinearLayout.VERTICAL);
            twofa.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView twofaGuide = new TextView(this);
            twofaGuide.setText(R.string.twofa_guide);
            twofaGuide.setLayoutParams(weightParams);
            final EditText twofaField = new EditText(this);
            twofaField.setHint(R.string.twofa_placeholder);
            twofaField.setLayoutParams(weightParams);
            twofa.addView(twofaGuide);
            twofa.addView(twofaField);
            Button sendbtn = new Button(this);
            sendbtn.setText(R.string.send_offer);
            sendbtn.setBackgroundColor(Color.parseColor("#007bff"));
            sendbtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String items_to_send = "";
                        for(int i = 0; i < myItems.size(); i++) {
                            JSONObject item = new JSONObject(myItems.get(i));
                            if(i == 0) {
                                items_to_send = items_to_send + item.getInt("id");
                            } else {
                                items_to_send = items_to_send + "," + item.getInt("id");
                            }
                        }
                        String items_to_receive = "";
                        for(int i = 0; i < theirItems.size(); i++) {
                            JSONObject item = new JSONObject(theirItems.get(i));
                            if(i == 0) {
                                items_to_receive = items_to_receive + item.getInt("id");
                            } else {
                                items_to_receive = items_to_receive + "," + item.getInt("id");
                            }
                        }

                        RequestUtils.makeOffer(ctx, tradeURL, twofaField.getText().toString(), messageField.getText().toString(), items_to_send, items_to_receive);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            send.addView(message);
            send.addView(twofa);
            send.addView(sendbtn);
            View sendDivider = new View(this);
            LinearLayout.LayoutParams sendDividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
            sendDividerParams.topMargin = 20;
            sendDividerParams.bottomMargin = 20;
            sendDivider.setLayoutParams(sendDividerParams);
            sendDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
            layout.addView(sendDivider);
            send.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(send);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1) {
            if(resultCode == RESULT_OK) {
                if (data.hasExtra("item")) {
                    myItems.add(data.getStringExtra("item"));
                }
            }
        } else if(requestCode == 2) {
            if(resultCode == RESULT_OK) {
                if(data.hasExtra("item")) {
                    theirItems.add(data.getStringExtra("item"));
                }
            }
        } else if(requestCode == 3) {
            if(resultCode == RESULT_OK) {
                if(data.hasExtra("item")) {
                    myItems.remove(myItems.indexOf(data.getStringExtra("item")));
                }
            }
        } else if(requestCode == 4) {
            if(resultCode == RESULT_OK) {
                if(data.hasExtra("item")) {
                    theirItems.remove(theirItems.indexOf(data.getStringExtra("item")));
                }
            }
        }
        LinearLayout.LayoutParams horizontalDividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        horizontalDividerParams.topMargin = 10;
        horizontalDividerParams.bottomMargin = 10;
        LinearLayout.LayoutParams verticalDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
        verticalDividerParams.leftMargin = 10;
        verticalDividerParams.rightMargin = 10;

        layout.removeAllViews();

        LinearLayout me = new LinearLayout(this);
        me.setOrientation(LinearLayout.VERTICAL);
        infoMe = new LinearLayout(this);
        infoMe.setOrientation(LinearLayout.HORIZONTAL);
        RequestUtils.displayInformation(infoMe, this, uidMy, R.string.you_name, myItems.size());
        me.addView(infoMe);
        myItemsLayout = new LinearLayout(this);
        myItemsLayout.setOrientation(LinearLayout.VERTICAL);
        itemsMe = 0;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        maxItems = ((int) Math.floor(metrics.widthPixels / 122));
        LinearLayout current = new LinearLayout(this);
        current.setOrientation(LinearLayout.HORIZONTAL);
        if(myItems.size() > 0) {
            try {
                for(int i = 0; i < myItems.size(); i++) {
                    final JSONObject item = new JSONObject(myItems.get(i));
                    View itemDivider = new View(this);
                    LinearLayout.LayoutParams itemDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                    itemDividerParams.leftMargin = 10;
                    itemDividerParams.rightMargin = 10;
                    itemDivider.setLayoutParams(itemDividerParams);
                    itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    ImageView img = new ImageView(this);
                    new ImageDownloader(img, 100, 100).execute(item.getJSONObject("image").getString("300px"));
                    img.setOnClickListener(new ImageView.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent itemIntent = new Intent(ctx, RemoveItemActivity.class);
                            itemIntent.putExtra("item", item.toString());
                            ((Activity) ctx).startActivityForResult(itemIntent, 3);
                        }
                    });

                    itemsMe++;
                    if (itemsMe > maxItems) {
                        itemsMe--;
                        current.addView(itemDivider);
                        myItemsLayout.addView(current);
                        current = new LinearLayout(this);
                        current.setOrientation(LinearLayout.HORIZONTAL);
                        itemDivider = new View(this);
                        itemDivider.setLayoutParams(itemDividerParams);
                        itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        current.addView(itemDivider);
                        current.addView(img);
                        View lineDivider = new View(this);
                        LinearLayout.LayoutParams lineDividerParams = new LinearLayout.LayoutParams(itemsMe * 122 + 12, 2);
                        lineDividerParams.topMargin = 10;
                        lineDividerParams.leftMargin = 5;
                        lineDividerParams.bottomMargin = 10;
                        lineDivider.setLayoutParams(lineDividerParams);
                        lineDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        myItemsLayout.addView(lineDivider);
                        itemsMe = 1;
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
                myItemsLayout.addView(current);
                currentMe = current;
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load information.", Toast.LENGTH_SHORT).show();
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
                return;
            }
        }
        me.addView(myItemsLayout);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = 10;
        buttonParams.leftMargin = 10;
        buttonParams.bottomMargin = 10;
        buttonParams.rightMargin = 10;
        Button addMyItems = new Button(this);
        addMyItems.setBackgroundColor(Color.parseColor("#007bff"));
        addMyItems.setText(R.string.add_items);
        addMyItems.setLayoutParams(buttonParams);
        addMyItems.setGravity(Gravity.CENTER_HORIZONTAL);
        addMyItems.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectItems = new Intent(ctx, SelectItemsActivity.class);
                selectItems.putExtra("uid", Integer.toString(uidMy));
                selectItems.putExtra("items", myItems);
                startActivityForResult(selectItems, 1);
            }
        });
        me.addView(addMyItems);
        View personDivider = new View(this);
        personDivider.setLayoutParams(horizontalDividerParams);
        personDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
        LinearLayout them = new LinearLayout(this);
        them.setOrientation(LinearLayout.VERTICAL);
        infoThem = new LinearLayout(this);
        infoThem.setOrientation(LinearLayout.HORIZONTAL);
        RequestUtils.displayInformation(infoThem, this, uid, R.string.them_name, theirItems.size());
        them.addView(infoThem);
        theirItemsLayout = new LinearLayout(this);
        theirItemsLayout.setOrientation(LinearLayout.VERTICAL);
        itemsThem = 0;
        current = new LinearLayout(this);
        current.setOrientation(LinearLayout.HORIZONTAL);
        if(theirItems.size() > 0) {
            try {
                for(int i = 0; i < theirItems.size(); i++) {
                    final JSONObject item = new JSONObject(theirItems.get(i));
                    View itemDivider = new View(this);
                    LinearLayout.LayoutParams itemDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                    itemDividerParams.leftMargin = 10;
                    itemDividerParams.rightMargin = 10;
                    itemDivider.setLayoutParams(itemDividerParams);
                    itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    ImageView img = new ImageView(this);
                    new ImageDownloader(img, 100, 100).execute(item.getJSONObject("image").getString("300px"));
                    img.setOnClickListener(new ImageView.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent itemIntent = new Intent(ctx, RemoveItemActivity.class);
                            itemIntent.putExtra("item", item.toString());
                            ((Activity) ctx).startActivityForResult(itemIntent, 4);
                        }
                    });

                    itemsThem++;
                    if (itemsThem > maxItems) {
                        itemsThem--;
                        current.addView(itemDivider);
                        theirItemsLayout.addView(current);
                        current = new LinearLayout(this);
                        current.setOrientation(LinearLayout.HORIZONTAL);
                        itemDivider = new View(this);
                        itemDivider.setLayoutParams(itemDividerParams);
                        itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        current.addView(itemDivider);
                        current.addView(img);
                        View lineDivider = new View(this);
                        LinearLayout.LayoutParams lineDividerParams = new LinearLayout.LayoutParams(itemsThem * 122 + 12, 2);
                        lineDividerParams.topMargin = 10;
                        lineDividerParams.leftMargin = 5;
                        lineDividerParams.bottomMargin = 10;
                        lineDivider.setLayoutParams(lineDividerParams);
                        lineDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        theirItemsLayout.addView(lineDivider);
                        itemsThem = 1;
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
                theirItemsLayout.addView(current);
                currentThem = current;
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load information.", Toast.LENGTH_SHORT).show();
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
                return;
            }
        }
        them.addView(theirItemsLayout);
        Button addTheirItems = new Button(this);
        addTheirItems.setBackgroundColor(Color.parseColor("#007bff"));
        addTheirItems.setText(R.string.add_items);
        addTheirItems.setGravity(Gravity.CENTER_HORIZONTAL);
        addTheirItems.setLayoutParams(buttonParams);
        addTheirItems.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectItems = new Intent(ctx, SelectItemsActivity.class);
                selectItems.putExtra("uid", Integer.toString(uid));
                if(theirItems != null) {
                    selectItems.putExtra("items", theirItems);
                }
                startActivityForResult(selectItems, 2);
            }
        });
        them.addView(addTheirItems);
        layout.addView(me);
        layout.addView(personDivider);
        layout.addView(them);

        if(myItems.size() + theirItems.size() > 0) {
            LinearLayout send = new LinearLayout(this);
            send.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            send.setOrientation(LinearLayout.VERTICAL);
            LinearLayout message = new LinearLayout(this);
            message.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            message.setOrientation(LinearLayout.VERTICAL);
            message.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView messageGuide = new TextView(this);
            messageGuide.setText(R.string.message_guide);
            messageGuide.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams weightParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            weightParams.weight = 1;
            messageGuide.setLayoutParams(weightParams);
            final EditText messageField = new EditText(this);
            messageField.setHint(R.string.message_placeholder);
            messageField.setLayoutParams(weightParams);
            messageField.setFilters(new InputFilter[] {new InputFilter.LengthFilter(190)});
            message.addView(messageGuide);
            message.addView(messageField);
            LinearLayout twofa = new LinearLayout(this);
            twofa.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            twofa.setOrientation(LinearLayout.VERTICAL);
            twofa.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView twofaGuide = new TextView(this);
            twofaGuide.setText(R.string.twofa_guide);
            twofaGuide.setLayoutParams(weightParams);
            final EditText twofaField = new EditText(this);
            twofaField.setHint(R.string.twofa_placeholder);
            twofaField.setLayoutParams(weightParams);
            twofa.addView(twofaGuide);
            twofa.addView(twofaField);
            Button sendbtn = new Button(this);
            sendbtn.setText(R.string.send_offer);
            sendbtn.setBackgroundColor(Color.parseColor("#007bff"));
            sendbtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String items_to_send = "";
                        for(int i = 0; i < myItems.size(); i++) {
                            JSONObject item = new JSONObject(myItems.get(i));
                            if(i == 0) {
                                items_to_send = items_to_send + item.getInt("id");
                            } else {
                                items_to_send = items_to_send + "," + item.getInt("id");
                            }
                        }
                        String items_to_receive = "";
                        for(int i = 0; i < theirItems.size(); i++) {
                            JSONObject item = new JSONObject(theirItems.get(i));
                            if(i == 0) {
                                items_to_receive = items_to_receive + item.getInt("id");
                            } else {
                                items_to_receive = items_to_receive + "," + item.getInt("id");
                            }
                        }

                        RequestUtils.makeOffer(ctx, tradeURL, twofaField.getText().toString(), messageField.getText().toString(), items_to_send, items_to_receive);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            send.addView(message);
            send.addView(twofa);
            send.addView(sendbtn);
            View sendDivider = new View(this);
            LinearLayout.LayoutParams sendDividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
            sendDividerParams.topMargin = 20;
            sendDividerParams.bottomMargin = 20;
            sendDivider.setLayoutParams(sendDividerParams);
            sendDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
            layout.addView(sendDivider);
            send.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(send);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if(savedInstanceState.containsKey("partner")) {
            savedInstanceState.remove("partner");
        }
        if(savedInstanceState.containsKey("tradeurl")) {
            savedInstanceState.remove("tradeurl");
        }
        if(savedInstanceState.containsKey("items_me")) {
            savedInstanceState.remove("items_me");
        }
        if(savedInstanceState.containsKey("items_them")) {
            savedInstanceState.remove("items_them");
        }

        savedInstanceState.putInt("partner", uid);
        savedInstanceState.putString("tradeurl", tradeURL);
        savedInstanceState.putStringArrayList("items_me", myItems);
        savedInstanceState.putStringArrayList("items_them", theirItems);
    }
}
