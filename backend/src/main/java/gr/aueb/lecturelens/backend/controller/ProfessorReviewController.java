package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.ProfessorReview;
import gr.aueb.lecturelens.backend.repository.ProfessorReviewRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/professor-reviews")
public class ProfessorReviewController {

    @Autowired
    private ProfessorReviewRepository profReviewRepository;

    @GetMapping
    public List<ProfessorReview> getAllReviews() {
        return profReviewRepository.findAll();
    }

    @GetMapping("/{professorId}")
    public List<ProfessorReview> getReviewsByProfessor(@PathVariable String professorId) {
        try {
            System.out.println("DEBUG: Fetching reviews for professor: " + professorId);
            List<ProfessorReview> reviews = profReviewRepository.findByProfessorId(professorId);
            System.out.println("DEBUG: Found " + (reviews != null ? reviews.size() : "0") + " reviews.");
            return reviews;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping
    public ProfessorReview createReview(@RequestBody ProfessorReview review) {
        review.setCreatedAt(Instant.now());
        return profReviewRepository.save(review);
    }
}