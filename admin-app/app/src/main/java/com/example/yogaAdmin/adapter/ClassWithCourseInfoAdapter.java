package com.example.yogaAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yogaAdmin.R;
import com.example.yogaAdmin.models.ClassWithCourseInfo;

public class ClassWithCourseInfoAdapter extends ListAdapter<ClassWithCourseInfo, ClassWithCourseInfoAdapter.ClassWithCourseInfoViewHolder> {

    private OnItemClickListener listener;

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
            return oldItem.yogaClass.getDate().equals(newItem.yogaClass.getDate()) &&
                    oldItem.yogaClass.getAssignedInstructor().equals(newItem.yogaClass.getAssignedInstructor()) &&
                    oldItem.yogaCourse.getClassType().equals(newItem.yogaCourse.getClassType());
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
        holder.tvClassDate.setText(currentClass.yogaClass.getDate());
        holder.tvAssignedInstructor.setText(currentClass.yogaClass.getAssignedInstructor());
        holder.tvCourseInfo.setText(currentClass.yogaCourse.getClassType());
        holder.tvClassDayOfWeek.setText(currentClass.yogaCourse.getDayOfWeek());
        holder.tvCapacity.setText(String.valueOf(currentClass.yogaClass.getActualCapacity()));
        holder.tvStatus.setText(currentClass.yogaClass.getStatus());
        if (currentClass.yogaClass.getAdditionalComments() != null && !currentClass.yogaClass.getAdditionalComments().isEmpty()) {
            holder.tvComments.setText(currentClass.yogaClass.getAdditionalComments());
            holder.tvComments.setVisibility(View.VISIBLE);
        } else {
            holder.tvComments.setVisibility(View.GONE);
        }
        holder.tvCreatedDate.setText(currentClass.yogaClass.getFormattedCreatedDate());
    }

    public class ClassWithCourseInfoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvClassDate;
        private TextView tvAssignedInstructor;
        private TextView tvCourseInfo;
        private TextView tvClassDayOfWeek;
        private TextView tvCapacity;
        private TextView tvStatus;
        private TextView tvComments;
        private TextView tvCreatedDate;


        public ClassWithCourseInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassDate = itemView.findViewById(R.id.tv_class_date);
            tvAssignedInstructor = itemView.findViewById(R.id.tv_assigned_instructor);
            tvCourseInfo = itemView.findViewById(R.id.tv_course_info);
            tvClassDayOfWeek = itemView.findViewById(R.id.tv_class_day_of_week);
            tvCapacity = itemView.findViewById(R.id.tv_capacity);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvComments = itemView.findViewById(R.id.tv_comments);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ClassWithCourseInfo classWithCourseInfo);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
