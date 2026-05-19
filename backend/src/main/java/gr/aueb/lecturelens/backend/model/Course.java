package gr.aueb.lecturelens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "courses")
public class Course {
    @Id
    private String id;
    private String code;
    private String title;
    private int semester;
    private int ects;
    private List<Integer> professorIds; // Matches the mapping array we discussed

    public Course() {}

    public Course(String code, String title, int semester, int ects, List<Integer> professorIds) {
        this.code = code;
        this.title = title;
        this.semester = semester;
        this.ects = ects;
        this.professorIds = professorIds;
    }

    // Getters and Setters
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

    public List<Integer> getProfessorIds() { return professorIds; }
    public void setProfessorIds(List<Integer> professorIds) { this.professorIds = professorIds; }
}