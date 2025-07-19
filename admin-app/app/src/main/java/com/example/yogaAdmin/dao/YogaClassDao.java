package com.example.yogaAdmin.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.yogaAdmin.models.YogaClass;

import java.util.List;

@Dao
public interface YogaClassDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(YogaClass yogaClass);

    @Update
    void update(YogaClass yogaClass);

    @Delete
    void delete(YogaClass yogaClass);

    @Query("DELETE FROM yoga_classes")
    void deleteAllClasses();

    @Query("SELECT * FROM yoga_classes WHERE courseId = :courseId ORDER BY SUBSTR(date, 7, 4) ASC, SUBSTR(date, 4, 2) ASC, SUBSTR(date, 1, 2) ASC")
    LiveData<List<YogaClass>> getClassesForCourse(long courseId);

    @Query("SELECT * FROM yoga_classes WHERE id = :classId")
    LiveData<YogaClass> getYogaClassById(long classId);

    @Query("SELECT COUNT(*) FROM yoga_classes WHERE courseId = :courseId AND date = :date")
    int classExists(long courseId, String date);

    @Query("DELETE FROM yoga_classes WHERE courseId = :courseId")
    void deleteClassesByCourseId(long courseId);

    @Query("SELECT * FROM yoga_classes ORDER BY date DESC")
    LiveData<List<YogaClass>> getAllClassesWithCourseInfo();
}
