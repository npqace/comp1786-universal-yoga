package com.example.yogaAdmin.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.repository.YogaCourseRepository;

import java.util.List;

public class YogaCourseViewModel extends AndroidViewModel {

    private YogaCourseRepository mRepository;
    private final LiveData<List<YogaCourse>> mAllCourses;

    public YogaCourseViewModel(Application application) {
        super(application);
        mRepository = new YogaCourseRepository(application);
        mAllCourses = mRepository.getAllCourses();
    }

    public LiveData<List<YogaCourse>> getAllCourses() {
        return mAllCourses;
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
}
