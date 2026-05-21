package gr.aueb.lecturelens.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "professors")
public class Professor {
    @Id
    private String id;

    @Field("firstName")
    @JsonProperty("firstName")
    private String firstName;

    @Field("lastName")
    @JsonProperty("lastName")
    private String lastName;

    @Field("title")
    @JsonProperty("title")
    private String title;

    @Field("rating")
    @JsonProperty("rating")
    private double rating;

    public Professor() {}

    public Professor(String firstName, String lastName, String title) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void setRating(double finalRating) {
    }
}