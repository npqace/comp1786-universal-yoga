package com.example.yogaAdmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.utils.UIUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private static final int CREATE_COURSE_REQUEST = 100;
    private FloatingActionButton fabAddCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIUtils.configureStatusBar(this);
        setContentView(R.layout.activity_main);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        fabAddCourse = findViewById(R.id.fab_add_course);
    }

    private void setupClickListeners() {
        fabAddCourse.setOnClickListener(v -> createNewCourse());
    }

    private void createNewCourse() {
        Intent intent = new Intent(this, CreateCourseActivity.class);
        startActivityForResult(intent, CREATE_COURSE_REQUEST);
    }
}