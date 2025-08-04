package com.example.yogaAdmin.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.repository.YogaClassRepository;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * ViewModel for managing the data of {@link YogaClass} entities.
 * It interacts with the {@link YogaClassRepository} to perform CRUD operations
 * and provides the data to the UI as {@link LiveData}.
 */
public class YogaClassViewModel extends AndroidViewModel {
    // The repository that handles data operations.
    private final YogaClassRepository repository;
    // LiveData holding the list of all classes for a specific course.
    private final LiveData<List<YogaClass>> allClasses;
    // The ID of the course whose classes are being managed.
    private final long courseId;

    /**
     * Constructor for the YogaClassViewModel.
     *
     * @param application The application context.
     * @param courseId The ID of the course for which to manage classes.
     */
    public YogaClassViewModel(@NonNull Application application, long courseId) {
        super(application);
        this.courseId = courseId;
        repository = YogaClassRepository.getInstance(application);
        // Fetch the list of classes for the given course ID.
        allClasses = repository.getClassesForCourse(courseId);
    }

    /**
     * Returns the LiveData list of all classes for the current course.
     *
     * @return A {@link LiveData} list of {@link YogaClass}.
     */
    public LiveData<List<YogaClass>> getAllClasses() {
        return allClasses;
    }

    /**
     * Gets the details of a course by its ID.
     *
     * @param courseId The ID of the course.
     * @return A {@link LiveData} object of the {@link YogaCourse}.
     */
    public LiveData<YogaCourse> getCourseById(long courseId) {
        return repository.getCourseById(courseId);
    }

    /**
     * Gets the details of a specific class by its ID.
     *
     * @param classId The ID of the class.
     * @return A {@link LiveData} object of the {@link YogaClass}.
     */
    public LiveData<YogaClass> getYogaClassById(long classId) {
        return repository.getYogaClassById(classId);
    }

    /**
     * Inserts a new yoga class.
     *
     * @param yogaClass The class to insert.
     */
    public void insert(YogaClass yogaClass) {
        repository.insert(yogaClass);
    }

    /**
     * Updates an existing yoga class.
     *
     * @param yogaClass The class to update.
     */
    public void update(YogaClass yogaClass) {
        repository.update(yogaClass);
    }

    /**
     * Deletes a yoga class.
     *
     * @param yogaClass The class to delete.
     */
    public void delete(YogaClass yogaClass) {
        repository.delete(yogaClass);
    }

    /**
     * Deletes all classes associated with a specific course ID.
     *
     * @param courseId The ID of the course.
     */
    public void deleteClassesByCourseId(long courseId) {
        repository.deleteClassesByCourseId(courseId);
    }

    /**
     * Checks if a class already exists for a given course and date.
     * This is a synchronous operation and should be called from a background thread.
     *
     * @param courseId The ID of the course.
     * @param date The date of the class.
     * @return {@code true} if the class exists, {@code false} otherwise.
     * @throws ExecutionException If the computation threw an exception.
     * @throws InterruptedException If the current thread was interrupted while waiting.
     */
    public boolean classExists(long courseId, String date) throws ExecutionException, InterruptedException {
        return repository.classExists(courseId, date);
    }

    /**
     * A factory class for creating instances of {@link YogaClassViewModel} with parameters.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final Application application;
        private final long courseId;

        /**
         * Constructor for the Factory.
         *
         * @param application The application context.
         * @param courseId The course ID to be passed to the ViewModel.
         */
        public Factory(Application application, long courseId) {
            this.application = application;
            this.courseId = courseId;
        }

        /**
         * Creates a new instance of the {@link YogaClassViewModel}.
         *
         * @param modelClass A {@code Class} whose instance is requested.
         * @param <T> The type parameter for the ViewModel.
         * @return A newly created ViewModel.
         */
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new YogaClassViewModel(application, courseId);
        }
    }
}
