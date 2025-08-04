package com.example.yogaAdmin.models;

/**
 * Represents a booking made by a user for a specific yoga class.
 * This class is a Plain Old Java Object (POJO) used for mapping data
 * from the Firebase Realtime Database.
 */
public class Booking {
    // Unique identifier for the booking.
    private String id;
    // ID of the user who made the booking.
    private String userId;
    // ID of the class that was booked.
    private String classId;
    // The date and time when the booking was made, typically in ISO 8601 format.
    private String bookingDate;

    // Denormalized data for easier display in the UI without needing additional queries.
    private String userName;
    private String userEmail;
    private String className;
    private String classDate;
    private String classTime;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(Booking.class).
     */
    public Booking() {
    }

    // Getters and Setters for all fields.

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassDate() {
        return classDate;
    }

    public void setClassDate(String classDate) {
        this.classDate = classDate;
    }

    public String getClassTime() {
        return classTime;
    }

    public void setClassTime(String classTime) {
        this.classTime = classTime;
    }
}
