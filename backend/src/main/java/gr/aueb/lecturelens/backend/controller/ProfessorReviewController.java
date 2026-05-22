package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.ProfessorReview;
import gr.aueb.lecturelens.backend.model.Review;
import gr.aueb.lecturelens.backend.repository.ProfessorRepository;
import gr.aueb.lecturelens.backend.repository.ProfessorReviewRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/professor-reviews")
public class ProfessorReviewController {

    @Autowired
    private ProfessorReviewRepository professorReviewRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping
    public List<ProfessorReview> getAllReviews() {
        return professorReviewRepository.findAll();
    }

    private void recalculateProfessorStats(String professorId) {
        System.out.println("=== recalculateProfessorStats ===");
        System.out.println("ProfessorId: " + professorId);

        List<ProfessorReview> professorReviews = professorReviewRepository.findByProfessorId(professorId);
        System.out.println("Reviews found in DB: " + professorReviews.size());

        double avgRating = 0.0;

        if (!professorReviews.isEmpty()) {
            avgRating = Math.round(
                    professorReviews.stream().mapToDouble(ProfessorReview::getRating).average().orElse(0.0) * 10.0) / 10.0;
        } else {
            System.out.println("No reviews left for this professor. Resetting statistics values to zero.");
        }

        System.out.println("Computed avgRating: " + avgRating);

        final double finalRating = avgRating;

        professorRepository.findById(professorId).ifPresent(professor -> {
            System.out.println("Updating professor: " + professor.getTitle());
            professor.setRating(finalRating);
            professorRepository.save(professor);
            System.out.println("Professor statistics saved successfully.");
        });

        if (!professorRepository.existsById(professorId)) {
            System.out.println("WARNING: No professor found for id: " + professorId);
        }
    }

    @GetMapping("/{professorId}")
    public List<ProfessorReview> getReviewsByProfessor(@PathVariable String professorId) {
        try {
            System.out.println("DEBUG: Fetching reviews for professor: " + professorId);
            List<ProfessorReview> reviews = professorReviewRepository.findByProfessorId(professorId);
            System.out.println("DEBUG: Found " + (reviews != null ? reviews.size() : "0") + " reviews.");
            return reviews;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/check")
    public ResponseEntity<ProfessorReview> checkUserReview(
            @RequestParam String professorId,
            @RequestParam String username) {

        Optional<ProfessorReview> existingReview = professorReviewRepository.findByProfessorIdAndUsername(professorId, username);

        if (existingReview.isPresent()) {
            return ResponseEntity.ok(existingReview.get());
        } else {
            // Not found. Send back a 204 No Content status so Android knows it's a new review
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/user/{username}")
    public List<ProfessorReview> getReviewsByUser(@PathVariable String username) {
        // 1. Fetch all raw reviews submitted by this specific user
        List<ProfessorReview> reviews = professorReviewRepository.findAll().stream()
                .filter(r -> username.equalsIgnoreCase(r.getUsername()))
                .collect(Collectors.toList());

        for (ProfessorReview review : reviews) {
            if (review.getProfessorId() != null) {
                professorRepository.findById(review.getProfessorId()).ifPresent(prof -> {
                    String fullName = prof.getFirstName() + " " + prof.getLastName();
                    review.setReviewText(review.getReviewText());

                    review.setProfessorName(fullName);
                });
            }
        }
        return reviews;
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessorReview> updateReview(@PathVariable String id, @RequestBody ProfessorReview updated) {
        return doUpdate(id, updated);
    }

    @PostMapping
    public ProfessorReview createReview(@RequestBody ProfessorReview professorReview) {
        ProfessorReview saved = professorReviewRepository.save(professorReview);
        recalculateProfessorStats(saved.getProfessorId());
        return saved;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        System.out.println("DELETE called with id: " + id);
        return professorReviewRepository.findById(id).map(professorReview -> {
            String professorId = professorReview.getProfessorId();
            professorReviewRepository.deleteById(id);
            recalculateProfessorStats(professorId);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<ProfessorReview> updateReviewPost(@PathVariable String id, @RequestBody ProfessorReview updated) {
        return doUpdate(id, updated);
    }

    private ResponseEntity<ProfessorReview> doUpdate(String id, ProfessorReview updated) {
        System.out.println("UPDATE called with id: " + id);
        return professorReviewRepository.findById(id).map(existing -> {
            existing.setRating(updated.getRating());
            existing.setReviewText(updated.getReviewText());
            existing.setAnonymous(updated.isAnonymous());
            ProfessorReview saved = professorReviewRepository.save(existing);
            recalculateProfessorStats(existing.getProfessorId());
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> {
            System.out.println("No review found for id: " + id);
            return ResponseEntity.notFound().build();
        });
    }


}