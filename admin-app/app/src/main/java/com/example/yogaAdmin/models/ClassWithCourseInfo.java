package com.example.yogaAdmin.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;

/**
 * A data class that combines a {@link YogaClass} with its corresponding {@link YogaCourse}.
 * This is used by Room to perform a relational query, embedding the parent YogaCourse
 * object within the YogaClass object. It implements {@link Serializable} to be passable
 * through intent extras.
 */
public class ClassWithCourseInfo implements Serializable {
    /**
     * The main {@link YogaClass} entity. The fields of this class will be treated
     * as if they are fields of the {@code ClassWithCourseInfo} object.
     */
    @Embedded
    public YogaClass yogaClass;

    /**
     * The related {@link YogaCourse} entity. Room will automatically query and populate
     * this object based on the relationship defined by the parent and entity columns.
     */
    @Relation(
            parentColumn = "courseId", // The foreign key in YogaClass
            entityColumn = "id"       // The primary key in YogaCourse
    )
    public YogaCourse yogaCourse;

    /**
     * Constructor to create an instance of {@code ClassWithCourseInfo}.
     *
     * @param yogaClass The yoga class.
     * @param yogaCourse The associated yoga course.
     */
    public ClassWithCourseInfo(YogaClass yogaClass, YogaCourse yogaCourse) {
        this.yogaClass = yogaClass;
        this.yogaCourse = yogaCourse;
    }
}
