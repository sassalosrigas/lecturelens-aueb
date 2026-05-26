package gr.aueb.lecturelens.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "courses")
public class Course {

    @Id
    @JsonProperty("id")
    private String id;

    private String code;
    private String title;
    private int semester;
    private int ects;
    @Field("professorName")
    @JsonProperty("professorName")
    private String professorName;

    private double rating;
    private int difficulty;
    private double hours;
    private String description;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public int getEcts() { return ects; }
    public void setEcts(int ects) { this.ects = ects; }

    public String getProfessorName() { return professorName; }
    public void setProfessorName(String professorName) { this.professorName = professorName; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public double getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public double getHours() { return hours; }
    public void setHours(double hours) { this.hours = hours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}