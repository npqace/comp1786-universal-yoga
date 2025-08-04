package com.example.yogaAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.models.Booking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying a list of {@link Booking} objects.
 * This adapter is used to show the details of users who have booked a specific class.
 */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    // List to hold the booking data.
    private List<Booking> bookings = new ArrayList<>();

    /**
     * Called when RecyclerView needs a new {@link BookingViewHolder} of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new BookingViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for a single booking.
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(itemView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link BookingViewHolder#itemView} to reflect the item at the given position.
     *
     * @param holder The BookingViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        // Get the booking at the current position.
        Booking currentBooking = bookings.get(position);
        // Bind the booking data to the views in the ViewHolder.
        holder.tvUserName.setText(currentBooking.getUserName());
        holder.tvUserEmail.setText(currentBooking.getUserEmail());
        
        try {
            // Attempt to parse and format the ISO date string to a more readable format.
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = isoFormat.parse(currentBooking.getBookingDate());
            SimpleDateFormat readableFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            holder.tvBookingDate.setText("Booked on: " + readableFormat.format(date));
        } catch (Exception e) {
            // If parsing fails, display the original date string as a fallback.
            holder.tvBookingDate.setText("Booked on: " + currentBooking.getBookingDate());
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return bookings.size();
    }

    /**
     * Updates the list of bookings and notifies the adapter to refresh the view.
     *
     * @param bookings The new list of bookings to display.
     */
    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged(); // Notify the adapter that the data set has changed.
    }

    /**
     * ViewHolder class for the booking item view.
     * Holds references to the UI components for a single item in the RecyclerView.
     */
    class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName;
        private TextView tvUserEmail;
        private TextView tvBookingDate;

        /**
         * Constructor for the BookingViewHolder.
         *
         * @param itemView The view of the single booking item.
         */
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the views within the item layout.
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvBookingDate = itemView.findViewById(R.id.tv_booking_date);
        }
    }
}
