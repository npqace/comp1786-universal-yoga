package com.example.yogaAdmin.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import com.example.yogaAdmin.utils.NetworkStatusLiveData;
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
    private ImageView btnClearDate;
    private Button btnClearAll;
    private ImageView btnAdvancedSearch;
    private LinearLayout advancedSearchContainer;
    private NetworkStatusLiveData networkStatusLiveData;
    private TextView tvOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        yogaClassViewModel = new ViewModelProvider(this).get(YogaClassViewModel.class);

        initViews();
        setupRecyclerView();
        setupListeners();

        tvOffline = findViewById(R.id.tv_offline);
        networkStatusLiveData = new NetworkStatusLiveData(getApplicationContext());
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                tvOffline.setVisibility(View.GONE);
            } else {
                tvOffline.setVisibility(View.VISIBLE);
            }
        });

        // Perform initial search to show all classes
        performSearch();
    }

    private void initViews() {
        editTeacherSearch = findViewById(R.id.edit_teacher_search);
        editDateSearch = findViewById(R.id.edit_date_search);
        spinnerDaySearch = findViewById(R.id.spinner_day_search);
        recyclerViewResults = findViewById(R.id.recycler_view_results);
        emptyStateLayout = findViewById(R.id.layout_empty_state);
        tvResultsCount = findViewById(R.id.tv_results_count);
        btnClearDate = findViewById(R.id.btn_clear_date);
        btnClearAll = findViewById(R.id.btn_clear_all);
        btnAdvancedSearch = findViewById(R.id.btn_advanced_search);
        advancedSearchContainer = findViewById(R.id.advanced_search_container);

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
        btnAdvancedSearch.setOnClickListener(v -> {
            if (advancedSearchContainer.getVisibility() == View.VISIBLE) {
                advancedSearchContainer.setVisibility(View.GONE);
            } else {
                advancedSearchContainer.setVisibility(View.VISIBLE);
            }
        });

        editTeacherSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editDateSearch.setOnClickListener(v -> showDatePickerDialog());

        editDateSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearDate.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        btnClearDate.setOnClickListener(v -> {
            editDateSearch.setText("");
        });

        spinnerDaySearch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnClearAll.setOnClickListener(v -> {
            editTeacherSearch.setText("");
            editDateSearch.setText("");
            spinnerDaySearch.setSelection(0);
            performSearch();
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
                }, year, month, day);
        datePickerDialog.show();
    }

    private void performSearch() {
        String teacherName = editTeacherSearch.getText().toString().trim();
        String date = editDateSearch.getText().toString().trim();
        String dayOfWeek = spinnerDaySearch.getSelectedItem().toString();

        yogaClassViewModel.search(teacherName, date, dayOfWeek).observe(this, results -> {
            if (results == null) return;
            adapter.submitList(results);
            updateUIWithResults(results.size());
        });
    }

    private void updateUIWithResults(int count) {
        boolean hasSearchQuery = !editTeacherSearch.getText().toString().trim().isEmpty() ||
                !editDateSearch.getText().toString().trim().isEmpty() ||
                (spinnerDaySearch.getSelectedItemPosition() > 0 && !spinnerDaySearch.getSelectedItem().toString().equals("All Days"));

        if (count > 0) {
            recyclerViewResults.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            tvResultsCount.setVisibility(View.VISIBLE);
            tvResultsCount.setText(String.format("%d results found", count));
        } else {
            recyclerViewResults.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            tvResultsCount.setVisibility(View.GONE);

            // Customize empty state message
            TextView emptyTitle = findViewById(R.id.tv_empty_title);
            TextView emptySubtitle = findViewById(R.id.tv_empty_subtitle);
            if (hasSearchQuery) {
                emptyTitle.setText("No Results Found");
                emptySubtitle.setText("No classes match your search criteria. Try adjusting your search terms.");
            } else {
                emptyTitle.setText("Ready to Search");
                emptySubtitle.setText("Enter a teacher name or use advanced search to find classes");
            }
        }
    }
}
