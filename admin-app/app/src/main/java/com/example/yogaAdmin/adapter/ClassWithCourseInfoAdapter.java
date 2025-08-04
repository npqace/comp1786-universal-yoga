package com.example.yogaAdmin.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.models.ClassWithCourseInfo;
import com.example.yogaAdmin.models.YogaClass;

import java.util.Arrays;
import java.util.Objects;

/**
 * A RecyclerView adapter for displaying a list of {@link ClassWithCourseInfo} objects.
 * This adapter is used in the search results screen to show combined information
 * from both a {@link YogaClass} and its associated {@link com.example.yogaAdmin.models.YogaCourse}.
 * It uses a {@link ListAdapter} with {@link DiffUtil} for efficient updates.
 */
public class ClassWithCourseInfoAdapter extends ListAdapter<ClassWithCourseInfo, ClassWithCourseInfoAdapter.ClassWithCourseInfoViewHolder> {

    // Listener for item click events.
    private OnItemClickListener listener;
    // Listener for status change events from the spinner.
    private OnStatusChangeListener statusChangeListener;

    /**
     * Default constructor for the adapter.
     */
    public ClassWithCourseInfoAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * DiffUtil.ItemCallback for calculating the difference between two non-null items in a list.
     * This allows the ListAdapter to determine which items have changed, been added, or been removed.
     */
    private static final DiffUtil.ItemCallback<ClassWithCourseInfo> DIFF_CALLBACK = new DiffUtil.ItemCallback<ClassWithCourseInfo>() {
        @Override
        public boolean areItemsTheSame(@NonNull ClassWithCourseInfo oldItem, @NonNull ClassWithCourseInfo newItem) {
            // Items are considered the same if their class IDs are identical.
            return oldItem.yogaClass.getId() == newItem.yogaClass.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ClassWithCourseInfo oldItem, @NonNull ClassWithCourseInfo newItem) {
            // Contents are considered the same if the objects are equal.
            // This relies on a well-defined equals() method in the data classes.
            return Objects.equals(oldItem, newItem);
        }
    };

    /**
     * Called when RecyclerView needs a new {@link ClassWithCourseInfoViewHolder}.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ClassWithCourseInfoViewHolder that holds a View for the item.
     */
    @NonNull
    @Override
    public ClassWithCourseInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seach_result, parent, false);
        return new ClassWithCourseInfoViewHolder(itemView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ClassWithCourseInfoViewHolder holder, int position) {
        ClassWithCourseInfo currentClass = getItem(position);
        holder.bind(currentClass, statusChangeListener);
    }

    /**
     * ViewHolder for the {@link ClassWithCourseInfo} item.
     * Contains references to the UI views and the logic to bind data to them.
     */
    public class ClassWithCourseInfoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvClassDate, tvAssignedInstructor, tvCourseInfo, tvClassDayOfWeek, tvCapacity, tvComments, tvCreatedDate;
        private final Spinner spinnerStatus;
        private final Context context;

        public ClassWithCourseInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            tvClassDate = itemView.findViewById(R.id.tv_class_date);
            tvAssignedInstructor = itemView.findViewById(R.id.tv_assigned_instructor);
            tvCourseInfo = itemView.findViewById(R.id.tv_course_info);
            tvClassDayOfWeek = itemView.findViewById(R.id.tv_class_day_of_week);
            tvCapacity = itemView.findViewById(R.id.tv_capacity);
            spinnerStatus = itemView.findViewById(R.id.spinner_status);
            tvComments = itemView.findViewById(R.id.tv_comments);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);

            // Set a click listener on the entire item view.
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        /**
         * Binds the data from a {@link ClassWithCourseInfo} object to the views in the ViewHolder.
         *
         * @param classWithCourseInfo The data object to bind.
         * @param statusChangeListener The listener for status changes.
         */
        public void bind(ClassWithCourseInfo classWithCourseInfo, OnStatusChangeListener statusChangeListener) {
            tvClassDate.setText(classWithCourseInfo.yogaClass.getDate());
            tvAssignedInstructor.setText(classWithCourseInfo.yogaClass.getAssignedInstructor());
            tvCourseInfo.setText(classWithCourseInfo.yogaCourse.getClassType());
            tvClassDayOfWeek.setText(classWithCourseInfo.yogaCourse.getDayOfWeek());
            tvCapacity.setText(String.valueOf(classWithCourseInfo.yogaClass.getActualCapacity()));

            // Show or hide the comments view based on whether comments exist.
            if (classWithCourseInfo.yogaClass.getAdditionalComments() != null && !classWithCourseInfo.yogaClass.getAdditionalComments().isEmpty()) {
                tvComments.setText(classWithCourseInfo.yogaClass.getAdditionalComments());
                tvComments.setVisibility(View.VISIBLE);
            } else {
                tvComments.setVisibility(View.GONE);
            }
            tvCreatedDate.setText(classWithCourseInfo.yogaClass.getFormattedCreatedDate());

            // Setup the status spinner with its adapter and listeners.
            setupStatusSpinner(classWithCourseInfo, statusChangeListener);
        }

        /**
         * Sets up the status spinner, including its adapter, initial selection, and item selection listener.
         *
         * @param classWithCourseInfo The data object for the current item.
         * @param statusChangeListener The listener to be notified of status changes.
         */
        private void setupStatusSpinner(ClassWithCourseInfo classWithCourseInfo, OnStatusChangeListener statusChangeListener) {
            String[] statuses = context.getResources().getStringArray(R.array.class_status_array);
            // Custom ArrayAdapter to style the spinner's selected item view.
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, statuses) {
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView textView = (TextView) view;
                    textView.setTextColor(ContextCompat.getColor(context, R.color.white));
                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                    textView.setGravity(Gravity.CENTER);
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView textView = (TextView) view;
                    textView.setTextColor(ContextCompat.getColor(context, R.color.primary_text));
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(adapter);

            // Set the initial selection of the spinner based on the class's current status.
            int currentStatusPosition = Arrays.asList(statuses).indexOf(classWithCourseInfo.yogaClass.getStatus());
            if (currentStatusPosition >= 0) {
                spinnerStatus.setSelection(currentStatusPosition, false); // false to prevent firing listener on setup
                updateSpinnerBackground(classWithCourseInfo.yogaClass.getStatus());
            }

            spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String newStatus = (String) parent.getItemAtPosition(position);
                    updateSpinnerBackground(newStatus);

                    // Notify the listener only if the status has actually changed.
                    if (!newStatus.equals(classWithCourseInfo.yogaClass.getStatus())) {
                        if (statusChangeListener != null) {
                            statusChangeListener.onStatusChanged(classWithCourseInfo.yogaClass, newStatus);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        /**
         * Updates the background drawable of the spinner based on the selected status.
         *
         * @param status The status string ("Active", "Completed", "Cancelled").
         */
        private void updateSpinnerBackground(String status) {
            int drawableId;
            switch (status) {
                case "Active":
                    drawableId = R.drawable.status_active_background;
                    break;
                case "Completed":
                    drawableId = R.drawable.status_completed_background;
                    break;
                case "Cancelled":
                    drawableId = R.drawable.status_cancelled_background;
                    break;
                default:
                    drawableId = R.drawable.details_card_background; // A default background
                    break;
            }
            spinnerStatus.setBackground(ContextCompat.getDrawable(context, drawableId));
        }
    }

    /**
     * Interface for receiving click events on items in the RecyclerView.
     */
    public interface OnItemClickListener {
        void onItemClick(ClassWithCourseInfo classWithCourseInfo);
    }

    /**
     * Sets the listener for item click events.
     *
     * @param listener The listener to set.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Interface for receiving status change events from the spinner in an item view.
     */
    public interface OnStatusChangeListener {
        void onStatusChanged(YogaClass yogaClass, String newStatus);
    }

    /**
     * Sets the listener for status change events.
     *
     * @param listener The listener to set.
     */
    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        this.statusChangeListener = listener;
    }
}


