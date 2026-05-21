package gr.aueb.lecturelens.backend.model;

import java.util.List;

public class ProfessorSearchResult {
    public Professor professor;
    public List<Course> courses;

    public ProfessorSearchResult(Professor professor, List<Course> courses) {
        this.professor = professor;
        this.courses = courses;
    }
}