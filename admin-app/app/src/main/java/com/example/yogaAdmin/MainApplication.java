package com.example.yogaAdmin;

import android.app.Application;
import com.example.yogaAdmin.services.FirebaseSyncManager;

public class MainApplication extends Application {

    private FirebaseSyncManager firebaseSyncManager;

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseSyncManager = new FirebaseSyncManager(this);
        firebaseSyncManager.performInitialSync();
    }
}