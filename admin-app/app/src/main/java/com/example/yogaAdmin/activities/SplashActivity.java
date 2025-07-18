package com.example.yogaAdmin.activities;

import com.example.yogaAdmin.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide action bar for splash screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeAnimations();

        // Navigate to MainActivity after delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

                // Add fade transition
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, SPLASH_DURATION);
    }

    private void initializeAnimations() {
        // Animate logo
        ImageView logoIcon = findViewById(R.id.logo_icon);
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_bounce);
        logoIcon.startAnimation(logoAnimation);

        // Animate title
        TextView appTitle = findViewById(R.id.app_title);
        Animation titleAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        appTitle.startAnimation(titleAnimation);

        // Animate subtitle
        TextView appSubtitle = findViewById(R.id.app_subtitle);
        Animation subtitleAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        subtitleAnimation.setStartOffset(300); // Delay by 300ms
        appSubtitle.startAnimation(subtitleAnimation);

        // Animate loading text
        TextView loadingText = findViewById(R.id.loading_text);
        Animation loadingAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        loadingAnimation.setStartOffset(600); // Delay by 600ms
        loadingText.startAnimation(loadingAnimation);
    }
}