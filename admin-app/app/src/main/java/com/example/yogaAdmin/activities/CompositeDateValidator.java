package com.example.yogaAdmin.activities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.material.datepicker.CalendarConstraints;

import java.util.List;

/**
 * A {@link CalendarConstraints.DateValidator} that combines multiple validators into one.
 * A date is considered valid only if it is deemed valid by all the composed validators.
 * This allows for creating complex validation rules, such as allowing only future Mondays.
 */
public class CompositeDateValidator implements CalendarConstraints.DateValidator {

    /**
     * A list holding the individual date validators.
     */
    private final List<CalendarConstraints.DateValidator> validators;

    /**
     * Constructs a {@code CompositeDateValidator}.
     *
     * @param validators A list of {@link CalendarConstraints.DateValidator} instances to be combined.
     */
    public CompositeDateValidator(List<CalendarConstraints.DateValidator> validators) {
        this.validators = validators;
    }

    /**
     * Checks if a given date is valid by iterating through all contained validators.
     *
     * @param date The date in milliseconds since the UTC epoch.
     * @return {@code true} if the date is valid according to all validators, {@code false} otherwise.
     */
    @Override
    public boolean isValid(long date) {
        // Iterate through all the validators in the list.
        for (CalendarConstraints.DateValidator validator : validators) {
            // If any validator considers the date invalid, the composite validator immediately returns false.
            if (!validator.isValid(date)) {
                return false;
            }
        }
        // If the loop completes, it means all validators considered the date valid.
        return true;
    }

    /**
     * Describes the kinds of special objects contained in this Parcelable instance's marshaled representation.
     *
     * @return a bitmask indicating the set of special object types marshaled by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        // This implementation does not have any special kinds of objects, so it returns 0.
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
        // Write the list of validators to the Parcel to be reconstructed later.
        dest.writeList(validators);
    }

    /**
     * A public static CREATOR field that generates instances of {@code CompositeDateValidator} from a Parcel.
     */
    public static final Parcelable.Creator<CompositeDateValidator> CREATOR = new Parcelable.Creator<CompositeDateValidator>() {
        /**
         * Creates a new instance of the {@code CompositeDateValidator} from a Parcel.
         *
         * @param source The Parcel to read the object's data from.
         * @return A new instance of the {@code CompositeDateValidator}.
         */
        @Override
        public CompositeDateValidator createFromParcel(Parcel source) {
            // Read the list of validators from the Parcel and create a new CompositeDateValidator.
            // The class loader is needed to correctly un-parcel the list of validators.
            return new CompositeDateValidator(source.readArrayList(CalendarConstraints.DateValidator.class.getClassLoader()));
        }

        /**
         * Creates a new array of the {@code CompositeDateValidator} class.
         *
         * @param size Size of the array to create.
         * @return An array of the {@code CompositeDateValidator} class, with every entry initialized to null.
         */
        @Override
        public CompositeDateValidator[] newArray(int size) {
            return new CompositeDateValidator[size];
        }
    };
}
