package com.example.yogaAdmin.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.yogaAdmin.R;

public class UIUtils {

    public static void configureStatusBar(Activity activity) {
        Window window = activity.getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(R.color.dark_blue, activity.getTheme()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    View decorView = window.getDecorView();
                    int flags = decorView.getSystemUiVisibility();
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    decorView.setSystemUiVisibility(flags);
                }
            }
        }
    }
}