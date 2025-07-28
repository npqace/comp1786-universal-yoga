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

public class FirebaseSyncService extends Service {

    private static final String TAG = "FirebaseSyncService";
    private YogaCourseDao yogaCourseDao;
    private YogaClassDao yogaClassDao;
    private SharedPreferencesManager sharedPreferencesManager;
    private DatabaseReference coursesRef;
    private DatabaseReference classesRef;

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        syncData();
        return START_NOT_STICKY;
    }

    private void syncData() {
        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaCourse course = snapshot.getValue(YogaCourse.class);
                    if (course != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            yogaCourseDao.insert(course);
                        });
                    }
                }
                // Once courses are synced, sync classes
                syncClasses();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read courses.", databaseError.toException());
            }
        });
    }

    private void syncClasses() {
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaClass yogaClass = snapshot.getValue(YogaClass.class);
                    if (yogaClass != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            yogaClassDao.insert(yogaClass);
                        });
                    }
                }
                // After both are synced, update the flag
                sharedPreferencesManager.setInitialSyncComplete(true);
                Log.d(TAG, "Initial sync complete.");
                stopSelf(); // Stop the service
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read classes.", databaseError.toException());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
