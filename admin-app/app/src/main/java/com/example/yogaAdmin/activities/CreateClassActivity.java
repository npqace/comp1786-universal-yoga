package com.example.yogaAdmin.activities;

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

/**
 * Activity for creating a new yoga class or editing an existing one.
 * It operates in two modes: 'create' and 'edit'.
 * In 'create' mode, it can also create multiple classes over a number of weeks.
 */
public class CreateClassActivity extends AppCompatActivity {

    // Keys for passing data via Intent extras.
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";
    public static final String EXTRA_COURSE_NAME = "EXTRA_COURSE_NAME";
    public static final String EXTRA_CLASS_ID = "EXTRA_CLASS_ID";

    private YogaClassViewModel yogaClassViewModel;
    private YogaCourse course; // The parent course for which the class is being created.
    private YogaClass existingClass; // The class being edited, if in edit mode.

    private long courseId;
    private long classId = -1;

    // UI Components
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

        // Hide the default action bar.
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Observe network status and show an offline indicator if needed.
        tvOffline = findViewById(R.id.tv_offline);
        networkStatusLiveData = new NetworkStatusLiveData(getApplicationContext());
        networkStatusLiveData.observe(this, isOnline -> {
            tvOffline.setVisibility(isOnline ? View.GONE : View.VISIBLE);
        });

        // Retrieve IDs from the intent to determine mode (create/edit) and context.
        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        classId = getIntent().getLongExtra(EXTRA_CLASS_ID, -1);
        isEditMode = classId != -1;

        // A course ID is mandatory.
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

    /**
     * Initializes all UI components and sets up the back button.
     * Adjusts UI elements based on whether the activity is in edit mode.
     */
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

        // If in edit mode, change the title, button text, and hide the repeat weeks field.
        if (isEditMode) {
            TextView tvTitle = findViewById(R.id.tv_title);
            tvTitle.setText("Edit Class");
            btnCreateClass.setText("Update Class");
            findViewById(R.id.repeat_weeks_container).setVisibility(View.GONE);
        }
    }

    /**
     * Sets up the ViewModel and observes LiveData for the course and class details.
     */
    private void setupViewModel() {
        YogaClassViewModel.Factory factory = new YogaClassViewModel.Factory(getApplication(), courseId);
        yogaClassViewModel = new ViewModelProvider(this, factory).get(YogaClassViewModel.class);

        // Observe the parent course details.
        yogaClassViewModel.getCourseById(courseId).observe(this, c -> {
            if (c != null) {
                course = c;
                populateCourseInfo();
            }
        });

        // If in edit mode, observe the specific class to be edited.
        if (isEditMode) {
            yogaClassViewModel.getYogaClassById(classId).observe(this, yc -> {
                if (yc != null) {
                    existingClass = yc;
                    populateClassInfo();
                }
            });
        }
    }

    /**
     * Populates the UI with details from the parent YogaCourse.
     */
    private void populateCourseInfo() {
        tvCourseName.setText(String.format(Locale.UK, "For: %s - %s at %s", course.getClassType(), course.getDayOfWeek(), course.getTime()));
        tvDefaultCapacity.setText(String.format("Default: %d", course.getCapacity()));

        // Build a detailed description string.
        StringBuilder details = new StringBuilder();
        details.append(String.format(Locale.UK, "📋 %s\n📅 %s at %s\n⏱️ %d minutes\n👥 %d people\n💷 £%.2f", course.getClassType(), course.getDayOfWeek(), course.getTime(), course.getDuration(), course.getCapacity(), course.getPrice()));

        // Pre-fill instructor name if available in the course.
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

    /**
     * Populates the form fields with data from an existing YogaClass when in edit mode.
     */
    private void populateClassInfo() {
        editDate.setText(existingClass.getDate());
        selectedDate = existingClass.getDate();
        editInstructor.setText(existingClass.getAssignedInstructor());
        editCapacity.setText(String.valueOf(existingClass.getActualCapacity()));
        editComments.setText(existingClass.getAdditionalComments());
        firebaseKey = existingClass.getFirebaseKey();
    }

    /**
     * Sets up the click listener for the date EditText to show the date picker.
     */
    private void setupDatePicker() {
        editDate.setOnClickListener(v -> showDatePicker());
    }

    /**
     * Creates and shows a MaterialDatePicker, constrained to future dates on the correct day of the week.
     */
    private void showDatePicker() {
        if (course == null) {
            Toast.makeText(this, "Course details not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set the initial date for the picker (today or the already selected date).
        long initialSelection = MaterialDatePicker.todayInUtcMilliseconds();
        if (selectedDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
                Date date = sdf.parse(selectedDate);
                if (date != null) {
                    initialSelection = date.getTime();
                }
            } catch (ParseException e) {
                // Ignore and use today's date if parsing fails.
            }
        }

        int courseDayOfWeek = getCalendarDayOfWeek(course.getDayOfWeek());

        // Set up constraints for the date picker.
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        long todayUtc = MaterialDatePicker.todayInUtcMilliseconds();

        // The validator must allow dates that are on the correct day of the week AND are in the future.
        List<CalendarConstraints.DateValidator> validators = new ArrayList<>();
        validators.add(new DayOfWeekValidator(courseDayOfWeek));
        validators.add(DateValidatorPointForward.from(todayUtc));
        constraintsBuilder.setValidator(new CompositeDateValidator(validators));

        // Build and show the date picker.
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(String.format("Select a %s", course.getDayOfWeek()))
                .setSelection(initialSelection)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Convert the selected UTC timestamp to a local date string.
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            Calendar localCalendar = Calendar.getInstance();
            localCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
            selectedDate = sdf.format(localCalendar.getTime());
            editDate.setText(selectedDate);
            hideFieldError(editDate, ivErrorDate); // Clear any previous error state.
        });

        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }


    /**
     * Converts a day of the week string (e.g., "Monday") to a Calendar day constant (e.g., Calendar.MONDAY).
     * @param dayOfWeek The string representation of the day.
     * @return The corresponding Calendar constant, or -1 if invalid.
     */
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

    /**
     * Sets up TextWatchers to hide field errors as the user types.
     */
    private void setupFormValidation() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // When the user starts typing in a field, hide its error indicator.
                if (editDate.hasFocus()) hideFieldError(editDate, ivErrorDate);
                if (editInstructor.hasFocus()) hideFieldError(editInstructor, ivErrorInstructor);
            }
        };
        editDate.addTextChangedListener(textWatcher);
        editInstructor.addTextChangedListener(textWatcher);
    }

    /**
     * Sets up click listeners for the clear and create/update buttons.
     */
    private void setupClickListeners() {
        btnClear.setOnClickListener(v -> clearForm());
        btnCreateClass.setOnClickListener(v -> validateAndSaveClass());
    }

    /**
     * Clears all input fields in the form.
     */
    private void clearForm() {
        editDate.setText("");
        // Reset instructor to the course default if available.
        editInstructor.setText(course != null && course.getInstructorName() != null ? course.getInstructorName() : "");
        editCapacity.setText("");
        editComments.setText("");
        editRepeatWeeks.setText("");
        selectedDate = null;
        // Hide any visible error indicators.
        hideFieldError(editDate, ivErrorDate);
        hideFieldError(editInstructor, ivErrorInstructor);
        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Validates the form input and then proceeds to save or update the class.
     */
    private void validateAndSaveClass() {
        if (!validateForm()) return; // Stop if validation fails.

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

    /**
     * Performs validation checks on the form fields.
     * @return True if the form is valid, false otherwise.
     */
    private boolean validateForm() {
        // Reset previous error states.
        hideFieldError(editDate, ivErrorDate);
        hideFieldError(editInstructor, ivErrorInstructor);

        // Check if a date has been selected.
        if (selectedDate == null || selectedDate.trim().isEmpty()) {
            showFieldError(editDate, ivErrorDate);
            Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if an instructor name has been entered.
        if (editInstructor.getText().toString().trim().isEmpty()) {
            showFieldError(editInstructor, ivErrorInstructor);
            Toast.makeText(this, "Please enter an instructor's name.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if a class for this course already exists on the selected date (only in create mode).
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

    /**
     * Updates an existing YogaClass with the new data from the form.
     */
    private void updateClass(String instructor, String comments, int capacity) {
        existingClass.setAssignedInstructor(instructor);
        existingClass.setAdditionalComments(comments);
        existingClass.setActualCapacity(capacity > 0 ? capacity : course.getCapacity());
        existingClass.setSlotsAvailable(existingClass.getActualCapacity()); // Reset available slots
        existingClass.setDate(selectedDate);
        existingClass.setFirebaseKey(firebaseKey);
        existingClass.setCourseFirebaseKey(course.getFirebaseKey()); // Ensure course key is set.
        yogaClassViewModel.update(existingClass);
        Toast.makeText(this, "Class updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Creates one or more new YogaClass instances based on the form data.
     * @param repeatWeeks The number of consecutive weeks to create the class for.
     */
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
                // Use custom capacity if provided, otherwise use the course default.
                yogaClass.setActualCapacity(customCapacity > 0 ? customCapacity : course.getCapacity());
                yogaClass.setSlotsAvailable(yogaClass.getActualCapacity()); // Initially, all slots are available.
                yogaClass.setAdditionalComments(comments.isEmpty() ? null : comments);
                yogaClass.setCreatedDate(System.currentTimeMillis());
                yogaClassViewModel.insert(yogaClass);
            }
        }
        String message = repeatWeeks == 1 ? "Class created successfully!" : String.format("%d classes created successfully!", repeatWeeks);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Calculates a new date string by adding a number of weeks to a start date.
     * @param startDate The starting date string in "dd/MM/yyyy" format.
     * @param weekOffset The number of weeks to add.
     * @return The new date string, or null if parsing fails.
     */
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

    /**
     * Shows a visual error indicator on a form field.
     * @param field The EditText field to highlight.
     * @param errorIcon The error icon to make visible.
     */
    private void showFieldError(EditText field, ImageView errorIcon) {
        field.setBackgroundResource(R.drawable.edit_text_error_background);
        errorIcon.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the visual error indicator on a form field.
     * @param field The EditText field to reset.
     * @param errorIcon The error icon to hide.
     */
    private void hideFieldError(EditText field, ImageView errorIcon) {
        field.setBackgroundResource(R.drawable.enhanced_edit_text_background);
        errorIcon.setVisibility(View.GONE);
    }
}
