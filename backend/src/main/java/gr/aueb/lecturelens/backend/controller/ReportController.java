package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Report;
import gr.aueb.lecturelens.backend.model.Review;
import gr.aueb.lecturelens.backend.model.ProfessorReview;
import gr.aueb.lecturelens.backend.repository.ReportRepository;
import gr.aueb.lecturelens.backend.repository.ReviewRepository;
import gr.aueb.lecturelens.backend.repository.ProfessorReviewRepository;
import gr.aueb.lecturelens.backend.repository.CourseRepository;
import gr.aueb.lecturelens.backend.repository.ProfessorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProfessorReviewRepository professorReviewRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody Report report) {
        try {
            if (report.getCreatedAt() == null) {
                report.setCreatedAt(Instant.now());
            }
            report.setStatus("PENDING");
            Report savedReport = reportRepository.save(report);
            System.out.println("New report submitted by: " + report.getReportedBy());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @GetMapping("/pending")
    public List<Report> getPendingReports() {
        return reportRepository.findByStatus("PENDING");
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Report> updateReportStatus(@PathVariable String id, @RequestParam String status) {
        return reportRepository.findById(id)
                .map(report -> {
                    report.setStatus(status);
                    Report updatedReport = reportRepository.save(report);
                    return ResponseEntity.ok(updatedReport);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable String id) {
        return reportRepository.findById(id).map(report -> {
            String reviewId = report.getReviewId();
            String targetId = report.getCourseId();

            if (reviewId != null && !reviewId.isEmpty()) {
                if (targetId != null && reviewRepository.existsById(reviewId)) {
                    reviewRepository.deleteById(reviewId);
                    System.out.println("Deleted course review from collection: " + reviewId);

                    recalculateCourseStats(targetId);
                } else if (professorReviewRepository.existsById(reviewId)) {
                    professorReviewRepository.deleteById(reviewId);
                    System.out.println("Deleted professor review from collection: " + reviewId);

                    recalculateProfessorStats(targetId);
                }
            }

            report.setStatus("DELETED");
            reportRepository.save(report);
            System.out.println("Report ID " + id + " status flipped to DELETED for tracking metrics.");

            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private void recalculateCourseStats(String courseId) {
        List<Review> reviews = reviewRepository.findByCourseId(courseId);
        double avgRating = 0.0;
        double avgDifficulty = 0.0;
        double avgHours = 0.0;

        if (!reviews.isEmpty()) {
            avgRating = Math.round(reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0) * 10.0) / 10.0;
            avgDifficulty = Math.round(reviews.stream().mapToDouble(Review::getDifficulty).average().orElse(0.0) * 10.0) / 10.0;
            avgHours = Math.round(reviews.stream().mapToDouble(Review::getStudyHours).average().orElse(0.0) * 10.0) / 10.0;
        }

        final double finalRating = avgRating;
        final double finalDifficulty = avgDifficulty;
        final double finalHours = avgHours;

        courseRepository.findById(courseId).ifPresent(course -> {
            course.setRating(finalRating);
            course.setDifficulty((int) finalDifficulty);
            course.setHours(finalHours);
            courseRepository.save(course);
        });
    }

    private void recalculateProfessorStats(String professorId) {
        List<ProfessorReview> professorReviews = professorReviewRepository.findByProfessorId(professorId);
        double avgRating = 0.0;

        if (!professorReviews.isEmpty()) {
            avgRating = Math.round(professorReviews.stream().mapToDouble(ProfessorReview::getRating).average().orElse(0.0) * 10.0) / 10.0;
        }

        final double finalRating = avgRating;

        professorRepository.findById(professorId).ifPresent(professor -> {
            professor.setRating(finalRating);
            professorRepository.save(professor);
        });
    }
}