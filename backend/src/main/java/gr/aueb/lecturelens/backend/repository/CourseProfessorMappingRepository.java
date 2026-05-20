package gr.aueb.lecturelens.backend.repository;

import gr.aueb.lecturelens.backend.model.CourseProfessorMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseProfessorMappingRepository extends MongoRepository<CourseProfessorMapping, String> {
    List<CourseProfessorMapping> findByProfessorId(String professorId);
}
