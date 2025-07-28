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

public class MainActivity extends AppCompatActivity {

    public static final int ADD_COURSE_REQUEST = 1;
    public static final int EDIT_COURSE_REQUEST = 2;
    public static final String EXTRA_COURSE = "com.example.yogaAdmin.EXTRA_COURSE";

    private YogaCourseViewModel yogaCourseViewModel;
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private ImageView btnMenu;
    private EditText editSearch;
    private YogaCourseAdapter adapter;
    private List<YogaCourse> allCourses = new ArrayList<>();
    private NetworkStatusLiveData networkStatusLiveData;
    private TextView tvOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOffline = findViewById(R.id.tv_offline);
        networkStatusLiveData = new NetworkStatusLiveData(getApplicationContext());
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                tvOffline.setVisibility(View.GONE);
            } else {
                tvOffline.setVisibility(View.VISIBLE);
            }
        });

        btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(this::showPopupMenu);

        ImageView btnAdvancedSearch = findViewById(R.id.btn_advanced_search);
        btnAdvancedSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        FloatingActionButton buttonAddCourse = findViewById(R.id.fab_add_course);
        buttonAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateCourseActivity.class);
            startActivityForResult(intent, ADD_COURSE_REQUEST);
        });

        recyclerView = findViewById(R.id.recycler_view_courses);
        emptyStateLayout = findViewById(R.id.layout_empty_state);
        editSearch = findViewById(R.id.edit_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new YogaCourseAdapter();
        recyclerView.setAdapter(adapter);

        yogaCourseViewModel = new ViewModelProvider(this).get(YogaCourseViewModel.class);
        yogaCourseViewModel.getAllCourses().observe(this, courses -> {
            allCourses.clear();
            if (courses != null) {
                allCourses.addAll(courses);
            }
            filterCourses(editSearch.getText().toString());
        });

        adapter.setOnItemClickListener(new YogaCourseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(YogaCourse course) {
                Intent intent = new Intent(MainActivity.this, YogaClassListActivity.class);
                intent.putExtra(YogaClassListActivity.EXTRA_COURSE_ID, course.getId());
                intent.putExtra(YogaClassListActivity.EXTRA_COURSE_NAME, course.getClassType());
                startActivity(intent);
            }

            @Override
            public void onEditClick(YogaCourse course) {
                Intent intent = new Intent(MainActivity.this, CreateCourseActivity.class);
                intent.putExtra(CreateCourseActivity.EXTRA_COURSE, course);
                startActivityForResult(intent, EDIT_COURSE_REQUEST);
            }

            @Override
            public void onDeleteClick(YogaCourse course) {
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
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(MainActivity.this, R.color.error_color));
            }
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterCourses(String query) {
        List<YogaCourse> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(allCourses);
        } else {
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

    private void updateEmptyState(int itemCount) {
        if (itemCount == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            YogaCourse course = (YogaCourse) data.getSerializableExtra(CreateCourseActivity.EXTRA_COURSE);
            if (course != null) {
                if (requestCode == ADD_COURSE_REQUEST) {
                    yogaCourseViewModel.insert(course);
                    Toast.makeText(this, "Course saved", Toast.LENGTH_SHORT).show();
                } else if (requestCode == EDIT_COURSE_REQUEST) {
                    yogaCourseViewModel.update(course);
                    Toast.makeText(this, "Course updated", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // Optional: Handle the case where the user cancels the create/edit activity
            Toast.makeText(this, "Course not saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_reset_database) {
                showResetConfirmationDialog();
                return true;
            } else if (itemId == R.id.action_about) {
                showAboutDialog();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

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

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.error_color));
        }
    }

    private void showAboutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("About Universal Yoga")
                .setMessage("This is an admin application for managing yoga courses.\n\nVersion: 1.0")
                .setPositiveButton("OK", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
    }
}