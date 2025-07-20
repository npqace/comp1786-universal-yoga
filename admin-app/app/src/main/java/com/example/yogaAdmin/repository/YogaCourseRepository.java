package com.example.yogaAdmin.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.yogaAdmin.dao.YogaClassDao;
import com.example.yogaAdmin.dao.YogaCourseDao;
import com.example.yogaAdmin.database.AppDatabase;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class YogaCourseRepository {

    private YogaCourseDao mYogaCourseDao;
    private YogaClassDao mYogaClassDao;
    private LiveData<List<YogaCourse>> mAllCourses;
    private DatabaseReference firebaseDatabase;


    public YogaCourseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mYogaCourseDao = db.yogaCourseDao();
        mYogaClassDao = db.yogaClassDao();
        mAllCourses = mYogaCourseDao.getAllCourses();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public LiveData<List<YogaCourse>> getAllCourses() {
        return mAllCourses;
    }

    public LiveData<List<YogaClass>> getAllClasses() {
        return mYogaClassDao.getAllClasses();
    }

    public List<YogaCourse> getCourseList() {
        return mYogaCourseDao.getCourseList();
    }

    public List<YogaClass> getClassList() {
        return mYogaClassDao.getClassList();
    }

    public LiveData<YogaCourse> getCourseById(long courseId) {
        return mYogaCourseDao.getCourseById(courseId);
    }

    public void insert(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = mYogaCourseDao.insert(yogaCourse);
            yogaCourse.setId(id);

            String firebaseKey = firebaseDatabase.child("courses").push().getKey();
            yogaCourse.setFirebaseKey(firebaseKey);
            mYogaCourseDao.update(yogaCourse);

            firebaseDatabase.child("courses").child(firebaseKey).setValue(yogaCourse);
        });
    }

    public void update(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaCourseDao.update(yogaCourse);
            if (yogaCourse.getFirebaseKey() != null) {
                firebaseDatabase.child("courses").child(yogaCourse.getFirebaseKey()).setValue(yogaCourse);
            }
        });
    }

    public void delete(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // First, delete all associated classes from Firebase
            List<YogaClass> classesToDelete = mYogaClassDao.getClassesForCourseSync(yogaCourse.getId());
            for (YogaClass yogaClass : classesToDelete) {
                if (yogaClass.getFirebaseKey() != null) {
                    firebaseDatabase.child("classes").child(yogaClass.getFirebaseKey()).removeValue();
                }
            }
            // The local DB will cascade delete the classes automatically.
            // Now, delete the course from local and Firebase
            mYogaCourseDao.delete(yogaCourse);
            if (yogaCourse.getFirebaseKey() != null) {
                firebaseDatabase.child("courses").child(yogaCourse.getFirebaseKey()).removeValue();
            }
        });
    }

    public void deleteAllCourses() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaCourseDao.deleteAllCourses();
            firebaseDatabase.child("courses").removeValue();
            firebaseDatabase.child("classes").removeValue();
        });
    }
}
