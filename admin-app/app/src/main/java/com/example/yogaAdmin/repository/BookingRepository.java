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

public class BookingRepository {
    private DatabaseReference databaseReference;

    public BookingRepository() {
        // Assuming your bookings are stored at the root under a "bookings" node
        databaseReference = FirebaseDatabase.getInstance().getReference("bookings");
    }

    public LiveData<List<Booking>> getBookingsForClass(String classId) {
        MutableLiveData<List<Booking>> bookingsLiveData = new MutableLiveData<>();
        
        // Query bookings for a specific classId
        databaseReference.orderByChild("classId").equalTo(classId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Booking> bookings = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null) {
                        bookings.add(booking);
                    }
                }
                bookingsLiveData.postValue(bookings);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                bookingsLiveData.postValue(null); // Or handle error appropriately
            }
        });
        
        return bookingsLiveData;
    }
}
