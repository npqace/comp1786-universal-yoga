package com.example.yogaAdmin.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.yogaAdmin.models.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for handling booking data.
 * This class abstracts the data source for bookings, which is the Firebase Realtime Database.
 * It provides a clean API for the rest of the app to interact with booking data.
 */
public class BookingRepository {
    // Reference to the "bookings" node in the Firebase Realtime Database.
    private DatabaseReference databaseReference;

    /**
     * Constructor for the BookingRepository.
     * Initializes the Firebase Database reference.
     */
    public BookingRepository() {
        // Get a reference to the 'bookings' node in the Firebase database.
        databaseReference = FirebaseDatabase.getInstance().getReference("bookings");
    }

    /**
     * Retrieves all bookings for a specific class from Firebase.
     *
     * @param classId The Firebase key of the class for which to fetch bookings.
     * @return A {@link LiveData} object containing a list of {@link Booking}s.
     *         The LiveData will be updated in real-time as the data changes in Firebase.
     */
    public LiveData<List<Booking>> getBookingsForClass(String classId) {
        // Use MutableLiveData to hold the list of bookings.
        MutableLiveData<List<Booking>> bookingsLiveData = new MutableLiveData<>();
        
        // Create a query to fetch bookings where the 'classId' child matches the provided classId.
        databaseReference.orderByChild("classId").equalTo(classId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Booking> bookings = new ArrayList<>();
                // Iterate through the results of the query.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Deserialize the data into a Booking object.
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null) {
                        bookings.add(booking);
                    }
                }
                // Post the updated list of bookings to the LiveData object.
                bookingsLiveData.postValue(bookings);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occur during the data fetch.
                // Post null to the LiveData to indicate an error.
                bookingsLiveData.postValue(null);
            }
        });
        
        return bookingsLiveData;
    }
}
