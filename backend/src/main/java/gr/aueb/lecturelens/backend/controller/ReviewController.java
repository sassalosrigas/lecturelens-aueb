// backend/controller/ReviewController.java
package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Review;
import gr.aueb.lecturelens.backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    // Submit a new review
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewRepository.save(review));
    }

    // Get all reviews for a course
    @GetMapping("/course/{courseId}")
    public List<Review> getReviewsByCourse(@PathVariable String courseId) {
        return reviewRepository.findByCourseId(courseId);
    }

    // Get all reviews by a user
    @GetMapping("/user/{userId}")
    public List<Review> getReviewsByUser(@PathVariable String userId) {
        return reviewRepository.findByUserId(userId);
    }
}