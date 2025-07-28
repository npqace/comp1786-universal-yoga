package com.example.yogaAdmin.services;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREFS_NAME = "YogaAdminPrefs";
    private static final String FIRST_SYNC = "isFirstSync";
    private final SharedPreferences sharedPreferences;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFirstSync() {
        return sharedPreferences.getBoolean(FIRST_SYNC, true);
    }

    public void setFirstSync(boolean isFirst) {
        sharedPreferences.edit().putBoolean(FIRST_SYNC, isFirst).apply();
    }
}
