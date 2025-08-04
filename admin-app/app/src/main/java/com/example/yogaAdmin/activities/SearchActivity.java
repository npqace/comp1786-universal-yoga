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
import com.example.yogaAdmin.viewmodel.SearchViewModel;
import com.example.yogaAdmin.viewmodel.YogaClassViewModel;

import java.util.Calendar;

/**
 * Activity that provides a dedicated search interface for finding yoga classes.
 * Users can search by instructor name, date, or day of the week.
 * The results are displayed in a RecyclerView, and users can interact with the results.
 */
public class SearchActivity extends AppCompatActivity implements ClassWithCourseInfoAdapter.OnStatusChangeListener {

    // UI elements for search input
    private EditText editInstructorSearch, editDateSearch;
    private Spinner spinnerDaySearch;
    // UI for displaying results
    private RecyclerView recyclerViewResults;
    private ClassWithCourseInfoAdapter adapter;
    private LinearLayout emptyStateLayout;
    private TextView tvResultsCount;
    // UI for controls
    private ImageView btnClearDate;
    private Button btnClearAll;
    private ImageView btnAdvancedSearch;
    private LinearLayout advancedSearchContainer;
    // ViewModel for handling search logic
    private SearchViewModel searchViewModel;
    // Network status monitoring
    private NetworkStatusLiveData networkStatusLiveData;
    private TextView tvOffline;

    /**
     * Called when the activity is first created.
     * Initializes the ViewModel, views, and listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize the ViewModel.
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // Initialize all UI views.
        initViews();
        // Setup the RecyclerView for displaying search results.
        setupRecyclerView();
        // Setup listeners for all interactive UI elements.
        setupListeners();

        // Setup network status observer.
        tvOffline = findViewById(R.id.tv_offline);
        networkStatusLiveData = new NetworkStatusLiveData(getApplicationContext());
        networkStatusLiveData.observe(this, isOnline -> {
            tvOffline.setVisibility(isOnline ? View.GONE : View.VISIBLE);
        });

        // Perform an initial search to populate the list with all classes.
        performSearch();
    }

    /**
     * Initializes all UI views by finding them by their ID from the layout file.
     */
    private void initViews() {
        editInstructorSearch = findViewById(R.id.edit_instructor_search);
        editDateSearch = findViewById(R.id.edit_date_search);
        spinnerDaySearch = findViewById(R.id.spinner_day_search);
        recyclerViewResults = findViewById(R.id.recycler_view_results);
        emptyStateLayout = findViewById(R.id.layout_empty_state);
        tvResultsCount = findViewById(R.id.tv_results_count);
        btnClearDate = findViewById(R.id.btn_clear_date);
        btnClearAll = findViewById(R.id.btn_clear_all);
        btnAdvancedSearch = findViewById(R.id.btn_advanced_search);
        advancedSearchContainer = findViewById(R.id.advanced_search_container);

        // Setup back button.
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Sets up the RecyclerView, its layout manager, and adapter.
     * Also sets listeners for item clicks and status changes.
     */
    private void setupRecyclerView() {
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setHasFixedSize(true);
        adapter = new ClassWithCourseInfoAdapter();
        recyclerViewResults.setAdapter(adapter);

        // Set a listener for when a user clicks on a search result item.
        adapter.setOnItemClickListener(classWithCourseInfo -> {
            Intent intent = new Intent(SearchActivity.this, ClassDetailsActivity.class);
            intent.putExtra(ClassDetailsActivity.EXTRA_CLASS_ID, classWithCourseInfo.yogaClass.getId());
            startActivity(intent);
        });

        // Set the listener for status changes from the adapter's spinner.
        adapter.setOnStatusChangeListener(this);
    }

    /**
     * Callback method from {@link ClassWithCourseInfoAdapter.OnStatusChangeListener}.
     * Called when the status of a class is changed via the spinner in the RecyclerView.
     *
     * @param yogaClass The class whose status was changed.
     * @param newStatus The new status string.
     */
    @Override
    public void onStatusChanged(com.example.yogaAdmin.models.YogaClass yogaClass, String newStatus) {
        yogaClass.setStatus(newStatus);
        searchViewModel.update(yogaClass);
    }

    /**
     * Sets up listeners for all interactive UI elements like buttons, text fields, and spinners.
     */
    private void setupListeners() {
        // Toggle visibility of the advanced search container.
        btnAdvancedSearch.setOnClickListener(v -> {
            advancedSearchContainer.setVisibility(advancedSearchContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        // Add a text watcher to the instructor search field to trigger search on text change.
        editInstructorSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Show date picker dialog when the date field is clicked.
        editDateSearch.setOnClickListener(v -> showDatePickerDialog());

        // Add a text watcher to the date search field.
        editDateSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show or hide the clear button based on whether the field is empty.
                btnClearDate.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                performSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear the date search field.
        btnClearDate.setOnClickListener(v -> {
            editDateSearch.setText("");
        });

        // Add a listener to the day of the week spinner to trigger search on selection change.
        spinnerDaySearch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performSearch();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Clear all search fields and perform a search to show all results.
        btnClearAll.setOnClickListener(v -> {
            editInstructorSearch.setText("");
            editDateSearch.setText("");
            spinnerDaySearch.setSelection(0);
            performSearch();
        });
    }

    /**
     * Displays a {@link DatePickerDialog} to allow the user to select a date for searching.
     */
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

    /**
     * Gathers the current search criteria from the input fields and triggers the search in the ViewModel.
     * The results are observed and submitted to the adapter.
     */
    private void performSearch() {
        String instructorName = editInstructorSearch.getText().toString().trim();
        String date = editDateSearch.getText().toString().trim();
        String dayOfWeek = spinnerDaySearch.getSelectedItem().toString();

        // Observe the search results from the ViewModel.
        searchViewModel.search(instructorName, date, dayOfWeek).observe(this, results -> {
            if (results == null) return;
            adapter.submitList(results);
            updateUIWithResults(results.size());
        });
    }

    /**
     * Updates the UI based on the search results.
     * Shows the results list or an empty state message.
     *
     * @param count The number of results found.
     */
    private void updateUIWithResults(int count) {
        // Determine if any search query has been entered.
        boolean hasSearchQuery = !editInstructorSearch.getText().toString().trim().isEmpty() ||
                !editDateSearch.getText().toString().trim().isEmpty() ||
                (spinnerDaySearch.getSelectedItemPosition() > 0 && !spinnerDaySearch.getSelectedItem().toString().equals("All Days"));

        if (count > 0) {
            // If results are found, show the RecyclerView and the results count.
            recyclerViewResults.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            tvResultsCount.setVisibility(View.VISIBLE);
            tvResultsCount.setText(String.format("%d results found", count));
        } else {
            // If no results are found, hide the RecyclerView and show the empty state layout.
            recyclerViewResults.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            tvResultsCount.setVisibility(View.GONE);

            // Customize the empty state message based on whether a search was performed.
            TextView emptyTitle = findViewById(R.id.tv_empty_title);
            TextView emptySubtitle = findViewById(R.id.tv_empty_subtitle);
            if (hasSearchQuery) {
                emptyTitle.setText("No Results Found");
                emptySubtitle.setText("No classes match your search criteria. Try adjusting your search terms.");
            } else {
                emptyTitle.setText("Ready to Search");
                emptySubtitle.setText("Enter an instructor name or use advanced search to find classes");
            }
        }
    }
}
