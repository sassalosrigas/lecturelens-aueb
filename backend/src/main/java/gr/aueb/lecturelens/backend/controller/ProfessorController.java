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

    @GetMapping("/{id}/courses")
    public List<Course> getCoursesByProfessor(@PathVariable String id) {
        Professor prof = professorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor not found: " + id));

        String fullName = prof.getFirstName() + " " + prof.getLastName();
        return courseRepository.findByProfessorName(fullName);
    }

    @GetMapping("/{id}/details")
    public ProfessorDetails getProfessorDetails(@PathVariable String id) {
        // 1. Find professor by MongoDB ObjectId
        Professor prof = professorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor not found: " + id));

        // 2. The mapping table uses a numeric "professor_id" field (e.g. 1, 2).
        //    Your Professor document has a separate numeric "id" field alongside the MongoDB "_id".
        //    We query the mapping collection using that numeric value.
        //    Use MongoTemplate to query the raw "id" field on Professor documents.
        Query profQuery = new Query(Criteria.where("_id").is(id));
        org.bson.Document rawProf = mongoTemplate.findOne(profQuery, org.bson.Document.class, "professors");

        List<Course> matchingCourses = List.of(); // default empty

        if (rawProf != null && rawProf.containsKey("id")) {
            int numericProfId = rawProf.getInteger("id");

            // 3. Find all mapping entries for this numeric professor id
            List<Integer> mappedCourseIds = mappingRepository.findByProfessorId(numericProfId)
                    .stream()
                    .map(CourseProfessorMapping::getCourseId)
                    .collect(Collectors.toList());

            System.out.println("DEBUG: numericProfId=" + numericProfId + ", mappedCourseIds=" + mappedCourseIds);

            // 4. Fetch courses whose numeric "id" field matches
            //    (Course documents also have a numeric "id" separate from "_id")
            if (!mappedCourseIds.isEmpty()) {
                Query courseQuery = new Query(Criteria.where("id").in(mappedCourseIds));
                matchingCourses = mongoTemplate.find(courseQuery, Course.class, "courses");
            }
        }

        double avgRating = matchingCourses.isEmpty() ? 0.0 : 4.8;
        int totalReviews = matchingCourses.size() * 5;

        return new ProfessorDetails(prof, matchingCourses, avgRating, totalReviews);
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
            List<String> courseIds = mappingRepository.findByProfessorId(prof.getId())
                    .stream().map(CourseProfessorMapping::getCourseId).collect(Collectors.toList());

            List<Course> courses = courseRepository.findAll().stream()
                    .filter(c -> courseIds.contains(c.getId()))
                    .collect(Collectors.toList());

            return new ProfessorSearchResult(prof, courses);
        }).collect(Collectors.toList());
    }
}

