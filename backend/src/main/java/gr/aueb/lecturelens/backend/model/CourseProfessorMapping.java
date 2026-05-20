package gr.aueb.lecturelens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "courses-professors")
public class CourseProfessorMapping {

    @Id
    private String id;

    @Field("course_id")
    private int courseId;

    @Field("professor_id")
    private int professorId;

    public CourseProfessorMapping() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public int getProfessorId() { return professorId; }
    public void setProfessorId(int professorId) { this.professorId = professorId; }
}