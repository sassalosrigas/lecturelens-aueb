package gr.aueb.lecturelens.backend.repository;

import gr.aueb.lecturelens.backend.model.ProfessorReview;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfessorReviewRepository extends MongoRepository<ProfessorReview, String> {
    List<ProfessorReview> findByProfessorId(String professorId);
}