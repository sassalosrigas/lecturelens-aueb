package gr.aueb.lecturelens.java;

public class Professor {
    private String id;
    //private String fullName; // Single variable for the complete name string
    private String firstName, lastName;
    private String title;

    // Updated constructor to take the pre-combined full name
    public Professor(String id, String firstName, String lastName, String title) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
    }

    public String getId() { return id; }
    public String getFullName() { return this.firstName + " " + this.lastName; } // Returns the full name cleanly
    public String getTitle() { return title; }
}
