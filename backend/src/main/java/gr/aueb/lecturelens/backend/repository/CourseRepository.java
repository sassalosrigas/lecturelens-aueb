package gr.aueb.lecturelens.backend.repository;

import gr.aueb.lecturelens.backend.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {
    @Query("{ 'professorName': ?0 }")
    List<Course> findByProfessorName(String professorName);
}

