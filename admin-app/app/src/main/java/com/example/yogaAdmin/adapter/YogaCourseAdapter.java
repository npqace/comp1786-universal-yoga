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

public class YogaCourseAdapter extends ListAdapter<YogaCourse, YogaCourseAdapter.CourseViewHolder> {

    private OnItemClickListener listener;

    public YogaCourseAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<YogaCourse> DIFF_CALLBACK = new DiffUtil.ItemCallback<YogaCourse>() {
        @Override
        public boolean areItemsTheSame(@NonNull YogaCourse oldItem, @NonNull YogaCourse newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull YogaCourse oldItem, @NonNull YogaCourse newItem) {
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

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_yoga_course, parent, false);
        return new CourseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        YogaCourse currentCourse = getItem(position);
        holder.tvCourseType.setText(String.format("%s (%s)", currentCourse.getClassType(), currentCourse.getDayOfWeek()));
        holder.tvPrice.setText(currentCourse.getFormattedPrice());
        holder.tvDayTime.setText(String.format("%s at %s", currentCourse.getDayOfWeek(), currentCourse.getTime()));
        holder.tvCapacityDuration.setText(String.format("%d people • %s", currentCourse.getCapacity(), currentCourse.getFormattedDuration()));
        holder.tvCreatedDate.setText(currentCourse.getFormattedCreatedDate());

        // Optional fields with conditional visibility
        updateFieldVisibility(holder.tvInstructor, "Instructor: ", currentCourse.getInstructorName());
        updateFieldVisibility(holder.tvRoom, "Room: ", currentCourse.getRoomNumber());
        updateFieldVisibility(holder.tvEquipment, "Equipment: ", currentCourse.getEquipmentNeeded());

//        // Conditional visibility for spinners
        updateSpinnerFieldVisibility(holder.tvDifficulty, "Difficulty: ", currentCourse.getDifficultyLevel(), "All Levels");
        updateSpinnerFieldVisibility(holder.tvAgeGroup, "Age Group: ", currentCourse.getAgeGroup(), "All Ages");

        updateFieldVisibility(holder.tvDescription, "", currentCourse.getDescription());
    }

    private void updateFieldVisibility(TextView textView, String prefix, String value) {
        if (value != null && !value.trim().isEmpty()) {
            textView.setText(prefix + value);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private void updateSpinnerFieldVisibility(TextView textView, String prefix, String value, String defaultValue) {
        if (value != null && !value.trim().isEmpty()) {
            textView.setText(prefix + value);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }


    public YogaCourse getCourseAt(int position) {
        return getItem(position);
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCourseType, tvPrice, tvDayTime, tvCapacityDuration, tvInstructor, tvRoom, tvDifficulty, tvDescription, tvCreatedDate, tvEquipment, tvAgeGroup;
        private Button btnEdit, btnDelete;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
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

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(getItem(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getItem(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(YogaCourse course);
        void onEditClick(YogaCourse course);
        void onDeleteClick(YogaCourse course);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}