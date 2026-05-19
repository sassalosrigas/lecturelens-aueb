package gr.aueb.lecturelens.backend.controller;

import gr.aueb.lecturelens.backend.model.Professor;
import gr.aueb.lecturelens.backend.repository.ProfessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professors")
public class ProfessorController {

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping
    public List<Professor> getAllProfessors() {
        return professorRepository.findAll();
    }

    @GetMapping("/random")
    public List<Professor> getRandomProfessors() {
        // Sample 4 random professors from the collection
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.sample(4));
        return mongoTemplate.aggregate(aggregation, "professors", Professor.class).getMappedResults();
    }
}
