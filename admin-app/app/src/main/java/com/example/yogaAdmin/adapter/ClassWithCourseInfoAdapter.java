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

public class ClassWithCourseInfoAdapter extends ListAdapter<ClassWithCourseInfo, ClassWithCourseInfoAdapter.ClassWithCourseInfoViewHolder> {

    private OnItemClickListener listener;
    private OnStatusChangeListener statusChangeListener;

    public ClassWithCourseInfoAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ClassWithCourseInfo> DIFF_CALLBACK = new DiffUtil.ItemCallback<ClassWithCourseInfo>() {
        @Override
        public boolean areItemsTheSame(@NonNull ClassWithCourseInfo oldItem, @NonNull ClassWithCourseInfo newItem) {
            return oldItem.yogaClass.getId() == newItem.yogaClass.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ClassWithCourseInfo oldItem, @NonNull ClassWithCourseInfo newItem) {
            // More robust check
            return Objects.equals(oldItem, newItem);
        }
    };

    @NonNull
    @Override
    public ClassWithCourseInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seach_result, parent, false);
        return new ClassWithCourseInfoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassWithCourseInfoViewHolder holder, int position) {
        ClassWithCourseInfo currentClass = getItem(position);
        holder.bind(currentClass, statusChangeListener);
    }

    public class ClassWithCourseInfoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvClassDate;
        private final TextView tvAssignedInstructor;
        private final TextView tvCourseInfo;
        private final TextView tvClassDayOfWeek;
        private final TextView tvCapacity;
        private final Spinner spinnerStatus;
        private final TextView tvComments;
        private final TextView tvCreatedDate;
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

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        public void bind(ClassWithCourseInfo classWithCourseInfo, OnStatusChangeListener statusChangeListener) {
            tvClassDate.setText(classWithCourseInfo.yogaClass.getDate());
            tvAssignedInstructor.setText(classWithCourseInfo.yogaClass.getAssignedInstructor());
            tvCourseInfo.setText(classWithCourseInfo.yogaCourse.getClassType());
            tvClassDayOfWeek.setText(classWithCourseInfo.yogaCourse.getDayOfWeek());
            tvCapacity.setText(String.valueOf(classWithCourseInfo.yogaClass.getActualCapacity()));

            if (classWithCourseInfo.yogaClass.getAdditionalComments() != null && !classWithCourseInfo.yogaClass.getAdditionalComments().isEmpty()) {
                tvComments.setText(classWithCourseInfo.yogaClass.getAdditionalComments());
                tvComments.setVisibility(View.VISIBLE);
            } else {
                tvComments.setVisibility(View.GONE);
            }
            tvCreatedDate.setText(classWithCourseInfo.yogaClass.getFormattedCreatedDate());

            setupStatusSpinner(classWithCourseInfo, statusChangeListener);
        }

        private void setupStatusSpinner(ClassWithCourseInfo classWithCourseInfo, OnStatusChangeListener statusChangeListener) {
            String[] statuses = context.getResources().getStringArray(R.array.class_status_array);
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

            // Set initial selection
            int currentStatusPosition = Arrays.asList(statuses).indexOf(classWithCourseInfo.yogaClass.getStatus());
            if (currentStatusPosition >= 0) {
                spinnerStatus.setSelection(currentStatusPosition, false);
                updateSpinnerBackground(classWithCourseInfo.yogaClass.getStatus());
            }

            spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String newStatus = (String) parent.getItemAtPosition(position);
                    updateSpinnerBackground(newStatus);

                    // Avoid triggering on initial setup
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

    public interface OnItemClickListener {
        void onItemClick(ClassWithCourseInfo classWithCourseInfo);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnStatusChangeListener {
        void onStatusChanged(YogaClass yogaClass, String newStatus);
    }

    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        this.statusChangeListener = listener;
    }
}


