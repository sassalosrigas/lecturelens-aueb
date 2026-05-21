package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Review;
import gr.aueb.lecturelens.backend.repository.CourseRepository;
import gr.aueb.lecturelens.backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    private void recalculateCourseStats(String courseId) {
        System.out.println("=== recalculateCourseStats ===");
        System.out.println("courseId: " + courseId);

        List<Review> reviews = reviewRepository.findByCourseId(courseId);
        System.out.println("Reviews found in DB: " + reviews.size());

        double avgRating = 0.0;
        double avgDifficulty = 0.0;
        double avgHours = 0.0;

        if (!reviews.isEmpty()) {
            avgRating = Math.round(
                    reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0) * 10.0) / 10.0;
            avgDifficulty = Math.round(
                    reviews.stream().mapToDouble(Review::getDifficulty).average().orElse(0.0) * 10.0) / 10.0;
            avgHours = Math.round(
                    reviews.stream().mapToDouble(Review::getStudyHours).average().orElse(0.0) * 10.0) / 10.0;
        } else {
            System.out.println("No reviews left for this course. Resetting statistics values to zero.");
        }

        System.out.println("Computed avgRating: " + avgRating);
        System.out.println("Computed avgDifficulty: " + avgDifficulty);
        System.out.println("Computed avgHours: " + avgHours);

        final double finalRating = avgRating;
        final double finalDifficulty = avgDifficulty;
        final double finalHours = avgHours;

        courseRepository.findById(courseId).ifPresent(course -> {
            System.out.println("Updating course: " + course.getTitle());
            course.setRating(finalRating);
            course.setDifficulty((int) finalDifficulty);
            course.setHours(finalHours);
            courseRepository.save(course);
            System.out.println("Course statistics saved successfully.");
        });

        if (!courseRepository.existsById(courseId)) {
            System.out.println("WARNING: No course found for id: " + courseId);
        }
    }
    @GetMapping("/check")
    public ResponseEntity<Review> checkUserReview(
            @RequestParam String courseId,
            @RequestParam String username) {

        Optional<Review> existingReview = reviewRepository.findByCourseIdAndUsername(courseId, username);

        if (existingReview.isPresent()) {
            return ResponseEntity.ok(existingReview.get());
        } else {
            // Not found. Send back a 204 No Content status so Android knows it's a new review
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/user/{username}")
    public List<Review> getReviewsByUsername(@PathVariable String username) {
        return reviewRepository.findByUsername(username);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable String id, @RequestBody Review updated) {
        return doUpdate(id, updated);
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<Review> updateReviewPost(@PathVariable String id, @RequestBody Review updated) {
        return doUpdate(id, updated);
    }

    private ResponseEntity<Review> doUpdate(String id, Review updated) {
        System.out.println("UPDATE called with id: " + id);
        return reviewRepository.findById(id).map(existing -> {
            existing.setRating(updated.getRating());
            existing.setDifficulty(updated.getDifficulty());
            existing.setStudyHours(updated.getStudyHours());
            existing.setReviewText(updated.getReviewText());
            existing.setAnonymous(updated.isAnonymous());
            Review saved = reviewRepository.save(existing);
            recalculateCourseStats(existing.getCourseId()); // ← add
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> {
            System.out.println("No review found for id: " + id);
            return ResponseEntity.notFound().build();
        });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        System.out.println("DELETE called with id: " + id);
        return reviewRepository.findById(id).map(review -> {
            String courseId = review.getCourseId(); // ← save before deleting
            reviewRepository.deleteById(id);
            recalculateCourseStats(courseId);       // ← add
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        Review saved = reviewRepository.save(review);
        recalculateCourseStats(saved.getCourseId()); // ← add
        return saved;
    }

}