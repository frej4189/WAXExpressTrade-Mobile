package com.jegerkatten.waxexpresstrade.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.jegerkatten.waxexpresstrade.ItemActivity;
import com.jegerkatten.waxexpresstrade.MainActivity;
import com.jegerkatten.waxexpresstrade.R;
import com.jegerkatten.waxexpresstrade.TradeActivity;
import com.jegerkatten.waxexpresstrade.adapters.SelectItemsPagerAdapter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class RequestUtils {

    static final Handler handler = new Handler();

    static boolean refreshProgress = false;

    static int uid = -1;

    static void setUserID(int id) {
        uid = id;
    }

    public static int getUserID(final Context context) {
        if(FileUtils.refreshNeeded(context) ) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getUserID(context);
                    }
                }, 1000);
            } else {
                updateBearerForGet(context, FileUtils.getRefresh(context), "userid");
            }
            return -1;
        } else if(uid < 0) {
            final String url = "https://api-trade.opskins.com/IUser/GetProfile/v1/";

            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONObject res = json.getJSONObject("response");
                        JSONObject user = res.getJSONObject("user");
                        int id = user.getInt("id");
                        setUserID(id);
                    } catch(Exception e) {
                        //TODO: Redirect to error activity
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //TODO: Redirect to error activity
                    if(error.networkResponse.statusCode == 401) {
                        updateBearerForGet(context, FileUtils.getRefresh(context), "userid");
                        return;
                    }
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();

                    headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                    return headers;
                }
            };
            Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
            return -1;
        } else {
            return uid;
        }
    }

    public static void displayTradeURL(final View view, final Context context) {
        if(FileUtils.refreshNeeded(context) ) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        displayTradeURL(view, context);
                    }
                }, 1000);
            } else {
                updateBearerForDisplay(context, FileUtils.getRefresh(context), "tradeurl", view);
            }
            return;
        }

        final String url = "https://api-trade.opskins.com/ITrade/GetTradeURL/v1/";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject res = json.getJSONObject("response");

                    ImageView qr = new ImageView(context);

                    final String shorturl = res.getString("short_url");
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    BitMatrix matrix = multiFormatWriter.encode(shorturl, BarcodeFormat.QR_CODE, 500, 500);
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    final Bitmap bitmap = encoder.createBitmap(matrix);
                    qr.setImageBitmap(bitmap);

                    LinearLayout layout = (LinearLayout) view;
                    layout.setGravity(Gravity.CENTER_VERTICAL);
                    LinearLayout qrlayout = new LinearLayout(context);
                    qrlayout.setOrientation(LinearLayout.HORIZONTAL);
                    qrlayout.setGravity(Gravity.CENTER_HORIZONTAL);
                    qrlayout.addView(qr);
                    LinearLayout urllayout = new LinearLayout(context);
                    urllayout.setOrientation(LinearLayout.HORIZONTAL);
                    urllayout.setGravity(Gravity.CENTER_HORIZONTAL);
                    TextView urlView = new TextView(context);
                    urlView.setText(shorturl);
                    urlView.setTextSize(12);
                    urlView.setGravity(Gravity.CENTER_HORIZONTAL);
                    urllayout.addView(urlView);
                    LinearLayout buttonlayout = new LinearLayout(context);
                    buttonlayout.setOrientation(LinearLayout.HORIZONTAL);
                    buttonlayout.setGravity(Gravity.CENTER_HORIZONTAL);
                    Button share = new Button(context);
                    Button copy = new Button(context);
                    share.setText(R.string.share_text);
                    copy.setText(R.string.copy_text);
                    share.setOnClickListener(new Button.OnClickListener() {
                        public void onClick(View v) {
                            Intent shareIntent;
                            shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            shareIntent.putExtra(Intent.EXTRA_TEXT,"Send me a trade - " + shorturl);
                            shareIntent.setType("text/plain");
                            context.startActivity(Intent.createChooser(shareIntent,"Share with"));
                        }
                    });
                    copy.setOnClickListener(new Button.OnClickListener() {
                       public void onClick(View v) {
                           ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                           ClipData clip = ClipData.newPlainText("ExpressTrade TradeURL", shorturl);
                           clipboard.setPrimaryClip(clip);
                           Toast.makeText(context.getApplicationContext(), "TradeURL copied to clipboard.", Toast.LENGTH_SHORT).show();
                       }
                    });
                    buttonlayout.addView(share);
                    buttonlayout.addView(copy);

                    layout.addView(qrlayout);
                    layout.addView(urllayout);
                    layout.addView(buttonlayout);
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForDisplay(context, FileUtils.getRefresh(context), "tradeurl", view);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    private static int incomingTradesMax = 0;
    private static int incomingTradesDone = 0;

    static boolean isDoneIncomingTrades() {
        incomingTradesDone++;
        return incomingTradesMax == incomingTradesDone;
    }

    static void setMaxIncomingTrades(int incomingTrades) {
        incomingTradesMax = incomingTrades;
    }

    static void displayIncomingTrades(View view, JSONArray offers) throws JSONException {
        final Context context = view.getContext();
        LinearLayout layout = view.findViewById(R.id.linear_main);
        if(layout == null) {
            layout = view.findViewById(R.id.linear_history);
        }
        layout.removeAllViews();

        int count = 0;

        for(int i = 0; i < offers.length(); i++) {
            if(i != 0) {
                View divider = new View(context);
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
                dividerParams.topMargin = 5;
                dividerParams.bottomMargin = 5;
                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                layout.addView(divider);
            }

            final JSONObject offer = offers.getJSONObject(0);
            JSONObject sender = offer.getJSONObject("sender");
            JSONObject recipient = offer.getJSONObject("recipient");
            final int id = offer.getInt("id");

            count++;

            LinearLayout graphics = new LinearLayout(context);
            graphics.setId(id);

            ImageView senderImg = new ImageView(context);
            new ImageDownloader(senderImg, 64, 64).execute(sender.getString("avatar").startsWith("http") ? sender.getString("avatar") : "https://opskins.com" + sender.getString("avatar"));
            View divider = new View(context);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
            dividerParams.leftMargin = 10;
            dividerParams.rightMargin = 10;
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(Color.parseColor("#FFFFFF"));
            TextView itemDesc = new TextView(context);
            JSONArray senderItems = sender.getJSONArray("items");
            JSONArray recipientItems = recipient.getJSONArray("items");
            Resources res = context.getResources();
            itemDesc.setText(res.getString(R.string.items_description, Integer.toString(senderItems.length()), Integer.toString(recipientItems.length())));
            itemDesc.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(64, 64);
            imgParams.gravity = Gravity.CENTER_VERTICAL;
            senderImg.setLayoutParams(imgParams);
            Button showbtn = new Button(context);
            showbtn.setText(R.string.show_button_text);
            showbtn.setTextSize(12);
            showbtn.setGravity(Gravity.CENTER_VERTICAL);
            showbtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent trade = new Intent(context, TradeActivity.class);
                    trade.putExtra("EXTRA_OFFER_DATA", offer.toString());
                    context.startActivity(trade);
                }
            });
            View divider1 = new View(context);
            divider1.setLayoutParams(dividerParams);
            divider1.setBackgroundColor(Color.parseColor("#FFFFFF"));
            graphics.addView(senderImg);
            graphics.addView(divider);
            graphics.addView(itemDesc);
            graphics.addView(divider1);
            graphics.addView(showbtn);
            graphics.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(graphics);
        }

        if(count == 0) {
            TextView noTrades = new TextView(context);
            noTrades.setText(R.string.no_trades_title);
            noTrades.setTextSize(24);
            noTrades.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView noTradesSubtitle = new TextView(context);
            noTradesSubtitle.setText(R.string.no_trades_subtitle_incoming);
            noTradesSubtitle.setTextSize(12);
            noTradesSubtitle.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.addView(noTrades);
            layout.addView(noTradesSubtitle);
        }
    }

    public static void displayIncomingTrades(final View view, final Context context) {
        if(FileUtils.refreshNeeded(context) ) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        displayIncomingTrades(view, context);
                    }
                }, 1000);
            } else {
                updateBearerForDisplay(context, FileUtils.getRefresh(context), "incoming", view);
            }
            return;
        }

        String url = "https://api-trade.opskins.com/ITrade/GetOffers/v1/?state=2&type=received";

        final JSONArray offers = new JSONArray();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject res = json.getJSONObject("response");

                    JSONArray offes = res.getJSONArray("offers");
                    int total = res.getInt("total");
                    final double remaining = total - 100;

                    for(int k = 0; k < offes.length(); k++) {
                        offers.put(offes.getJSONObject(k));
                    }

                    if(remaining <= 0) {
                        displayIncomingTrades(view, offers);
                        return;
                    }

                    setMaxIncomingTrades((int)Math.ceil(remaining / 100));

                    for(int i = 0; i < Math.ceil(remaining / 100); i++) {
                        final int j = i + 2;
                        String url = "https://api-trade.opskins.com/ITrade/GetOffers/v1/?state=2&type=received&page=" + j;
                        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);

                                    JSONArray offs = json.getJSONObject("response").getJSONArray("offers");

                                    for(int k = 0; k < offs.length(); k++) {
                                        offers.put(offs.getJSONObject(k));
                                    }

                                    if(isDoneIncomingTrades()) {
                                        displayIncomingTrades(view, offers);
                                    }
                                } catch(Exception e) {
                                    //TODO: Redirect to error activity
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //TODO: Redirect to error activity
                                if(error.networkResponse.statusCode == 401) {
                                    updateBearerForDisplay(context, FileUtils.getRefresh(context), "incoming", view);
                                    return;
                                } else if(error.networkResponse.statusCode == 400) {
                                    try {
                                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                                        if(json.getString("error").equals("invalid_grant")) {
                                            FileUtils.writeData(context, "access.txt", "", true);
                                            return;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                try {
                                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                        return;
                                    } else if(errorResponse.has("message")) {
                                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                HashMap<String, String> headers = new HashMap<String, String>();

                                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                                return headers;
                            }
                        };
                        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(req);
                    }
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForDisplay(context, FileUtils.getRefresh(context), "incoming", view);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    public static void displayIncomingTradesHistory(final View view, final Context context) {
        if(FileUtils.refreshNeeded(context) ) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        displayIncomingTradesHistory(view, context);
                    }
                }, 1000);
            } else {
                updateBearerForDisplay(context, FileUtils.getRefresh(context), "incominghistory", view);
            }
            return;
        }

        String url = "https://api-trade.opskins.com/ITrade/GetOffers/v1/?state=3,5,6,7,8&type=received";

        final JSONArray offers = new JSONArray();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject res = json.getJSONObject("response");

                    JSONArray offes = res.getJSONArray("offers");
                    int total = res.getInt("total");
                    final double remaining = total - 100;

                    for(int k = 0; k < offes.length(); k++) {
                        offers.put(offes.getJSONObject(k));
                    }

                    if(remaining <= 0) {
                        displayIncomingTrades(view, offers);
                        return;
                    }

                    setMaxIncomingTrades((int)Math.ceil(remaining / 100));

                    for(int i = 0; i < Math.ceil(remaining / 100); i++) {
                        final int j = i + 2;
                        String url = "https://api-trade.opskins.com/ITrade/GetOffers/v1/?state=3,5,6,7,8&type=received&page=" + j;
                        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);

                                    JSONArray offs = json.getJSONObject("response").getJSONArray("offers");

                                    for(int k = 0; k < offs.length(); k++) {
                                        offers.put(offs.getJSONObject(k));
                                    }

                                    if(isDoneIncomingTrades()) {
                                        displayIncomingTrades(view, offers);
                                    }
                                } catch(Exception e) {
                                    //TODO: Redirect to error activity
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //TODO: Redirect to error activity
                                if(error.networkResponse.statusCode == 401) {
                                    updateBearerForDisplay(context, FileUtils.getRefresh(context), "incominghistory", view);
                                    return;
                                } else if(error.networkResponse.statusCode == 400) {
                                    try {
                                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                                        if(json.getString("error").equals("invalid_grant")) {
                                            FileUtils.writeData(context, "access.txt", "", true);
                                            return;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                        return;
                                    } else if(errorResponse.has("message")) {
                                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                HashMap<String, String> headers = new HashMap<String, String>();

                                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                                return headers;
                            }
                        };
                        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(req);
                    }
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForDisplay(context, FileUtils.getRefresh(context), "incoming", view);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    private static int sentTradesMax = 0;
    private static int sentTradesDone = 0;

    static boolean isDoneSentTrades() {
        sentTradesDone++;
        return sentTradesMax == sentTradesDone;
    }

    static void setMaxSentTrades(int incomingTrades) {
        incomingTradesMax = incomingTrades;
    }

    static void displaySentTrades(View view, JSONArray offers) throws JSONException {
        final Context context = view.getContext();
        LinearLayout layout = view.findViewById(R.id.linear_main);
        if(layout == null) {
            layout = view.findViewById(R.id.linear_history);
        }
        layout.removeAllViews();

        int count = 0;

        for(int i = 0; i < offers.length(); i++) {
            if(i != 0) {
                View divider = new View(context);
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
                dividerParams.topMargin = 5;
                dividerParams.bottomMargin = 5;
                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                layout.addView(divider);
            }

            final JSONObject offer = offers.getJSONObject(0);
            JSONObject sender = offer.getJSONObject("sender");
            JSONObject recipient = offer.getJSONObject("recipient");
            final int id = offer.getInt("id");

            count++;

            LinearLayout graphics = new LinearLayout(context);
            graphics.setId(id);

            ImageView senderImg = new ImageView(context);
            new ImageDownloader(senderImg, 64, 64).execute(recipient.getString("avatar").startsWith("http") ? recipient.getString("avatar") : "https://opskins.com" + recipient.getString("avatar"));
            View divider = new View(context);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
            dividerParams.leftMargin = 10;
            dividerParams.rightMargin = 10;
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(Color.parseColor("#FFFFFF"));
            TextView itemDesc = new TextView(context);
            JSONArray senderItems = sender.getJSONArray("items");
            JSONArray recipientItems = recipient.getJSONArray("items");
            Resources res = context.getResources();
            itemDesc.setText(res.getString(R.string.items_description, Integer.toString(recipientItems.length()), Integer.toString(senderItems.length())));
            itemDesc.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(64, 64);
            imgParams.gravity = Gravity.CENTER_VERTICAL;
            senderImg.setLayoutParams(imgParams);
            Button showbtn = new Button(context);
            showbtn.setText(R.string.show_button_text);
            showbtn.setTextSize(12);
            showbtn.setGravity(Gravity.CENTER_VERTICAL);
            showbtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent trade = new Intent(context, TradeActivity.class);
                    trade.putExtra("EXTRA_OFFER_DATA", offer.toString());
                    context.startActivity(trade);
                }
            });
            View divider1 = new View(context);
            divider1.setLayoutParams(dividerParams);
            divider1.setBackgroundColor(Color.parseColor("#FFFFFF"));
            graphics.addView(senderImg);
            graphics.addView(divider);
            graphics.addView(itemDesc);
            graphics.addView(divider1);
            graphics.addView(showbtn);
            graphics.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(graphics);
        }

        if(count == 0) {
            TextView noTrades = new TextView(context);
            noTrades.setText(R.string.no_trades_title);
            noTrades.setTextSize(24);
            noTrades.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView noTradesSubtitle = new TextView(context);
            noTradesSubtitle.setText(R.string.no_trades_subtitle_sent);
            noTradesSubtitle.setTextSize(12);
            noTradesSubtitle.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.addView(noTrades);
            layout.addView(noTradesSubtitle);
        }
    }

    public static void displaySentTrades(final View view, final Context context) {
        if(FileUtils.refreshNeeded(context) ) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        displaySentTrades(view, context);
                    }
                }, 1000);
            } else {
                updateBearerForDisplay(context, FileUtils.getRefresh(context), "sent", view);
            }
            return;
        }

        String url = "https://api-trade.opskins.com/ITrade/GetOffers/v1/?state=2&type=sent";

        final JSONArray offers = new JSONArray();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject res = json.getJSONObject("response");

                    JSONArray offes = res.getJSONArray("offers");
                    int total = res.getInt("total");
                    final double remaining = total - 100;

                    for(int k = 0; k < offes.length(); k++) {
                        offers.put(offes.getJSONObject(k));
                    }

                    if(remaining <= 0) {
                        displaySentTrades(view, offers);
                        return;
                    }

                    setMaxSentTrades((int)Math.ceil(remaining / 100));

                    for(int i = 0; i < Math.ceil(remaining / 100); i++) {
                        final int j = i + 2;
                        String url = "https://api-trade.opskins.com/ITrade/GetOffers/v1/?state=2&type=sent&page=" + j;
                        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);

                                    JSONArray offs = json.getJSONObject("response").getJSONArray("offers");

                                    for(int k = 0; k < offs.length(); k++) {
                                        offers.put(offs.getJSONObject(k));
                                    }

                                    if(isDoneSentTrades()) {
                                        displaySentTrades(view, offers);
                                    }
                                } catch(Exception e) {
                                    //TODO: Redirect to error activity
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //TODO: Redirect to error activity
                                if(error.networkResponse.statusCode == 401) {
                                    updateBearerForDisplay(context, FileUtils.getRefresh(context), "sent", view);
                                    return;
                                } else if(error.networkResponse.statusCode == 400) {
                                    try {
                                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                                        if(json.getString("error").equals("invalid_grant")) {
                                            FileUtils.writeData(context, "access.txt", "", true);
                                            return;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                                try {
                                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                        return;
                                    } else if(errorResponse.has("message")) {
                                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                HashMap<String, String> headers = new HashMap<String, String>();

                                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                                return headers;
                            }
                        };
                        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(req);
                    }
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForDisplay(context, FileUtils.getRefresh(context), "sent", view);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    public static void displaySentTradesHistory(final View view, final Context context) {
        if(FileUtils.refreshNeeded(context) ) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        displaySentTradesHistory(view, context);
                    }
                }, 1000);
            } else {
                updateBearerForDisplay(context, FileUtils.getRefresh(context), "senthistory", view);
            }
            return;
        }

        String url = "https://api-trade.opskins.com/ITrade/GetOffers/v1/?state=3,5,6,7,8&type=sent";

        final JSONArray offers = new JSONArray();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject res = json.getJSONObject("response");

                    JSONArray offes = res.getJSONArray("offers");
                    int total = res.getInt("total");
                    final double remaining = total - 100;

                    for(int k = 0; k < offes.length(); k++) {
                        offers.put(offes.getJSONObject(k));
                    }

                    if(remaining <= 0) {
                        displaySentTrades(view, offers);
                        return;
                    }

                    setMaxSentTrades((int)Math.ceil(remaining / 100));

                    for(int i = 0; i < Math.ceil(remaining / 100); i++) {
                        final int j = i + 2;
                        String url = "https://api-trade.opskins.com/ITrade/GetOffers/v1/?state=3,5,6,7,8&type=sent&page=" + j;
                        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);

                                    JSONArray offs = json.getJSONObject("response").getJSONArray("offers");

                                    for(int k = 0; k < offs.length(); k++) {
                                        offers.put(offs.getJSONObject(k));
                                    }

                                    if(isDoneSentTrades()) {
                                        displaySentTrades(view, offers);
                                    }
                                } catch(Exception e) {
                                    //TODO: Redirect to error activity
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //TODO: Redirect to error activity
                                if(error.networkResponse.statusCode == 401) {
                                    updateBearerForDisplay(context, FileUtils.getRefresh(context), "senthistory", view);
                                    return;
                                } else if(error.networkResponse.statusCode == 400) {
                                    try {
                                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                                        if(json.getString("error").equals("invalid_grant")) {
                                            FileUtils.writeData(context, "access.txt", "", true);
                                            return;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                                try {
                                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                        return;
                                    } else if(errorResponse.has("message")) {
                                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                HashMap<String, String> headers = new HashMap<String, String>();

                                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                                return headers;
                            }
                        };
                        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(req);
                    }
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForDisplay(context, FileUtils.getRefresh(context), "sent", view);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    public static void displayInformation(final LinearLayout view, final Context context, final int uid, final int placeholder, final int items) {
        final String url = "https://api-trade.opskins.com/ITrade/GetUserInventory/v1/?uid=" + Integer.toString(uid) + "&app_id=1";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject res = json.getJSONObject("response");
                    JSONObject user = res.getJSONObject("user_data");

                    ImageView img = new ImageView(context);
                    new ImageDownloader(img, 64, 64).execute(user.getString("avatar").startsWith("http") ? user.getString("avatar") : "https://opskins.com" + user.getString("avatar"));
                    View divider = new View(context);
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                    dividerParams.leftMargin = 10;
                    dividerParams.rightMargin = 10;
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    TextView senderName = new TextView(context);
                    senderName.setText(context.getResources().getString(placeholder, user.getString("username"), Integer.toString(items)));
                    view.setPadding(10, 10, 10,0);
                    view.addView(img);
                    view.addView(divider);
                    view.addView(senderName);
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    //wat
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        });
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    public static void addTwoFactor(final Context context, final String user, final String secret) {
        if(FileUtils.refreshNeeded(context) ) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addTwoFactor(context, user, secret);
                    }
                }, 1000);
            } else {
                updateBearerForAddTwoFactor(context, FileUtils.getRefresh(context), user, secret);
            }
            return;
        }

        final String url = "https://api.opskins.com/IUser/GetProfile/v1/";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject res = json.getJSONObject("response");
                    if(user.equals("/OPSkins:" + res.getString("username")) || user.equals("manual")) {
                        FileUtils.writeData(context, "twofa.txt", secret + "\n" + res.getString("username"), "Generator enabled - Finish setup on OPSkins account page.");
                    } else {
                        Toast.makeText(context, R.string.qr_mismatch, Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForAddTwoFactor(context, FileUtils.getRefresh(context), user, secret);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }

            ;
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    public static void setDrawerInfo(final Context context, final View view) {
        if(FileUtils.refreshNeeded(context)) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setDrawerInfo(context, view);
                    }
                }, 1000);
            } else {
                updateBearerForDrawerInfo(context, FileUtils.getRefresh(context), view);
            }
            return;
        }

        final String url = "https://api.opskins.com/IUser/GetProfile/v1/";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject res = json.getJSONObject("response");
                    ImageView avatar = view.findViewById(R.id.user_info_avatar);
                    LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    avatarParams.gravity = Gravity.BOTTOM;
                    avatarParams.setMargins(0, 64, 0, 0);
                    avatar.setLayoutParams(avatarParams);
                    new ImageDownloader(avatar, 128, 128).execute(res.getString("avatar").startsWith("http") ? res.getString("avatar") : "https://opskins.com" + res.getString("avatar"));;
                    TextView username = view.findViewById(R.id.user_info_name);
                    username.setText(res.getString("username"));
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForDrawerInfo(context, FileUtils.getRefresh(context), view);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }

            ;
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    static void displayItemPick(final LinearLayout view, final JSONObject user, final JSONArray items) {
        final Context ctx = view.getContext();
        try {
            LinearLayout info = new LinearLayout(ctx);
            info.setOrientation(LinearLayout.HORIZONTAL);
            ImageView avatar = new ImageView(ctx);
            new ImageDownloader(avatar, 64, 64).execute(user.getString("avatar").startsWith("http") ? user.getString("avatar") : "https://opskins.com" + user.getString("avatar"));
            View divider = new View(ctx);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
            dividerParams.leftMargin = 10;
            dividerParams.rightMargin = 10;
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(Color.parseColor("#FFFFFF"));
            TextView name = new TextView(ctx);
            name.setText(ctx.getResources().getString(R.string.name, user.getString("username"), Integer.toString(items.length())));
            info.setPadding(10, 10, 10,0);
            info.addView(avatar);
            info.addView(divider);
            info.addView(name);
            LinearLayout itemsLayout = new LinearLayout(ctx);
            itemsLayout.setOrientation(LinearLayout.VERTICAL);
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int maxItems = ((int) Math.floor(metrics.widthPixels / 122));
            int curitems = 0;
            LinearLayout current = new LinearLayout(ctx);
            current.setOrientation(LinearLayout.HORIZONTAL);
            if(items.length() > 0) {
                for (int i = 0; i < items.length(); i++) {
                    View itemDivider = new View(ctx);
                    LinearLayout.LayoutParams itemDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                    itemDividerParams.leftMargin = 10;
                    itemDividerParams.rightMargin = 10;
                    itemDivider.setLayoutParams(itemDividerParams);
                    itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    final JSONObject item = items.getJSONObject(i);

                    ImageView img = new ImageView(ctx);
                    new ImageDownloader(img, 100, 100).execute(item.getJSONObject("image").getString("300px"));
                    img.setOnClickListener(new ImageView.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent itemIntent = new Intent(ctx, ItemActivity.class);
                            itemIntent.putExtra("item", item.toString());
                            ((Activity) ctx).startActivityForResult(itemIntent, 1);
                        }
                    });

                    curitems++;
                    if (curitems > maxItems) {
                        curitems--;
                        current.addView(itemDivider);
                        itemsLayout.addView(current);
                        current = new LinearLayout(ctx);
                        current.setOrientation(LinearLayout.HORIZONTAL);
                        itemDivider = new View(ctx);
                        itemDivider.setLayoutParams(itemDividerParams);
                        itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        current.addView(itemDivider);
                        current.addView(img);
                        View lineDivider = new View(ctx);
                        LinearLayout.LayoutParams lineDividerParams = new LinearLayout.LayoutParams(curitems * 122 + 12, 2);
                        lineDividerParams.topMargin = 10;
                        lineDividerParams.leftMargin = 5;
                        lineDividerParams.bottomMargin = 10;
                        lineDivider.setLayoutParams(lineDividerParams);
                        lineDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        itemsLayout.addView(lineDivider);
                        curitems = 1;
                    } else {
                        current.addView(itemDivider);
                        current.addView(img);
                    }
                }
                View itemDivider = new View(ctx);
                LinearLayout.LayoutParams itemDividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                itemDividerParams.leftMargin = 10;
                itemDividerParams.rightMargin = 10;
                itemDivider.setLayoutParams(itemDividerParams);
                itemDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                current.addView(itemDivider);
                itemsLayout.addView(current);
            } else {
                TextView noItems = new TextView(ctx);
                noItems.setText(R.string.no_items);
                noItems.setGravity(Gravity.CENTER);
                itemsLayout.addView(noItems);
            }
            view.addView(info);
            view.addView(itemsLayout);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void displayItemPick(final LinearLayout view, final Context context, final String app_id, final ArrayList<String> exclude) {
        String url = "https://api-trade.opskins.com/IUser/GetInventory/v1?sort=2&per_page=500&app_id=" + app_id;

        final JSONArray items = new JSONArray();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject res = json.getJSONObject("response");

                    JSONArray ites = res.getJSONArray("items");
                    final JSONObject inOffer = res.getJSONObject("items_in_active_offers");

                    int total = res.getInt("total");
                    final double remaining = total - 500;

                    for(int k = 0; k < ites.length(); k++) {
                        JSONObject item = ites.getJSONObject(k);
                        if(exclude.contains(item.toString())) {
                            continue;
                        }
                        boolean offer = false;
                        if(inOffer.has(Integer.toString(item.getInt("id")))) {
                            offer = true;
                        }
                        item.put("in_offer", offer);
                        items.put(item);
                    }

                    if(remaining <= 0) {
                        String uri = "https://api-trade.opskins.com/IUser/GetProfile/v1/";
                        StringRequest req = new StringRequest(Request.Method.GET, uri, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);
                                    JSONObject res = json.getJSONObject("response");
                                    JSONObject user = res.getJSONObject("user");
                                    user.put("username", user.getString("display_name"));

                                    displayItemPick(view, user, items);
                                } catch(Exception e) {
                                    //TODO: Redirect to error activity
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //TODO: Redirect to error activity
                                if(error.networkResponse.statusCode == 401) {
                                    updateBearerForDisplay(context, FileUtils.getRefresh(context), view, app_id, Integer.toString(getUserID(context)), exclude);
                                    return;
                                } else if(error.networkResponse.statusCode == 400) {
                                    try {
                                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                                        if(json.getString("error").equals("invalid_grant")) {
                                            FileUtils.writeData(context, "access.txt", "", true);
                                            return;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                                try {
                                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                        return;
                                    } else if(errorResponse.has("message")) {
                                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                HashMap<String, String> headers = new HashMap<String, String>();

                                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                                return headers;
                            }
                        };
                        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(req);
                        return;
                    }

                    final int max = (int)Math.ceil(remaining / 500);

                    for(int i = 0; i < Math.ceil(remaining / 500); i++) {
                        final int j = i + 2;
                        final int current = i + 1;
                        String url = "https://api-trade.opskins.com/IUser/GetInventory/v1?sort=2&per_page=500&app_id=" + app_id + "&page=" + j;
                        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);

                                    JSONArray its = json.getJSONObject("response").getJSONArray("items");

                                    for(int k = 0; k < its.length(); k++) {
                                        JSONObject item = its.getJSONObject(k);
                                        if(exclude.contains(item.toString())) {
                                            continue;
                                        }
                                        boolean offer = false;
                                        if(inOffer.has(Integer.toString(item.getInt("id")))) {
                                            offer = true;
                                        }
                                        item.put("in_offer", offer);
                                        items.put(item);
                                    }

                                    if(current == max) {
                                        String uri = "https://api-trade.opskins.com/IUser/GetProfile/v1/";
                                        StringRequest req = new StringRequest(Request.Method.GET, uri, new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                try {
                                                    JSONObject json = new JSONObject(response);
                                                    JSONObject res = json.getJSONObject("response");
                                                    JSONObject user = res.getJSONObject("user");
                                                    user.put("username", user.getString("display_name"));

                                                    displayItemPick(view, user, items);
                                                } catch(Exception e) {
                                                    //TODO: Redirect to error activity
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                //TODO: Redirect to error activity
                                                if(error.networkResponse.statusCode == 401) {
                                                    updateBearerForDisplay(context, FileUtils.getRefresh(context), view, app_id, Integer.toString(getUserID(context)), exclude);
                                                    return;
                                                } else if(error.networkResponse.statusCode == 400) {
                                                    try {
                                                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                                                        if(json.getString("error").equals("invalid_grant")) {
                                                            FileUtils.writeData(context, "access.txt", "", true);
                                                            return;
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                                try {
                                                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                                                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                                                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                                                        new Thread() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    this.sleep(2500);
                                                                    Intent trades = new Intent(context, MainActivity.class);
                                                                    context.startActivity(trades);
                                                                } catch(Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }.start();
                                                        return;
                                                    } else if(errorResponse.has("message")) {
                                                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                                        new Thread() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    this.sleep(2500);
                                                                    Intent trades = new Intent(context, MainActivity.class);
                                                                    context.startActivity(trades);
                                                                } catch(Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }.start();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                } catch (UnsupportedEncodingException e) {
                                                    e.printStackTrace();
                                                }
                                                Intent intent = new Intent(context, MainActivity.class);
                                                context.startActivity(intent);
                                            }
                                        }) {
                                            @Override
                                            public Map<String, String> getHeaders() {
                                                HashMap<String, String> headers = new HashMap<String, String>();

                                                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                                                return headers;
                                            }
                                        };
                                        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(req);
                                        return;
                                    }
                                } catch(Exception e) {
                                    //TODO: Redirect to error activity
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //TODO: Redirect to error activity
                                if(error.networkResponse.statusCode == 401) {
                                    updateBearerForDisplay(context, FileUtils.getRefresh(context), view, app_id, Integer.toString(getUserID(context)), exclude);
                                    return;
                                } else if(error.networkResponse.statusCode == 400) {
                                    try {
                                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                                        if(json.getString("error").equals("invalid_grant")) {
                                            FileUtils.writeData(context, "access.txt", "", true);
                                            return;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                                try {
                                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                        return;
                                    } else if(errorResponse.has("message")) {
                                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                HashMap<String, String> headers = new HashMap<String, String>();

                                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                                return headers;
                            }
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("per_page", "500");
                                params.put("sort", "2");
                                params.put("page", Integer.toString(j));
                                return params;
                            }
                        };
                        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(req);
                    }
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForDisplay(context, FileUtils.getRefresh(context), view, app_id, Integer.toString(getUserID(context)), exclude);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    public static void displayItemPick(final LinearLayout view, final Context context, final String app_id, final String uid, final ArrayList<String> exclude) {
        if(FileUtils.refreshNeeded(context) ) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        displayItemPick(view, context, app_id, uid, exclude);
                    }
                }, 1000);
            } else {
                updateBearerForDisplay(context, FileUtils.getRefresh(context), view, app_id, uid, exclude);
            }
            return;
        }

        if(Integer.parseInt(uid) == getUserID(context)) {
            displayItemPick(view, context, app_id, exclude);
            return;
        }

        String url = "https://api-trade.opskins.com/ITrade/GetUserInventory/v1/?sort=2&per_page=500&uid=" + uid + "&app_id=" + app_id;

        final JSONArray items = new JSONArray();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    final JSONObject res = json.getJSONObject("response");

                    JSONArray ites = res.getJSONArray("items");
                    int total = res.getInt("total");
                    final double remaining = total - 500;

                    for(int k = 0; k < ites.length(); k++) {
                        if(exclude.contains(ites.getJSONObject(k).toString())) {
                            continue;
                        }
                        items.put(ites.getJSONObject(k));
                    }

                    if(remaining <= 0) {
                        displayItemPick(view, res.getJSONObject("user_data"), items);
                        return;
                    }

                    final int max = (int)Math.ceil(remaining / 500);

                    for(int i = 0; i < Math.ceil(remaining / 500); i++) {
                        final int j = i + 2;
                        final int current = i + 1;
                        String url = "https://api-trade.opskins.com/ITrade/GetUserInventory/v1/?sort=2&per_page=500&uid=" + uid + "&app_id=" + app_id + "&page=" + j;
                        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);

                                    JSONArray its = json.getJSONObject("response").getJSONArray("items");

                                    for(int k = 0; k < its.length(); k++) {
                                        if(exclude.contains(its.getJSONObject(k).toString())) {
                                            continue;
                                        }
                                        items.put(its.getJSONObject(k));
                                    }

                                    if(current == max) {
                                        displayItemPick(view, res.getJSONObject("user_data"), items);
                                    }
                                } catch(Exception e) {
                                    //TODO: Redirect to error activity
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //TODO: Redirect to error activity
                                if(error.networkResponse.statusCode == 401) {
                                    updateBearerForDisplay(context, FileUtils.getRefresh(context), view, app_id, uid, exclude);
                                    return;
                                } else if(error.networkResponse.statusCode == 400) {
                                    try {
                                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                                        if(json.getString("error").equals("invalid_grant")) {
                                            FileUtils.writeData(context, "access.txt", "", true);
                                            return;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                                try {
                                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                        return;
                                    } else if(errorResponse.has("message")) {
                                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    this.sleep(2500);
                                                    Intent trades = new Intent(context, MainActivity.class);
                                                    context.startActivity(trades);
                                                } catch(Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                HashMap<String, String> headers = new HashMap<String, String>();

                                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                                return headers;
                            }
                        };
                        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(req);
                    }
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForDisplay(context, FileUtils.getRefresh(context), view, app_id, uid, exclude);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    static void updateBearerForDisplay(final Context context, final String refresh, final String type, final View view) {
        String url = "https://oauth.opskins.com/v1/access_token/";

        refreshProgress = true;

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    String access = json.getString("access_token");
                    String expires = (json.has("expires_in") ? Long.toString(Long.valueOf(Integer.toString(json.getInt("expires_in") * 1000)) + System.currentTimeMillis()) : Long.toString(System.currentTimeMillis()));

                    String data = access + "\n" + expires + "\n" + (json.has("refresh_token") ? json.getString("refresh_token") : refresh);
                    String file = "access.txt";
                    FileUtils.writeData(context, file, data);
                    if(type.equals("incoming")) {
                        displayIncomingTrades(view, context);
                    } else if(type.equals("sent")) {
                        displaySentTrades(view, context);
                    } else if(type.equals("tradeurl")) {
                        displayTradeURL(view, context);
                    } else if(type.equals("incominghistory")) {
                        displayIncomingTradesHistory(view, context);
                    } else if(type.equals("senthistory")) {
                        displaySentTradesHistory(view, context);
                    }
                    refreshProgress = false;
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401) {
                    //Not logged in, or invalid refresh token, either way, make the user re-log.
                    FileUtils.writeData(context, "access.txt", "", true);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                String key = "2f2856914a25:" + FileUtils.getState(context);
                String hash;
                try {
                    hash = Base64.encodeToString(key.getBytes(), Base64.DEFAULT);
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                    return null;
                }

                headers.put("authorization", "Basic " + hash);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "refresh_token");
                params.put("refresh_token", refresh);
                params.put("state", FileUtils.getState(context));
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    public static void makeOffer(final Context context, final String tradeURL, final String two_factor_code, final String message, final String items_to_send, final String items_to_receive) {
        if(FileUtils.refreshNeeded(context) ) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        makeOffer(context, tradeURL, two_factor_code, message, items_to_send, items_to_receive);
                    }
                }, 1000);
            } else {
                updateBearerForMakeOffer(context, FileUtils.getRefresh(context), tradeURL, two_factor_code, message, items_to_send, items_to_receive);
            }
            return;
        }

        final String url = "https://api-trade.opskins.com/ITrade/SendOffer/v1/";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject offer;
                try {
                    JSONObject res = new JSONObject(response);
                    if(!res.has("response")) {
                        if(res.has("message")) {
                            Toast.makeText(context, res.getString("message"), Toast.LENGTH_SHORT).show();
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        this.sleep(2500);
                                        Intent trades = new Intent(context, MainActivity.class);
                                        context.startActivity(trades);
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        } else {
                            Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        this.sleep(2500);
                                        Intent trades = new Intent(context, MainActivity.class);
                                        context.startActivity(trades);
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                        return;
                    }
                    offer = res.getJSONObject("response").getJSONObject("offer");
                    Intent trade = new Intent(context, TradeActivity.class);
                    trade.putExtra("EXTRA_OFFER_DATA", offer.toString());
                    context.startActivity(trade);
                    ((Activity) context).finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForMakeOffer(context, FileUtils.getRefresh(context), tradeURL, two_factor_code, message, items_to_send, items_to_receive);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.has("error") && json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("twofactor_code", two_factor_code);
                params.put("trade_url", tradeURL);
                params.put("items_to_send", items_to_send);
                params.put("items_to_receive", items_to_receive);
                params.put("message", message);
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    public static void updateApps(final Context context, final SelectItemsPagerAdapter adapter) {
        final String url = "https://api-trade.opskins.com/ITrade/GetApps/v1/";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject res = new JSONObject(response).getJSONObject("response");
                    JSONArray apps = res.getJSONArray("apps");
                    adapter.setApps(apps);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    public static void acceptOffer(final Context context, final int offerid, final String two_factor_code) {
        if(FileUtils.refreshNeeded(context) ) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        acceptOffer(context, offerid, two_factor_code);
                    }
                }, 1000);
            } else {
                updateBearerForAcceptOffer(context, FileUtils.getRefresh(context), offerid, two_factor_code);
            }
            return;
        }

        final String url = "https://api-trade.opskins.com/ITrade/AcceptOffer/v1/";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    if(!res.has("response")) {
                        if(res.has("message")) {
                            Toast.makeText(context, res.getString("message"), Toast.LENGTH_SHORT).show();
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        this.sleep(2500);
                                        Intent trades = new Intent(context, MainActivity.class);
                                        context.startActivity(trades);
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        } else {
                            Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        this.sleep(2500);
                                        Intent trades = new Intent(context, MainActivity.class);
                                        context.startActivity(trades);
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Toast.makeText(context, "Offer accepted.", Toast.LENGTH_SHORT).show();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            this.sleep(2500);
                            Intent trades = new Intent(context, MainActivity.class);
                            context.startActivity(trades);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForAcceptOffer(context, FileUtils.getRefresh(context), offerid, two_factor_code);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("twofactor_code", two_factor_code);
                params.put("offer_id", Integer.toString(offerid));
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    static void updateBearerForAcceptOffer(final Context context, final String refresh, final int offerid, final String two_factor_code) {
        String url = "https://oauth.opskins.com/v1/access_token/";

        refreshProgress = true;

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);

                    String access = json.getString("access_token");
                    String expires = (json.has("expires_in") ? Long.toString(Long.valueOf(Integer.toString(json.getInt("expires_in") * 1000)) + System.currentTimeMillis()) : Long.toString(System.currentTimeMillis()));

                    String data = access + "\n" + expires + "\n" + (json.has("refresh_token") ? json.getString("refresh_token") : refresh);

                    String file = "access.txt";
                    FileUtils.writeData(context, file, data);
                    acceptOffer(context, offerid, two_factor_code);
                    refreshProgress = false;
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401) {
                    //Not logged in, or invalid refresh token, either way, make the user re-log.
                    FileUtils.writeData(context, "access.txt", "", true);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                String key = "2f2856914a25:" + FileUtils.getState(context);
                String hash;
                try {
                    hash = Base64.encodeToString(key.getBytes(), Base64.DEFAULT);
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                    return null;
                }

                headers.put("authorization", "Basic " + hash);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "refresh_token");
                params.put("refresh_token", refresh);
                params.put("state", FileUtils.getState(context));
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    static void updateBearerForDrawerInfo(final Context context, final String refresh, final View view) {
        String url = "https://oauth.opskins.com/v1/access_token/";

        refreshProgress = true;

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);

                    String access = json.getString("access_token");
                    String expires = (json.has("expires_in") ? Long.toString(Long.valueOf(Integer.toString(json.getInt("expires_in") * 1000)) + System.currentTimeMillis()) : Long.toString(System.currentTimeMillis()));

                    String data = access + "\n" + expires + "\n" + (json.has("refresh_token") ? json.getString("refresh_token") : refresh);

                    String file = "access.txt";
                    FileUtils.writeData(context, file, data);
                    setDrawerInfo(context, view);
                    refreshProgress = false;
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401) {
                    //Not logged in, or invalid refresh token, either way, make the user re-log.
                    FileUtils.writeData(context, "access.txt", "", true);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                String key = "2f2856914a25:" + FileUtils.getState(context);
                String hash;
                try {
                    hash = Base64.encodeToString(key.getBytes(), Base64.DEFAULT);
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                    return null;
                }

                headers.put("authorization", "Basic " + hash);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "refresh_token");
                params.put("refresh_token", refresh);
                params.put("state", FileUtils.getState(context));
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    public static void cancelOffer(final Context context, final int offerid, final boolean cancel) {
        if(FileUtils.refreshNeeded(context) ) {
            if(refreshProgress) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cancelOffer(context, offerid, cancel);
                    }
                }, 1000);
            } else {
                updateBearerForCancelOffer(context, FileUtils.getRefresh(context), offerid, cancel);
            }
            return;
        }

        final String url = "https://api-trade.opskins.com/ITrade/CancelOffer/v1/";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(context, cancel ? "Offer canceled." : "Offer declined.", Toast.LENGTH_SHORT).show();

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            this.sleep(2500);
                            Intent trades = new Intent(context, MainActivity.class);
                            context.startActivity(trades);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    updateBearerForCancelOffer(context, FileUtils.getRefresh(context), offerid, cancel);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data, "UTF-8"));
                    if(errorResponse.has("status") && errorResponse.getInt("status") == 312) {
                        Toast.makeText(context, "An identical offer already exists.", Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return;
                    } else if(errorResponse.has("message")) {
                        Toast.makeText(context, errorResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    this.sleep(2500);
                                    Intent trades = new Intent(context, MainActivity.class);
                                    context.startActivity(trades);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + FileUtils.getBearer(context));
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("offer_id", Integer.toString(offerid));
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    static void updateBearerForCancelOffer(final Context context, final String refresh, final int offerid, final boolean cancel) {
        String url = "https://oauth.opskins.com/v1/access_token/";

        refreshProgress = true;

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);

                    String access = json.getString("access_token");
                    String expires = (json.has("expires_in") ? Long.toString(Long.valueOf(Integer.toString(json.getInt("expires_in") * 1000)) + System.currentTimeMillis()) : Long.toString(System.currentTimeMillis()));

                    String data = access + "\n" + expires + "\n" + (json.has("refresh_token") ? json.getString("refresh_token") : refresh);

                    String file = "access.txt";
                    FileUtils.writeData(context, file, data);
                    cancelOffer(context, offerid, cancel);
                    refreshProgress = false;
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401) {
                    //Not logged in, or invalid refresh token, either way, make the user re-log.
                    FileUtils.writeData(context, "access.txt", "", true);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                String key = "2f2856914a25:" + FileUtils.getState(context);
                String hash;
                try {
                    hash = Base64.encodeToString(key.getBytes(), Base64.DEFAULT);
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                    return null;
                }

                headers.put("authorization", "Basic " + hash);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "refresh_token");
                params.put("refresh_token", refresh);
                params.put("state", FileUtils.getState(context));
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    static void updateBearerForMakeOffer(final Context context, final String refresh, final String tradeURL, final String two_factor_code, final String message, final String items_to_send, final String items_to_receive) {
        String url = "https://oauth.opskins.com/v1/access_token/";

        refreshProgress = true;

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);

                    String access = json.getString("access_token");
                    String expires = (json.has("expires_in") ? Long.toString(Long.valueOf(Integer.toString(json.getInt("expires_in") * 1000)) + System.currentTimeMillis()) : Long.toString(System.currentTimeMillis()));

                    String data = access + "\n" + expires + "\n" + (json.has("refresh_token") ? json.getString("refresh_token") : refresh);

                    String file = "access.txt";
                    FileUtils.writeData(context, file, data);
                    makeOffer(context, tradeURL, two_factor_code, message, items_to_send, items_to_receive);
                    refreshProgress = false;
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401) {
                    //Not logged in, or invalid refresh token, either way, make the user re-log.
                    FileUtils.writeData(context, "access.txt", "", true);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                String key = "2f2856914a25:" + FileUtils.getState(context);
                String hash;
                try {
                    hash = Base64.encodeToString(key.getBytes(), Base64.DEFAULT);
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                    return null;
                }

                headers.put("authorization", "Basic " + hash);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "refresh_token");
                params.put("refresh_token", refresh);
                params.put("state", FileUtils.getState(context));
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    static void updateBearerForDisplay(final Context context, final String refresh, final LinearLayout view, final String app_id, final String uid, final ArrayList<String> exclude) {
        String url = "https://oauth.opskins.com/v1/access_token/";

        refreshProgress = true;

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);

                    String access = json.getString("access_token");
                    String expires = (json.has("expires_in") ? Long.toString(Long.valueOf(Integer.toString(json.getInt("expires_in") * 1000)) + System.currentTimeMillis()) : Long.toString(System.currentTimeMillis()));

                    String data = access + "\n" + expires + "\n" + (json.has("refresh_token") ? json.getString("refresh_token") : refresh);

                    String file = "access.txt";
                    FileUtils.writeData(context, file, data);
                    displayItemPick(view, context, app_id, uid, exclude);
                    refreshProgress = false;
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401) {
                    //Not logged in, or invalid refresh token, either way, make the user re-log.
                    FileUtils.writeData(context, "access.txt", "", true);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                String key = "2f2856914a25:" + FileUtils.getState(context);
                String hash;
                try {
                    hash = Base64.encodeToString(key.getBytes(), Base64.DEFAULT);
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                    return null;
                }

                headers.put("authorization", "Basic " + hash);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "refresh_token");
                params.put("refresh_token", refresh);
                params.put("state", FileUtils.getState(context));
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    static void updateBearerForAddTwoFactor(final Context context, final String refresh, final String user, final String secret) {
        String url = "https://oauth.opskins.com/v1/access_token/";

        refreshProgress = true;

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);

                    String access = json.getString("access_token");
                    String expires = (json.has("expires_in") ? Long.toString(Long.valueOf(Integer.toString(json.getInt("expires_in") * 1000)) + System.currentTimeMillis()) : Long.toString(System.currentTimeMillis()));

                    String ref = refresh;
                    if(json.has("refresh_token")) {
                        ref = json.getString("refresh_token");
                    }

                    String data = access + "\n" + expires + "\n" + ref;

                    String file = "access.txt";
                    FileUtils.writeData(context, file, data);
                    addTwoFactor(context, user, secret);
                    refreshProgress = false;
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401) {
                    //Not logged in, or invalid refresh token, either way, make the user re-log.
                    FileUtils.writeData(context, "access.txt", "", true);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                String key = "2f2856914a25:" + FileUtils.getState(context);
                String hash;
                try {
                    hash = Base64.encodeToString(key.getBytes(), Base64.DEFAULT);
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                    return null;
                }

                headers.put("authorization", "Basic " + hash);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "refresh_token");
                params.put("refresh_token", refresh);
                params.put("state", FileUtils.getState(context));
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    static void updateBearerForGet(final Context context, final String refresh, final String type) {
        String url = "https://oauth.opskins.com/v1/access_token/";

        refreshProgress = true;

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);

                    String access = json.getString("access_token");
                    String expires = (json.has("expires_in") ? Long.toString(Long.valueOf(Integer.toString(json.getInt("expires_in") * 1000)) + System.currentTimeMillis()) : Long.toString(System.currentTimeMillis()));

                    String ref = refresh;
                    if(json.has("refresh_token")) {
                        ref = json.getString("refresh_token");
                    }

                    String data = access + "\n" + expires + "\n" + ref;

                    String file = "access.txt";
                    FileUtils.writeData(context, file, data);
                    if(type.equals("userid")) {
                        getUserID(context);
                    }
                    refreshProgress = false;
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401) {
                    //Not logged in, or invalid refresh token, either way, make the user re-log.
                    FileUtils.writeData(context, "access.txt", "", true);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                String key = "2f2856914a25:" + FileUtils.getState(context);
                String hash;
                try {
                    hash = Base64.encodeToString(key.getBytes(), Base64.DEFAULT);
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                    return null;
                }

                headers.put("authorization", "Basic " + hash);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "refresh_token");
                params.put("refresh_token", refresh);
                params.put("state", FileUtils.getState(context));
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }

    public static void logout(final Context context, final Activity activity) {
        final String refresh = FileUtils.getRefresh(context);

        String url = "https://oauth.opskins.com/v1/revoke_token/";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);

                    if(json.has("success")) {
                        if(json.getBoolean("success")) {
                            //Update access file and redirect to login screen.
                            FileUtils.writeData(context, "access.txt", "", true);
                            activity.finish();
                        }
                    }
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Redirect to error activity
                if(error.networkResponse.statusCode == 401) {
                    FileUtils.writeData(context, "access.txt", "", true);
                    return;
                } else if(error.networkResponse.statusCode == 400) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        if(json.getString("error").equals("invalid_grant")) {
                            FileUtils.writeData(context, "access.txt", "", true);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                String key = "2f2856914a25:" + FileUtils.getState(context);
                String hash;
                try {
                    hash = Base64.encodeToString(key.getBytes(), Base64.DEFAULT);
                } catch(Exception e) {
                    //TODO: Redirect to error activity
                    e.printStackTrace();
                    return null;
                }

                headers.put("authorization", "Basic " + hash);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("token_type", "refresh");
                params.put("token", refresh);
                return params;
            }
        };
        Singleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
    }
}
