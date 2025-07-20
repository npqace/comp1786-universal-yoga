package com.example.yogaAdmin.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.adapter.ClassWithCourseInfoAdapter;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.viewmodel.YogaClassViewModel;

import java.util.Calendar;

public class SearchActivity extends AppCompatActivity {

    private EditText editTeacherSearch, editDateSearch;
    private Spinner spinnerDaySearch;
    private RecyclerView recyclerViewResults;
    private ClassWithCourseInfoAdapter adapter;
    private YogaClassViewModel yogaClassViewModel;
    private LinearLayout emptyStateLayout;
    private TextView tvResultsCount;
    private ImageView btnClearDate, btnClearDay;
    private Button btnClearAll, btnAdvancedSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        yogaClassViewModel = new ViewModelProvider(this).get(YogaClassViewModel.class);

        initViews();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews() {
        editTeacherSearch = findViewById(R.id.edit_teacher_search);
        editDateSearch = findViewById(R.id.edit_date_search);
        spinnerDaySearch = findViewById(R.id.spinner_day_search);
        recyclerViewResults = findViewById(R.id.recycler_view_results);
        emptyStateLayout = findViewById(R.id.layout_empty_state);
        tvResultsCount = findViewById(R.id.tv_results_count);
        btnClearDate = findViewById(R.id.btn_clear_date);
        btnClearDay = findViewById(R.id.btn_clear_day);
        btnClearAll = findViewById(R.id.btn_clear_all);
        btnAdvancedSearch = findViewById(R.id.btn_advanced_search);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setHasFixedSize(true);
        adapter = new ClassWithCourseInfoAdapter();
        recyclerViewResults.setAdapter(adapter);

        adapter.setOnItemClickListener(classWithCourseInfo -> {
            Intent intent = new Intent(SearchActivity.this, ClassDetailsActivity.class);
            intent.putExtra(ClassDetailsActivity.EXTRA_CLASS_INFO, classWithCourseInfo);
            startActivity(intent);
        });
    }

    private void setupListeners() {
        editTeacherSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    searchByTeacher(s.toString());
                } else {
                    clearResults();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editDateSearch.setOnClickListener(v -> showDatePickerDialog());

        btnAdvancedSearch.setOnClickListener(v -> performAdvancedSearch());

        btnClearDate.setOnClickListener(v -> {
            editDateSearch.setText("");
            btnClearDate.setVisibility(View.GONE);
        });

        spinnerDaySearch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                btnClearDay.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnClearDay.setOnClickListener(v -> {
            spinnerDaySearch.setSelection(0);
            btnClearDay.setVisibility(View.GONE);
        });

        btnClearAll.setOnClickListener(v -> {
            editTeacherSearch.setText("");
            editDateSearch.setText("");
            spinnerDaySearch.setSelection(0);
            clearResults();
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    editDateSearch.setText(selectedDate);
                    btnClearDate.setVisibility(View.VISIBLE);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void searchByTeacher(String teacherName) {
        yogaClassViewModel.searchByTeacher(teacherName).observe(this, results -> {
            adapter.submitList(results);
            updateUIWithResults(results.size());
        });
    }

    private void performAdvancedSearch() {
        String date = editDateSearch.getText().toString();
        String dayOfWeek = spinnerDaySearch.getSelectedItem().toString();

        if (!date.isEmpty()) {
            yogaClassViewModel.searchByDate(date).observe(this, results -> {
                adapter.submitList(results);
                updateUIWithResults(results.size());
            });
        } else if (!dayOfWeek.equals("All")) {
            yogaClassViewModel.searchByDayOfWeek(dayOfWeek).observe(this, results -> {
                adapter.submitList(results);
                updateUIWithResults(results.size());
            });
        }
    }

    private void clearResults() {
        adapter.submitList(null);
        updateUIWithResults(0);
    }

    private void updateUIWithResults(int count) {
        if (count > 0) {
            recyclerViewResults.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            tvResultsCount.setVisibility(View.VISIBLE);
            tvResultsCount.setText(getString(R.string.results_found, count));
        } else {
            recyclerViewResults.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            tvResultsCount.setVisibility(View.GONE);
        }
    }
}