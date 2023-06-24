package com.example.client_view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String root = Environment.getDataDirectory().getPath() + "/data/com.example.client_view/";
        File myDir = new File(root);
        String fname = "data" + ".txt";
        File file = new File(myDir, fname);
        Intent intent;
        if (file.exists()) {
            // file exists, start MainActivity
            intent = new Intent(this, MainActivity.class);
        } else {
            // file does not exist, start LoginActivity
            intent = new Intent(this, RegisterFormActivity.class);
        }
        startActivity(intent);
        // finish the splash activity
        finish();
    }
}