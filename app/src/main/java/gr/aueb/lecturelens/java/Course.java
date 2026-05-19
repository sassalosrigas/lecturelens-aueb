package gr.aueb.lecturelens.java;

public class Course {
    private String id;
    private String code;
    private String title;
    private int semester;
    private int ects;
    private String professorName;
    private double rating;
    private String difficulty;
    private String hours;
    private String description;

    public Course(String id, String code, String title, int semester, int ects,
                  String professorName, double rating, String difficulty, String hours, String description) {
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

    // Getters
    public String getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getSemester() { return semester; }
    public int getEcts() { return ects; }
    public String getProfessorName() { return professorName; }
    public double getRating() { return rating; }
    public String getDifficulty() { return difficulty; }
    public String getHours() { return hours; }
    public String getDescription() { return description; }
}