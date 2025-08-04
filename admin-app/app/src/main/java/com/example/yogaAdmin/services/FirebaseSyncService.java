package com.example.yogaAdmin.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.yogaAdmin.dao.YogaClassDao;
import com.example.yogaAdmin.dao.YogaCourseDao;
import com.example.yogaAdmin.database.AppDatabase;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.utils.SharedPreferencesManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A background service for performing the initial data synchronization from Firebase.
 * This service is started once when the app is first launched to populate the local
 * Room database with data from the Firebase Realtime Database.
 * It stops itself once the synchronization is complete.
 */
public class FirebaseSyncService extends Service {

    private static final String TAG = "FirebaseSyncService";
    // DAOs for database access.
    private YogaCourseDao yogaCourseDao;
    private YogaClassDao yogaClassDao;
    // Manager for shared preferences to track sync status.
    private SharedPreferencesManager sharedPreferencesManager;
    // Firebase database references.
    private DatabaseReference coursesRef;
    private DatabaseReference classesRef;

    /**
     * Called by the system when the service is first created.
     * Initializes database and Firebase references.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        yogaCourseDao = db.yogaCourseDao();
        yogaClassDao = db.yogaClassDao();
        sharedPreferencesManager = new SharedPreferencesManager(this);
        coursesRef = FirebaseDatabase.getInstance().getReference("courses");
        classesRef = FirebaseDatabase.getInstance().getReference("classes");
    }

    /**
     * Called by the system every time a client starts the service.
     * This method is the entry point for the service's work.
     *
     * @param intent The Intent supplied to startService().
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return The return value indicates what semantics the system should use for the service's current started state.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        // Begin the data synchronization process.
        syncData();
        // The service will not be recreated if it's killed by the system.
        return START_NOT_STICKY;
    }

    /**
     * Starts the process of syncing courses from Firebase.
     * After courses are synced, it proceeds to sync classes.
     */
    private void syncData() {
        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaCourse course = snapshot.getValue(YogaCourse.class);
                    if (course != null) {
                        // Insert each course into the local database on a background thread.
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            yogaCourseDao.insert(course);
                        });
                    }
                }
                // Once courses are synced, proceed to sync the classes.
                syncClasses();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read courses.", databaseError.toException());
            }
        });
    }

    /**
     * Syncs all classes from Firebase to the local Room database.
     * After completion, it marks the initial sync as complete and stops the service.
     */
    private void syncClasses() {
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaClass yogaClass = snapshot.getValue(YogaClass.class);
                    if (yogaClass != null) {
                        // Insert each class into the local database on a background thread.
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            yogaClassDao.insert(yogaClass);
                        });
                    }
                }
                // After both courses and classes are synced, update the flag in SharedPreferences.
                sharedPreferencesManager.setInitialSyncComplete(true);
                Log.d(TAG, "Initial sync complete.");
                // Stop the service as its work is done.
                stopSelf();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read classes.", databaseError.toException());
            }
        });
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    /**
     * This service does not support binding, so it returns null.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return Return null because clients cannot bind to this service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
