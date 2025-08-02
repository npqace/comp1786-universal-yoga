package com.example.yogaAdmin;

import android.app.Application;
import com.example.yogaAdmin.services.FirebaseSyncManager;
import com.google.firebase.database.FirebaseDatabase;

public class MainApplication extends Application {

    private FirebaseSyncManager firebaseSyncManager;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        firebaseSyncManager = new FirebaseSyncManager(this);
        firebaseSyncManager.performInitialSync();
    }
}
