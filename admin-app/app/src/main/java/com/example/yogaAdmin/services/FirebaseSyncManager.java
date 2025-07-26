package com.example.yogaAdmin.services;

import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FirebaseSyncManager {

    private final DatabaseReference databaseReference;

    public FirebaseSyncManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void uploadAllData(List<YogaCourse> courses, List<YogaClass> classes) {
        DatabaseReference coursesRef = databaseReference.child("courses");
        coursesRef.removeValue(); // Clear all courses before syncing
        if (courses != null) {
            for (YogaCourse course : courses) {
                coursesRef.child(course.getFirebaseKey()).setValue(course);
            }
        }

        DatabaseReference classesRef = databaseReference.child("classes");
        classesRef.removeValue(); // Clear all classes before syncing
        if (classes != null) {
            for (YogaClass yogaClass : classes) {
                classesRef.child(yogaClass.getFirebaseKey()).setValue(yogaClass);
            }
        }
    }
}
