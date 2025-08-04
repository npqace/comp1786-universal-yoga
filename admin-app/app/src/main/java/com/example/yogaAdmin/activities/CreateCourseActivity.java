package com.example.yogaAdmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.dialogs.TimePickerDialog;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.utils.NetworkStatusLiveData;

/**
 * Activity for creating a new yoga course or editing an existing one.
 * This activity provides a form to input all the details of a yoga course,
 * such as class type, schedule, capacity, price, and other optional information.
 */
public class CreateCourseActivity extends AppCompatActivity {
    // Constant for passing a YogaCourse object via an Intent extra.
    public static final String EXTRA_COURSE = "com.example.yogaAdmin.EXTRA_COURSE";

    // UI elements for the form - Required fields
    private Spinner spinnerDayOfWeek, spinnerClassType;
    private EditText editTime, editCapacity, editDuration, editPrice;

    // UI elements for the form - Optional fields
    private EditText editDescription, editInstructor, editRoom, editEquipment;
    private Spinner spinnerDifficulty, spinnerAgeGroup;

    // Action buttons
    private Button btnClear, btnCreateCourse;

    // Error icons for visual feedback on validation for required fields
    private ImageView ivErrorDayOfWeek, ivErrorTime, ivErrorCapacity, ivErrorDuration, ivErrorPrice, ivErrorClassType;

    // State variables
    private long courseId = -1; // -1 indicates a new course, otherwise it's the ID of the course being edited.
    private long existingCourseCreatedDate = 0; // To preserve the original creation date on edit.
    private String firebaseKey = null; // Firebase key for the course being edited.

    // Network status monitoring
    private NetworkStatusLiveData networkStatusLiveData;
    private TextView tvOffline;

    /**
     * Called when the activity is first created.
     * Initializes the UI, sets up listeners, and populates fields if editing an existing course.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_course);

        // Hide the default action bar for a custom look.
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Setup network status observer to show an offline indicator.
        tvOffline = findViewById(R.id.tv_offline);
        networkStatusLiveData = new NetworkStatusLiveData(getApplicationContext());
        networkStatusLiveData.observe(this, isOnline -> {
            tvOffline.setVisibility(isOnline ? View.GONE : View.VISIBLE);
        });

        // Initialize all UI views.
        initializeViews();
        // Setup input formatters for fields like time and price.
        setupInputFormatters();
        // Setup real-time validation listeners.
        setupRealTimeValidation();
        // Setup click listeners for buttons.
        setupClickListeners();

        // Check if the activity was started with an existing course to edit.
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_COURSE)) {
            // Set the title and button text for "Edit Mode".
            setTitle("Edit Course");
            btnCreateCourse.setText("UPDATE COURSE");
            YogaCourse course = (YogaCourse) intent.getSerializableExtra(EXTRA_COURSE);
            if (course != null) {
                // Store details of the existing course.
                courseId = course.getId();
                existingCourseCreatedDate = course.getCreatedDate();
                firebaseKey = course.getFirebaseKey();
                // Populate the form fields with the course data.
                populateFields(course);
            }
        } else {
            // Set the title for "Create Mode".
            setTitle("Add Course");
        }
    }

    /**
     * Initializes all UI views by finding them by their ID from the layout file.
     */
    private void initializeViews() {
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Required fields
        spinnerDayOfWeek = findViewById(R.id.spinner_day_of_week);
        editTime = findViewById(R.id.edit_time);
        editCapacity = findViewById(R.id.edit_capacity);
        editDuration = findViewById(R.id.edit_duration);
        editPrice = findViewById(R.id.edit_price);
        spinnerClassType = findViewById(R.id.spinner_class_type);

        // Optional fields
        editDescription = findViewById(R.id.edit_description);
        editInstructor = findViewById(R.id.edit_instructor);
        editRoom = findViewById(R.id.edit_room);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        editEquipment = findViewById(R.id.edit_equipment);
        spinnerAgeGroup = findViewById(R.id.spinner_age_group);

        // Buttons
        btnClear = findViewById(R.id.btn_clear);
        btnCreateCourse = findViewById(R.id.btn_create_course);

        // Error icons
        ivErrorDayOfWeek = findViewById(R.id.iv_error_day_of_week);
        ivErrorTime = findViewById(R.id.iv_error_time);
        ivErrorCapacity = findViewById(R.id.iv_error_capacity);
        ivErrorDuration = findViewById(R.id.iv_error_duration);
        ivErrorPrice = findViewById(R.id.iv_error_price);
        ivErrorClassType = findViewById(R.id.iv_error_class_type);
    }

    /**
     * Populates the form fields with the data from a given YogaCourse object.
     * Used when editing an existing course.
     *
     * @param course The YogaCourse object containing the data to display.
     */
    private void populateFields(YogaCourse course) {
        setSpinnerSelection(spinnerDayOfWeek, course.getDayOfWeek());
        editTime.setText(course.getTime());
        editCapacity.setText(String.valueOf(course.getCapacity()));
        editDuration.setText(String.valueOf(course.getDuration()));
        editPrice.setText(String.format(java.util.Locale.UK, "£%.2f", course.getPrice()));
        setSpinnerSelection(spinnerClassType, course.getClassType());
        editDescription.setText(course.getDescription());
        editInstructor.setText(course.getInstructorName());
        editRoom.setText(course.getRoomNumber());
        setSpinnerSelection(spinnerDifficulty, course.getDifficultyLevel());
        editEquipment.setText(course.getEquipmentNeeded());
        setSpinnerSelection(spinnerAgeGroup, course.getAgeGroup());
    }

    /**
     * Helper method to set the selection of a spinner based on a string value.
     *
     * @param spinner The spinner to modify.
     * @param value   The string value to select in the spinner.
     */
    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        if (value != null) {
            int position = adapter.getPosition(value);
            spinner.setSelection(position);
        }
    }

    /**
     * Sets up input formatters and pickers for specific EditText fields.
     */
    private void setupInputFormatters() {
        setupTimePicker(editTime);
        setupPriceFormatter(editPrice);
        editCapacity.setInputType(InputType.TYPE_CLASS_NUMBER);
        editDuration.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    /**
     * Configures an EditText to show a custom time picker dialog on click.
     *
     * @param editText The EditText to attach the time picker to.
     */
    private void setupTimePicker(EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setCursorVisible(false);
        editText.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, time -> editText.setText(time));
            timePickerDialog.show();
        });
    }

    /**
     * Sets up a TextWatcher for the price field to automatically format the input as currency.
     *
     * @param editText The EditText for the price.
     */
    private void setupPriceFormatter(EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (editText.getText().toString().isEmpty()) {
            editText.setText("£ ");
            editText.setSelection(2);
        }

        editText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String text = s.toString();
                if (!text.startsWith("£ ")) {
                    text = "£ " + text.replaceAll("£\\s*", "");
                }
                editText.setText(text);
                editText.setSelection(editText.getText().length());
                isFormatting = false;
            }
        });
    }

    /**
     * Sets up click listeners for the clear and create/update buttons.
     */
    private void setupClickListeners() {
        btnClear.setOnClickListener(v -> clearForm());
        btnCreateCourse.setOnClickListener(v -> validateAndCreateCourse());
    }

    /**
     * Validates the form fields and, if successful, creates or updates a YogaCourse object.
     * The result is sent back to the calling activity.
     */
    private void validateAndCreateCourse() {
        clearAllErrorStates();
        boolean hasErrors = false;

        // Validate Day of Week
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();
        if (dayOfWeek.equals("Select Day of Week")) {
            showFieldError(spinnerDayOfWeek, ivErrorDayOfWeek, true);
            hasErrors = true;
        }

        // Validate Time
        String time = editTime.getText().toString().trim();
        if (time.isEmpty()) {
            showFieldError(editTime, ivErrorTime, false);
            hasErrors = true;
        }

        // Validate Capacity
        String capacityStr = editCapacity.getText().toString().trim();
        int capacity = 0;
        if (capacityStr.isEmpty() || (capacity = Integer.parseInt(capacityStr)) <= 0) {
            showFieldError(editCapacity, ivErrorCapacity, false);
            hasErrors = true;
        }

        // Validate Duration
        String durationStr = editDuration.getText().toString().trim();
        int duration = 0;
        if (durationStr.isEmpty() || (duration = Integer.parseInt(durationStr)) <= 0) {
            showFieldError(editDuration, ivErrorDuration, false);
            hasErrors = true;
        }

        // Validate Price
        String priceStr = editPrice.getText().toString().replace("£ ".trim(), "").trim();
        double price = 0;
        if (priceStr.isEmpty() || (price = Double.parseDouble(priceStr)) < 0) {
            showFieldError(editPrice, ivErrorPrice, false);
            hasErrors = true;
        }

        // Validate Class Type
        String classType = spinnerClassType.getSelectedItem().toString();
        if (classType.equals("Select Class Type")) {
            showFieldError(spinnerClassType, ivErrorClassType, true);
            hasErrors = true;
        }

        // If there are errors, show a toast and stop.
        if (hasErrors) {
            Toast.makeText(this, "Please fill all the required fields", Toast.LENGTH_LONG).show();
            return;
        }

        // Create a new YogaCourse object with the form data.
        YogaCourse course = new YogaCourse(dayOfWeek, time, capacity, duration, price, classType);
        course.setDescription(editDescription.getText().toString().trim());
        course.setInstructorName(editInstructor.getText().toString().trim());
        course.setRoomNumber(editRoom.getText().toString().trim());
        course.setDifficultyLevel(spinnerDifficulty.getSelectedItem().toString());
        course.setEquipmentNeeded(editEquipment.getText().toString().trim());
        course.setAgeGroup(spinnerAgeGroup.getSelectedItem().toString());

        // If editing, set the original ID, creation date, and Firebase key.
        if (courseId != -1) {
            course.setId(courseId);
            course.setCreatedDate(existingCourseCreatedDate);
            course.setFirebaseKey(firebaseKey);
        }

        // Return the created/updated course to the previous activity.
        Intent data = new Intent();
        data.putExtra(EXTRA_COURSE, course);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * Shows a visual error indicator on a form field.
     *
     * @param field     The View (EditText or Spinner) to highlight.
     * @param errorIcon The ImageView for the error icon to show.
     * @param isSpinner A boolean to determine if the field is a Spinner, for applying the correct background.
     */
    private void showFieldError(View field, ImageView errorIcon, boolean isSpinner) {
        errorIcon.setVisibility(View.VISIBLE);
        int errorBg = isSpinner ? R.drawable.spinner_error_background : R.drawable.edit_text_error_background;
        field.setBackground(ResourcesCompat.getDrawable(getResources(), errorBg, getTheme()));
    }

    /**
     * Hides the visual error indicator on a form field.
     *
     * @param field     The View (EditText or Spinner) to reset.
     * @param errorIcon The ImageView for the error icon to hide.
     * @param isSpinner A boolean to determine if the field is a Spinner.
     */
    private void hideFieldError(View field, ImageView errorIcon, boolean isSpinner) {
        errorIcon.setVisibility(View.GONE);
        int normalBg = isSpinner ? R.drawable.enhanced_spinner_background : R.drawable.enhanced_edit_text_background;
        field.setBackground(ResourcesCompat.getDrawable(getResources(), normalBg, getTheme()));
    }

    /**
     * Clears all error indicators from the form fields.
     */
    private void clearAllErrorStates() {
        hideFieldError(spinnerDayOfWeek, ivErrorDayOfWeek, true);
        hideFieldError(editTime, ivErrorTime, false);
        hideFieldError(editCapacity, ivErrorCapacity, false);
        hideFieldError(editDuration, ivErrorDuration, false);
        hideFieldError(editPrice, ivErrorPrice, false);
        hideFieldError(spinnerClassType, ivErrorClassType, true);
    }

    /**
     * Clears all input fields in the form and resets them to their default state.
     */
    private void clearForm() {
        clearAllErrorStates();
        spinnerDayOfWeek.setSelection(0);
        editTime.setText("");
        editCapacity.setText("");
        editDuration.setText("");
        editPrice.setText("£ ");
        editPrice.setSelection(2);
        spinnerClassType.setSelection(0);
        editDescription.setText("");
        editInstructor.setText("");
        editRoom.setText("");
        spinnerDifficulty.setSelection(0);
        editEquipment.setText("");
        spinnerAgeGroup.setSelection(0);
        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Sets up real-time validation by adding listeners to the input fields.
     * This provides immediate feedback to the user as they fill out the form.
     */
    private void setupRealTimeValidation() {
        // Listener for spinners to clear errors when a valid option is selected.
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    if (parent.getId() == R.id.spinner_day_of_week) hideFieldError(spinnerDayOfWeek, ivErrorDayOfWeek, true);
                    if (parent.getId() == R.id.spinner_class_type) hideFieldError(spinnerClassType, ivErrorClassType, true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        spinnerDayOfWeek.setOnItemSelectedListener(spinnerListener);
        spinnerClassType.setOnItemSelectedListener(spinnerListener);

        // Listener for the time field.
        editTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) hideFieldError(editTime, ivErrorTime, false);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // A shared watcher for fields that require a positive number.
        TextWatcher positiveNumberWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (!text.isEmpty()) {
                    try {
                        if (Integer.parseInt(text) > 0) {
                            if (this == editCapacity.getTag()) hideFieldError(editCapacity, ivErrorCapacity, false);
                            if (this == editDuration.getTag()) hideFieldError(editDuration, ivErrorDuration, false);
                        }
                    } catch (NumberFormatException e) { /* ignore */ }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        editCapacity.addTextChangedListener(positiveNumberWatcher);
        editCapacity.setTag(positiveNumberWatcher);
        editDuration.addTextChangedListener(positiveNumberWatcher);
        editDuration.setTag(positiveNumberWatcher);

        // Listener for the price field.
        editPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().replace(getString(R.string.format_price_prefix).trim(), "").trim();
                if (!text.isEmpty()) {
                    try {
                        if (Double.parseDouble(text) >= 0) hideFieldError(editPrice, ivErrorPrice, false);
                    }
                    catch (NumberFormatException e) { /* ignore */ }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }
}

