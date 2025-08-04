package com.example.yogaAdmin;

import android.app.Application;
import com.example.yogaAdmin.services.FirebaseSyncManager;
import com.google.firebase.database.FirebaseDatabase;

/**
 * The main application class for the Yoga Admin app.
 * This class is instantiated when the application is started.
 */
public class MainApplication extends Application {

    // Manages the synchronization of data with Firebase.
    private FirebaseSyncManager firebaseSyncManager;

    /**
     * Called when the application is starting, before any other application objects have been created.
     * Used for global initialization that needs to be shared across all other components.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Firebase offline persistence to allow the app to work with cached data when offline.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // Initialize the FirebaseSyncManager to handle data synchronization.
        firebaseSyncManager = new FirebaseSyncManager(this);
        // Perform the initial data sync from Firebase to the local database.
        firebaseSyncManager.performInitialSync();
    }
}
