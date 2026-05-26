package gr.aueb.lecturelens.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "professor-reviews")
public class ProfessorReview {

    @Id
    private String id;

    private String reviewId;
    private String professorId;
    private String professorName;
    private String username;
    private String reviewText;
    private float rating;

    @JsonProperty("isAnonymous")
    private boolean isAnonymous;

    @CreatedDate
    private Instant createdAt;

    public ProfessorReview() {}

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

    public String getProfessorName() { return professorName; }

    public void setProfessorName(String fullName) {
        this.professorName = fullName;
    }
}