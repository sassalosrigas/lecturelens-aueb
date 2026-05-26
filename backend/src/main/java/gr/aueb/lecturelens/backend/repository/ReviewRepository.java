package gr.aueb.lecturelens.backend.repository;

import gr.aueb.lecturelens.backend.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByCourseId(String courseId);

    Optional<Review> findByCourseIdAndUsername(String courseId, String username);
    List<Review> findByUsername(String username);
}