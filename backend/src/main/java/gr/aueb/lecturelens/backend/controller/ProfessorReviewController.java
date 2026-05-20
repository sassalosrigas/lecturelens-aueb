package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Review;
import gr.aueb.lecturelens.backend.repository.ProfessorReviewRepository;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/professor-reviews")
public class ProfessorReviewController {

    @Autowired
    private ProfessorReviewRepository profReviewRepository;

    // 1. ADD THIS GET ENDPOINT TO FIX THE 405 ERROR
    @GetMapping
    public List<Review> getAllReviews() {
        return profReviewRepository.findAll(); // Fetches everything from MongoDB
    }

    @GetMapping("/{professorId}")
    public List<Review> getReviewsByProfessor(@PathVariable String professorId) {
        try {
            System.out.println("DEBUG: Fetching reviews for professor: " + professorId);
            List<Review> reviews = profReviewRepository.findByProfessorId(professorId);
            System.out.println("DEBUG: Found " + (reviews != null ? reviews.size() : "0") + " reviews.");
            return reviews;
        } catch (Exception e) {
            e.printStackTrace(); // This prints the full error to your console
            throw e; // Re-throw to keep the 500 status for your mobile app to see
        }
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        review.setCreatedAt(Instant.now());
        return profReviewRepository.save(review);
    }

}