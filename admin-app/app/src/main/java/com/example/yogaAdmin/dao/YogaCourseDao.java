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

/**
 * Data Access Object (DAO) for the {@link YogaCourse} entity.
 * This interface defines the database interactions for yoga courses,
 * including inserting, updating, deleting, and querying.
 */
@Dao
public interface YogaCourseDao {

    /**
     * Inserts a yoga course into the database. If the course already exists, it replaces it.
     * @param yogaCourse The yoga course to insert.
     * @return The row ID of the newly inserted course.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(YogaCourse yogaCourse);

    /**
     * Updates an existing yoga course in the database.
     * @param yogaCourse The yoga course with updated information.
     */
    @Update
    void update(YogaCourse yogaCourse);

    /**
     * Deletes a specific yoga course from the database.
     * @param yogaCourse The yoga course to delete.
     */
    @Delete
    void delete(YogaCourse yogaCourse);

    /**
     * Deletes all yoga courses from the database.
     */
    @Query("DELETE FROM yoga_courses")
    void deleteAllCourses();

    /**
     * Retrieves all yoga courses from the database, ordered by day of the week and time.
     * The result is wrapped in {@link LiveData} to be observable.
     * @return A LiveData list of all yoga courses.
     */
    @Query("SELECT * FROM yoga_courses ORDER BY dayOfWeek, time ASC")
    LiveData<List<YogaCourse>> getAllCourses();

    /**
     * Retrieves all yoga courses as a synchronous list.
     * This should be called from a background thread.
     * @return A list of all yoga courses.
     */
    @Query("SELECT * FROM yoga_courses")
    List<YogaCourse> getCourseList();

    /**
     * Retrieves a specific yoga course by its ID.
     * @param courseId The ID of the course.
     * @return A LiveData object containing the yoga course.
     */
    @Query("SELECT * FROM yoga_courses WHERE id = :courseId")
    LiveData<YogaCourse> getCourseById(long courseId);

    /**
     * Retrieves a yoga course by its Firebase key synchronously.
     * @param firebaseKey The Firebase key of the course.
     * @return The yoga course with the matching Firebase key.
     */
    @Query("SELECT * FROM yoga_courses WHERE firebaseKey = :firebaseKey")
    YogaCourse getCourseByFirebaseKey(String firebaseKey);
}
