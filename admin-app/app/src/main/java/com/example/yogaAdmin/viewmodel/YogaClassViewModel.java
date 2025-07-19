package com.example.yogaAdmin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.repository.YogaClassRepository;
import com.example.yogaAdmin.repository.YogaCourseRepository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class YogaClassViewModel extends AndroidViewModel {

    private YogaClassRepository mRepository;
    private YogaCourseRepository mCourseRepository;
    private final LiveData<List<YogaClass>> mAllClasses;

    public YogaClassViewModel(Application application, long courseId) {
        super(application);
        mRepository = new YogaClassRepository(application);
        mCourseRepository = new YogaCourseRepository(application);
        mAllClasses = mRepository.getClassesForCourse(courseId);
    }

    public LiveData<List<YogaClass>> getAllClasses() {
        return mAllClasses;
    }

    public LiveData<YogaCourse> getCourseById(long courseId) {
        return mCourseRepository.getCourseById(courseId);
    }

    public LiveData<YogaClass> getYogaClassById(long classId) {
        return mRepository.getYogaClassById(classId);
    }

    public boolean classExists(long courseId, String date) throws ExecutionException, InterruptedException {
        Future<Integer> future = mRepository.classExists(courseId, date);
        return future.get() > 0;
    }

    public void insert(YogaClass yogaClass) {
        mRepository.insert(yogaClass);
    }

    public void update(YogaClass yogaClass) {
        mRepository.update(yogaClass);
    }

    public void delete(YogaClass yogaClass) {
        mRepository.delete(yogaClass);
    }

    public void deleteClassesByCourseId(long courseId) {
        mRepository.deleteClassesByCourseId(courseId);
    }

    public void deleteAllClasses() {
        mRepository.deleteAllClasses();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @NonNull
        private final Application mApplication;
        private final long mCourseId;

        public Factory(@NonNull Application application, long courseId) {
            mApplication = application;
            mCourseId = courseId;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new YogaClassViewModel(mApplication, mCourseId);
        }
    }
}
