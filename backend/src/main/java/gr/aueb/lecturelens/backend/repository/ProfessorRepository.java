package gr.aueb.lecturelens.backend.repository;

import gr.aueb.lecturelens.backend.model.Professor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends MongoRepository<Professor, String> {
}