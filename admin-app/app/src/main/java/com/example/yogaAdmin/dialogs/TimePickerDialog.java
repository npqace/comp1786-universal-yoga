package com.example.yogaAdmin.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.yogaAdmin.R;

/**
 * A custom dialog for selecting a time (hour, minute, AM/PM).
 * It provides a user-friendly interface with number pickers and buttons,
 * and returns the selected time in a 24-hour format string.
 */
public class TimePickerDialog extends Dialog {

    private OnTimeSelectedListener listener;
    private NumberPicker hourPicker, minutePicker;
    private Button btnAM, btnPM;
    private Button btnConfirm, btnCancel;
    private TextView tvSelectedTime;

    private boolean isAM = true;

    /**
     * Interface for callback when a time is selected.
     */
    public interface OnTimeSelectedListener {
        /**
         * Called when the user confirms a time selection.
         * @param time The selected time as a string in "HH:mm" format.
         */
        void onTimeSelected(String time);
    }

    /**
     * Constructs a new TimePickerDialog.
     * @param context The context in which the dialog should run.
     * @param listener The callback that will run when a time is selected.
     */
    public TimePickerDialog(@NonNull Context context, OnTimeSelectedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_time_picker);

        setCancelable(true);
        initializeViews();
        setupNumberPickers();
        setupClickListeners();
        updateSelectedTime();
    }

    /**
     * Initializes all the view components from the layout file.
     */
    private void initializeViews() {
        hourPicker = findViewById(R.id.hour_picker);
        minutePicker = findViewById(R.id.minute_picker);
        btnAM = findViewById(R.id.btn_am);
        btnPM = findViewById(R.id.btn_pm);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnCancel = findViewById(R.id.btn_cancel);
        tvSelectedTime = findViewById(R.id.tv_selected_time);

        // Set initial AM selection
        selectAMPM(true);
    }

    /**
     * Configures the number pickers for hours and minutes.
     */
    private void setupNumberPickers() {
        // Setup hour picker (1-12)
        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);
        hourPicker.setValue(12); // Default to 12
        hourPicker.setWrapSelectorWheel(true);

        // Setup minute picker (0-59, every minute)
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(0); // Default to 00
        minutePicker.setWrapSelectorWheel(true);

        // Add listeners to update the selected time display whenever the value changes.
        hourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> updateSelectedTime());
        minutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> updateSelectedTime());
    }



    /**
     * Toggles the state between AM and PM and updates the UI accordingly.
     * @param isAMSelected True if AM is selected, false for PM.
     */
    private void selectAMPM(boolean isAMSelected) {
        isAM = isAMSelected;

        // Update button backgrounds and text colors to indicate selection
        if (isAM) {
            btnAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.time_button_selected_background));
            btnAM.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            btnPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.time_button_background));
            btnPM.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_text));
        } else {
            btnPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.time_button_selected_background));
            btnPM.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            btnAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.time_button_background));
            btnAM.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_text));
        }

        updateSelectedTime();
    }

    /**
     * Updates the TextView to display the currently selected time in 24-hour format.
     */
    private void updateSelectedTime() {
        // Get values from pickers
        int selectedHour = hourPicker.getValue();
        int selectedMinute = minutePicker.getValue();

        // Convert to 24-hour format for display consistency
        int hour24 = selectedHour;
        if (!isAM && selectedHour != 12) { // PM case, not 12 PM
            hour24 = selectedHour + 12;
        } else if (isAM && selectedHour == 12) { // 12 AM case (midnight)
            hour24 = 0;
        }

        // Format and display the time
        String timeString = String.format("%02d:%02d", hour24, selectedMinute);
        tvSelectedTime.setText(timeString);
        btnConfirm.setEnabled(true);
    }

    /**
     * Sets up click listeners for the AM/PM, confirm, and cancel buttons.
     */
    private void setupClickListeners() {
        btnAM.setOnClickListener(v -> selectAMPM(true));
        btnPM.setOnClickListener(v -> selectAMPM(false));

        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                // Get final values from pickers
                int selectedHour = hourPicker.getValue();
                int selectedMinute = minutePicker.getValue();

                // Convert to 24-hour format
                int hour24 = selectedHour;
                if (!isAM && selectedHour != 12) { // PM case, not 12 PM
                    hour24 = selectedHour + 12;
                } else if (isAM && selectedHour == 12) { // 12 AM case (midnight)
                    hour24 = 0;
                }

                // Pass the formatted time string to the listener
                String timeString = String.format("%02d:%02d", hour24, selectedMinute);
                listener.onTimeSelected(timeString);
            }
            dismiss(); // Close the dialog
        });

        btnCancel.setOnClickListener(v -> dismiss()); // Close the dialog
    }
}
