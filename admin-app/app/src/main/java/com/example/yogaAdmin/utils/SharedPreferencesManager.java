package com.example.yogaAdmin.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A utility class for managing SharedPreferences.
 * This class provides a simple interface for storing and retrieving a flag
 * that indicates whether the initial data sync from Firebase has been completed.
 */
public class SharedPreferencesManager {

    // The name of the SharedPreferences file.
    private static final String PREF_NAME = "YogaAdminPrefs";
    // The key for the boolean flag that tracks the initial sync completion.
    private static final String KEY_INITIAL_SYNC_COMPLETE = "isInitialSyncComplete";

    // The SharedPreferences instance.
    private final SharedPreferences sharedPreferences;

    /**
     * Constructor for the SharedPreferencesManager.
     *
     * @param context The application context, used to get the SharedPreferences instance.
     */
    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Checks if the initial data sync has been completed.
     *
     * @return {@code true} if the sync has been completed, {@code false} otherwise.
     *         Defaults to {@code false}.
     */
    public boolean isInitialSyncComplete() {
        return sharedPreferences.getBoolean(KEY_INITIAL_SYNC_COMPLETE, false);
    }

    /**
     * Sets the flag indicating that the initial sync has been completed.
     *
     * @param isComplete {@code true} to mark the sync as complete, {@code false} otherwise.
     */
    public void setInitialSyncComplete(boolean isComplete) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_INITIAL_SYNC_COMPLETE, isComplete);
        editor.apply();
    }
}
