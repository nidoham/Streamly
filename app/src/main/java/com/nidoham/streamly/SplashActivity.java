package com.nidoham.streamly;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Duration of wait time in milliseconds
    private static final int SPLASH_DURATION = 2500; // 2.5 seconds

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Using Handler with main looper for API 26+ compatibility
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Launch MainActivity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            // Close splash activity to prevent going back to it
            finish();
            // Apply proper transition animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
         },
        SPLASH_DURATION);
    }
}
