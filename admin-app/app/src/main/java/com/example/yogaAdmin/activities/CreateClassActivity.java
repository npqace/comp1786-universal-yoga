package com.example.yogaAdmin.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.utils.NetworkStatusLiveData;
import com.example.yogaAdmin.viewmodel.YogaClassViewModel;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class CreateClassActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";
    public static final String EXTRA_COURSE_NAME = "EXTRA_COURSE_NAME";
    public static final String EXTRA_CLASS_ID = "EXTRA_CLASS_ID";

    private YogaClassViewModel yogaClassViewModel;
    private YogaCourse course;
    private YogaClass existingClass;

    private long courseId;
    private long classId = -1;

    private EditText editDate, editInstructor, editCapacity, editComments, editRepeatWeeks;
    private TextView tvCourseName, tvCourseDetails, tvDefaultCapacity;
    private Button btnClear, btnCreateClass;
    private ImageView ivErrorDate, ivErrorInstructor;

    private String selectedDate = null;
    private boolean isEditMode = false;
    private String firebaseKey = null;
    private NetworkStatusLiveData networkStatusLiveData;
    private TextView tvOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tvOffline = findViewById(R.id.tv_offline);
        networkStatusLiveData = new NetworkStatusLiveData(getApplicationContext());
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                tvOffline.setVisibility(View.GONE);
            } else {
                tvOffline.setVisibility(View.VISIBLE);
            }
        });

        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        classId = getIntent().getLongExtra(EXTRA_CLASS_ID, -1);
        isEditMode = classId != -1;

        if (courseId == -1) {
            Toast.makeText(this, "Error: Course not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupViewModel();
        setupClickListeners();
        setupFormValidation();
        setupDatePicker();
    }

    private void initializeViews() {
        editDate = findViewById(R.id.edit_date);
        editInstructor = findViewById(R.id.edit_instructor);
        editCapacity = findViewById(R.id.edit_capacity);
        editComments = findViewById(R.id.edit_comments);
        editRepeatWeeks = findViewById(R.id.edit_repeat_weeks);
        tvCourseName = findViewById(R.id.tv_course_name);
        tvCourseDetails = findViewById(R.id.tv_course_details);
        tvDefaultCapacity = findViewById(R.id.tv_default_capacity);
        btnClear = findViewById(R.id.btn_clear);
        btnCreateClass = findViewById(R.id.btn_create_class);
        ivErrorDate = findViewById(R.id.iv_error_date);
        ivErrorInstructor = findViewById(R.id.iv_error_instructor);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        if (isEditMode) {
            TextView tvTitle = findViewById(R.id.tv_title);
            tvTitle.setText("Edit Class");
            btnCreateClass.setText("Update Class");
            findViewById(R.id.repeat_weeks_container).setVisibility(View.GONE);
        }
    }

    private void setupViewModel() {
        YogaClassViewModel.Factory factory = new YogaClassViewModel.Factory(getApplication(), courseId);
        yogaClassViewModel = new ViewModelProvider(this, factory).get(YogaClassViewModel.class);

        yogaClassViewModel.getCourseById(courseId).observe(this, c -> {
            if (c != null) {
                course = c;
                populateCourseInfo();
            }
        });

        if (isEditMode) {
            yogaClassViewModel.getYogaClassById(classId).observe(this, yc -> {
                if (yc != null) {
                    existingClass = yc;
                    populateClassInfo();
                }
            });
        }
    }

    private void populateCourseInfo() {
        tvCourseName.setText(String.format(Locale.UK, "For: %s - %s at %s", course.getClassType(), course.getDayOfWeek(), course.getTime()));
        tvDefaultCapacity.setText(String.format("Default: %d", course.getCapacity()));

        StringBuilder details = new StringBuilder();
        details.append(String.format(Locale.UK, "📋 %s\n📅 %s at %s\n⏱️ %d minutes\n👥 %d people\n💷 £%.2f", course.getClassType(), course.getDayOfWeek(), course.getTime(), course.getDuration(), course.getCapacity(), course.getPrice()));

        if (course.getInstructorName() != null && !course.getInstructorName().trim().isEmpty()) {
            details.append(String.format("\n👨‍🏫 Default instructor: %s", course.getInstructorName()));
            if (!isEditMode) {
                editInstructor.setText(course.getInstructorName());
            }
        }

        if (course.getDescription() != null && !course.getDescription().trim().isEmpty()) {
            details.append(String.format("\n📝 %s", course.getDescription()));
        }

        tvCourseDetails.setText(details.toString());
    }

    private void populateClassInfo() {
        editDate.setText(existingClass.getDate());
        selectedDate = existingClass.getDate();
        editInstructor.setText(existingClass.getAssignedInstructor());
        editCapacity.setText(String.valueOf(existingClass.getActualCapacity()));
        editComments.setText(existingClass.getAdditionalComments());
        firebaseKey = existingClass.getFirebaseKey();
    }

    private void setupDatePicker() {
        editDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        if (course == null) {
            Toast.makeText(this, "Course details not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        long initialSelection = MaterialDatePicker.todayInUtcMilliseconds();
        if (selectedDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
                Date date = sdf.parse(selectedDate);
                if (date != null) {
                    initialSelection = date.getTime();
                }
            } catch (ParseException e) {
                // Ignore and use today's date
            }
        }

        int courseDayOfWeek = getCalendarDayOfWeek(course.getDayOfWeek());

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        // Set the validator to allow dates from today onwards in the local timezone.
        Calendar localToday = Calendar.getInstance(TimeZone.getDefault());
        localToday.set(Calendar.HOUR_OF_DAY, 0);
        localToday.set(Calendar.MINUTE, 0);
        localToday.set(Calendar.SECOND, 0);
        localToday.set(Calendar.MILLISECOND, 0);
        long todayUtc = localToday.getTimeInMillis();

        List<CalendarConstraints.DateValidator> validators = new ArrayList<>();
        validators.add(new DayOfWeekValidator(courseDayOfWeek));
        validators.add(DateValidatorPointForward.from(todayUtc));
        constraintsBuilder.setValidator(new CompositeDateValidator(validators));


        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(String.format("Select a %s", course.getDayOfWeek()))
                .setSelection(initialSelection)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // The selection is in UTC milliseconds. Convert it to the local time zone.
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            // The above calendar is in UTC. To format it correctly in the local timezone,
            // we create a new calendar instance with the default (local) timezone
            // and set its fields from the UTC calendar.
            Calendar localCalendar = Calendar.getInstance();
            localCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
            selectedDate = sdf.format(localCalendar.getTime());
            editDate.setText(selectedDate);
            hideFieldError(editDate, ivErrorDate);
        });

        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }


    private int getCalendarDayOfWeek(String dayOfWeek) {
        switch (dayOfWeek.toLowerCase()) {
            case "sunday": return Calendar.SUNDAY;
            case "monday": return Calendar.MONDAY;
            case "tuesday": return Calendar.TUESDAY;
            case "wednesday": return Calendar.WEDNESDAY;
            case "thursday": return Calendar.THURSDAY;
            case "friday": return Calendar.FRIDAY;
            case "saturday": return Calendar.SATURDAY;
            default: return -1; // Invalid day
        }
    }

    private void setupFormValidation() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (editDate.hasFocus()) hideFieldError(editDate, ivErrorDate);
                if (editInstructor.hasFocus()) hideFieldError(editInstructor, ivErrorInstructor);
            }
        };
        editDate.addTextChangedListener(textWatcher);
        editInstructor.addTextChangedListener(textWatcher);
    }

    private void setupClickListeners() {
        btnClear.setOnClickListener(v -> clearForm());
        btnCreateClass.setOnClickListener(v -> validateAndSaveClass());
    }

    private void clearForm() {
        editDate.setText("");
        editInstructor.setText(course != null && course.getInstructorName() != null ? course.getInstructorName() : "");
        editCapacity.setText("");
        editComments.setText("");
        editRepeatWeeks.setText("");
        selectedDate = null;
        hideFieldError(editDate, ivErrorDate);
        hideFieldError(editInstructor, ivErrorInstructor);
        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
    }

    private void validateAndSaveClass() {
        if (!validateForm()) return;

        String instructor = editInstructor.getText().toString().trim();
        String comments = editComments.getText().toString().trim();
        int customCapacity = -1;
        if (!editCapacity.getText().toString().trim().isEmpty()) {
            customCapacity = Integer.parseInt(editCapacity.getText().toString().trim());
        }

        if (isEditMode) {
            updateClass(instructor, comments, customCapacity);
        } else {
            int repeatWeeks = 1;
            if (!editRepeatWeeks.getText().toString().trim().isEmpty()) {
                repeatWeeks = Integer.parseInt(editRepeatWeeks.getText().toString().trim());
            }
            createClasses(selectedDate, instructor, customCapacity, comments, repeatWeeks);
        }
    }

    private boolean validateForm() {
        hideFieldError(editDate, ivErrorDate);
        hideFieldError(editInstructor, ivErrorInstructor);

        if (selectedDate == null || selectedDate.trim().isEmpty()) {
            showFieldError(editDate, ivErrorDate);
            Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (editInstructor.getText().toString().trim().isEmpty()) {
            showFieldError(editInstructor, ivErrorInstructor);
            Toast.makeText(this, "Please enter a instructor's name.", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            if (!isEditMode && yogaClassViewModel.classExists(courseId, selectedDate)) {
                Toast.makeText(this, String.format("A class already exists for this course on %s", selectedDate), Toast.LENGTH_LONG).show();
                showFieldError(editDate, ivErrorDate);
                return false;
            }
        } catch (ExecutionException | InterruptedException e) {
            Toast.makeText(this, "Error checking for existing classes.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateClass(String instructor, String comments, int capacity) {
        existingClass.setAssignedInstructor(instructor);
        existingClass.setAdditionalComments(comments);
        existingClass.setActualCapacity(capacity > 0 ? capacity : course.getCapacity());
        existingClass.setSlotsAvailable(existingClass.getActualCapacity());
        existingClass.setDate(selectedDate);
        existingClass.setFirebaseKey(firebaseKey);
        // The courseFirebaseKey should already be set, but we ensure it is.
        existingClass.setCourseFirebaseKey(course.getFirebaseKey());
        yogaClassViewModel.update(existingClass);
        Toast.makeText(this, "Class updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void createClasses(String startDate, String instructor, int customCapacity, String comments, int repeatWeeks) {
        for (int i = 0; i < repeatWeeks; i++) {
            String classDate = calculateDateForWeek(startDate, i);
            if (classDate != null) {
                YogaClass yogaClass = new YogaClass();
                yogaClass.setCourseId(courseId);
                yogaClass.setCourseFirebaseKey(course.getFirebaseKey());
                yogaClass.setDate(classDate);
                yogaClass.setAssignedInstructor(instructor);
                yogaClass.setStatus("Active");
                yogaClass.setActualCapacity(customCapacity > 0 ? customCapacity : course.getCapacity());
                yogaClass.setSlotsAvailable(yogaClass.getActualCapacity());
                yogaClass.setAdditionalComments(comments.isEmpty() ? null : comments);
                yogaClass.setCreatedDate(System.currentTimeMillis());
                yogaClassViewModel.insert(yogaClass);
            }
        }
        String message = repeatWeeks == 1 ? "Class created successfully!" : String.format("%d classes created successfully!", repeatWeeks);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private String calculateDateForWeek(String startDate, int weekOffset) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
            Date date = sdf.parse(startDate);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.WEEK_OF_YEAR, weekOffset);
                return sdf.format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showFieldError(EditText field, ImageView errorIcon) {
        field.setBackgroundResource(R.drawable.edit_text_error_background);
        errorIcon.setVisibility(View.VISIBLE);
    }

    private void hideFieldError(EditText field, ImageView errorIcon) {
        field.setBackgroundResource(R.drawable.enhanced_edit_text_background);
        errorIcon.setVisibility(View.GONE);
    }
}