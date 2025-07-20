package com.example.yogaAdmin.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;

public class ClassWithCourseInfo implements Serializable {
    @Embedded
    public YogaClass yogaClass;

    @Relation(
            parentColumn = "courseId",
            entityColumn = "id"
    )
    public YogaCourse yogaCourse;

    public ClassWithCourseInfo(YogaClass yogaClass, YogaCourse yogaCourse) {
        this.yogaClass = yogaClass;
        this.yogaCourse = yogaCourse;
    }
}
