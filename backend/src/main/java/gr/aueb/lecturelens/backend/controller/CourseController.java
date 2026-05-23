package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Course;
import gr.aueb.lecturelens.backend.model.CourseSearchResult;
import gr.aueb.lecturelens.backend.model.Professor;
import gr.aueb.lecturelens.backend.repository.CourseRepository;
import gr.aueb.lecturelens.backend.repository.ProfessorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    // This handles the GET request from your Android MainActivity
    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /*
    @GetMapping("/search")
    public List<Course> searchCourses(@RequestParam String q) {
        String regex = ".*" + q + ".*";
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("code").regex(regex, "i"),
                Criteria.where("title").regex(regex, "i")
        ));
        return mongoTemplate.find(query, Course.class);
    }

     */
    @GetMapping("/search")
    public List<CourseSearchResult> searchCourses(@RequestParam String q) {
        String regex = ".*" + q + ".*";
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("title").regex(regex, "i"),
                Criteria.where("code").regex(regex, "i")
        ));
        List<Course> matchedCourses = mongoTemplate.find(query, Course.class);

        return matchedCourses.stream().map(course -> {
            List<Professor> matchedProfessors = new ArrayList<>();

            // Extract and split names to search for matching professor records
            String profName = course.getProfessorName();
            if (profName != null && !profName.trim().isEmpty()) {
                String[] parts = profName.trim().split("\\s+");
                if (parts.length >= 2) {
                    String firstName = parts[0];
                    String lastName = parts[1]; // Handles basic "First Last" formats

                    professorRepository.findByFirstNameAndLastName(firstName, lastName)
                            .ifPresent(matchedProfessors::add);
                }
            }

            return new CourseSearchResult(course, matchedProfessors);
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable String id) {
        return courseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Allows you to add new courses manually via Postman if needed
    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return courseRepository.save(course);
    }

    @GetMapping("/random")
    public List<Course> getRandomCourses() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.sample(5)
        );
        return mongoTemplate.aggregate(aggregation, "courses", Course.class).getMappedResults();
    }
}