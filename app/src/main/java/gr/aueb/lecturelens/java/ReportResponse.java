package gr.aueb.lecturelens.java;

public class ReportResponse {
    private String id;
    private String reviewId;
    private String courseId;
    private String authorUsername;
    private String reportedBy;
    private String reviewText;
    private String status;

    // Getters
    public String getId() { return id; }
    public String getReviewId() { return reviewId; }
    public String getCourseId() { return courseId; }
    public String getAuthorUsername() { return authorUsername; }
    public String getReportedBy() { return reportedBy; }
    public String getReviewText() { return reviewText; }
    public String getStatus() { return status; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public void setStatus(String status) { this.status = status; }
}