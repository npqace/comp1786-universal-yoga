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

public class CreateCourseActivity extends AppCompatActivity {
    public static final String EXTRA_COURSE = "com.example.yogaAdmin.EXTRA_COURSE";

    // Form controls - Required fields
    private Spinner spinnerDayOfWeek, spinnerClassType;
    private EditText editTime, editCapacity, editDuration, editPrice;

    // Form controls - Optional fields
    private EditText editDescription, editInstructor, editRoom, editEquipment;
    private Spinner spinnerDifficulty, spinnerAgeGroup;

    // Buttons
    private Button btnClear, btnCreateCourse;

    // Error icons for required fields
    private ImageView ivErrorDayOfWeek, ivErrorTime, ivErrorCapacity, ivErrorDuration, ivErrorPrice, ivErrorClassType;

    private long courseId = -1;
    private long existingCourseCreatedDate = 0;
    private String firebaseKey = null;
    private NetworkStatusLiveData networkStatusLiveData;
    private TextView tvOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_course);

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

        

        initializeViews();
        setupInputFormatters();
        setupRealTimeValidation();
        setupClickListeners();

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_COURSE)) {
            setTitle("Edit Course");
            btnCreateCourse.setText("UPDATE COURSE");
            YogaCourse course = (YogaCourse) intent.getSerializableExtra(EXTRA_COURSE);
            if (course != null) {
                courseId = course.getId();
                existingCourseCreatedDate = course.getCreatedDate();
                firebaseKey = course.getFirebaseKey();
                populateFields(course);
            }
        } else {
            setTitle("Add Course");
        }
    }

    private void initializeViews() {
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        spinnerDayOfWeek = findViewById(R.id.spinner_day_of_week);
        editTime = findViewById(R.id.edit_time);
        editCapacity = findViewById(R.id.edit_capacity);
        editDuration = findViewById(R.id.edit_duration);
        editPrice = findViewById(R.id.edit_price);
        spinnerClassType = findViewById(R.id.spinner_class_type);

        editDescription = findViewById(R.id.edit_description);
        editInstructor = findViewById(R.id.edit_instructor);
        editRoom = findViewById(R.id.edit_room);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        editEquipment = findViewById(R.id.edit_equipment);
        spinnerAgeGroup = findViewById(R.id.spinner_age_group);

        btnClear = findViewById(R.id.btn_clear);
        btnCreateCourse = findViewById(R.id.btn_create_course);

        ivErrorDayOfWeek = findViewById(R.id.iv_error_day_of_week);
        ivErrorTime = findViewById(R.id.iv_error_time);
        ivErrorCapacity = findViewById(R.id.iv_error_capacity);
        ivErrorDuration = findViewById(R.id.iv_error_duration);
        ivErrorPrice = findViewById(R.id.iv_error_price);
        ivErrorClassType = findViewById(R.id.iv_error_class_type);
    }

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

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        if (value != null) {
            int position = adapter.getPosition(value);
            spinner.setSelection(position);
        }
    }

    private void setupInputFormatters() {
        setupTimePicker(editTime);
        setupPriceFormatter(editPrice);
        editCapacity.setInputType(InputType.TYPE_CLASS_NUMBER);
        editDuration.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    private void setupTimePicker(EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setCursorVisible(false);
        editText.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, time -> editText.setText(time));
            timePickerDialog.show();
        });
    }

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

    private void setupClickListeners() {
        btnClear.setOnClickListener(v -> clearForm());
        btnCreateCourse.setOnClickListener(v -> validateAndCreateCourse());
    }

    private void validateAndCreateCourse() {
        clearAllErrorStates();
        boolean hasErrors = false;

        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();
        if (dayOfWeek.equals("Select Day of Week")) {
            showFieldError(spinnerDayOfWeek, ivErrorDayOfWeek, true);
            hasErrors = true;
        }

        String time = editTime.getText().toString().trim();
        if (time.isEmpty()) {
            showFieldError(editTime, ivErrorTime, false);
            hasErrors = true;
        }

        String capacityStr = editCapacity.getText().toString().trim();
        int capacity = 0;
        if (capacityStr.isEmpty() || (capacity = Integer.parseInt(capacityStr)) <= 0) {
            showFieldError(editCapacity, ivErrorCapacity, false);
            hasErrors = true;
        }

        String durationStr = editDuration.getText().toString().trim();
        int duration = 0;
        if (durationStr.isEmpty() || (duration = Integer.parseInt(durationStr)) <= 0) {
            showFieldError(editDuration, ivErrorDuration, false);
            hasErrors = true;
        }

        String priceStr = editPrice.getText().toString().replace("£ ".trim(), "").trim();
        double price = 0;
        if (priceStr.isEmpty() || (price = Double.parseDouble(priceStr)) < 0) {
            showFieldError(editPrice, ivErrorPrice, false);
            hasErrors = true;
        }

        String classType = spinnerClassType.getSelectedItem().toString();
        if (classType.equals("Select Class Type")) {
            showFieldError(spinnerClassType, ivErrorClassType, true);
            hasErrors = true;
        }

        if (hasErrors) {
            Toast.makeText(this, "Please fill all the required fields", Toast.LENGTH_LONG).show();
            return;
        }

        YogaCourse course = new YogaCourse(dayOfWeek, time, capacity, duration, price, classType);
        course.setDescription(editDescription.getText().toString().trim());
        course.setInstructorName(editInstructor.getText().toString().trim());
        course.setRoomNumber(editRoom.getText().toString().trim());
        course.setDifficultyLevel(spinnerDifficulty.getSelectedItem().toString());
        course.setEquipmentNeeded(editEquipment.getText().toString().trim());
        course.setAgeGroup(spinnerAgeGroup.getSelectedItem().toString());

        if (courseId != -1) {
            course.setId(courseId);
            course.setCreatedDate(existingCourseCreatedDate);
            course.setFirebaseKey(firebaseKey);
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_COURSE, course);
        setResult(RESULT_OK, data);
        finish();
    }

    private void showFieldError(View field, ImageView errorIcon, boolean isSpinner) {
        errorIcon.setVisibility(View.VISIBLE);
        int errorBg = isSpinner ? R.drawable.spinner_error_background : R.drawable.edit_text_error_background;
        field.setBackground(ResourcesCompat.getDrawable(getResources(), errorBg, getTheme()));
    }

    private void hideFieldError(View field, ImageView errorIcon, boolean isSpinner) {
        errorIcon.setVisibility(View.GONE);
        int normalBg = isSpinner ? R.drawable.enhanced_spinner_background : R.drawable.enhanced_edit_text_background;
        field.setBackground(ResourcesCompat.getDrawable(getResources(), normalBg, getTheme()));
    }

    private void clearAllErrorStates() {
        hideFieldError(spinnerDayOfWeek, ivErrorDayOfWeek, true);
        hideFieldError(editTime, ivErrorTime, false);
        hideFieldError(editCapacity, ivErrorCapacity, false);
        hideFieldError(editDuration, ivErrorDuration, false);
        hideFieldError(editPrice, ivErrorPrice, false);
        hideFieldError(spinnerClassType, ivErrorClassType, true);
    }

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

    private void setupRealTimeValidation() {
        // Listeners for spinners
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

        // Listeners for EditText fields
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

