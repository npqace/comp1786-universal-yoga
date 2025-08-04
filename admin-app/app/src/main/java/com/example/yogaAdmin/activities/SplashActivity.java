package com.example.yogaAdmin.activities;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.utils.NetworkStatusLiveData;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaAdmin.services.FirebaseSyncService;
import com.example.yogaAdmin.utils.SharedPreferencesManager;

/**
 * The splash screen activity, displayed when the application is launched.
 * It shows a logo and branding, plays animations, and handles initial setup tasks
 * like checking for network connectivity and triggering the initial data sync.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // Duration of the splash screen in milliseconds (3 seconds)
    private NetworkStatusLiveData networkStatusLiveData;
    private TextView tvOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide the action bar for a full-screen splash experience
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tvOffline = findViewById(R.id.tv_offline);
        // Observe network status and show an offline indicator if needed
        networkStatusLiveData = new NetworkStatusLiveData(getApplicationContext());
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                tvOffline.setVisibility(View.GONE);
                // If online, check if the initial data sync from Firebase has been completed.
                SharedPreferencesManager prefsManager = new SharedPreferencesManager(this);
                if (!prefsManager.isInitialSyncComplete()) {
                    // If not, start the background service to perform the sync.
                    Intent syncIntent = new Intent(this, FirebaseSyncService.class);
                    startService(syncIntent);
                }
            } else {
                // If offline, show the offline indicator.
                tvOffline.setVisibility(View.VISIBLE);
            }
        });

        // Start the UI animations.
        initializeAnimations();

        // Use a Handler to delay the transition to the MainActivity.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Create an Intent to start MainActivity.
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                // Finish SplashActivity so the user cannot navigate back to it.
                finish();

                // Apply a fade transition for a smooth visual effect.
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, SPLASH_DURATION);
    }

    /**
     * Loads and starts animations for the UI elements on the splash screen.
     */
    private void initializeAnimations() {
        // Animate the logo with a bounce effect.
        ImageView logoIcon = findViewById(R.id.logo_icon);
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_bounce);
        logoIcon.startAnimation(logoAnimation);

        // Animate the app title with a fade-in-up effect.
        TextView appTitle = findViewById(R.id.app_title);
        Animation titleAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        appTitle.startAnimation(titleAnimation);

        // Animate the app subtitle with a delayed fade-in-up effect.
        TextView appSubtitle = findViewById(R.id.app_subtitle);
        Animation subtitleAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        subtitleAnimation.setStartOffset(300); // Delay start by 300ms
        appSubtitle.startAnimation(subtitleAnimation);

        // Animate the "Loading..." text with a further delayed fade-in-up effect.
        TextView loadingText = findViewById(R.id.loading_text);
        Animation loadingAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        loadingAnimation.setStartOffset(600); // Delay start by 600ms
        loadingText.startAnimation(loadingAnimation);
    }
}
