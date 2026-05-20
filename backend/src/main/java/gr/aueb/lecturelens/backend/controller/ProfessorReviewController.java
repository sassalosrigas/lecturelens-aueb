package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Review;
import gr.aueb.lecturelens.backend.repository.ProfessorReviewRepository;
import gr.aueb.lecturelens.backend.repository.ReviewRepository;
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
        return profReviewRepository.findByProfessorId(professorId);
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        review.setCreatedAt(Instant.now());
        return profReviewRepository.save(review);
    }

}