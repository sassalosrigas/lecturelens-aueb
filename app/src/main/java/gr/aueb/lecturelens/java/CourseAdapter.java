package gr.aueb.lecturelens.java;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import gr.aueb.lecturelens.R;
import gr.aueb.lecturelens.java.Course;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private final List<Course> courseList;
    private final OnCourseClickListener clickListener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public CourseAdapter(List<Course> courseList, OnCourseClickListener clickListener) {
        this.courseList = courseList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_card, parent, false);
        return new CourseViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.txtCourseTitle.setText(course.getTitle());
        holder.txtCourseRating.setText(course.getRating() == 0 ? "N/A" : String.format("%.1f", course.getRating()));
        holder.txtProfessorName.setText(course.getProfessorName());
        holder.txtCourseMetadata.setText(String.format("%s • Sem %d • %d ECTS", course.getCode(), course.getSemester(), course.getEcts()));
        holder.txtDifficultyChip.setText("Difficulty: " + course.getDifficulty());
        holder.txtHoursChip.setText(course.getHours() + " hrs/wk");
        holder.txtCourseDescription.setText(course.getDescription());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onCourseClick(course);
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView txtCourseTitle, txtCourseRating, txtProfessorName, txtCourseMetadata, txtDifficultyChip, txtHoursChip, txtCourseDescription;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCourseTitle = itemView.findViewById(R.id.courseTitle);
            txtCourseRating = itemView.findViewById(R.id.courseRating);
            txtProfessorName = itemView.findViewById(R.id.professorName);
            txtCourseMetadata = itemView.findViewById(R.id.courseMetadata);
            txtDifficultyChip = itemView.findViewById(R.id.difficultyChip);
            txtHoursChip = itemView.findViewById(R.id.hoursChip);
            txtCourseDescription = itemView.findViewById(R.id.courseDescription);
        }
    }
}