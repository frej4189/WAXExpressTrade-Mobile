package com.jegerkatten.waxexpresstrade.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

    private static Map<String, Bitmap> downloaded = new HashMap<String, Bitmap>();

    ImageView bmImage;
    int width;
    int height;

    public ImageDownloader(ImageView bmImage, int width, int height) {
        this.bmImage = bmImage;
        this.width = width;
        this.height = height;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        if(downloaded.containsKey(urldisplay)) {
            mIcon11 = Bitmap.createScaledBitmap(downloaded.get(urldisplay), width, height, false);
        } else {
            try {
                InputStream in = new URL(urldisplay).openStream();
                mIcon11 = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(in), width, height, false);
                downloaded.put(urldisplay, mIcon11);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }

}
