package com.example.yogaAdmin.services;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.example.yogaAdmin.database.AppDatabase;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.repository.YogaClassRepository;
import com.example.yogaAdmin.repository.YogaCourseRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FirebaseSyncManager {

    private static final String TAG = "FirebaseSyncManager";
    private final DatabaseReference databaseReference;
    private final YogaCourseRepository courseRepository;
    private final YogaClassRepository classRepository;
    private final SharedPreferencesManager prefsManager;

    private ValueEventListener coursesListener;
    private ValueEventListener classesListener;
    private DatabaseReference coursesRef;
    private DatabaseReference classesRef;

    public FirebaseSyncManager(Context context) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        Application application = (Application) context.getApplicationContext();
        courseRepository = new YogaCourseRepository(application);
        classRepository = YogaClassRepository.getInstance(application);
        prefsManager = new SharedPreferencesManager(context);
    }

    public void performInitialSync() {
        if (prefsManager.isFirstSync()) {
            Log.d(TAG, "Performing initial data sync...");
            syncCourses(() -> {
                syncClasses(() -> {
                    Log.d(TAG, "Initial sync completed.");
                    prefsManager.setFirstSync(false);
                    // After initial sync, start real-time listeners
                    startRealtimeSync();
                });
            });
        } else {
            Log.d(TAG, "Initial sync already performed. Starting real-time sync.");
            startRealtimeSync();
        }
    }

    private void syncCourses(final Runnable onComplete) {
        databaseReference.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaCourse course = snapshot.getValue(YogaCourse.class);
                    if (course != null) {
                        courseRepository.insertFromSync(course);
                    }
                }
                Log.d(TAG, "Courses synced.");
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to sync courses.", databaseError.toException());
            }
        });
    }

    private void syncClasses(final Runnable onComplete) {
        databaseReference.child("classes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaClass yogaClass = snapshot.getValue(YogaClass.class);
                    if (yogaClass != null) {
                        classRepository.insertFromSync(yogaClass);
                    }
                }
                Log.d(TAG, "Classes synced.");
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to sync classes.", databaseError.toException());
            }
        });
    }

    public void startRealtimeSync() {
        stopRealtimeSync(); // Ensure no duplicate listeners

        coursesRef = databaseReference.child("courses");
        coursesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaCourse course = snapshot.getValue(YogaCourse.class);
                    if (course != null && course.getFirebaseKey() != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            YogaCourse existingCourse = courseRepository.getCourseByFirebaseKey(course.getFirebaseKey());
                            if (existingCourse != null) {
                                course.setId(existingCourse.getId());
                                courseRepository.updateFromSync(course);
                            } else {
                                courseRepository.insertFromSync(course);
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        coursesRef.addValueEventListener(coursesListener);

        classesRef = databaseReference.child("classes");
        classesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaClass yogaClass = snapshot.getValue(YogaClass.class);
                    if (yogaClass != null && yogaClass.getFirebaseKey() != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            YogaClass existingClass = classRepository.getClassByFirebaseKey(yogaClass.getFirebaseKey());
                            if (existingClass != null) {
                                yogaClass.setId(existingClass.getId());
                                classRepository.updateFromSync(yogaClass);
                            } else {
                                classRepository.insertFromSync(yogaClass);
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        classesRef.addValueEventListener(classesListener);
        Log.d(TAG, "Real-time sync listeners started.");
    }

    public void stopRealtimeSync() {
        if (coursesListener != null && coursesRef != null) {
            coursesRef.removeEventListener(coursesListener);
            coursesListener = null;
        }
        if (classesListener != null && classesRef != null) {
            classesRef.removeEventListener(classesListener);
            classesListener = null;
        }
        Log.d(TAG, "Real-time sync listeners stopped.");
    }

    public void uploadAllData(List<YogaCourse> courses, List<YogaClass> classes) {
        DatabaseReference coursesRef = databaseReference.child("courses");
        if (courses != null) {
            for (YogaCourse course : courses) {
                if (course.getFirebaseKey() != null && !course.getFirebaseKey().isEmpty()) {
                    coursesRef.child(course.getFirebaseKey()).setValue(course);
                }
            }
        }

        DatabaseReference classesRef = databaseReference.child("classes");
        if (classes != null) {
            for (YogaClass yogaClass : classes) {
                if (yogaClass.getFirebaseKey() != null && !yogaClass.getFirebaseKey().isEmpty()) {
                    classesRef.child(yogaClass.getFirebaseKey()).setValue(yogaClass);
                }
            }
        }
    }
}
