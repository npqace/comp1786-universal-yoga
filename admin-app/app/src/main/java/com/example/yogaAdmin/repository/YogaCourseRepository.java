package com.example.yogaAdmin.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.yogaAdmin.dao.YogaClassDao;
import com.example.yogaAdmin.dao.YogaCourseDao;
import com.example.yogaAdmin.database.AppDatabase;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

import com.google.firebase.database.Query;

/**
 * Repository for handling {@link YogaCourse} data.
 * This class abstracts the data sources (Room database and Firebase) for yoga courses,
 * providing a clean API for the ViewModels to interact with the data.
 */
public class YogaCourseRepository {

    // Data Access Objects for local database operations.
    private YogaCourseDao mYogaCourseDao;
    private YogaClassDao mYogaClassDao;
    // LiveData list of all courses, observed by the UI.
    private LiveData<List<YogaCourse>> mAllCourses;
    // Firebase database references.
    private DatabaseReference firebaseDatabase;
    private DatabaseReference coursesRef;

    /**
     * Constructor for the repository.
     *
     * @param application The application context, used to get the database instance.
     */
    public YogaCourseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mYogaCourseDao = db.yogaCourseDao();
        mYogaClassDao = db.yogaClassDao();
        mAllCourses = mYogaCourseDao.getAllCourses();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        coursesRef = firebaseDatabase.child("courses");
    }

    /**
     * Returns a LiveData list of all yoga courses.
     *
     * @return A {@link LiveData} list of {@link YogaCourse}.
     */
    public LiveData<List<YogaCourse>> getAllCourses() {
        return mAllCourses;
    }

    /**
     * Returns a LiveData list of all yoga classes.
     *
     * @return A {@link LiveData} list of {@link YogaClass}.
     */
    public LiveData<List<YogaClass>> getAllClasses() {
        return mYogaClassDao.getAllClasses();
    }

    /**
     * Returns a synchronous list of all yoga courses. Should be called on a background thread.
     *
     * @return A list of {@link YogaCourse}.
     */
    public List<YogaCourse> getCourseList() {
        return mYogaCourseDao.getCourseList();
    }

    /**
     * Returns a synchronous list of all yoga classes. Should be called on a background thread.
     *
     * @return A list of {@link YogaClass}.
     */
    public List<YogaClass> getClassList() {
        return mYogaClassDao.getClassList();
    }

    /**
     * Returns a LiveData object for a single course by its ID.
     *
     * @param courseId The ID of the course.
     * @return A {@link LiveData} object of the {@link YogaCourse}.
     */
    public LiveData<YogaCourse> getCourseById(long courseId) {
        return mYogaCourseDao.getCourseById(courseId);
    }

    /**
     * Returns a single course by its Firebase key synchronously.
     *
     * @param firebaseKey The Firebase key of the course.
     * @return The {@link YogaCourse} object.
     */
    public YogaCourse getCourseByFirebaseKey(String firebaseKey) {
        return mYogaCourseDao.getCourseByFirebaseKey(firebaseKey);
    }

    /**
     * Inserts a new yoga course into both the local Room database and Firebase.
     *
     * @param yogaCourse The yoga course to insert.
     */
    public void insert(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Insert into Room to get a local ID.
            long id = mYogaCourseDao.insert(yogaCourse);
            yogaCourse.setId(id);

            // Generate a new key in Firebase.
            String firebaseKey = firebaseDatabase.child("courses").push().getKey();
            yogaCourse.setFirebaseKey(firebaseKey);
            // Update the local entry with the Firebase key.
            mYogaCourseDao.update(yogaCourse);

            // Push the full object to Firebase.
            firebaseDatabase.child("courses").child(firebaseKey).setValue(yogaCourse);
        });
    }

    /**
     * Inserts a yoga course from a Firebase sync operation into the local database.
     *
     * @param yogaCourse The yoga course to insert.
     */
    public void insertFromSync(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> mYogaCourseDao.insert(yogaCourse));
    }

    /**
     * Updates a yoga course from a Firebase sync operation in the local database.
     *
     * @param yogaCourse The yoga course to update.
     */
    public void updateFromSync(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> mYogaCourseDao.update(yogaCourse));
    }

    /**
     * Updates an existing yoga course in both the local Room database and Firebase.
     *
     * @param yogaCourse The yoga course to update.
     */
    public void update(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaCourseDao.update(yogaCourse);
            if (yogaCourse.getFirebaseKey() != null) {
                firebaseDatabase.child("courses").child(yogaCourse.getFirebaseKey()).setValue(yogaCourse);
            }
        });
    }

    /**
     * Deletes a yoga course and all its associated classes and bookings from both Room and Firebase.
     *
     * @param yogaCourse The yoga course to delete.
     */
    public void delete(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Get all associated classes to delete them from Firebase.
            List<YogaClass> classesToDelete = mYogaClassDao.getClassesForCourseSync(yogaCourse.getId());
            Map<String, Object> childUpdates = new HashMap<>();

            // Prepare to delete the course from Firebase.
            childUpdates.put("/courses/" + yogaCourse.getFirebaseKey(), null);

            // Prepare to delete all associated classes from Firebase.
            for (YogaClass yogaClass : classesToDelete) {
                if (yogaClass.getFirebaseKey() != null) {
                    childUpdates.put("/classes/" + yogaClass.getFirebaseKey(), null);
                }
            }

            // Also delete associated bookings from Firebase for each class.
            for (YogaClass yogaClass : classesToDelete) {
                if (yogaClass.getFirebaseKey() != null) {
                    DatabaseReference bookingsRef = firebaseDatabase.child("bookings");
                    Query bookingQuery = bookingsRef.orderByChild("classId").equalTo(yogaClass.getFirebaseKey());
                    bookingQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot bookingSnapshot : dataSnapshot.getChildren()) {
                                bookingSnapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle potential errors during booking deletion.
                        }
                    });
                }
            }

            // Perform the atomic deletion from Firebase for courses and classes.
            firebaseDatabase.updateChildren(childUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Once Firebase deletion is complete, delete the course from Room.
                    // The associated classes are deleted automatically due to cascading delete.
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        mYogaCourseDao.delete(yogaCourse);
                    });
                }
            });
        });
    }

    /**
     * Deletes all courses, classes, and bookings from both the local database and Firebase.
     * This is a destructive operation.
     */
    public void deleteAllCourses() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Delete all courses from Room. Cascading delete will handle classes.
            mYogaCourseDao.deleteAllCourses();
            // Remove all data from the corresponding Firebase nodes.
            firebaseDatabase.child("courses").removeValue();
            firebaseDatabase.child("classes").removeValue();
            firebaseDatabase.child("bookings").removeValue();
        });
    }
}
