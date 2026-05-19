package gr.aueb.lecturelens.java;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import gr.aueb.lecturelens.R;
import java.util.List;

public class CourseChipAdapter extends RecyclerView.Adapter<CourseChipAdapter.ChipViewHolder> {

    private final List<Course> courseList;
    private final OnCourseChipClickListener listener;

    public interface OnCourseChipClickListener {
        void onCourseChipClick(Course course);
    }

    public CourseChipAdapter(List<Course> courseList, OnCourseChipClickListener listener) {
        this.courseList = courseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure you match the layout name layout_item_course_chip or item_course_chip
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_chip, parent, false);
        return new ChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.txtTitle.setText(course.getTitle());
        holder.txtCode.setText(course.getCode());
        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onCourseChipClick(course); });
    }

    @Override
    public int getItemCount() { return courseList.size(); }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtCode;
        public ChipViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.courseChipTitle);
            txtCode = itemView.findViewById(R.id.courseChipCode);
        }
    }
}
