package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Review;
import gr.aueb.lecturelens.backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

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

    // This is likely your existing working endpoint
    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewRepository.save(review);
    }

}