package gr.aueb.lecturelens.java;

import java.io.Serializable;

public class Professor implements Serializable {
    private String id;
    //private String fullName; // Single variable for the complete name string
    private String firstName, lastName;
    private String title;
    private double rating;

    // Updated constructor to take the pre-combined full name
    public Professor(String id, String firstName, String lastName, String title, double rating) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.rating = rating;
    }

    public String getId() { return id; }
    public String getFullName() { return this.firstName + " " + this.lastName; } // Returns the full name cleanly
    public String getTitle() { return title; }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
