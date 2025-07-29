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

public class YogaClassAdapter extends ListAdapter<YogaClass, YogaClassAdapter.YogaClassHolder> {

    private final OnClassActionListener actionListener;
    private OnItemClickListener listener;
    private YogaCourse yogaCourse;

    public YogaClassAdapter(OnClassActionListener actionListener) {
        super(DIFF_CALLBACK);
        this.actionListener = actionListener;
    }

    public void setYogaCourse(YogaCourse yogaCourse) {
        this.yogaCourse = yogaCourse;
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<YogaClass> DIFF_CALLBACK = new DiffUtil.ItemCallback<YogaClass>() {
        @Override
        public boolean areItemsTheSame(@NonNull YogaClass oldItem, @NonNull YogaClass newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull YogaClass oldItem, @NonNull YogaClass newItem) {
            return oldItem.getDate().equals(newItem.getDate()) &&
                    oldItem.getStatus().equals(newItem.getStatus()) &&
                    oldItem.getAssignedInstructor().equals(newItem.getAssignedInstructor()) &&
                    oldItem.getCreatedDate() == newItem.getCreatedDate();
        }
    };

    @NonNull
    @Override
    public YogaClassHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_yoga_class, parent, false);
        return new YogaClassHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull YogaClassHolder holder, int position) {
        YogaClass currentClass = getItem(position);
        holder.bind(currentClass, yogaCourse, actionListener);
    }

    public YogaClass getClassAt(int position) {
        return getItem(position);
    }

     class YogaClassHolder extends RecyclerView.ViewHolder {
        private final TextView tvClassDate, tvClassDayOfWeek, tvAssignedInstructor, tvCourseInfo, tvCapacity, tvComments, tvCreatedDate;
        private final Spinner spinnerStatus;
        private final View btnEdit, btnDelete;
        private final Context context;

        public YogaClassHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position));
                    }
                }
            });
        }

        public void bind(YogaClass yogaClass, YogaCourse yogaCourse, OnClassActionListener actionListener) {
            tvClassDate.setText(yogaClass.getDate());
            tvAssignedInstructor.setText("With " + yogaClass.getAssignedInstructor());
            if (yogaCourse != null) {
                tvCourseInfo.setText(String.format(Locale.UK, "%s • %s • %s", yogaCourse.getClassType(), yogaCourse.getTime(), yogaCourse.getFormattedPrice()));
                tvClassDayOfWeek.setText(yogaCourse.getDayOfWeek());
            } else {
                tvCourseInfo.setText("Course Info Placeholder"); // Placeholder
            }
            tvCapacity.setText(yogaClass.getActualCapacity() + " spots");
            tvCreatedDate.setText(yogaClass.getFormattedCreatedDate());

            if (yogaClass.getAdditionalComments() != null && !yogaClass.getAdditionalComments().isEmpty()) {
                tvComments.setText(yogaClass.getAdditionalComments());
                tvComments.setVisibility(View.VISIBLE);
            } else {
                tvComments.setVisibility(View.GONE);
            }

            setupStatusSpinner(yogaClass, actionListener);

            btnEdit.setOnClickListener(v -> actionListener.onEditClass(yogaClass));
            btnDelete.setOnClickListener(v -> actionListener.onDeleteClass(yogaClass));
        }

        private void setupStatusSpinner(YogaClass yogaClass, OnClassActionListener actionListener) {
            // Custom ArrayAdapter to set text color and style
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
                    // Optional: Customize dropdown item appearance
                    textView.setPadding(32, 32, 32, 32);
                    textView.setGravity(Gravity.CENTER_VERTICAL);
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(adapter);

            // Set the initial selection
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

                    // Update text color for the newly selected item view
                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(ContextCompat.getColor(context, android.R.color.white));
                        ((TextView) view).setTypeface(null, Typeface.BOLD);
                        ((TextView) view).setGravity(Gravity.CENTER);
                    }

                    if (!newStatus.equals(yogaClass.getStatus())) {
                        actionListener.onUpdateStatus(yogaClass, newStatus);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        private void updateSpinnerBackground(String status) {
            int backgroundRes;
            switch (status) {
                case "Completed":
                    backgroundRes = R.drawable.status_completed_background;
                    break;
                case "Cancelled":
                    backgroundRes = R.drawable.status_cancelled_background;
                    break;
                default: // Active
                    backgroundRes = R.drawable.status_active_background;
                    break;
            }
            spinnerStatus.setBackground(ContextCompat.getDrawable(context, backgroundRes));
        }
    }

    public interface OnClassActionListener {
        void onEditClass(YogaClass yogaClass);
        void onDeleteClass(YogaClass yogaClass);
        void onUpdateStatus(YogaClass yogaClass, String newStatus);
    }

    public interface OnItemClickListener {
        void onItemClick(YogaClass yogaClass);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}


