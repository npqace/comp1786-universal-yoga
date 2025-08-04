package com.example.yogaAdmin.repository;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.example.yogaAdmin.dao.YogaClassDao;
import com.example.yogaAdmin.dao.YogaCourseDao;
import com.example.yogaAdmin.database.AppDatabase;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Repository for handling {@link YogaClass} data.
 * This class abstracts the data sources (Room database and Firebase) for yoga classes,
 * providing a clean API for the ViewModels to interact with the data.
 * It follows a singleton pattern to ensure a single instance throughout the app.
 */
public class YogaClassRepository {
    private static volatile YogaClassRepository INSTANCE;
    private final YogaClassDao yogaClassDao;
    private final YogaCourseDao yogaCourseDao;
    private final DatabaseReference firebaseDatabase;
    private final Map<Long, ValueEventListener> activeListeners = new HashMap<>();

    /**
     * Private constructor for the singleton pattern.
     *
     * @param application The application context.
     */
    private YogaClassRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        yogaClassDao = db.yogaClassDao();
        yogaCourseDao = db.yogaCourseDao();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @param application The application context.
     * @return The single instance of {@link YogaClassRepository}.
     */
    public static YogaClassRepository getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (YogaClassRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new YogaClassRepository(application);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Gets all classes for a specific course as {@link LiveData}.
     *
     * @param courseId The ID of the course.
     * @return A LiveData list of {@link YogaClass}.
     */
    public LiveData<List<YogaClass>> getClassesForCourse(long courseId) {
        return yogaClassDao.getClassesForCourse(courseId);
    }

    /**
     * Gets a specific course by its ID as {@link LiveData}.
     *
     * @param courseId The ID of the course.
     * @return A LiveData object of the {@link YogaCourse}.
     */
    public LiveData<YogaCourse> getCourseById(long courseId) {
        return yogaCourseDao.getCourseById(courseId);
    }

    /**
     * Gets a specific yoga class by its ID as {@link LiveData}.
     *
     * @param classId The ID of the class.
     * @return A LiveData object of the {@link YogaClass}.
     */
    public LiveData<YogaClass> getYogaClassById(long classId) {
        return yogaClassDao.getYogaClassById(classId);
    }

    /**
     * Gets a yoga class by its Firebase key synchronously.
     *
     * @param firebaseKey The Firebase key of the class.
     * @return The {@link YogaClass} object.
     */
    public YogaClass getClassByFirebaseKey(String firebaseKey) {
        return yogaClassDao.getClassByFirebaseKey(firebaseKey);
    }

    /**
     * Inserts a new yoga class into both the local Room database and Firebase.
     *
     * @param yogaClass The yoga class to insert.
     */
    public void insert(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Insert into Room to get a local ID.
            long id = yogaClassDao.insert(yogaClass);
            yogaClass.setId(id);
            // Generate a new key in Firebase.
            String firebaseKey = firebaseDatabase.child("classes").push().getKey();
            yogaClass.setFirebaseKey(firebaseKey);
            // Update the local entry with the Firebase key.
            yogaClassDao.update(yogaClass);
            // Push the full object to Firebase.
            firebaseDatabase.child("classes").child(firebaseKey).setValue(yogaClass);
        });
    }

    /**
     * Inserts a yoga class from a Firebase sync operation.
     * It ensures the associated course exists locally before inserting.
     *
     * @param yogaClass The yoga class to insert.
     */
    public void insertFromSync(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (yogaClass.getCourseFirebaseKey() != null) {
                YogaCourse course = yogaCourseDao.getCourseByFirebaseKey(yogaClass.getCourseFirebaseKey());
                if (course != null) {
                    // Set the local courseId and insert the class.
                    yogaClass.setCourseId(course.getId());
                    yogaClassDao.insert(yogaClass);
                }
            }
        });
    }

    /**
     * Updates a yoga class from a Firebase sync operation.
     *
     * @param yogaClass The yoga class to update.
     */
    public void updateFromSync(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> yogaClassDao.update(yogaClass));
    }

    /**
     * Updates an existing yoga class in both the local Room database and Firebase.
     *
     * @param yogaClass The yoga class to update.
     */
    public void update(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            yogaClassDao.update(yogaClass);
            if (yogaClass.getFirebaseKey() != null) {
                firebaseDatabase.child("classes").child(yogaClass.getFirebaseKey()).setValue(yogaClass);
            }
        });
    }

    /**
     * Deletes a yoga class from both the local Room database and Firebase.
     *
     * @param yogaClass The yoga class to delete.
     */
    public void delete(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            yogaClassDao.delete(yogaClass);
            if (yogaClass.getFirebaseKey() != null) {
                firebaseDatabase.child("classes").child(yogaClass.getFirebaseKey()).removeValue();
            }
        });
    }

    /**
     * Deletes all classes associated with a specific course ID from both Room and Firebase.
     *
     * @param courseId The ID of the course whose classes are to be deleted.
     */
    public void deleteClassesByCourseId(long courseId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Get all classes for the course to delete them from Firebase.
            List<YogaClass> classes = yogaClassDao.getClassesForCourseSync(courseId);
            Map<String, Object> updates = new HashMap<>();
            for(YogaClass yogaClass : classes) {
                if (yogaClass.getFirebaseKey() != null) {
                    updates.put("/classes/" + yogaClass.getFirebaseKey(), null);
                }
            }
            if (!updates.isEmpty()){
                firebaseDatabase.updateChildren(updates);
            }
            // Delete all classes for the course from Room.
            yogaClassDao.deleteClassesByCourseId(courseId);
        });
    }

    /**
     * Checks if a class already exists for a given course and date.
     *
     * @param courseId The ID of the course.
     * @param date The date of the class.
     * @return True if a class exists, false otherwise.
     * @throws ExecutionException If the computation threw an exception.
     * @throws InterruptedException If the current thread was interrupted while waiting.
     */
    public boolean classExists(long courseId, String date) throws ExecutionException, InterruptedException {
        Callable<Integer> callable = () -> yogaClassDao.classExists(courseId, date);
        Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(callable);
        return future.get() > 0;
    }

    /**
     * Performs a search for classes based on instructor name, date, and day of the week.
     *
     * @param instructorName The name of the instructor (can be partial).
     * @param date The specific date of the class.
     * @param dayOfWeek The day of the week.
     * @return A LiveData list of {@link ClassWithCourseInfo} matching the criteria.
     */
    public LiveData<List<ClassWithCourseInfo>> search(String instructorName, String date, String dayOfWeek) {
        // Prepare the query parameters, adding wildcards for LIKE queries.
        String instructorQuery = (instructorName == null || instructorName.isEmpty()) ? null : "%" + instructorName + "%";
        String dateQuery = (date == null || date.isEmpty()) ? null : date;
        String dayOfWeekQuery = (dayOfWeek == null || dayOfWeek.isEmpty() || dayOfWeek.equals("All Days")) ? null : "%" + dayOfWeek + "%";
        return yogaClassDao.search(instructorQuery, dateQuery, dayOfWeekQuery);
    }
}
