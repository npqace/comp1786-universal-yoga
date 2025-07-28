package com.example.yogaAdmin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.yogaAdmin.models.Booking;
import com.example.yogaAdmin.repository.BookingRepository;

import java.util.List;

public class BookingViewModel extends AndroidViewModel {
    private BookingRepository repository;
    private LiveData<List<Booking>> bookings;

    public BookingViewModel(@NonNull Application application, String classId) {
        super(application);
        repository = new BookingRepository();
        bookings = repository.getBookingsForClass(classId);
    }

    public LiveData<List<Booking>> getBookings() {
        return bookings;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application mApplication;
        private final String mClassId;

        public Factory(Application application, String classId) {
            mApplication = application;
            mClassId = classId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new BookingViewModel(mApplication, mClassId);
        }
    }
}
