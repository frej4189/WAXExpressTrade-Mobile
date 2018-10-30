package com.jegerkatten.waxexpresstrade.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.jegerkatten.waxexpresstrade.LoadingActivity;
import com.jegerkatten.waxexpresstrade.MainActivity;
import com.jegerkatten.waxexpresstrade.R;

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

    public static void writeData(final Context context, String file, String data, String reason) {
        FileOutputStream out;

        try {
            out = context.openFileOutput(file, Context.MODE_PRIVATE);
            out.write(data.getBytes());
            out.close();
            Toast.makeText(context, reason, Toast.LENGTH_SHORT).show();
            new Thread() {
                public void run() {
                    try {
                        this.sleep(2500);
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
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

    public static boolean refreshNeeded(final Context context) {
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
                    if(Long.valueOf(line) - System.currentTimeMillis() <= 0) {
                        return true;
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
            Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
            new Thread() {
                public void run() {
                    try {
                        this.sleep(2500);
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            e.printStackTrace();
            return false;
        }
    }

    public static String getState(final Context context) {
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
            Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
            new Thread() {
                public void run() {
                    try {
                        this.sleep(2500);
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            e.printStackTrace();
            return "error";
        }
    }

    public static String getBearer(final Context context) {
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
            Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
            new Thread() {
                public void run() {
                    try {
                        this.sleep(2500);
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            e.printStackTrace();
            return "error";
        }
    }

    public static String getRefresh(final Context context) {
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
            Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
            new Thread() {
                public void run() {
                    try {
                        this.sleep(2500);
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            e.printStackTrace();
            return "error";
        }
    }

    public static String get2FAUser(final Context context) {
        try {
            String secret = null;
            FileInputStream input = context.openFileInput("twofa.txt");
            InputStreamReader reader = new InputStreamReader(input);
            BufferedReader br = new BufferedReader(reader);
            int lines = 0;
            String line;
            while((line = br.readLine()) != null) {
                lines++;
                if(lines == 2) {
                    secret = line;
                }
            }
            return secret;
        } catch(FileNotFoundException e) {
            return null;
        } catch(IOException e) {
            Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
            new Thread() {
                public void run() {
                    try {
                        this.sleep(2500);
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            e.printStackTrace();
            return null;
        }
    }

    public static String get2FASecret(final Context context) {
        try {
            String secret = null;
            FileInputStream input = context.openFileInput("twofa.txt");
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
            return null;
        } catch(IOException e) {
            Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
            new Thread() {
                public void run() {
                    try {
                        this.sleep(2500);
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            e.printStackTrace();
            return null;
        }
    }
}
