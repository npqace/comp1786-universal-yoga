package com.example.yogaAdmin.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.repository.YogaClassRepository;
import java.util.List;

public class SearchViewModel extends AndroidViewModel {
    private final YogaClassRepository repository;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        repository = YogaClassRepository.getInstance(application);
    }

    public LiveData<List<ClassWithCourseInfo>> search(String instructorName, String date, String dayOfWeek) {
        return repository.search(instructorName, date, dayOfWeek);
    }

    public void update(YogaClass yogaClass) {
        repository.update(yogaClass);
    }
}
