package gr.aueb.lecturelens.java;

import java.io.Serializable;

public class Course implements Serializable {
    private String id;
    private String code;
    private String title;
    private int semester;
    private int ects;
    private String professorName;
    private double rating;
    private double difficulty;  // ← changed
    private double hours;       // ← changed
    private String description;

    public Course(String id, String code, String title, int semester, int ects,
                  String professorName, double rating, double difficulty, double hours, String description) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.semester = semester;
        this.ects = ects;
        this.professorName = professorName;
        this.rating = rating;
        this.difficulty = difficulty;
        this.hours = hours;
        this.description = description;
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getSemester() { return semester; }
    public int getEcts() { return ects; }
    public String getProfessorName() { return professorName; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public double getDifficulty() { return difficulty; }
    public void setDifficulty(double difficulty) { this.difficulty = difficulty; }
    public double getHours() { return hours; }
    public void setHours(double hours) { this.hours = hours; }
    public String getDescription() { return description; }
}