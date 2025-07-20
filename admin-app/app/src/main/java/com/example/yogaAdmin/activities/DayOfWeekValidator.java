package com.example.yogaAdmin.activities;

import android.os.Parcel;
import com.google.android.material.datepicker.CalendarConstraints;
import java.util.Calendar;
import java.util.TimeZone;

public class DayOfWeekValidator implements CalendarConstraints.DateValidator {

    private final int dayOfWeek;

    public DayOfWeekValidator(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    private DayOfWeekValidator(Parcel source) {
        dayOfWeek = source.readInt();
    }

    @Override
    public boolean isValid(long date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(dayOfWeek);
    }

    public static final Creator<DayOfWeekValidator> CREATOR = new Creator<DayOfWeekValidator>() {
        @Override
        public DayOfWeekValidator createFromParcel(Parcel source) {
            return new DayOfWeekValidator(source);
        }

        @Override
        public DayOfWeekValidator[] newArray(int size) {
            return new DayOfWeekValidator[size];
        }
    };
}
