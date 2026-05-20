package gr.aueb.lecturelens.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "reviews") // Storing both in "reviews" collection, distinguished by isCourse
public class Review {
    @Id
    private String id;
    private String courseId;
    private String professorId;  // ADDED: Links the review to a professor
    private String username;

    private float rating;
    private int difficulty;
    private float studyHours;
    private String reviewText;

    @JsonProperty("isAnonymous")
    private boolean isAnonymous;

    @JsonProperty("isCourse")
    private boolean isCourse;    // ADDED: Differentiates course vs professor reviews

    @CreatedDate
    private Instant createdAt;

    public Review() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public float getStudyHours() { return studyHours; }
    public void setStudyHours(float studyHours) { this.studyHours = studyHours; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public boolean isCourse() { return isCourse; }
    public void setCourse(boolean course) { isCourse = course; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}