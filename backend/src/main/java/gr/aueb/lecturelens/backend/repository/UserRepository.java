package gr.aueb.lecturelens.backend.repository;

import gr.aueb.lecturelens.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
}
