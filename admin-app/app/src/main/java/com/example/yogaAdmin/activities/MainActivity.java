package com.example.yogaAdmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.adapter.YogaCourseAdapter;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.utils.NetworkStatusLiveData;
import com.example.yogaAdmin.viewmodel.YogaCourseViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * The main activity of the application, displaying a list of all yoga courses.
 * It allows users to view, search, add, edit, and delete courses.
 */
public class MainActivity extends AppCompatActivity {

    // Request codes for starting activities for a result.
    public static final int ADD_COURSE_REQUEST = 1;
    public static final int EDIT_COURSE_REQUEST = 2;
    // Key for passing YogaCourse data in Intents.
    public static final String EXTRA_COURSE = "com.example.yogaAdmin.EXTRA_COURSE";

    private YogaCourseViewModel yogaCourseViewModel;
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private EditText editSearch;
    private YogaCourseAdapter adapter;
    private List<YogaCourse> allCourses = new ArrayList<>(); // Cached list of all courses
    private NetworkStatusLiveData networkStatusLiveData;
    private TextView tvOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Observe network status and show an offline indicator if needed.
        tvOffline = findViewById(R.id.tv_offline);
        networkStatusLiveData = new NetworkStatusLiveData(getApplicationContext());
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                tvOffline.setVisibility(View.GONE);
            } else {
                tvOffline.setVisibility(View.VISIBLE);
            }
        });

        // Setup menu button with a popup menu.
        ImageView btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(this::showPopupMenu);

        // Setup advanced search button to navigate to SearchActivity.
        ImageView btnAdvancedSearch = findViewById(R.id.btn_advanced_search);
        btnAdvancedSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Setup Floating Action Button to open CreateCourseActivity for adding a new course.
        FloatingActionButton buttonAddCourse = findViewById(R.id.fab_add_course);
        buttonAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateCourseActivity.class);
            startActivityForResult(intent, ADD_COURSE_REQUEST);
        });

        // Initialize RecyclerView and its adapter.
        recyclerView = findViewById(R.id.recycler_view_courses);
        emptyStateLayout = findViewById(R.id.layout_empty_state);
        editSearch = findViewById(R.id.edit_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new YogaCourseAdapter();
        recyclerView.setAdapter(adapter);

        // Initialize ViewModel and observe the list of courses.
        yogaCourseViewModel = new ViewModelProvider(this).get(YogaCourseViewModel.class);
        yogaCourseViewModel.getAllCourses().observe(this, courses -> {
            allCourses.clear();
            if (courses != null) {
                allCourses.addAll(courses);
            }
            // Re-apply the current filter when the data changes.
            filterCourses(editSearch.getText().toString());
        });

        // Set up click listeners for the adapter items.
        adapter.setOnItemClickListener(new YogaCourseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(YogaCourse course) {
                // Navigate to the list of classes for the selected course.
                Intent intent = new Intent(MainActivity.this, YogaClassListActivity.class);
                intent.putExtra(YogaClassListActivity.EXTRA_COURSE_ID, course.getId());
                intent.putExtra(YogaClassListActivity.EXTRA_COURSE_NAME, course.getClassType());
                startActivity(intent);
            }

            @Override
            public void onEditClick(YogaCourse course) {
                // Open CreateCourseActivity in edit mode.
                Intent intent = new Intent(MainActivity.this, CreateCourseActivity.class);
                intent.putExtra(CreateCourseActivity.EXTRA_COURSE, course);
                startActivityForResult(intent, EDIT_COURSE_REQUEST);
            }

            @Override
            public void onDeleteClick(YogaCourse course) {
                // Show a confirmation dialog before deleting a course.
                showDeleteConfirmationDialog(course);
            }
        });

        // Add a TextWatcher to the search field to filter the list in real-time.
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Filters the displayed list of courses based on a search query.
     * The search is performed on the course type, instructor name, and day of the week.
     * @param query The text to search for.
     */
    private void filterCourses(String query) {
        List<YogaCourse> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            // If the query is empty, show all courses.
            filteredList.addAll(allCourses);
        } else {
            // Otherwise, filter the list.
            String lowerCaseQuery = query.toLowerCase().trim();
            for (YogaCourse course : allCourses) {
                if ((course.getClassType() != null && course.getClassType().toLowerCase().contains(lowerCaseQuery)) ||
                        (course.getInstructorName() != null && course.getInstructorName().toLowerCase().contains(lowerCaseQuery)) ||
                        (course.getDayOfWeek() != null && course.getDayOfWeek().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(course);
                }
            }
        }
        adapter.submitList(filteredList);
        updateEmptyState(filteredList.size());
    }

    /**
     * Shows or hides the "empty state" layout depending on whether the list is empty.
     * @param itemCount The number of items in the current list.
     */
    private void updateEmptyState(int itemCount) {
        if (itemCount == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }


    /**
     * Handles the result from activities started with startActivityForResult.
     * This is used to process the creation or update of a YogaCourse.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            YogaCourse course = (YogaCourse) data.getSerializableExtra(CreateCourseActivity.EXTRA_COURSE);
            if (course != null) {
                if (requestCode == ADD_COURSE_REQUEST) {
                    // If a new course was added, insert it into the database.
                    yogaCourseViewModel.insert(course);
                    Toast.makeText(this, "Course saved", Toast.LENGTH_SHORT).show();
                } else if (requestCode == EDIT_COURSE_REQUEST) {
                    // If a course was edited, update it in the database.
                    yogaCourseViewModel.update(course);
                    Toast.makeText(this, "Course updated", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // User cancelled the operation.
            Toast.makeText(this, "Course not saved", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays a popup menu with options for Sync, Reset, and About.
     * @param view The view to which the popup menu should be anchored.
     */
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_sync) {
                Toast.makeText(this, "Syncing data...", Toast.LENGTH_SHORT).show();
                yogaCourseViewModel.syncData(); // Trigger manual data sync.
                return true;
            } else if (itemId == R.id.action_reset_database) {
                showResetConfirmationDialog(); // Show confirmation for database reset.
                return true;
            } else if (itemId == R.id.action_about) {
                showAboutDialog(); // Show the about dialog.
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showDeleteConfirmationDialog(YogaCourse course) {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course? This will also delete all associated classes.")
                .setPositiveButton("Delete", (dialogInterface, which) -> {
                    yogaCourseViewModel.delete(course);
                    Toast.makeText(MainActivity.this, "Course deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
        // Set the delete button text color to red for emphasis.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(MainActivity.this, R.color.error_color));
    }

    /**
     * Shows a confirmation dialog before resetting the entire database.
     */
    private void showResetConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Reset Database")
                .setMessage("Are you sure you want to delete all courses? This action cannot be undone.")
                .setPositiveButton("Reset", (dialogInterface, which) -> {
                    yogaCourseViewModel.deleteAllCourses();
                    Toast.makeText(MainActivity.this, "All courses deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Set the reset button text color to red for emphasis.
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.error_color));
        }
    }

    /**
     * Shows a simple dialog with information about the application.
     */
    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About Universal Yoga")
                .setMessage("This is an admin application for managing yoga courses.\n\nVersion: 1.0")
                .setPositiveButton("OK", null)
                .create()
                .show();
    }
}
