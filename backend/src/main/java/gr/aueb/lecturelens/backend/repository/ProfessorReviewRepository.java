package gr.aueb.lecturelens.backend.repository;

import gr.aueb.lecturelens.backend.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProfessorReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProfessorId(String professorId);
}