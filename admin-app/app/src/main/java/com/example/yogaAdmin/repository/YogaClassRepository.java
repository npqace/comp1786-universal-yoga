package com.example.yogaAdmin.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.yogaAdmin.dao.YogaClassDao;
import com.example.yogaAdmin.dao.YogaCourseDao;
import com.example.yogaAdmin.database.AppDatabase;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class YogaClassRepository {
    private YogaClassDao yogaClassDao;
    private YogaCourseDao yogaCourseDao;
    private LiveData<List<YogaClass>> allClasses;

    public YogaClassRepository(Application application, long courseId) {
        AppDatabase db = AppDatabase.getDatabase(application);
        yogaClassDao = db.yogaClassDao();
        yogaCourseDao = db.yogaCourseDao();
        if (courseId != -1) {
            allClasses = yogaClassDao.getClassesForCourse(courseId);
        }
    }
    public YogaClassRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        yogaClassDao = db.yogaClassDao();
        yogaCourseDao = db.yogaCourseDao();
    }

    public LiveData<List<YogaClass>> getAllClasses() {
        return allClasses;
    }

    public LiveData<YogaCourse> getCourseById(long courseId) {
        return yogaCourseDao.getCourseById(courseId);
    }

    public LiveData<YogaClass> getYogaClassById(long classId) {
        return yogaClassDao.getYogaClassById(classId);
    }

    public void insert(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            yogaClassDao.insert(yogaClass);
        });
    }

    public void update(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            yogaClassDao.update(yogaClass);
        });
    }

    public void delete(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            yogaClassDao.delete(yogaClass);
        });
    }

    public void deleteClassesByCourseId(long courseId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            yogaClassDao.deleteClassesByCourseId(courseId);
        });
    }

    public boolean classExists(long courseId, String date) throws ExecutionException, InterruptedException {
        return new ClassExistsAsyncTask(yogaClassDao).execute(courseId, date).get();
    }

    private static class ClassExistsAsyncTask extends AsyncTask<Object, Void, Boolean> {
        private YogaClassDao asyncTaskDao;

        ClassExistsAsyncTask(YogaClassDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            long courseId = (long) params[0];
            String date = (String) params[1];
            return asyncTaskDao.classExists(courseId, date) > 0;
        }
    }

    public LiveData<List<ClassWithCourseInfo>> searchByTeacher(String teacherName) {
        return yogaClassDao.searchByTeacher("%" + teacherName + "%");
    }

    public LiveData<List<ClassWithCourseInfo>> searchByDate(String date) {
        return yogaClassDao.searchByDate(date);
    }

    public LiveData<List<ClassWithCourseInfo>> searchByDayOfWeek(String dayOfWeek) {
        return yogaClassDao.searchByDayOfWeek(dayOfWeek);
    }

    public LiveData<List<ClassWithCourseInfo>> search(String teacherName, String date, String dayOfWeek) {
        String teacherQuery = (teacherName == null || teacherName.isEmpty()) ? null : "%" + teacherName + "%";
        String dateQuery = (date == null || date.isEmpty()) ? null : date;
        String dayOfWeekQuery = (dayOfWeek == null || dayOfWeek.isEmpty() || dayOfWeek.equals("All Days")) ? null : dayOfWeek;
        return yogaClassDao.search(teacherQuery, dateQuery, dayOfWeekQuery);
    }
}