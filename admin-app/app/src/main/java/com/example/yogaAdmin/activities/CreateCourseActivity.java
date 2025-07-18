package com.example.yogaAdmin.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.dialogs.TimePickerDialog;
import com.example.yogaAdmin.models.YogaCourse;
import com.example.yogaAdmin.utils.UIUtils;

public class CreateCourseActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIUtils.configureStatusBar(this);
        setContentView(R.layout.activity_create_course);

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
        setupInputFormatters();
        setupRealTimeValidation();
        setupClickListeners();
    }

    private void initializeViews() {
        // Back button
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

    private void setupInputFormatters() {
        // Set up time picker instead of formatter
        setupTimePicker(editTime);

        // Set up price formatter (£ prefix)
        setupPriceFormatter(editPrice);

        // Set numeric input for capacity and duration
        editCapacity.setInputType(InputType.TYPE_CLASS_NUMBER);
        editDuration.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    private void setupTimePicker(EditText editText) {
        // Make EditText non-editable but clickable
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setCursorVisible(false);

        editText.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSelectedListener() {
                @Override
                public void onTimeSelected(String time) {
                    editText.setText(time);
                }
            });
            timePickerDialog.show();
        });
    }

    private void setupPriceFormatter(EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // Set initial text with £ symbol
        editText.setText("£ ");
        editText.setSelection(2); // Position cursor after £

        editText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                String text = s.toString();

                // Ensure it always starts with £
                if (!text.startsWith("£ ")) {
                    isFormatting = true;
                    editText.setText(getString(R.string.format_price_input, text.replaceAll("£\\s*", "")));
                    editText.setSelection(editText.getText().length());
                    isFormatting = false;
                    return;
                }

                // Remove £  and check if remaining text is valid
                String numberPart = text.substring(2).trim();

                // Allow only digits and one decimal point
                if (!numberPart.matches("^\\d*\\.?\\d*$")) {
                    isFormatting = true;
                    // Remove invalid characters
                    String cleaned = numberPart.replaceAll("[^\\d.]", "");

                    // Ensure only one decimal point
                    String[] parts = cleaned.split("\\.");
                    if (parts.length > 2) {
                        cleaned = parts[0] + "." + parts[1];
                    }

                    editText.setText(getString(R.string.format_price_input, cleaned));
                    editText.setSelection(editText.getText().length());
                    isFormatting = false;
                }
            }
        });

        // Prevent cursor from going before £
        editText.setOnClickListener(v -> {
            if (editText.getSelectionStart() < 2) {
                editText.setSelection(2);
            }
        });
    }

    private void setupClickListeners() {
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearForm();
            }
        });

        btnCreateCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndCreateCourse();
            }
        });
    }

    private void validateAndCreateCourse() {
        // Clear all error states first
        clearAllErrorStates();

        // Get values from required fields
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();
        String time = editTime.getText().toString().trim();
        String capacityStr = editCapacity.getText().toString().trim();
        String durationStr = editDuration.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();
        String classType = spinnerClassType.getSelectedItem().toString();

        // Validate required fields
        boolean hasErrors = false;

        // Validate Day of Week
        if (dayOfWeek.equals("Select Day of Week") || spinnerDayOfWeek.getSelectedItemPosition() == 0) {
            showFieldError(spinnerDayOfWeek, ivErrorDayOfWeek, true);
            hasErrors = true;
        }

        // Validate Time
        if (time.isEmpty()) {
            showFieldError(editTime, ivErrorTime, false);
            hasErrors = true;
        }

        // Validate Capacity
        if (capacityStr.isEmpty()) {
            showFieldError(editCapacity, ivErrorCapacity, false);
            hasErrors = true;
        } else {
            try {
                int capacity = Integer.parseInt(capacityStr);
                if (capacity <= 0) {
                    showFieldError(editCapacity, ivErrorCapacity, false);
                    hasErrors = true;
                }
            } catch (NumberFormatException e) {
                showFieldError(editCapacity, ivErrorCapacity, false);
                hasErrors = true;
            }
        }

        // Validate Duration
        if (durationStr.isEmpty()) {
            showFieldError(editDuration, ivErrorDuration, false);
            hasErrors = true;
        } else {
            try {
                int duration = Integer.parseInt(durationStr);
                if (duration <= 0) {
                    showFieldError(editDuration, ivErrorDuration, false);
                    hasErrors = true;
                }
            } catch (NumberFormatException e) {
                showFieldError(editDuration, ivErrorDuration, false);
                hasErrors = true;
            }
        }

        // Validate Price
        if (priceStr.isEmpty() || priceStr.equals("£ ") || priceStr.trim().equals("£")) {
            showFieldError(editPrice, ivErrorPrice, false);
            hasErrors = true;
        } else {
            try {
                // Remove £ symbol for validation
                String priceValue = priceStr.startsWith("£") ? priceStr.substring(priceStr.indexOf("£") + 1).trim() : priceStr;
                if (priceValue.isEmpty()) {
                    showFieldError(editPrice, ivErrorPrice, false);
                    hasErrors = true;
                } else {
                    double price = Double.parseDouble(priceValue);
                    if (price < 0) {
                        showFieldError(editPrice, ivErrorPrice, false);
                        hasErrors = true;
                    }
                }
            } catch (NumberFormatException e) {
                showFieldError(editPrice, ivErrorPrice, false);
                hasErrors = true;
            }
        }

        // Validate Class Type
        if (classType.equals("Select Class Type") || spinnerClassType.getSelectedItemPosition() == 0) {
            showFieldError(spinnerClassType, ivErrorClassType, true);
            hasErrors = true;
        }

        // Show errors if any
        if (hasErrors) {
            Toast.makeText(this, getString(R.string.toast_error_validation), Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void showFieldError(View field, ImageView errorIcon, boolean isSpinner) {
        // Show error icon
        errorIcon.setVisibility(View.VISIBLE);

        // Change background to error state
        if (isSpinner) {
            field.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.spinner_error_background, getTheme()));
        } else {
            field.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_error_background, getTheme()));
        }
    }

    private void hideFieldError(View field, ImageView errorIcon, boolean isSpinner) {
        // Hide error icon
        errorIcon.setVisibility(View.GONE);

        // Restore normal background
        if (isSpinner) {
            field.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.enhanced_spinner_background, getTheme()));
        } else {
            field.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.enhanced_edit_text_background, getTheme()));
        }
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
        // Clear all error states first
        clearAllErrorStates();

        // Clear required fields
        spinnerDayOfWeek.setSelection(0);
        editTime.setText("");
        editCapacity.setText("");
        editDuration.setText("");
        editPrice.setText("£ "); // Reset to just the £ symbol
        editPrice.setSelection(2); // Position cursor after £
        spinnerClassType.setSelection(0);

        // Clear optional fields
        editDescription.setText("");
        editInstructor.setText("");
        editRoom.setText("");
        spinnerDifficulty.setSelection(0);
        editEquipment.setText("");
        spinnerAgeGroup.setSelection(0);

        Toast.makeText(this, getString(R.string.toast_form_cleared), Toast.LENGTH_SHORT).show();
    }

    private void setupRealTimeValidation() {
        // Day of Week spinner listener
        spinnerDayOfWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Not the default "Select" option
                    hideFieldError(spinnerDayOfWeek, ivErrorDayOfWeek, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Class Type spinner listener
        spinnerClassType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Not the default "Select" option
                    hideFieldError(spinnerClassType, ivErrorClassType, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Time field listener (clears error when time is selected)
        editTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    hideFieldError(editTime, ivErrorTime, false);
                }
            }
        });

        // Capacity field listener
        editCapacity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (!text.isEmpty()) {
                    try {
                        int capacity = Integer.parseInt(text);
                        if (capacity > 0) {
                            hideFieldError(editCapacity, ivErrorCapacity, false);
                        }
                    } catch (NumberFormatException e) {
                        // Keep error state if invalid number
                    }
                }
            }
        });

        // Duration field listener
        editDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (!text.isEmpty()) {
                    try {
                        int duration = Integer.parseInt(text);
                        if (duration > 0) {
                            hideFieldError(editDuration, ivErrorDuration, false);
                        }
                    } catch (NumberFormatException e) {
                        // Keep error state if invalid number
                    }
                }
            }
        });

        // Price field listener (more complex due to £ symbol)
        editPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (!text.isEmpty() && !text.equals("£") && !text.equals("£ ")) {
                    try {
                        String priceValue = text.startsWith("£") ? text.substring(text.indexOf("£") + 1).trim() : text;
                        if (!priceValue.isEmpty()) {
                            double price = Double.parseDouble(priceValue);
                            if (price >= 0) {
                                hideFieldError(editPrice, ivErrorPrice, false);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Keep error state if invalid number
                    }
                }
            }
        });
    }
}