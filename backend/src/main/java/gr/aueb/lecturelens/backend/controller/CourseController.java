package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Course;
import gr.aueb.lecturelens.backend.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    // This handles the GET request from your Android MainActivity
    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
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