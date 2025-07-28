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

public class ClassDetailsViewModel extends AndroidViewModel {
    private final YogaClassRepository classRepository;
    private final YogaCourseRepository courseRepository;
    private final LiveData<YogaClass> yogaClass;
    public final LiveData<YogaCourse> yogaCourse;

    public ClassDetailsViewModel(@NonNull Application application, long classId) {
        super(application);
        classRepository = YogaClassRepository.getInstance(application);
        courseRepository = new YogaCourseRepository(application);

        // Get the LiveData for the specific class
        yogaClass = classRepository.getYogaClassById(classId);

        // When the class LiveData changes, get the corresponding course LiveData
        yogaCourse = Transformations.switchMap(yogaClass, yc -> {
            if (yc != null) {
                return courseRepository.getCourseById(yc.getCourseId());
            }
            return null;
        });
    }

    public LiveData<YogaClass> getYogaClass() {
        return yogaClass;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final long classId;

        public Factory(@NonNull Application application, long classId) {
            this.application = application;
            this.classId = classId;
        }

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
