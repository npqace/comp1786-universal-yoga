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

public class YogaCourseViewModel extends AndroidViewModel {

    private YogaCourseRepository mRepository;
    private final LiveData<List<YogaCourse>> mAllCourses;
    private FirebaseSyncManager firebaseSyncManager;

    public YogaCourseViewModel(Application application) {
        super(application);
        mRepository = new YogaCourseRepository(application);
        mAllCourses = mRepository.getAllCourses();
        firebaseSyncManager = new FirebaseSyncManager();
    }

    public LiveData<List<YogaCourse>> getAllCourses() {
        return mAllCourses;
    }

    public LiveData<List<YogaClass>> getAllClasses() {
        return mRepository.getAllClasses();
    }

    public LiveData<YogaCourse> getCourseById(long courseId) {
        return mRepository.getCourseById(courseId);
    }

    public void insert(YogaCourse yogaCourse) {
        mRepository.insert(yogaCourse);
    }

    public void update(YogaCourse yogaCourse) {
        mRepository.update(yogaCourse);
    }

    public void delete(YogaCourse yogaCourse) {
        mRepository.delete(yogaCourse);
    }

    public void deleteAllCourses() {
        mRepository.deleteAllCourses();
    }

    public void syncAllData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<YogaCourse> courses = mRepository.getCourseList();
            List<YogaClass> classes = mRepository.getClassList();
            firebaseSyncManager.uploadAllData(courses, classes);
        });
    }
}
