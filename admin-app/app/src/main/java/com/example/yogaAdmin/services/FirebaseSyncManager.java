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

/**
 * Manages the synchronization of data between the local Room database and the Firebase Realtime Database.
 * It handles the initial data pull and sets up real-time listeners for continuous updates.
 */
public class FirebaseSyncManager {

    private static final String TAG = "FirebaseSyncManager";
    private final DatabaseReference databaseReference;
    private final YogaCourseRepository courseRepository;
    private final YogaClassRepository classRepository;
    private final SharedPreferencesManager prefsManager;

    // Listeners for real-time data changes from Firebase.
    private ValueEventListener coursesListener;
    private ValueEventListener classesListener;
    // References to the specific nodes in Firebase.
    private DatabaseReference coursesRef;
    private DatabaseReference classesRef;

    /**
     * Constructor for the FirebaseSyncManager.
     *
     * @param context The application context.
     */
    public FirebaseSyncManager(Context context) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        Application application = (Application) context.getApplicationContext();
        courseRepository = new YogaCourseRepository(application);
        classRepository = YogaClassRepository.getInstance(application);
        prefsManager = new SharedPreferencesManager(context);
    }

    /**
     * Performs the initial data synchronization if it's the first time the app is run.
     * After the initial sync, it starts the real-time listeners.
     */
    public void performInitialSync() {
        if (prefsManager.isFirstSync()) {
            Log.d(TAG, "Performing initial data sync...");
            // Sync courses first, then classes, then start real-time sync.
            syncCourses(() -> {
                syncClasses(() -> {
                    Log.d(TAG, "Initial sync completed.");
                    prefsManager.setFirstSync(false);
                    startRealtimeSync();
                });
            });
        } else {
            Log.d(TAG, "Initial sync already performed. Starting real-time sync.");
            startRealtimeSync();
        }
    }

    /**
     * Fetches all courses from Firebase and inserts them into the local Room database.
     *
     * @param onComplete A callback to be executed after the sync is complete.
     */
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

    /**
     * Fetches all classes from Firebase and inserts them into the local Room database.
     *
     * @param onComplete A callback to be executed after the sync is complete.
     */
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

    /**
     * Starts real-time synchronization by attaching listeners to the Firebase database references.
     * These listeners will update the local database whenever data changes in Firebase.
     */
    public void startRealtimeSync() {
        stopRealtimeSync(); // Ensure no duplicate listeners are attached.

        // Listener for the 'courses' node.
        coursesRef = databaseReference.child("courses");
        coursesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaCourse course = snapshot.getValue(YogaCourse.class);
                    if (course != null && course.getFirebaseKey() != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            // Check if the course already exists locally.
                            YogaCourse existingCourse = courseRepository.getCourseByFirebaseKey(course.getFirebaseKey());
                            if (existingCourse != null) {
                                // If it exists, update it.
                                course.setId(existingCourse.getId());
                                courseRepository.updateFromSync(course);
                            } else {
                                // If not, insert it.
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

        // Listener for the 'classes' node.
        classesRef = databaseReference.child("classes");
        classesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaClass yogaClass = snapshot.getValue(YogaClass.class);
                    if (yogaClass != null && yogaClass.getFirebaseKey() != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            // Check if the class already exists locally.
                            YogaClass existingClass = classRepository.getClassByFirebaseKey(yogaClass.getFirebaseKey());
                            if (existingClass != null) {
                                // If it exists, update it.
                                yogaClass.setId(existingClass.getId());
                                classRepository.updateFromSync(yogaClass);
                            } else {
                                // If not, insert it.
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

    /**
     * Stops the real-time synchronization by removing the Firebase listeners.
     */
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

    /**
     * Uploads all local data (courses and classes) to Firebase.
     * This can be used to overwrite Firebase data with local data.
     *
     * @param courses The list of courses to upload.
     * @param classes The list of classes to upload.
     */
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
