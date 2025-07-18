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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(YogaCourse yogaCourse);

    @Update
    void update(YogaCourse yogaCourse);

    @Delete
    void delete(YogaCourse yogaCourse);

    @Query("DELETE FROM yoga_courses")
    void deleteAllCourses();

    @Query("SELECT * FROM yoga_courses ORDER BY dayOfWeek, time ASC")
    LiveData<List<YogaCourse>> getAllCourses();
}