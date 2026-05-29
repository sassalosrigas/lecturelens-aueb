package gr.aueb.lecturelens.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String courseId;
    private String professorId;
    private String username;

    private String courseTitle;
    private double rating;
    private int difficulty;
    private double studyHours;
    private String reviewText;
    @JsonProperty("isAnonymous")
    private boolean isAnonymous;
    @CreatedDate
    private Instant createdAt;
    public Review() {}

    public Review(String courseId, String username, String courseTitle, int difficulty, float studyHours, String reviewText, boolean isAnonymous) {
        this.courseId = courseId;
        this.username = username;
        this.courseTitle = courseTitle;
        this.difficulty = difficulty;
        this.studyHours = studyHours;
        this.reviewText = reviewText;
        this.isAnonymous = isAnonymous;
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }

    public String getCourseTitle(){
        return this.courseTitle;
    }

    public void setCourseTitle(String title){
        this.courseTitle = title;
    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public double getStudyHours() { return studyHours; }
    public void setStudyHours(double studyHours) { this.studyHours = studyHours; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}