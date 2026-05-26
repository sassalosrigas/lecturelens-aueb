package gr.aueb.lecturelens.backend.model;

import java.util.List;

public class CourseSearchResult {
    private Course course;
    private List<Professor> professors;

    public CourseSearchResult(Course course, List<Professor> professors) {
        this.course = course;
        this.professors = professors;
    }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public List<Professor> getProfessors() { return professors; }
    public void setProfessors(List<Professor> professors) { this.professors = professors; }
}