package gr.aueb.lecturelens.backend.repository;

import gr.aueb.lecturelens.backend.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    // Helpful if you want to pull reviews for a specific course page later!
    List<Review> findByCourseId(String courseId);
}