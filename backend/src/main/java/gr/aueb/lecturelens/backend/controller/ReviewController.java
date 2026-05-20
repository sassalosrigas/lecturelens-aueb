package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Review;
import gr.aueb.lecturelens.backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    // 1. ADD THIS GET ENDPOINT TO FIX THE 405 ERROR
    @GetMapping
    public List<Review> getAllReviews() {
        return reviewRepository.findAll(); // Fetches everything from MongoDB
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
            return ResponseEntity.ok(reviewRepository.save(existing));
        }).orElseGet(() -> {
            System.out.println("No review found for id: " + id);
            return ResponseEntity.notFound().build();
        });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        System.out.println("DELETE called with id: " + id);
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // This is likely your existing working endpoint
    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewRepository.save(review);
    }

}