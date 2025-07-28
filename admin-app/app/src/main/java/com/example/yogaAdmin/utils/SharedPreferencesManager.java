package com.example.yogaAdmin.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME = "YogaAdminPrefs";
    private static final String KEY_INITIAL_SYNC_COMPLETE = "isInitialSyncComplete";

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isInitialSyncComplete() {
        return sharedPreferences.getBoolean(KEY_INITIAL_SYNC_COMPLETE, false);
    }

    public void setInitialSyncComplete(boolean isComplete) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_INITIAL_SYNC_COMPLETE, isComplete);
        editor.apply();
    }
}
