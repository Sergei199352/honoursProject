package com.example.ReMemoryHons;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity  extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This is where you could initialize things you need at app startup.

        // Using a Handler to delay the splash for 2 seconds.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Intent to start the main activity after the splash screen.
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                // Close this activity
                finish();
            }
        }, 3000); // Delay in milliseconds
    }
}
