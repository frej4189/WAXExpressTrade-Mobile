package com.jegerkatten.waxexpresstrade;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jegerkatten.waxexpresstrade.utils.RequestUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class LoadingActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_loading);

        if(checkAuthenticated()) {
            RequestUtils.getUserID(this);
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
        } else {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        }
        finish();
    }
}
