package gr.aueb.lecturelens.backend.repository;

import gr.aueb.lecturelens.backend.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByCourseId(String courseId);
    List<Review> findByUserId(String userId);
}