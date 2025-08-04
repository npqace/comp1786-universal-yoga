package com.example.yogaAdmin.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;

import java.util.List;

/**
 * Data Access Object (DAO) for the {@link YogaClass} entity.
 * This interface defines the database interactions for yoga classes,
 * including inserting, updating, deleting, and querying.
 */
@Dao
public interface YogaClassDao {

    /**
     * Inserts a yoga class into the database. If the class already exists, it replaces it.
     * @param yogaClass The yoga class to insert.
     * @return The row ID of the newly inserted class.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(YogaClass yogaClass);

    /**
     * Updates an existing yoga class in the database.
     * @param yogaClass The yoga class with updated information.
     */
    @Update
    void update(YogaClass yogaClass);

    /**
     * Deletes a specific yoga class from the database.
     * @param yogaClass The yoga class to delete.
     */
    @Delete
    void delete(YogaClass yogaClass);

    /**
     * Deletes all yoga classes from the database.
     */
    @Query("DELETE FROM yoga_classes")
    void deleteAllClasses();

    /**
     * Retrieves all yoga classes for a specific course, ordered by date.
     * The result is wrapped in {@link LiveData} to be observable.
     * @param courseId The ID of the course.
     * @return A LiveData list of yoga classes for the given course.
     */
    @Query("SELECT * FROM yoga_classes WHERE courseId = :courseId ORDER BY SUBSTR(date, 7, 4) ASC, SUBSTR(date, 4, 2) ASC, SUBSTR(date, 1, 2) ASC")
    LiveData<List<YogaClass>> getClassesForCourse(long courseId);

    /**
     * Retrieves all yoga classes for a specific course synchronously.
     * This should be called from a background thread.
     * @param courseId The ID of the course.
     * @return A list of yoga classes for the given course.
     */
    @Query("SELECT * FROM yoga_classes WHERE courseId = :courseId")
    List<YogaClass> getClassesForCourseSync(long courseId);

    /**
     * Retrieves a specific yoga class by its ID.
     * @param classId The ID of the class.
     * @return A LiveData object containing the yoga class.
     */
    @Query("SELECT * FROM yoga_classes WHERE id = :classId")
    LiveData<YogaClass> getYogaClassById(long classId);

    /**
     * Retrieves a yoga class by its Firebase key synchronously.
     * @param firebaseKey The Firebase key of the class.
     * @return The yoga class with the matching Firebase key.
     */
    @Query("SELECT * FROM yoga_classes WHERE firebaseKey = :firebaseKey")
    YogaClass getClassByFirebaseKey(String firebaseKey);

    /**
     * Checks if a class for a specific course and date already exists.
     * @param courseId The ID of the course.
     * @param date The date of the class.
     * @return The number of classes that match the criteria (0 or 1).
     */
    @Query("SELECT COUNT(*) FROM yoga_classes WHERE courseId = :courseId AND date = :date")
    int classExists(long courseId, String date);

    /**
     * Deletes all classes associated with a specific course ID.
     * @param courseId The ID of the course whose classes should be deleted.
     */
    @Query("DELETE FROM yoga_classes WHERE courseId = :courseId")
    void deleteClassesByCourseId(long courseId);

    /**
     * Retrieves all yoga classes from the database, ordered by date descending.
     * @return A LiveData list of all yoga classes.
     */
    @Query("SELECT * FROM yoga_classes ORDER BY date DESC")
    LiveData<List<YogaClass>> getAllClasses();

    /**
     * Retrieves all yoga classes as a synchronous list.
     * @return A list of all yoga classes.
     */
    @Query("SELECT * FROM yoga_classes")
    List<YogaClass> getClassList();

    /**
     * Retrieves all yoga classes with their associated course info.
     * @return A LiveData list of all yoga classes with course info.
     */
    @Query("SELECT * FROM yoga_classes ORDER BY date DESC")
    LiveData<List<YogaClass>> getAllClassesWithCourseInfo();

    /**
     * Searches for classes by instructor name.
     * @param instructorName The name of the instructor to search for.
     * @return A LiveData list of classes with course info matching the search criteria.
     */
    @Transaction
    @Query("SELECT * FROM yoga_classes WHERE assignedInstructor LIKE :instructorName")
    LiveData<List<ClassWithCourseInfo>> searchByInstructor(String instructorName);

    /**
     * Searches for classes by a specific date.
     * @param date The date to search for.
     * @return A LiveData list of classes with course info matching the search criteria.
     */
    @Transaction
    @Query("SELECT * FROM yoga_classes WHERE date = :date")
    LiveData<List<ClassWithCourseInfo>> searchByDate(String date);

    /**
     * Searches for classes by the day of the week.
     * @param dayOfWeek The day of the week to search for.
     * @return A LiveData list of classes with course info matching the search criteria.
     */
    @Transaction
    @Query("SELECT * FROM yoga_classes WHERE courseId IN (SELECT id FROM yoga_courses WHERE dayOfWeek LIKE :dayOfWeek)")
    LiveData<List<ClassWithCourseInfo>> searchByDayOfWeek(String dayOfWeek);

    /**
     * Performs a combined search for classes based on instructor name, date, and day of the week.
     * All parameters are optional.
     * @param instructorName The instructor name to search for (can be null).
     * @param date The date to search for (can be null).
     * @param dayOfWeek The day of the week to search for (can be null).
     * @return A LiveData list of classes with course info matching the search criteria.
     */
    @Transaction
    @Query("SELECT * FROM yoga_classes " +
            "WHERE (:instructorName IS NULL OR assignedInstructor LIKE :instructorName) " +
            "AND (:date IS NULL OR date = :date) " +
            "AND (:dayOfWeek IS NULL OR courseId IN (SELECT id FROM yoga_courses WHERE dayOfWeek LIKE :dayOfWeek))" +
            "ORDER BY SUBSTR(date, 7, 4) ASC, SUBSTR(date, 4, 2) ASC, SUBSTR(date, 1, 2) ASC")
    LiveData<List<ClassWithCourseInfo>> search(String instructorName, String date, String dayOfWeek);
}