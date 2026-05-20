package gr.aueb.lecturelens.java;

import java.io.Serializable;

public class Review implements Serializable {
    private String id;
    private String courseId;
    private String professorId; // ADDED
    private String username;
    private int difficulty;
    private float studyHours;
    private String reviewText;
    private boolean isAnonymous;
    private boolean isCourse; // ADDED
    private String createdAt;
    private float rating;

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

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public float getStudyHours() { return studyHours; }
    public void setStudyHours(float studyHours) { this.studyHours = studyHours; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public boolean isCourse() { return isCourse; }
    public void setCourse(boolean course) { isCourse = course; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}