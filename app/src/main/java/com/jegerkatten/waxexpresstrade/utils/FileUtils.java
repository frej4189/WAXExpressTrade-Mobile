package com.jegerkatten.waxexpresstrade.utils;

import android.content.Context;
import android.content.Intent;

import com.jegerkatten.waxexpresstrade.LoadingActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {

    public static void writeData(Context context, String file, String data) {
        FileOutputStream out;

        try {
            out = context.openFileOutput(file, Context.MODE_PRIVATE);
            out.write(data.getBytes());
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeData(Context context, String file, String data, boolean redirect) {
        FileOutputStream out;

        try {
            out = context.openFileOutput(file, Context.MODE_PRIVATE);
            out.write(data.getBytes());
            out.close();

            if(redirect) {
                Intent loading = new Intent(context, LoadingActivity.class);
                context.startActivity(loading);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean refreshNeeded(Context context) {
        try {
            boolean refreshNeeded = false;
            FileInputStream input = context.openFileInput("access.txt");
            InputStreamReader reader = new InputStreamReader(input);
            BufferedReader br = new BufferedReader(reader);
            int lines = 0;
            String line;
            while((line = br.readLine()) != null) {
                lines++;
                if(lines == 2) {
                    if(System.currentTimeMillis() >= Long.valueOf(line)) {
                        refreshNeeded = true;
                    }
                }
            }
            return refreshNeeded;
        } catch(FileNotFoundException e) {
            //Not logged in, redirect to loading screen.
            Intent intent = new Intent(context, LoadingActivity.class);
            context.startActivity(intent);
            return false;
        } catch(IOException e) {
            return refreshNeeded(context);
        }
    }

    public static String getState(Context context) {
        try {
            String secret = "";
            FileInputStream input = context.openFileInput("secret.txt");
            InputStreamReader reader = new InputStreamReader(input);
            BufferedReader br = new BufferedReader(reader);
            int lines = 0;
            String line;
            while((line = br.readLine()) != null) {
                lines++;
                if(lines == 1) {
                    secret = line;
                }
            }
            return secret;
        } catch(FileNotFoundException e) {
            //Not logged in, redirect to loading screen.
            Intent intent = new Intent(context, LoadingActivity.class);
            context.startActivity(intent);
            return "error";
        } catch(IOException e) {
            return getState(context);
        }
    }

    public static String getBearer(Context context) {
        try {
            String secret = "";
            FileInputStream input = context.openFileInput("access.txt");
            InputStreamReader reader = new InputStreamReader(input);
            BufferedReader br = new BufferedReader(reader);
            int lines = 0;
            String line;
            while((line = br.readLine()) != null) {
                lines++;
                if(lines == 1) {
                    secret = line;
                }
            }
            return secret;
        } catch(FileNotFoundException e) {
            //Not logged in, redirect to loading screen.
            Intent intent = new Intent(context, LoadingActivity.class);
            context.startActivity(intent);
            return "error";
        } catch(IOException e) {
            return getBearer(context);
        }
    }

    public static String getRefresh(Context context) {
        try {
            String secret = "";
            FileInputStream input = context.openFileInput("access.txt");
            InputStreamReader reader = new InputStreamReader(input);
            BufferedReader br = new BufferedReader(reader);
            int lines = 0;
            String line;
            while((line = br.readLine()) != null) {
                lines++;
                if(lines == 3) {
                    secret = line;
                }
            }
            return secret;
        } catch(FileNotFoundException e) {
            //Not logged in, redirect to loading screen.
            Intent intent = new Intent(context, LoadingActivity.class);
            context.startActivity(intent);
            return "error";
        } catch(IOException e) {
            return getRefresh(context);
        }
    }
}
