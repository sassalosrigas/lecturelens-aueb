package gr.aueb.lecturelens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reviews")
public class Review {
    @Id
    private String id;

    private String userId;     // who wrote it
    private String courseId;   // which course
    private int difficulty;    // 1-5
    private float hours;       // from slider
    private String reviewText;

    public Review() {}

    public Review(String userId, String courseId, int difficulty, float hours, String reviewText) {
        this.userId = userId;
        this.courseId = courseId;
        this.difficulty = difficulty;
        this.hours = hours;
        this.reviewText = reviewText;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    public float getHours() { return hours; }
    public void setHours(float hours) { this.hours = hours; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
}