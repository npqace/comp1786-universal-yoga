package com.example.yogaAdmin.activities;

import android.os.Parcel;
import com.google.android.material.datepicker.CalendarConstraints;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * A {@link CalendarConstraints.DateValidator} that validates whether a given date
 * falls on a specific day of the week (e.g., Monday, Tuesday).
 * This is used with the Material Date Picker to restrict date selection to only certain days.
 * It implements {@link android.os.Parcelable} to be correctly saved and restored with the DatePicker's state.
 */
public class DayOfWeekValidator implements CalendarConstraints.DateValidator {

    /**
     * The target day of the week to validate against, using {@link Calendar} constants
     * (e.g., {@code Calendar.MONDAY}, {@code Calendar.TUESDAY}).
     */
    private final int dayOfWeek;

    /**
     * Constructs a {@code DayOfWeekValidator}.
     *
     * @param dayOfWeek The day of the week to validate against, as a {@link Calendar} constant.
     */
    public DayOfWeekValidator(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    /**
     * Private constructor used for reconstructing the object from a {@link Parcel}.
     *
     * @param source The Parcel to read the day of the week from.
     */
    private DayOfWeekValidator(Parcel source) {
        // Read the integer representing the day of the week from the Parcel.
        dayOfWeek = source.readInt();
    }

    /**
     * Checks if the given date is valid, i.e., if it falls on the specified day of the week.
     *
     * @param date The date in milliseconds since the UTC epoch.
     * @return {@code true} if the date's day of the week matches the specified {@code dayOfWeek}, {@code false} otherwise.
     */
    @Override
    public boolean isValid(long date) {
        // Create a Calendar instance with UTC timezone to ensure consistency across devices.
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        // Set the calendar to the given date.
        calendar.setTimeInMillis(date);
        // Check if the day of the week of the given date matches the target dayOfWeek.
        return calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek;
    }

    /**
     * Describes the kinds of special objects contained in this Parcelable instance's marshaled representation.
     * For this class, it's always 0 as there are no special objects.
     *
     * @return A bitmask indicating the set of special object types.
     */
    @Override
    public int describeContents() {
        // No special objects to describe.
        return 0;
    }

    /**
     * Flattens this object into a Parcel.
     *
     * @param dest The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write the day of the week integer to the Parcel.
        dest.writeInt(dayOfWeek);
    }

    /**
     * A public static CREATOR field that generates instances of {@code DayOfWeekValidator} from a Parcel.
     * This is required for any class that implements {@link android.os.Parcelable}.
     */
    public static final Creator<DayOfWeekValidator> CREATOR = new Creator<DayOfWeekValidator>() {
        /**
         * Creates a new instance of the {@code DayOfWeekValidator} from the given Parcel.
         *
         * @param source The Parcel to read the object's data from.
         * @return A new instance of the {@code DayOfWeekValidator}.
         */
        @Override
        public DayOfWeekValidator createFromParcel(Parcel source) {
            return new DayOfWeekValidator(source);
        }

        /**
         * Creates a new array of the {@code DayOfWeekValidator} class.
         *
         * @param size Size of the array to create.
         * @return An array of the {@code DayOfWeekValidator} class, with every entry initialized to null.
         */
        @Override
        public DayOfWeekValidator[] newArray(int size) {
            return new DayOfWeekValidator[size];
        }
    };
}
