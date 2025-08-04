package com.example.yogaAdmin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Typeface;
import android.view.Gravity;
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
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;

import java.util.Locale;

/**
 * RecyclerView adapter for displaying a list of {@link YogaClass} objects.
 * It uses a {@link ListAdapter} with {@link DiffUtil} for efficient list updates.
 * This adapter also handles user actions like editing, deleting, and updating the status of a class.
 */
public class YogaClassAdapter extends ListAdapter<YogaClass, YogaClassAdapter.YogaClassHolder> {

    // Listener for actions performed on a class item (edit, delete, status update).
    private final OnClassActionListener actionListener;
    // Listener for clicks on the entire class item.
    private OnItemClickListener listener;
    // The course associated with the list of classes, used for displaying shared details.
    private YogaCourse yogaCourse;

    /**
     * Constructor for the adapter.
     *
     * @param actionListener A listener to handle actions like edit, delete, and status updates.
     */
    public YogaClassAdapter(OnClassActionListener actionListener) {
        super(DIFF_CALLBACK);
        this.actionListener = actionListener;
    }

    /**
     * Sets the {@link YogaCourse} for the classes being displayed.
     * This is used to show details like the course type, time, and price on each class item.
     *
     * @param yogaCourse The course to associate with the classes.
     */
    public void setYogaCourse(YogaCourse yogaCourse) {
        this.yogaCourse = yogaCourse;
        notifyDataSetChanged(); // Redraw the list to show the new course info.
    }

    /**
     * DiffUtil.ItemCallback for calculating the difference between two {@link YogaClass} objects.
     * This helps the ListAdapter to perform efficient updates.
     */
    private static final DiffUtil.ItemCallback<YogaClass> DIFF_CALLBACK = new DiffUtil.ItemCallback<YogaClass>() {
        @Override
        public boolean areItemsTheSame(@NonNull YogaClass oldItem, @NonNull YogaClass newItem) {
            // Items are the same if their IDs match.
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull YogaClass oldItem, @NonNull YogaClass newItem) {
            // Contents are the same if these specific fields match.
            return oldItem.getDate().equals(newItem.getDate()) &&
                    oldItem.getStatus().equals(newItem.getStatus()) &&
                    oldItem.getAssignedInstructor().equals(newItem.getAssignedInstructor()) &&
                    oldItem.getCreatedDate() == newItem.getCreatedDate();
        }
    };

    /**
     * Called when RecyclerView needs a new {@link YogaClassHolder}.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new YogaClassHolder that holds a View for the item.
     */
    @NonNull
    @Override
    public YogaClassHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_yoga_class, parent, false);
        return new YogaClassHolder(itemView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull YogaClassHolder holder, int position) {
        YogaClass currentClass = getItem(position);
        holder.bind(currentClass, yogaCourse, actionListener);
    }

    /**
     * Returns the {@link YogaClass} at the given position.
     *
     * @param position The position of the item.
     * @return The YogaClass at the specified position.
     */
    public YogaClass getClassAt(int position) {
        return getItem(position);
    }

    /**
     * ViewHolder for the {@link YogaClass} item.
     * Contains references to the UI views and the logic to bind data to them.
     */
     class YogaClassHolder extends RecyclerView.ViewHolder {
        private final TextView tvClassDate, tvClassDayOfWeek, tvAssignedInstructor, tvCourseInfo, tvCapacity, tvComments, tvCreatedDate;
        private final Spinner spinnerStatus;
        private final View btnEdit, btnDelete;
        private final Context context;

        public YogaClassHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            // Initialize all views from the item layout.
            tvClassDate = itemView.findViewById(R.id.tv_class_date);
            tvClassDayOfWeek = itemView.findViewById(R.id.tv_class_day_of_week);
            tvAssignedInstructor = itemView.findViewById(R.id.tv_assigned_instructor);
            tvCourseInfo = itemView.findViewById(R.id.tv_course_info);
            tvCapacity = itemView.findViewById(R.id.tv_capacity);
            tvComments = itemView.findViewById(R.id.tv_comments);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            spinnerStatus = itemView.findViewById(R.id.spinner_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            // Set a click listener for the entire item view.
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        /**
         * Binds the data from a {@link YogaClass} and its {@link YogaCourse} to the views.
         *
         * @param yogaClass The class data to bind.
         * @param yogaCourse The associated course data.
         * @param actionListener The listener for actions.
         */
        public void bind(YogaClass yogaClass, YogaCourse yogaCourse, OnClassActionListener actionListener) {
            tvClassDate.setText(yogaClass.getDate());
            tvAssignedInstructor.setText("With " + yogaClass.getAssignedInstructor());
            if (yogaCourse != null) {
                // Display combined course information if available.
                tvCourseInfo.setText(String.format(Locale.UK, "%s • %s • %s", yogaCourse.getClassType(), yogaCourse.getTime(), yogaCourse.getFormattedPrice()));
                tvClassDayOfWeek.setText(yogaCourse.getDayOfWeek());
            } else {
                tvCourseInfo.setText("Course Info Placeholder"); // Placeholder text if course data is not yet loaded.
            }
            tvCapacity.setText(yogaClass.getActualCapacity() + " spots");
            tvCreatedDate.setText(yogaClass.getFormattedCreatedDate());

            // Show or hide the comments view based on content.
            if (yogaClass.getAdditionalComments() != null && !yogaClass.getAdditionalComments().isEmpty()) {
                tvComments.setText(yogaClass.getAdditionalComments());
                tvComments.setVisibility(View.VISIBLE);
            } else {
                tvComments.setVisibility(View.GONE);
            }

            // Setup the status spinner.
            setupStatusSpinner(yogaClass, actionListener);

            // Set click listeners for the edit and delete buttons.
            btnEdit.setOnClickListener(v -> actionListener.onEditClass(yogaClass));
            btnDelete.setOnClickListener(v -> actionListener.onDeleteClass(yogaClass));
        }

        /**
         * Sets up the status spinner with a custom adapter, initial selection, and listener.
         *
         * @param yogaClass The class data for this item.
         * @param actionListener The listener to notify of status changes.
         */
        private void setupStatusSpinner(YogaClass yogaClass, OnClassActionListener actionListener) {
            // Custom ArrayAdapter to style the spinner's appearance.
            final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, context.getResources().getTextArray(R.array.class_status_array)) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView textView = (TextView) view;
                    textView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                    textView.setTypeface(null, Typeface.BOLD);
                    textView.setGravity(Gravity.CENTER);
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView textView = (TextView) view;
                    textView.setPadding(32, 32, 32, 32);
                    textView.setGravity(Gravity.CENTER_VERTICAL);
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(adapter);

            // Set the initial selection based on the class's current status.
            String currentStatus = yogaClass.getStatus();
            if (currentStatus != null) {
                int spinnerPosition = adapter.getPosition(currentStatus);
                spinnerStatus.setSelection(spinnerPosition);
                updateSpinnerBackground(currentStatus);
            }

            spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String newStatus = (String) parent.getItemAtPosition(position);
                    updateSpinnerBackground(newStatus);

                    // Style the selected item's text view.
                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(ContextCompat.getColor(context, android.R.color.white));
                        ((TextView) view).setTypeface(null, Typeface.BOLD);
                        ((TextView) view).setGravity(Gravity.CENTER);
                    }

                    // Notify the listener only if the status has actually changed.
                    if (!newStatus.equals(yogaClass.getStatus())) {
                        actionListener.onUpdateStatus(yogaClass, newStatus);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        /**
         * Updates the background of the spinner based on the selected status.
         *
         * @param status The current status string.
         */
        private void updateSpinnerBackground(String status) {
            int backgroundRes;
            switch (status) {
                case "Completed":
                    backgroundRes = R.drawable.status_completed_background;
                    break;
                case "Cancelled":
                    backgroundRes = R.drawable.status_cancelled_background;
                    break;
                default: // "Active"
                    backgroundRes = R.drawable.status_active_background;
                    break;
            }
            spinnerStatus.setBackground(ContextCompat.getDrawable(context, backgroundRes));
        }
    }

    /**
     * Interface for handling actions on a class item.
     */
    public interface OnClassActionListener {
        void onEditClass(YogaClass yogaClass);
        void onDeleteClass(YogaClass yogaClass);
        void onUpdateStatus(YogaClass yogaClass, String newStatus);
    }

    /**
     * Interface for handling clicks on a class item.
     */
    public interface OnItemClickListener {
        void onItemClick(YogaClass yogaClass);
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


