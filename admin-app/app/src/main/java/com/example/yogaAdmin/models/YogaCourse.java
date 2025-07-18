package com.example.yogaAdmin.models;

public class YogaCourse {
    // Required fields
    private String dayOfWeek;
    private String time;
    private int capacity;
    private int duration; // in minutes
    private double price;
    private String classType;

    // Optional fields
    private String description;
    private String instructorName;
    private String roomNumber;
    private String difficultyLevel;
    private String equipmentNeeded;
    private String ageGroup;

    // Default constructor
    public YogaCourse() {}

    // Constructor with required fields
    public YogaCourse(String dayOfWeek, String time, int capacity, int duration,
                     double price, String classType) {
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.classType = classType;
    }

    // Getters and Setters
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



    // Get formatted price string
    public String getFormattedPrice() {
        // Use Locale.UK to ensure the currency symbol and decimal format are correct for GBP
        // This resolves potential locale-dependent formatting issues.
        return String.format(java.util.Locale.UK, "£%.2f", price);
    }

    // Get formatted duration string
    public String getFormattedDuration() {
        return duration + " minutes";
    }
}
