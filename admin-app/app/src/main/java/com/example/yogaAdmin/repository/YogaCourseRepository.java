package com.example.yogaAdmin.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.yogaAdmin.dao.YogaCourseDao;
import com.example.yogaAdmin.database.AppDatabase;
import com.example.yogaAdmin.models.YogaCourse;

import java.util.List;

public class YogaCourseRepository {

    private YogaCourseDao mYogaCourseDao;
    private LiveData<List<YogaCourse>> mAllCourses;

    public YogaCourseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mYogaCourseDao = db.yogaCourseDao();
        mAllCourses = mYogaCourseDao.getAllCourses();
    }

    public LiveData<List<YogaCourse>> getAllCourses() {
        return mAllCourses;
    }

    public LiveData<YogaCourse> getCourseById(long courseId) {
        return mYogaCourseDao.getCourseById(courseId);
    }

    public void insert(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaCourseDao.insert(yogaCourse);
        });
    }

    public void update(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaCourseDao.update(yogaCourse);
        });
    }

    public void delete(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaCourseDao.delete(yogaCourse);
        });
    }

    public void deleteAllCourses() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaCourseDao.deleteAllCourses();
        });
    }
}
