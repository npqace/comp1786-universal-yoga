package com.example.yogaAdmin.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a specific instance of a yoga class scheduled on a particular date.
 * This entity is stored in the 'yoga_classes' table in the Room database and is
 * linked to a {@link YogaCourse} by a foreign key.
 */
@Entity(tableName = "yoga_classes",
        // Defines a foreign key relationship to the YogaCourse table.
        foreignKeys = @ForeignKey(entity = YogaCourse.class,
                parentColumns = "id",
                childColumns = "courseId",
                onDelete = ForeignKey.CASCADE), // Deleting a course will delete its classes.
        // Creates an index on the courseId column for faster queries.
        indices = {@Index("courseId")})
public class YogaClass implements Serializable {
    /**
     * The unique identifier for the class in the local Room database.
     * It is auto-generated.
     */
    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * The unique key for this class in the Firebase Realtime Database.
     */
    private String firebaseKey;

    /**
     * The Firebase key of the parent {@link YogaCourse}. Used for synchronization.
     */
    private String courseFirebaseKey;

    /**
     * The foreign key linking this class to its parent {@link YogaCourse}.
     */
    private long courseId;

    // Fields specific to this class instance.
    private String date; // The specific date of the class (e.g., "dd/MM/yyyy").
    private String assignedInstructor;
    private int actualCapacity;
    private int slotsAvailable;
    private String additionalComments;
    private String status; // e.g., "Active", "Completed", "Cancelled".
    private long createdDate; // Timestamp of when the class was created.

    /**
     * Default constructor required by Room and Firebase.
     */
    public YogaClass() {}

    /**
     * Constructor for creating a new YogaClass instance.
     * Marked with {@code @Ignore} so Room doesn't try to use it for object creation.
     *
     * @param courseId The ID of the parent course.
     * @param date The date of the class.
     * @param assignedInstructor The name of the instructor.
     * @param actualCapacity The capacity for this specific class instance.
     * @param additionalComments Any extra comments for the class.
     */
    @Ignore
    public YogaClass(long courseId, String date, String assignedInstructor, int actualCapacity, String additionalComments) {
        this.courseId = courseId;
        this.date = date;
        this.assignedInstructor = assignedInstructor;
        this.actualCapacity = actualCapacity;
        this.additionalComments = additionalComments;
        this.status = "Active"; // Default status for a new class.
        this.createdDate = System.currentTimeMillis();
    }

    // --- Getters and Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFirebaseKey() {
        return firebaseKey;
    }
    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public long getCourseId() { return courseId; }
    public void setCourseId(long courseId) { this.courseId = courseId; }

    public String getCourseFirebaseKey() {
        return courseFirebaseKey;
    }
    public void setCourseFirebaseKey(String courseFirebaseKey) {
        this.courseFirebaseKey = courseFirebaseKey;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getAssignedInstructor() { return assignedInstructor; }
    public void setAssignedInstructor(String assignedInstructor) { this.assignedInstructor = assignedInstructor; }

    public int getActualCapacity() { return actualCapacity; }
    public void setActualCapacity(int actualCapacity) { this.actualCapacity = actualCapacity; }

    public int getSlotsAvailable() { return slotsAvailable; }
    public void setSlotsAvailable(int slotsAvailable) { this.slotsAvailable = slotsAvailable; }

    public String getAdditionalComments() { return additionalComments; }
    public void setAdditionalComments(String additionalComments) { this.additionalComments = additionalComments; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }

    /**
     * Helper method to get the creation date as a formatted string.
     * Marked with {@code @Exclude} to prevent Firebase from trying to serialize it.
     *
     * @return The formatted creation date string (e.g., "dd/MM/yyyy HH:mm").
     */
    @Exclude
    public String getFormattedCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return "Created: " + sdf.format(new Date(createdDate));
    }
}

