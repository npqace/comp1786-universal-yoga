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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

import com.google.firebase.database.Query;

public class YogaCourseRepository {

    private YogaCourseDao mYogaCourseDao;
    private YogaClassDao mYogaClassDao;
    private LiveData<List<YogaCourse>> mAllCourses;
    private DatabaseReference firebaseDatabase;
    private DatabaseReference coursesRef;


    public YogaCourseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mYogaCourseDao = db.yogaCourseDao();
        mYogaClassDao = db.yogaClassDao();
        mAllCourses = mYogaCourseDao.getAllCourses();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        coursesRef = firebaseDatabase.child("courses");
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

    public YogaCourse getCourseByFirebaseKey(String firebaseKey) {
        return mYogaCourseDao.getCourseByFirebaseKey(firebaseKey);
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

    public void insertFromSync(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> mYogaCourseDao.insert(yogaCourse));
    }

    public void updateFromSync(YogaCourse yogaCourse) {
        AppDatabase.databaseWriteExecutor.execute(() -> mYogaCourseDao.update(yogaCourse));
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
            List<YogaClass> classesToDelete = mYogaClassDao.getClassesForCourseSync(yogaCourse.getId());
            Map<String, Object> childUpdates = new HashMap<>();

            // Add the course to the update map
            childUpdates.put("/courses/" + yogaCourse.getFirebaseKey(), null);

            // Add all associated classes to the update map
            for (YogaClass yogaClass : classesToDelete) {
                if (yogaClass.getFirebaseKey() != null) {
                    childUpdates.put("/classes/" + yogaClass.getFirebaseKey(), null);
                }
            }

            // Also delete associated bookings from Firebase
            for (YogaClass yogaClass : classesToDelete) {
                if (yogaClass.getFirebaseKey() != null) {
                    DatabaseReference bookingsRef = firebaseDatabase.child("bookings");
                    Query bookingQuery = bookingsRef.orderByChild("classId").equalTo(yogaClass.getFirebaseKey());
                    bookingQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot bookingSnapshot : dataSnapshot.getChildren()) {
                                bookingSnapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                }
            }

            // Perform the atomic deletion from Firebase
            firebaseDatabase.updateChildren(childUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Once the Firebase deletion is complete, delete from the local database
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        mYogaCourseDao.delete(yogaCourse);
                    });
                }
            });
        });
    }

    public void deleteAllCourses() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mYogaCourseDao.deleteAllCourses();
            firebaseDatabase.child("courses").removeValue();
            firebaseDatabase.child("classes").removeValue();
            firebaseDatabase.child("bookings").removeValue(); // Also clear all bookings
        });
    }
}
