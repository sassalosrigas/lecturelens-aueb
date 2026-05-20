package gr.aueb.lecturelens.backend.model;

import java.util.List;

public class ProfessorDetails {
    private Professor professor;
    private List<Course> courses;
    private double averageRating;
    private int totalReviews;

    public ProfessorDetails() {}

    public ProfessorDetails(Professor professor, List<Course> courses, double averageRating, int totalReviews) {
        this.professor = professor;
        this.courses = courses;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }

    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
}
