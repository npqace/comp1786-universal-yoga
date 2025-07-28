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

public class YogaClassViewModel extends AndroidViewModel {
    private final YogaClassRepository repository;
    private final LiveData<List<YogaClass>> allClasses;
    private final long courseId;

    public YogaClassViewModel(@NonNull Application application, long courseId) {
        super(application);
        this.courseId = courseId;
        repository = YogaClassRepository.getInstance(application);
        allClasses = repository.getClassesForCourse(courseId);
    }

    public LiveData<List<YogaClass>> getAllClasses() {
        return allClasses;
    }

    public LiveData<YogaCourse> getCourseById(long courseId) {
        return repository.getCourseById(courseId);
    }

    public LiveData<YogaClass> getYogaClassById(long classId) {
        return repository.getYogaClassById(classId);
    }

    public void insert(YogaClass yogaClass) {
        repository.insert(yogaClass);
    }

    public void update(YogaClass yogaClass) {
        repository.update(yogaClass);
    }

    public void delete(YogaClass yogaClass) {
        repository.delete(yogaClass);
    }

    public void deleteClassesByCourseId(long courseId) {
        repository.deleteClassesByCourseId(courseId);
    }

    public boolean classExists(long courseId, String date) throws ExecutionException, InterruptedException {
        return repository.classExists(courseId, date);
    }

    public LiveData<List<ClassWithCourseInfo>> search(String instructorName, String date, String dayOfWeek) {
        return repository.search(instructorName, date, dayOfWeek);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final Application application;
        private final long courseId;

        public Factory(Application application, long courseId) {
            this.application = application;
            this.courseId = courseId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new YogaClassViewModel(application, courseId);
        }
    }
}