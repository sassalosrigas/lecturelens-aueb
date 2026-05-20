package gr.aueb.lecturelens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "course_professor_mappings")
public class CourseProfessorMapping {
    @Id
    private String id;
    private String courseId;
    private String professorId;

    public CourseProfessorMapping() {}

    public CourseProfessorMapping(String courseId, String professorId) {
        this.courseId = courseId;
        this.professorId = professorId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }
}
