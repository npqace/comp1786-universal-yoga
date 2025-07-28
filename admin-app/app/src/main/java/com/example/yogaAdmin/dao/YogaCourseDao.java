package com.example.yogaAdmin.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.yogaAdmin.models.YogaCourse;

import java.util.List;

@Dao
public interface YogaCourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(YogaCourse yogaCourse);

    @Update
    void update(YogaCourse yogaCourse);

    @Delete
    void delete(YogaCourse yogaCourse);

    @Query("DELETE FROM yoga_courses")
    void deleteAllCourses();

    @Query("SELECT * FROM yoga_courses ORDER BY dayOfWeek, time ASC")
    LiveData<List<YogaCourse>> getAllCourses();

    @Query("SELECT * FROM yoga_courses")
    List<YogaCourse> getCourseList();

    @Query("SELECT * FROM yoga_courses WHERE id = :courseId")
    LiveData<YogaCourse> getCourseById(long courseId);

    @Query("SELECT * FROM yoga_courses WHERE firebaseKey = :firebaseKey")
    YogaCourse getCourseByFirebaseKey(String firebaseKey);
}
