package com.example.yogaAdmin.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.repository.YogaClassRepository;
import java.util.List;

/**
 * ViewModel for the {@link com.example.yogaAdmin.activities.SearchActivity}.
 * It is responsible for handling the search logic by communicating with the
 * {@link YogaClassRepository} and providing the search results to the UI.
 */
public class SearchViewModel extends AndroidViewModel {
    // The repository that handles data operations for yoga classes.
    private final YogaClassRepository repository;

    /**
     * Constructor for the SearchViewModel.
     *
     * @param application The application context.
     */
    public SearchViewModel(@NonNull Application application) {
        super(application);
        // Get the singleton instance of the repository.
        repository = YogaClassRepository.getInstance(application);
    }

    /**
     * Executes a search for yoga classes based on the provided criteria.
     *
     * @param instructorName The name of the instructor to search for.
     * @param date The date of the class to search for.
     * @param dayOfWeek The day of the week to search for.
     * @return A {@link LiveData} list of {@link ClassWithCourseInfo} objects that match the search criteria.
     */
    public LiveData<List<ClassWithCourseInfo>> search(String instructorName, String date, String dayOfWeek) {
        return repository.search(instructorName, date, dayOfWeek);
    }

    /**
     * Updates a yoga class in the repository.
     * This is used, for example, when the status of a class is changed from the search results.
     *
     * @param yogaClass The {@link YogaClass} object to update.
     */
    public void update(YogaClass yogaClass) {
        repository.update(yogaClass);
    }
}
