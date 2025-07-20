package com.example.yogaAdmin.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.yogaAdmin.dao.YogaClassDao;
import com.example.yogaAdmin.dao.YogaCourseDao;
import com.example.yogaAdmin.database.AppDatabase;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class YogaClassRepository {
    private YogaClassDao yogaClassDao;
    private YogaCourseDao yogaCourseDao;
    private LiveData<List<YogaClass>> allClasses;
    private DatabaseReference firebaseDatabase;

    public YogaClassRepository(Application application, long courseId) {
        AppDatabase db = AppDatabase.getDatabase(application);
        yogaClassDao = db.yogaClassDao();
        yogaCourseDao = db.yogaCourseDao();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        if (courseId != -1) {
            allClasses = yogaClassDao.getClassesForCourse(courseId);
        }
    }
    public YogaClassRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        yogaClassDao = db.yogaClassDao();
        yogaCourseDao = db.yogaCourseDao();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
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
            long id = yogaClassDao.insert(yogaClass);
            yogaClass.setId(id);

            String firebaseKey = firebaseDatabase.child("classes").push().getKey();
            yogaClass.setFirebaseKey(firebaseKey);
            yogaClassDao.update(yogaClass); // Update with the new key

            firebaseDatabase.child("classes").child(firebaseKey).setValue(yogaClass);
        });
    }

    public void update(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            yogaClassDao.update(yogaClass);
            if (yogaClass.getFirebaseKey() != null) {
                firebaseDatabase.child("classes").child(yogaClass.getFirebaseKey()).setValue(yogaClass);
            }
        });
    }

    public void delete(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            yogaClassDao.delete(yogaClass);
            if (yogaClass.getFirebaseKey() != null) {
                firebaseDatabase.child("classes").child(yogaClass.getFirebaseKey()).removeValue();
            }
        });
    }

    public void deleteClassesByCourseId(long courseId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<YogaClass> classes = yogaClassDao.getClassesForCourseSync(courseId);
            for(YogaClass yogaClass : classes) {
                if (yogaClass.getFirebaseKey() != null) {
                    firebaseDatabase.child("classes").child(yogaClass.getFirebaseKey()).removeValue();
                }
            }
            yogaClassDao.deleteClassesByCourseId(courseId);
        });
    }

    public boolean classExists(long courseId, String date) throws ExecutionException, InterruptedException {
        Callable<Integer> callable = () -> yogaClassDao.classExists(courseId, date);
        Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(callable);
        return future.get() > 0;
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
