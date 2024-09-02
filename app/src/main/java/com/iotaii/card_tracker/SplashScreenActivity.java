package com.iotaii.card_tracker;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.iotaii.card_tracker.R;


public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // Splash screen delay time in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Delay for a specified amount of time, then start the main activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start main activity
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                // Close this activity
                finish();
            }
        }, SPLASH_DELAY);
    }
}

