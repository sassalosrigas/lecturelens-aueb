package gr.aueb.lecturelens.backend.repository;

import gr.aueb.lecturelens.backend.model.ProfessorReview;
import gr.aueb.lecturelens.backend.model.Review;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessorReviewRepository extends MongoRepository<ProfessorReview, String> {
    List<ProfessorReview> findByProfessorId(String professorId);

    Optional<ProfessorReview> findByProfessorIdAndUsername(String professorId, String username);

    List<ProfessorReview> findByUsername(String username);


}