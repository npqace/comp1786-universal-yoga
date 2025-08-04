package com.example.yogaAdmin.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.adapter.YogaClassAdapter;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.utils.NetworkStatusLiveData;
import com.example.yogaAdmin.viewmodel.YogaClassViewModel;
import com.example.yogaAdmin.viewmodel.YogaCourseViewModel;

/**
 * Activity that displays a list of all scheduled classes for a specific yoga course.
 * It allows the admin to view, create, edit, and delete classes for the selected course.
 */
public class YogaClassListActivity extends AppCompatActivity {

    // Constants for passing data via Intent extras.
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";
    public static final String EXTRA_COURSE_NAME = "EXTRA_COURSE_NAME";
    public static final String EXTRA_CLASS_ID = "EXTRA_CLASS_ID";

    // UI elements
    private RecyclerView recyclerView;
    private YogaClassAdapter adapter;
    private LinearLayout tvNoClasses; // Layout shown when there are no classes
    private TextView tvTitle;

    // ViewModels for data management
    private YogaClassViewModel yogaClassViewModel;
    private YogaCourseViewModel yogaCourseViewModel;

    // Data variables
    private long courseId;
    private String courseName;
    private YogaCourse yogaCourse;

    // Network status monitoring
    private NetworkStatusLiveData networkStatusLiveData;
    private TextView tvOffline;

    /**
     * Called when the activity is first created.
     * Initializes views, ViewModels, and retrieves data passed from the previous activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_class_list);

        // Hide the default action bar.
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Setup network status observer.
        tvOffline = findViewById(R.id.tv_offline);
        networkStatusLiveData = new NetworkStatusLiveData(getApplicationContext());
        networkStatusLiveData.observe(this, isOnline -> {
            tvOffline.setVisibility(isOnline ? View.GONE : View.VISIBLE);
        });

        // Retrieve course ID and name from the intent.
        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        courseName = getIntent().getStringExtra(EXTRA_COURSE_NAME);

        // If the course ID is invalid, show an error and finish the activity.
        if (courseId == -1) {
            Toast.makeText(this, "Error: Course not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components and setup behavior.
        initViews();
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();
    }

    /**
     * Initializes all UI views from the layout file.
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_classes);
        tvNoClasses = findViewById(R.id.tv_no_classes);
        tvTitle = findViewById(R.id.tv_title);
        // Set the title of the activity to the course name.
        tvTitle.setText(courseName != null ? courseName : "Course Classes");
    }

    /**
     * Sets up the ViewModels and observes LiveData to update the UI.
     */
    private void setupViewModel() {
        // Factory for creating YogaClassViewModel with the courseId.
        YogaClassViewModel.Factory factory = new YogaClassViewModel.Factory(getApplication(), courseId);
        yogaClassViewModel = new ViewModelProvider(this, factory).get(YogaClassViewModel.class);

        // Observe the list of classes for the current course.
        yogaClassViewModel.getAllClasses().observe(this, yogaClasses -> {
            if (yogaClasses != null && !yogaClasses.isEmpty()) {
                adapter.submitList(yogaClasses);
                recyclerView.setVisibility(View.VISIBLE);
                tvNoClasses.setVisibility(View.GONE);
            } else {
                // Show the "no classes" message if the list is empty.
                recyclerView.setVisibility(View.GONE);
                tvNoClasses.setVisibility(View.VISIBLE);
            }
        });

        // Observe the details of the course itself to pass to the adapter.
        yogaCourseViewModel = new ViewModelProvider(this).get(YogaCourseViewModel.class);
        yogaCourseViewModel.getCourseById(courseId).observe(this, course -> {
            if (course != null) {
                this.yogaCourse = course;
                adapter.setYogaCourse(course);
            }
        });
    }

    /**
     * Sets up the RecyclerView, its adapter, and listeners for item interactions.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize the adapter with action listeners for edit, delete, and status updates.
        adapter = new YogaClassAdapter(new YogaClassAdapter.OnClassActionListener() {
            @Override
            public void onEditClass(YogaClass yogaClass) {
                editYogaClass(yogaClass);
            }

            @Override
            public void onDeleteClass(YogaClass yogaClass) {
                showDeleteConfirmDialog(yogaClass);
            }

            @Override
            public void onUpdateStatus(YogaClass yogaClass, String newStatus) {
                updateClassStatus(yogaClass, newStatus);
            }
        });
        recyclerView.setAdapter(adapter);

        // Set a listener for clicking on a whole item to view its details.
        adapter.setOnItemClickListener(new YogaClassAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(YogaClass yogaClass) {
                if (yogaCourse != null) {
                    Intent intent = new Intent(YogaClassListActivity.this, ClassDetailsActivity.class);
                    intent.putExtra(ClassDetailsActivity.EXTRA_CLASS_ID, yogaClass.getId());
                    startActivity(intent);
                } else {
                    Toast.makeText(YogaClassListActivity.this, "Course details not available yet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Sets up click listeners for all the buttons in the activity.
     */
    private void setupClickListeners() {
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        Button btnCreateClass = findViewById(R.id.btn_create_class);
        btnCreateClass.setOnClickListener(v -> createNewClass());

        Button btnCreateFirstClass = findViewById(R.id.btn_create_first_class);
        btnCreateFirstClass.setOnClickListener(v -> createNewClass());

        ImageView btnResetClasses = findViewById(R.id.btn_reset_classes);
        btnResetClasses.setOnClickListener(v -> showResetClassesDialog());

        Button btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Navigates to the {@link CreateClassActivity} to create a new class for the current course.
     */
    private void createNewClass() {
        Intent intent = new Intent(this, CreateClassActivity.class);
        intent.putExtra(CreateClassActivity.EXTRA_COURSE_ID, courseId);
        intent.putExtra(CreateClassActivity.EXTRA_COURSE_NAME, courseName);
        startActivity(intent);
    }

    /**
     * Navigates to the {@link CreateClassActivity} in edit mode for the selected class.
     *
     * @param yogaClass The class to be edited.
     */
    private void editYogaClass(YogaClass yogaClass) {
        Intent intent = new Intent(this, CreateClassActivity.class);
        intent.putExtra(EXTRA_CLASS_ID, yogaClass.getId());
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_COURSE_NAME, courseName);
        startActivity(intent);
    }

    /**
     * Shows a confirmation dialog before deleting a class.
     *
     * @param yogaClass The class to be deleted.
     */
    private void showDeleteConfirmDialog(YogaClass yogaClass) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Class?")
                .setMessage("Are you sure you want to delete this class on " + yogaClass.getDate() + "?")
                .setPositiveButton("Delete", (dialogInterface, which) -> {
                    yogaClassViewModel.delete(yogaClass);
                    Toast.makeText(this, "Class deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
        // Set the color of the positive button to indicate a destructive action.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.error_color));
    }

    /**
     * Updates the status of a class (e.g., "Active", "Completed", "Cancelled").
     *
     * @param yogaClass The class to update.
     * @param newStatus The new status string.
     */
    private void updateClassStatus(YogaClass yogaClass, String newStatus) {
        yogaClass.setStatus(newStatus);
        yogaClassViewModel.update(yogaClass);
        Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows a confirmation dialog before deleting all classes associated with the current course.
     */
    private void showResetClassesDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Reset All Classes?")
                .setMessage("Are you sure you want to delete ALL classes for this course? This action cannot be undone.")
                .setPositiveButton("Reset", (dialogInterface, which) -> {
                    yogaClassViewModel.deleteClassesByCourseId(courseId);
                    Toast.makeText(this, "All classes for this course have been deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
        // Set the color of the positive button to indicate a destructive action.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.error_color));
    }
}

