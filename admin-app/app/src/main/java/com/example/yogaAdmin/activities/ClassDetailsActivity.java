package com.example.yogaAdmin.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.utils.NetworkStatusLiveData;

public class ClassDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CLASS_INFO = "com.example.yogaAdmin.EXTRA_CLASS_INFO";

    private TextView tvClassDate, tvAssignedInstructor, tvCapacity, tvStatus, tvComments;
    private TextView tvClassType, tvDayOfWeek, tvTime, tvDuration, tvPrice, tvDescription;
    private NetworkStatusLiveData networkStatusLiveData;
    private TextView tvOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);

        tvOffline = findViewById(R.id.tv_offline);
        networkStatusLiveData = new NetworkStatusLiveData(getApplicationContext());
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                tvOffline.setVisibility(View.GONE);
            } else {
                tvOffline.setVisibility(View.VISIBLE);
            }
        });

        initViews();

        ClassWithCourseInfo classWithCourseInfo = (ClassWithCourseInfo) getIntent().getSerializableExtra(EXTRA_CLASS_INFO);

        if (classWithCourseInfo != null) {
            populateUI(classWithCourseInfo);
        }
    }

    private void initViews() {
        // Class Details
        tvClassDate = findViewById(R.id.tv_class_date);
        tvAssignedInstructor = findViewById(R.id.tv_assigned_instructor);
        tvCapacity = findViewById(R.id.tv_capacity);
        tvStatus = findViewById(R.id.tv_status);
        tvComments = findViewById(R.id.tv_comments);

        // Course Details
        tvClassType = findViewById(R.id.tv_class_type);
        tvDayOfWeek = findViewById(R.id.tv_day_of_week);
        tvTime = findViewById(R.id.tv_time);
        tvDuration = findViewById(R.id.tv_duration);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void populateUI(ClassWithCourseInfo classWithCourseInfo) {
        YogaClass yogaClass = classWithCourseInfo.yogaClass;
        YogaCourse yogaCourse = classWithCourseInfo.yogaCourse;

        // Populate Class Details
        tvClassDate.setText(yogaClass.getDate());
        tvAssignedInstructor.setText(yogaClass.getAssignedInstructor());
        tvCapacity.setText(String.valueOf(yogaClass.getActualCapacity()));
        tvStatus.setText(yogaClass.getStatus());
        tvComments.setText(yogaClass.getAdditionalComments() != null ? yogaClass.getAdditionalComments() : "");

        // Populate Course Details
        tvClassType.setText(yogaCourse.getClassType());
        tvDayOfWeek.setText(yogaCourse.getDayOfWeek());
        tvTime.setText(yogaCourse.getTime());
        tvDuration.setText(yogaCourse.getFormattedDuration());
        tvPrice.setText(yogaCourse.getFormattedPrice());
        tvDescription.setText(yogaCourse.getDescription() != null ? yogaCourse.getDescription() : "");
    }
}
