package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Course;
import gr.aueb.lecturelens.backend.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    // This handles the GET request from your Android MainActivity
    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // Allows you to add new courses manually via Postman if needed
    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return courseRepository.save(course);
    }
}