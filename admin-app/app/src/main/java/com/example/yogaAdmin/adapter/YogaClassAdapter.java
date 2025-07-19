package com.example.yogaAdmin.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
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
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;

import java.util.Locale;

public class YogaClassAdapter extends ListAdapter<YogaClass, YogaClassAdapter.YogaClassHolder> {

    private final OnClassActionListener actionListener;
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
                    oldItem.getAssignedTeacher().equals(newItem.getAssignedTeacher()) &&
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

    static class YogaClassHolder extends RecyclerView.ViewHolder {
        private final TextView tvClassDate, tvClassDayOfWeek, tvAssignedTeacher, tvCourseInfo, tvCapacity, tvComments, tvCreatedDate;
        private final Spinner spinnerStatus;
        private final View btnEdit, btnDelete;
        private final Context context;

        public YogaClassHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            tvClassDate = itemView.findViewById(R.id.tv_class_date);
            tvClassDayOfWeek = itemView.findViewById(R.id.tv_class_day_of_week);
            tvAssignedTeacher = itemView.findViewById(R.id.tv_assigned_teacher);
            tvCourseInfo = itemView.findViewById(R.id.tv_course_info);
            tvCapacity = itemView.findViewById(R.id.tv_capacity);
            tvComments = itemView.findViewById(R.id.tv_comments);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            spinnerStatus = itemView.findViewById(R.id.spinner_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(YogaClass yogaClass, YogaCourse yogaCourse, OnClassActionListener actionListener) {
            tvClassDate.setText(yogaClass.getDate());
            tvAssignedTeacher.setText("With " + yogaClass.getAssignedTeacher());
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
            String[] statuses = {"Scheduled", "Completed", "Cancelled"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, statuses);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(adapter);

            int currentStatusPosition = adapter.getPosition(yogaClass.getStatus());
            spinnerStatus.setSelection(currentStatusPosition);
            updateSpinnerBackground(yogaClass.getStatus());


            spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String newStatus = (String) parent.getItemAtPosition(position);
                    if (!newStatus.equals(yogaClass.getStatus())) {
                        yogaClass.setStatus(newStatus);
                        actionListener.onUpdateStatus(yogaClass, newStatus);
                        updateSpinnerBackground(newStatus);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        private void updateSpinnerBackground(String status) {
            int colorRes;
            switch (status) {
                case "Completed":
                    colorRes = R.color.status_completed;
                    break;
                case "Cancelled":
                    colorRes = R.color.status_cancelled;
                    break;
                default: // Scheduled
                    colorRes = R.color.status_scheduled;
                    break;
            }
            Drawable background = spinnerStatus.getBackground();
            if (background instanceof StateListDrawable) {
                StateListDrawable stateListDrawable = (StateListDrawable) background;
                Drawable.ConstantState constantState = stateListDrawable.getConstantState();
                if (constantState != null) {
                    Drawable newDrawable = constantState.newDrawable().mutate();
                    if (newDrawable instanceof GradientDrawable) {
                        ((GradientDrawable) newDrawable).setColor(ContextCompat.getColor(context, colorRes));
                        spinnerStatus.setBackground(newDrawable);
                    }
                }
            } else if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(ContextCompat.getColor(context, colorRes));
            }
        }
    }

    public interface OnClassActionListener {
        void onEditClass(YogaClass yogaClass);
        void onDeleteClass(YogaClass yogaClass);
        void onUpdateStatus(YogaClass yogaClass, String newStatus);
    }
}


