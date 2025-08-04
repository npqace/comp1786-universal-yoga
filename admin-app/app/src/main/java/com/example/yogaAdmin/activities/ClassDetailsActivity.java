package com.example.yogaAdmin.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.utils.NetworkStatusLiveData;

import com.example.yogaAdmin.adapter.BookingAdapter;
import com.example.yogaAdmin.viewmodel.BookingViewModel;
import com.example.yogaAdmin.viewmodel.ClassDetailsViewModel;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

/**
 * Activity that displays the details of a specific yoga class, including its course information
 * and any bookings made for that class.
 * This activity provides a comprehensive view of a single class instance.
 */
public class ClassDetailsActivity extends AppCompatActivity {

    /**
     * Constant for passing the class ID via an Intent extra.
     * This key is used to retrieve the ID of the class to be displayed.
     */
    public static final String EXTRA_CLASS_ID = "com.example.yogaAdmin.EXTRA_CLASS_ID";

    // UI elements for displaying class-specific details.
    private TextView tvClassDate, tvAssignedInstructor, tvCapacity, tvStatus, tvComments;
    // UI elements for displaying course-specific details.
    private TextView tvClassType, tvDayOfWeek, tvTime, tvDuration, tvPrice, tvDescription;
    // LiveData to observe network connectivity status and display an offline message.
    private NetworkStatusLiveData networkStatusLiveData;
    // TextView to display an "offline" message when the device has no internet connection.
    private TextView tvOffline;

    // UI elements for displaying the list of bookings.
    private RecyclerView recyclerViewBookings;
    // Adapter for the bookings RecyclerView.
    private BookingAdapter bookingAdapter;
    // ViewModel for managing booking data.
    private BookingViewModel bookingViewModel;
    // ViewModel for managing class details data.
    private ClassDetailsViewModel classDetailsViewModel;
    // TextView to display a message when there are no bookings for the class.
    private TextView tvNoBookings;

    /**
     * Called when the activity is first created.
     * This method initializes the UI, ViewModels, and observers for class details and bookings.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);

        // Initialize the offline TextView and observe network status.
        tvOffline = findViewById(R.id.tv_offline);
        networkStatusLiveData = new NetworkStatusLiveData(getApplicationContext());
        networkStatusLiveData.observe(this, isOnline -> {
            // Show or hide the offline message based on network connectivity.
            if (isOnline) {
                tvOffline.setVisibility(View.GONE); // Hide offline message if online.
            } else {
                tvOffline.setVisibility(View.VISIBLE); // Show offline message if offline.
            }
        });

        // Initialize all UI views by finding them by their ID.
        initViews();

        // Retrieve the class ID passed from the previous activity via an Intent extra.
        long classId = getIntent().getLongExtra(EXTRA_CLASS_ID, -1);

        // If a valid class ID is received, proceed to fetch and display class details.
        if (classId != -1) {
            // Create and initialize the ClassDetailsViewModel using a factory to pass the classId.
            ClassDetailsViewModel.Factory factory = new ClassDetailsViewModel.Factory(getApplication(), classId);
            classDetailsViewModel = new ViewModelProvider(this, factory).get(ClassDetailsViewModel.class);

            // Observe changes in the YogaClass data from the ViewModel.
            classDetailsViewModel.getYogaClass().observe(this, yogaClass -> {
                if (yogaClass != null) {
                    // Once YogaClass data is available, setup the BookingViewModel to fetch bookings.
                    setupBookingViewModel(yogaClass.getFirebaseKey());
                    // Observe changes in the associated YogaCourse data.
                    classDetailsViewModel.yogaCourse.observe(this, yogaCourse -> {
                        if (yogaCourse != null) {
                            // Once both YogaClass and YogaCourse data are available, populate the UI.
                            populateUI(new ClassWithCourseInfo(yogaClass, yogaCourse));
                        }
                    });
                }
            });
        }
    }

    /**
     * Initializes all the views used in this activity by finding them by their ID from the layout.
     */
    private void initViews() {
        // Initialize TextViews for Class Details
        tvClassDate = findViewById(R.id.tv_class_date);
        tvAssignedInstructor = findViewById(R.id.tv_assigned_instructor);
        tvCapacity = findViewById(R.id.tv_capacity);
        tvStatus = findViewById(R.id.tv_status);
        tvComments = findViewById(R.id.tv_comments);

        // Initialize TextViews for Course Details
        tvClassType = findViewById(R.id.tv_class_type);
        tvDayOfWeek = findViewById(R.id.tv_day_of_week);
        tvTime = findViewById(R.id.tv_time);
        tvDuration = findViewById(R.id.tv_duration);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);

        // Initialize RecyclerView and related views for Bookings
        recyclerViewBookings = findViewById(R.id.recycler_view_bookings);
        tvNoBookings = findViewById(R.id.tv_no_bookings);
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new BookingAdapter();
        recyclerViewBookings.setAdapter(bookingAdapter);

        // Initialize the back button and set its click listener to finish the activity.
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Sets up the BookingViewModel to fetch and display bookings for the given class ID.
     *
     * @param classId The Firebase key of the class for which to fetch bookings.
     */
    private void setupBookingViewModel(String classId) {
        // If the classId is invalid (null or empty), show "no bookings" message and hide the RecyclerView.
        if (classId == null || classId.isEmpty()) {
            tvNoBookings.setVisibility(View.VISIBLE);
            recyclerViewBookings.setVisibility(View.GONE);
            return;
        }

        // Create and initialize the BookingViewModel using a factory to pass the classId.
        BookingViewModel.Factory factory = new BookingViewModel.Factory(getApplication(), classId);
        bookingViewModel = new ViewModelProvider(this, factory).get(BookingViewModel.class);
        // Observe the list of bookings from the ViewModel.
        bookingViewModel.getBookings().observe(this, bookings -> {
            if (bookings != null && !bookings.isEmpty()) {
                // If bookings are available, update the adapter and show the RecyclerView.
                bookingAdapter.setBookings(bookings);
                tvNoBookings.setVisibility(View.GONE);
                recyclerViewBookings.setVisibility(View.VISIBLE);
            } else {
                // If there are no bookings, show the "no bookings" message and hide the RecyclerView.
                tvNoBookings.setVisibility(View.VISIBLE);
                recyclerViewBookings.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Populates the UI elements with data from a {@link ClassWithCourseInfo} object.
     *
     * @param classWithCourseInfo An object containing both {@link YogaClass} and {@link YogaCourse} details.
     */
    private void populateUI(ClassWithCourseInfo classWithCourseInfo) {
        // Extract YogaClass and YogaCourse from the combined object for easier access.
        YogaClass yogaClass = classWithCourseInfo.yogaClass;
        YogaCourse yogaCourse = classWithCourseInfo.yogaCourse;

        // Populate Class Details section of the UI.
        tvClassDate.setText(yogaClass.getDate());
        tvAssignedInstructor.setText(yogaClass.getAssignedInstructor());
        tvCapacity.setText(String.valueOf(yogaClass.getActualCapacity()));
        tvStatus.setText(yogaClass.getStatus());
        tvComments.setText(yogaClass.getAdditionalComments() != null ? yogaClass.getAdditionalComments() : "");

        // Populate Course Details section of the UI.
        tvClassType.setText(yogaCourse.getClassType());
        tvDayOfWeek.setText(yogaCourse.getDayOfWeek());
        tvTime.setText(yogaCourse.getTime());
        tvDuration.setText(yogaCourse.getFormattedDuration());
        tvPrice.setText(yogaCourse.getFormattedPrice());
        tvDescription.setText(yogaCourse.getDescription() != null ? yogaCourse.getDescription() : "");
    }
}
