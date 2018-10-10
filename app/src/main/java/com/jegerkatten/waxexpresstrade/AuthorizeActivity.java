package com.jegerkatten.waxexpresstrade;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.jegerkatten.waxexpresstrade.utils.RandomUtils;
import com.jegerkatten.waxexpresstrade.utils.RequestUtils;
import com.jegerkatten.waxexpresstrade.utils.Singleton;
import com.jegerkatten.waxexpresstrade.utils.URIUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AuthorizeActivity extends AppCompatActivity {

    private boolean checkAuthenticated() {
        try {
            FileInputStream input = openFileInput("access.txt");
            InputStreamReader reader = new InputStreamReader(input);
            BufferedReader br = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            int lines = 0;
            String line;
            while((line = br.readLine()) != null) {
                lines++;
                sb.append(line);
            }

            if(lines == 3)
                return true;
        } catch(FileNotFoundException e) {
            return false;
        } catch(IOException e) {
            //TODO: Redirect to error activity
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(checkAuthenticated()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }
        setContentView(R.layout.activity_authorize);

        Intent intent = getIntent();
        Uri data = intent.getData();

        try {
            Map<String, String> query = URIUtils.splitQuery(new URL(data.toString()));
            if(!query.containsKey("code")) {
                Intent loadingIntent = new Intent(this, LoadingActivity.class);
                startActivity(loadingIntent);
                return;
            }
            final String code = query.get("code");
            final String state = query.get("state");

            String url = "https://oauth.opskins.com/v1/access_token/";

            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject json = new JSONObject(response);

                        String access = json.getString("access_token");
                        String expires = Long.toString(Long.valueOf(Integer.toString(json.getInt("expires_in") * 1000)) + System.currentTimeMillis());
                        String refresh = "no refresh";
                        if(json.has("refresh_token")) {
                            refresh = json.getString("refresh_token");
                        }

                        String data = access + "\n" + expires + "\n" + refresh;

                        String file = "access.txt";

                        FileOutputStream out;

                        try {
                            out = openFileOutput(file, Context.MODE_PRIVATE);
                            out.write(data.getBytes());
                            out.close();

                            launchMain();
                        } catch(Exception e) {
                            e.printStackTrace();
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
                    System.out.println("Error: " + error
                            + "\nStatus Code: " + error.networkResponse.statusCode
                            + "\nResponse Data: " + new String(error.networkResponse.data)
                            + "\nCause: " + error.getCause()
                            + "\nMessage: " + error.getMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    String key = "2f2856914a25:" + state;
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
                    params.put("grant_type", "authorization_code");
                    params.put("code", code);
                    return params;
                }
            };
            Singleton.getInstance(this.getApplicationContext()).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO: Redirect to error activity
        }
    }

    public void launchMain() {
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }
}
