package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.*;
import gr.aueb.lecturelens.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/professors")
public class ProfessorController {

    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseProfessorMappingRepository mappingRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping
    public List<Professor> getAllProfessors() {
        return professorRepository.findAll();
    }

    @GetMapping("/random")
    public List<Professor> getRandomProfessors() {
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.sample(4));
        return mongoTemplate.aggregate(aggregation, "professors", Professor.class).getMappedResults();
    }

    @GetMapping("/{id}/details")
    public ProfessorDetails getProfessorDetails(@PathVariable String id) {
        Professor prof = professorRepository.findById(id).orElseThrow(() -> new RuntimeException("Professor not found"));

        List<String> courseIds = mappingRepository.findByProfessorId(id).stream()
                .map(CourseProfessorMapping::getCourseId)
                .collect(Collectors.toList());

        List<Course> matchingCourses = courseRepository.findAll().stream()
                .filter(course -> courseIds.contains(course.getId()))
                .collect(Collectors.toList());

        double avgRating = matchingCourses.isEmpty() ? 0.0 : 4.8;
        int totalReviews = matchingCourses.size() * 5;

        return new ProfessorDetails(prof, matchingCourses, avgRating, totalReviews);
    }
}