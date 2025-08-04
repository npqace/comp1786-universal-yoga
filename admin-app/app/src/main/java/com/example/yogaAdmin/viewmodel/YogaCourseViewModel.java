package com.example.yogaAdmin.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.yogaAdmin.database.AppDatabase;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.repository.YogaCourseRepository;
import com.example.yogaAdmin.services.FirebaseSyncManager;

import java.util.List;

/**
 * ViewModel for managing the data of {@link YogaCourse} entities.
 * It serves as the bridge between the UI and the data layer (repository).
 * It provides data to the UI and survives configuration changes.
 */
public class YogaCourseViewModel extends AndroidViewModel {

    // The repository that handles data operations for courses.
    private YogaCourseRepository mRepository;
    // LiveData holding the list of all courses.
    private final LiveData<List<YogaCourse>> mAllCourses;
    // Manager for handling Firebase synchronization.
    private FirebaseSyncManager firebaseSyncManager;

    /**
     * Constructor for the YogaCourseViewModel.
     *
     * @param application The application context.
     */
    public YogaCourseViewModel(Application application) {
        super(application);
        mRepository = new YogaCourseRepository(application);
        mAllCourses = mRepository.getAllCourses();
        firebaseSyncManager = new FirebaseSyncManager(application);
    }

    /**
     * Returns the LiveData list of all courses.
     * The UI can observe this to get real-time updates.
     *
     * @return A {@link LiveData} list of {@link YogaCourse}.
     */
    public LiveData<List<YogaCourse>> getAllCourses() {
        return mAllCourses;
    }

    /**
     * Returns the LiveData list of all classes.
     *
     * @return A {@link LiveData} list of {@link YogaClass}.
     */
    public LiveData<List<YogaClass>> getAllClasses() {
        return mRepository.getAllClasses();
    }

    /**
     * Gets the details of a course by its ID.
     *
     * @param courseId The ID of the course.
     * @return A {@link LiveData} object of the {@link YogaCourse}.
     */
    public LiveData<YogaCourse> getCourseById(long courseId) {
        return mRepository.getCourseById(courseId);
    }

    /**
     * Inserts a new yoga course.
     *
     * @param yogaCourse The course to insert.
     */
    public void insert(YogaCourse yogaCourse) {
        mRepository.insert(yogaCourse);
    }

    /**
     * Updates an existing yoga course.
     *
     * @param yogaCourse The course to update.
     */
    public void update(YogaCourse yogaCourse) {
        mRepository.update(yogaCourse);
    }

    /**
     * Deletes a yoga course.
     *
     * @param yogaCourse The course to delete.
     */
    public void delete(YogaCourse yogaCourse) {
        mRepository.delete(yogaCourse);
    }

    /**
     * Deletes all yoga courses from the database.
     */
    public void deleteAllCourses() {
        mRepository.deleteAllCourses();
    }

    /**
     * Triggers a manual sync of all local data to Firebase.
     * It fetches all courses and classes from the local database and uploads them.
     */
    public void syncData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<YogaCourse> courses = mRepository.getCourseList();
            List<YogaClass> classes = mRepository.getClassList();
            firebaseSyncManager.uploadAllData(courses, classes);
        });
    }
}
