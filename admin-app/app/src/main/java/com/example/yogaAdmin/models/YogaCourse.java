package com.example.yogaAdmin.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.firebase.database.Exclude;

/**
 * Represents a single yoga course.
 * This class is used as a Room entity for local database storage and as a data model for Firebase.
 */
@Entity(tableName = "yoga_courses")
public class YogaCourse implements Serializable {
    /**
     * Unique identifier for the course in the local database.
     */
    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * The key for the course entry in the Firebase Realtime Database.
     */
    private String firebaseKey;

    // Required fields
    /**
     * The day of the week the course takes place (e.g., "Monday").
     */
    private String dayOfWeek;
    /**
     * The time the course starts (e.g., "10:00 AM").
     */
    private String time;
    /**
     * The maximum number of participants for the course.
     */
    private int capacity;
    /**
     * The duration of the course in minutes.
     */
    private int duration; // in minutes
    /**
     * The price of the course.
     */
    private double price;
    /**
     * The type of yoga class (e.g., "Hatha", "Vinyasa").
     */
    private String classType;

    // Optional fields
    /**
     * A detailed description of the course.
     */
    private String description;
    /**
     * The name of the instructor teaching the course.
     */
    private String instructorName;
    /**
     * The room number where the course is held.
     */
    private String roomNumber;
    /**
     * The difficulty level of the course (e.g., "Beginner", "Intermediate").
     */
    private String difficultyLevel;
    /**
     * Any equipment needed for the course.
     */
    private String equipmentNeeded;
    /**
     * The target age group for the course (e.g., "Adults", "Seniors").
     */
    private String ageGroup;
    /**
     * The timestamp when the course was created.
     */
    private long createdDate;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(YogaCourse.class)
     * and for Room.
     */
    public YogaCourse() {}

    /**
     * Constructs a new YogaCourse with essential details.
     * This constructor is ignored by Room because the ID is auto-generated.
     *
     * @param dayOfWeek The day of the week for the course.
     * @param time The start time of the course.
     * @param capacity The maximum number of participants.
     * @param duration The duration of the course in minutes.
     * @param price The price of the course.
     * @param classType The type of yoga class.
     */
    @Ignore
    public YogaCourse(String dayOfWeek, String time, int capacity, int duration,
                      double price, String classType) {
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.classType = classType;
        this.createdDate = System.currentTimeMillis(); // Set creation date to current time
    }

    // Getters and Setters for all fields.
    // These are necessary for Room and Firebase to access the fields.

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public String getEquipmentNeeded() { return equipmentNeeded; }
    public void setEquipmentNeeded(String equipmentNeeded) { this.equipmentNeeded = equipmentNeeded; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }

    /**
     * Returns the price formatted as a currency string (e.g., "£25.00").
     * This method is excluded from Firebase serialization.
     * @return A formatted string representing the price.
     */
    @Exclude
    public String getFormattedPrice() {
        return String.format(java.util.Locale.UK, "£%.2f", price);
    }

    /**
     * Returns the duration formatted as a string (e.g., "60 minutes").
     * This method is excluded from Firebase serialization.
     * @return A formatted string representing the duration.
     */
    @Exclude
    public String getFormattedDuration() {
        return duration + " minutes";
    }

    /**
     * Returns the creation date formatted as a string (e.g., "Created: 01/01/2023 14:30").
     * This method is excluded from Firebase serialization.
     * @return A formatted string representing the creation date.
     */
    @Exclude
    public String getFormattedCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return "Created: " + sdf.format(new Date(createdDate));
    }
}
