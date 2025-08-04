package com.example.yogaAdmin.services;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A utility class for managing SharedPreferences.
 * This class provides a simple interface for storing and retrieving a flag
 * that indicates whether the initial data sync from Firebase has been performed.
 */
public class SharedPreferencesManager {
    // The name of the SharedPreferences file.
    private static final String PREFS_NAME = "YogaAdminPrefs";
    // The key for the boolean flag that tracks the first sync.
    private static final String FIRST_SYNC = "isFirstSync";
    // The SharedPreferences instance.
    private final SharedPreferences sharedPreferences;

    /**
     * Constructor for the SharedPreferencesManager.
     *
     * @param context The application context, used to get the SharedPreferences instance.
     */
    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Checks if it is the first time the app is performing the sync.
     *
     * @return {@code true} if the sync has not been performed before, {@code false} otherwise.
     *         Defaults to {@code true}.
     */
    public boolean isFirstSync() {
        return sharedPreferences.getBoolean(FIRST_SYNC, true);
    }

    /**
     * Sets the flag indicating whether the initial sync has been performed.
     *
     * @param isFirst {@code true} to indicate that the next sync will be the first,
     *                {@code false} to indicate that the initial sync has been completed.
     */
    public void setFirstSync(boolean isFirst) {
        sharedPreferences.edit().putBoolean(FIRST_SYNC, isFirst).apply();
    }
}
