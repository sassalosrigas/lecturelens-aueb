package gr.aueb.lecturelens.backend.repository;

import gr.aueb.lecturelens.backend.model.Professor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorRepository extends MongoRepository<Professor, String> {
    Optional<Professor> findByFirstNameAndLastName(String firstName, String lastName);
}