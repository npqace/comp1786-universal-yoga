package com.example.yogaAdmin.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.yogaAdmin.dao.YogaClassDao;
import com.example.yogaAdmin.database.AppDatabase;
import com.example.yogaAdmin.models.YogaClass;

import java.util.List;
import java.util.concurrent.Future;

public class YogaClassRepository {

    private YogaClassDao mYogaClassDao;


    public YogaClassRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mYogaClassDao = db.yogaClassDao();
    }

    public LiveData<List<YogaClass>> getClassesForCourse(long courseId) {
        return mYogaClassDao.getClassesForCourse(courseId);
    }

    public LiveData<YogaClass> getYogaClassById(long classId) {
        return mYogaClassDao.getYogaClassById(classId);
    }

    public Future<Integer> classExists(long courseId, String date) {
        return AppDatabase.databaseWriteExecutor.submit(() -> {
            return mYogaClassDao.classExists(courseId, date);
        });
    }

    public void insert(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaClassDao.insert(yogaClass);
        });
    }

    public void update(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaClassDao.update(yogaClass);
        });
    }

    public void delete(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaClassDao.delete(yogaClass);
        });
    }

    public void deleteClassesByCourseId(long courseId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaClassDao.deleteClassesByCourseId(courseId);
        });
    }

    public void deleteAllClasses() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaClassDao.deleteAllClasses();
        });
    }

    public LiveData<List<YogaClass>> getAllClassesWithCourseInfo() {
        return mYogaClassDao.getAllClassesWithCourseInfo();
    }
}
