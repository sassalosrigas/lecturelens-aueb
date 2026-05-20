package gr.aueb.lecturelens.java;

import java.io.Serializable;

public class Review implements Serializable {
    private String id;
    private String courseId;
    private String username;
    private int difficulty;
    private float studyHours;
    private String reviewText;
    private boolean isAnonymous;
    private String createdAt; // Received from MongoDB as an ISO-8601 String timestamp
    private float rating; // Standard rating for professors

    public Review() {}

    public Review(String id, String courseId, String username, int difficulty, float studyHours, String reviewText, boolean isAnonymous, String createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.username = username;
        this.difficulty = difficulty;
        this.studyHours = studyHours;
        this.reviewText = reviewText;
        this.isAnonymous = isAnonymous;
        this.createdAt = createdAt;
    }

    // New constructor including rating
    public Review(String id, String courseId, String username, int difficulty, float studyHours, float rating, String reviewText, boolean isAnonymous, String createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.username = username;
        this.difficulty = difficulty;
        this.studyHours = studyHours;
        this.rating = rating;
        this.reviewText = reviewText;
        this.isAnonymous = isAnonymous;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
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

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public void setProfessorId(String targetProfessorId) {
    }
}