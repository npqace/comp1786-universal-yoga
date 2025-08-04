package com.example.yogaAdmin.repository;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.example.yogaAdmin.dao.YogaClassDao;
import com.example.yogaAdmin.dao.YogaCourseDao;
import com.example.yogaAdmin.database.AppDatabase;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class YogaClassRepository {
    private static volatile YogaClassRepository INSTANCE;
    private final YogaClassDao yogaClassDao;
    private final YogaCourseDao yogaCourseDao;
    private final DatabaseReference firebaseDatabase;
    private final Map<Long, ValueEventListener> activeListeners = new HashMap<>();

    private YogaClassRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        yogaClassDao = db.yogaClassDao();
        yogaCourseDao = db.yogaCourseDao();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static YogaClassRepository getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (YogaClassRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new YogaClassRepository(application);
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<YogaClass>> getClassesForCourse(long courseId) {
        return yogaClassDao.getClassesForCourse(courseId);
    }

    public LiveData<YogaCourse> getCourseById(long courseId) {
        return yogaCourseDao.getCourseById(courseId);
    }

    public LiveData<YogaClass> getYogaClassById(long classId) {
        return yogaClassDao.getYogaClassById(classId);
    }

    public YogaClass getClassByFirebaseKey(String firebaseKey) {
        return yogaClassDao.getClassByFirebaseKey(firebaseKey);
    }

    public void insert(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = yogaClassDao.insert(yogaClass);
            yogaClass.setId(id);
            String firebaseKey = firebaseDatabase.child("classes").push().getKey();
            yogaClass.setFirebaseKey(firebaseKey);
            yogaClassDao.update(yogaClass);
            firebaseDatabase.child("classes").child(firebaseKey).setValue(yogaClass);
        });
    }

    public void insertFromSync(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // First, check if the course exists using the courseFirebaseKey from the yogaClass
            if (yogaClass.getCourseFirebaseKey() != null) {
                YogaCourse course = yogaCourseDao.getCourseByFirebaseKey(yogaClass.getCourseFirebaseKey());
                if (course != null) {
                    // If the course exists, set the local courseId on the yogaClass and insert it
                    yogaClass.setCourseId(course.getId());
                    yogaClassDao.insert(yogaClass);
                }
            }
        });
    }

    public void updateFromSync(YogaClass yogaClass) {
        AppDatabase.databaseWriteExecutor.execute(() -> yogaClassDao.update(yogaClass));
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

    public LiveData<List<ClassWithCourseInfo>> search(String instructorName, String date, String dayOfWeek) {
        String instructorQuery = (instructorName == null || instructorName.isEmpty()) ? null : "%" + instructorName + "%";
        String dateQuery = (date == null || date.isEmpty()) ? null : date;
        String dayOfWeekQuery = (dayOfWeek == null || dayOfWeek.isEmpty() || dayOfWeek.equals("All Days")) ? null : "%" + dayOfWeek + "%";
        return yogaClassDao.search(instructorQuery, dateQuery, dayOfWeekQuery);
    }
}
