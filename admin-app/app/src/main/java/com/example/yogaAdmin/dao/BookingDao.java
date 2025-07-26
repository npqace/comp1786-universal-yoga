package com.example.yogaAdmin.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.yogaAdmin.models.Booking;
import java.util.List;

@Dao
public interface BookingDao {
    @Insert
    void insert(Booking booking);

    @Query("SELECT * FROM bookings WHERE userId = :userId")
    LiveData<List<Booking>> getBookingsForUser(String userId);

    @Query("SELECT * FROM bookings WHERE classId = :classId")
    LiveData<List<Booking>> getBookingsForClass(String classId);
}
