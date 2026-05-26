package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.*;
import gr.aueb.lecturelens.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    @GetMapping("/{id}/courses")
    public List<Course> getCoursesByProfessor(@PathVariable String id) {
        Professor prof = professorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor not found: " + id));

        String fullName = prof.getFirstName() + " " + prof.getLastName();
        return courseRepository.findByProfessorName(fullName);
    }


    @GetMapping("/search")
    public List<ProfessorSearchResult> searchProfessors(@RequestParam String q) {
        String regex = ".*" + q + ".*";
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("firstName").regex(regex, "i"),
                Criteria.where("lastName").regex(regex, "i")
        ));
        List<Professor> matchedProfessors = mongoTemplate.find(query, Professor.class);

        return matchedProfessors.stream().map(prof -> {
            List<Course> courses = getCoursesByProfessor(prof.getId());

            return new ProfessorSearchResult(prof, courses);
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Professor getProfessorById(@PathVariable String id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor not found: " + id));
    }
}