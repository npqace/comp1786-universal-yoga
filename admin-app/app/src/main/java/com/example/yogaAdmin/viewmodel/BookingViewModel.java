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

/**
 * ViewModel for managing booking data for a specific yoga class.
 * It communicates with the {@link BookingRepository} to fetch booking information
 * and provides it to the UI as {@link LiveData}.
 */
public class BookingViewModel extends AndroidViewModel {
    // The repository that handles data operations for bookings.
    private BookingRepository repository;
    // LiveData holding the list of bookings for a specific class.
    private LiveData<List<Booking>> bookings;

    /**
     * Constructor for the BookingViewModel.
     *
     * @param application The application context.
     * @param classId The Firebase key of the class for which to fetch bookings.
     */
    public BookingViewModel(@NonNull Application application, String classId) {
        super(application);
        repository = new BookingRepository();
        // Fetch the bookings for the given class ID from the repository.
        bookings = repository.getBookingsForClass(classId);
    }

    /**
     * Returns the LiveData list of bookings.
     * The UI can observe this LiveData to get real-time updates.
     *
     * @return A {@link LiveData} object containing a list of {@link Booking}s.
     */
    public LiveData<List<Booking>> getBookings() {
        return bookings;
    }

    /**
     * A factory class for creating instances of {@link BookingViewModel} with parameters.
     * This is necessary because ViewModels with constructor parameters cannot be created
     * by the default ViewModelProvider.
     */
    public static class Factory implements ViewModelProvider.Factory {
        private final Application mApplication;
        private final String mClassId;

        /**
         * Constructor for the Factory.
         *
         * @param application The application context.
         * @param classId The class ID to be passed to the ViewModel.
         */
        public Factory(Application application, String classId) {
            mApplication = application;
            mClassId = classId;
        }

        /**
         * Creates a new instance of the given {@code Class}.
         *
         * @param modelClass A {@code Class} whose instance is requested.
         * @param <T> The type parameter for the ViewModel.
         * @return A newly created ViewModel.
         */
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            // Create and return a new BookingViewModel instance.
            return (T) new BookingViewModel(mApplication, mClassId);
        }
    }
}
