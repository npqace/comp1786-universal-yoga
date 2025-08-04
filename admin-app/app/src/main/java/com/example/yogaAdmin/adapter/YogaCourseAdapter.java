package com.example.yogaAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.models.YogaCourse;

import java.util.Objects;

/**
 * RecyclerView adapter for displaying a list of {@link YogaCourse} objects.
 * It uses a {@link ListAdapter} with {@link DiffUtil} for efficient list updates.
 * This adapter is responsible for creating and binding the views for each course item.
 */
public class YogaCourseAdapter extends ListAdapter<YogaCourse, YogaCourseAdapter.CourseViewHolder> {

    // Listener for item click events (view, edit, delete).
    private OnItemClickListener listener;

    /**
     * Default constructor for the adapter.
     * Initializes the adapter with the DiffUtil callback for efficient updates.
     */
    public YogaCourseAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * DiffUtil.ItemCallback for calculating the difference between two {@link YogaCourse} objects.
     * This allows the ListAdapter to determine which items have changed, been added, or been removed,
     * leading to efficient UI updates.
     */
    private static final DiffUtil.ItemCallback<YogaCourse> DIFF_CALLBACK = new DiffUtil.ItemCallback<YogaCourse>() {
        @Override
        public boolean areItemsTheSame(@NonNull YogaCourse oldItem, @NonNull YogaCourse newItem) {
            // Two items are considered the same if they have the same ID.
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull YogaCourse oldItem, @NonNull YogaCourse newItem) {
            // The contents are the same if all relevant fields are equal.
            // This relies on the Objects.equals() method for null-safe comparisons.
            return oldItem.getCapacity() == newItem.getCapacity() &&
                    oldItem.getDuration() == newItem.getDuration() &&
                    Double.compare(oldItem.getPrice(), newItem.getPrice()) == 0 &&
                    oldItem.getCreatedDate() == newItem.getCreatedDate() &&
                    Objects.equals(oldItem.getDayOfWeek(), newItem.getDayOfWeek()) &&
                    Objects.equals(oldItem.getTime(), newItem.getTime()) &&
                    Objects.equals(oldItem.getClassType(), newItem.getClassType()) &&
                    Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
                    Objects.equals(oldItem.getInstructorName(), newItem.getInstructorName()) &&
                    Objects.equals(oldItem.getRoomNumber(), newItem.getRoomNumber()) &&
                    Objects.equals(oldItem.getDifficultyLevel(), newItem.getDifficultyLevel()) &&
                    Objects.equals(oldItem.getEquipmentNeeded(), newItem.getEquipmentNeeded()) &&
                    Objects.equals(oldItem.getAgeGroup(), newItem.getAgeGroup());
        }
    };

    /**
     * Called when RecyclerView needs a new {@link CourseViewHolder}.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new CourseViewHolder that holds a View for the course item.
     */
    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_yoga_course, parent, false);
        return new CourseViewHolder(itemView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        YogaCourse currentCourse = getItem(position);
        // Bind the data from the currentCourse object to the views in the holder.
        holder.tvCourseType.setText(String.format("%s (%s)", currentCourse.getClassType(), currentCourse.getDayOfWeek()));
        holder.tvPrice.setText(currentCourse.getFormattedPrice());
        holder.tvDayTime.setText(String.format("%s at %s", currentCourse.getDayOfWeek(), currentCourse.getTime()));
        holder.tvCapacityDuration.setText(String.format("%d people • %s", currentCourse.getCapacity(), currentCourse.getFormattedDuration()));
        holder.tvCreatedDate.setText(currentCourse.getFormattedCreatedDate());

        // Handle visibility of optional fields.
        updateFieldVisibility(holder.tvInstructor, "Instructor: ", currentCourse.getInstructorName());
        updateFieldVisibility(holder.tvRoom, "Room: ", currentCourse.getRoomNumber());
        updateFieldVisibility(holder.tvEquipment, "Equipment: ", currentCourse.getEquipmentNeeded());
        updateSpinnerFieldVisibility(holder.tvDifficulty, "Difficulty: ", currentCourse.getDifficultyLevel(), "All Levels");
        updateSpinnerFieldVisibility(holder.tvAgeGroup, "Age Group: ", currentCourse.getAgeGroup(), "All Ages");
        updateFieldVisibility(holder.tvDescription, "", currentCourse.getDescription());
    }

    /**
     * Helper method to set the text of a TextView and manage its visibility.
     * If the value is null or empty, the TextView is hidden.
     *
     * @param textView The TextView to update.
     * @param prefix A prefix to add to the text (e.g., "Instructor: ").
     * @param value The string value to display.
     */
    private void updateFieldVisibility(TextView textView, String prefix, String value) {
        if (value != null && !value.trim().isEmpty()) {
            textView.setText(prefix + value);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    /**
     * Helper method specifically for fields that come from spinners, to manage visibility.
     *
     * @param textView The TextView to update.
     * @param prefix A prefix for the text.
     * @param value The value from the spinner.
     * @param defaultValue The default value of the spinner (which should not be displayed).
     */
    private void updateSpinnerFieldVisibility(TextView textView, String prefix, String value, String defaultValue) {
        if (value != null && !value.trim().isEmpty()) {
            textView.setText(prefix + value);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    /**
     * Returns the {@link YogaCourse} at the given position.
     *
     * @param position The position of the item.
     * @return The YogaCourse at the specified position.
     */
    public YogaCourse getCourseAt(int position) {
        return getItem(position);
    }

    /**
     * ViewHolder for the {@link YogaCourse} item.
     * Holds references to the UI views and sets up click listeners.
     */
    class CourseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCourseType, tvPrice, tvDayTime, tvCapacityDuration, tvInstructor, tvRoom, tvDifficulty, tvDescription, tvCreatedDate, tvEquipment, tvAgeGroup;
        private Button btnEdit, btnDelete;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize all views from the item layout.
            tvCourseType = itemView.findViewById(R.id.tv_course_type);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvDayTime = itemView.findViewById(R.id.tv_day_time);
            tvCapacityDuration = itemView.findViewById(R.id.tv_capacity_duration);
            tvInstructor = itemView.findViewById(R.id.tv_instructor);
            tvRoom = itemView.findViewById(R.id.tv_room);
            tvDifficulty = itemView.findViewById(R.id.tv_difficulty);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            tvEquipment = itemView.findViewById(R.id.tv_equipment);
            tvAgeGroup = itemView.findViewById(R.id.tv_age_group);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            // Set a click listener for the entire item view.
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });

            // Set a click listener for the edit button.
            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(getItem(position));
                }
            });

            // Set a click listener for the delete button.
            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getItem(position));
                }
            });
        }
    }

    /**
     * Interface for receiving click events on items in the RecyclerView.
     */
    public interface OnItemClickListener {
        void onItemClick(YogaCourse course);
        void onEditClick(YogaCourse course);
        void onDeleteClick(YogaCourse course);
    }

    /**
     * Sets the listener for item click events.
     *
     * @param listener The listener to set.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
