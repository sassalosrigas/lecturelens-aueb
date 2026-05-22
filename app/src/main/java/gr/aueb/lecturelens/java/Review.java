package gr.aueb.lecturelens.java;

import java.io.Serializable;

public class Review implements Serializable {
    private String id;
    private String courseId;
    private String professorId;
    private String courseTitle;
    private String professorName;
    private String username;
    private int difficulty;
    private float studyHours;
    private String reviewText;
    private boolean isAnonymous;
    private boolean isCourse;
    private String createdAt;
    private float rating;

    public Review() {}

    public Review(String id, String courseId, String username, float rating, String courseTitle,
                  int difficulty, float studyHours, String reviewText,
                  boolean isAnonymous, String createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.username = username;
        this.rating = rating;
        this.courseTitle = courseTitle;
        this.difficulty = difficulty;
        this.studyHours = studyHours;
        this.reviewText = reviewText;
        this.isAnonymous = isAnonymous;
        this.createdAt = createdAt;
        this.isCourse = true;
    }

    public Review(String id, String professorId, String professorName, String username,
                  float rating, String reviewText,
                  boolean isAnonymous, String createdAt) {
        this.id = id;
        this.professorId = professorId;
        this.professorName = professorName;
        this.username = username;
        this.rating = rating;
        this.reviewText = reviewText;
        this.isAnonymous = isAnonymous;
        this.createdAt = createdAt;
        this.isCourse = false;
    }

    // Getters and Setters
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getProfessorName() { return professorName; }
    public void setProfessorName(String professorName) { this.professorName = professorName; }

    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

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

    public boolean isCourse() { return isCourse; }
    public void setCourse(boolean course) { isCourse = course; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}