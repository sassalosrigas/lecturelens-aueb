package gr.aueb.lecturelens.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "professor-reviews")
public class ProfessorReview {

    @Id
    private String id;

    private String reviewId;       // e.g. "rev_001" — the extra identifier in your DB docs
    private String professorId;    // MongoDB ObjectId string, e.g. "6a0cc5faf87324f52c2fd5d9"
    private String username;
    private String reviewText;
    private float rating;

    @JsonProperty("isAnonymous")
    private boolean isAnonymous;

    private Instant createdAt;

    public ProfessorReview() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}