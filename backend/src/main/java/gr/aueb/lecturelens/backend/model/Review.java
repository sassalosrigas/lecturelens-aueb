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
    private String courseId;     // Links the review to a specific course
    private String username;     // Tracks who left the review
    private int difficulty;      // 1 to 5 from your button selection
    private float studyHours;    // From your Material Slider
    private String reviewText;   // From your EditText
    @JsonProperty("isAnonymous")
    private boolean isAnonymous;
    @CreatedDate
    private Instant createdAt; // Automatically managed timestamp field
    public Review() {}

    public Review(String courseId, String username, int difficulty, float studyHours, String reviewText, boolean isAnonymous) {
        this.courseId = courseId;
        this.username = username;
        this.difficulty = difficulty;
        this.studyHours = studyHours;
        this.reviewText = reviewText;
        this.isAnonymous = isAnonymous;
    }

    // Getters and Setters
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public float getStudyHours() { return studyHours; }
    public void setStudyHours(float studyHours) { this.studyHours = studyHours; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
}