package com.example.yogaAdmin.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "yoga_classes",
        foreignKeys = @ForeignKey(entity = YogaCourse.class,
                parentColumns = "id",
                childColumns = "courseId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("courseId")})
public class YogaClass implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String firebaseKey;
    private long courseId;

    // Instance specific
    private String date; // Specific date of the class
    private String assignedInstructor;
    private int actualCapacity;
    private int slotsAvailable;
    private String additionalComments;
    private String status; // e.g., "Scheduled", "Completed", "Cancelled"
    private long createdDate;


    // Default constructor for Room
    public YogaClass() {}

    @Ignore
    public YogaClass(long courseId, String date, String assignedInstructor, int actualCapacity, String additionalComments) {
        this.courseId = courseId;
        this.date = date;
        this.assignedInstructor = assignedInstructor;
        this.actualCapacity = actualCapacity;
        this.additionalComments = additionalComments;
        this.status = "Scheduled"; // Default status
        this.createdDate = System.currentTimeMillis();
    }

    // Getters and Setters
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


    // Helper methods
    public String getFormattedCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return "Created: " + sdf.format(new Date(createdDate));
    }
}

