package com.example.yogaAdmin.activities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.material.datepicker.CalendarConstraints;

import java.util.List;

public class CompositeDateValidator implements CalendarConstraints.DateValidator {

    private final List<CalendarConstraints.DateValidator> validators;

    public CompositeDateValidator(List<CalendarConstraints.DateValidator> validators) {
        this.validators = validators;
    }

    @Override
    public boolean isValid(long date) {
        for (CalendarConstraints.DateValidator validator : validators) {
            if (!validator.isValid(date)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(validators);
    }

    public static final Parcelable.Creator<CompositeDateValidator> CREATOR = new Parcelable.Creator<CompositeDateValidator>() {
        @Override
        public CompositeDateValidator createFromParcel(Parcel source) {
            return new CompositeDateValidator(source.readArrayList(CalendarConstraints.DateValidator.class.getClassLoader()));
        }

        @Override
        public CompositeDateValidator[] newArray(int size) {
            return new CompositeDateValidator[size];
        }
    };
}
