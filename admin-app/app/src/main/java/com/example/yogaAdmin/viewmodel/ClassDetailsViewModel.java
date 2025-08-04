package com.example.yogaAdmin.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.repository.YogaClassRepository;
import com.example.yogaAdmin.repository.YogaCourseRepository;

/**
 * ViewModel for the {@link com.example.yogaAdmin.activities.ClassDetailsActivity}.
 * It is responsible for fetching the details of a specific {@link YogaClass} and its
 * associated {@link YogaCourse} from the repositories and providing them to the UI.
 */
public class ClassDetailsViewModel extends AndroidViewModel {
    // Repositories for accessing class and course data.
    private final YogaClassRepository classRepository;
    private final YogaCourseRepository courseRepository;
    // LiveData object for the specific yoga class.
    private final LiveData<YogaClass> yogaClass;
    // LiveData object for the associated yoga course.
    public final LiveData<YogaCourse> yogaCourse;

    /**
     * Constructor for the ClassDetailsViewModel.
     *
     * @param application The application context.
     * @param classId The ID of the class to be displayed.
     */
    public ClassDetailsViewModel(@NonNull Application application, long classId) {
        super(application);
        classRepository = YogaClassRepository.getInstance(application);
        courseRepository = new YogaCourseRepository(application);

        // Fetch the LiveData for the specific class from the repository.
        yogaClass = classRepository.getYogaClassById(classId);

        // Use a Transformation to fetch the associated course whenever the yogaClass LiveData changes.
        // This creates a reactive chain: when the class data is loaded, the course data is then fetched.
        yogaCourse = Transformations.switchMap(yogaClass, yc -> {
            if (yc != null) {
                // If the class is loaded, return the LiveData for its course.
                return courseRepository.getCourseById(yc.getCourseId());
            }
            // If the class is null, return null for the course.
            return null;
        });
    }

    /**
     * Returns the LiveData object for the yoga class.
     * The UI can observe this to get updates on the class details.
     *
     * @return A {@link LiveData} object of the {@link YogaClass}.
     */
    public LiveData<YogaClass> getYogaClass() {
        return yogaClass;
    }

    /**
     * A factory class for creating instances of {@link ClassDetailsViewModel} with parameters.
     */
    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final long classId;

        /**
         * Constructor for the Factory.
         *
         * @param application The application context.
         * @param classId The class ID to be passed to the ViewModel.
         */
        public Factory(@NonNull Application application, long classId) {
            this.application = application;
            this.classId = classId;
        }

        /**
         * Creates a new instance of the {@link ClassDetailsViewModel}.
         *
         * @param modelClass A {@code Class} whose instance is requested.
         * @param <T> The type parameter for the ViewModel.
         * @return A newly created ViewModel.
         * @throws IllegalArgumentException if the modelClass is not assignable from ClassDetailsViewModel.
         */
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ClassDetailsViewModel.class)) {
                return (T) new ClassDetailsViewModel(application, classId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
