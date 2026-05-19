package gr.aueb.lecturelens.java;

public class Professor {
    private String id;
    private String fullName; // Single variable for the complete name string
    private String title;

    // Updated constructor to take the pre-combined full name
    public Professor(String id, String fullName, String title) {
        this.id = id;
        this.fullName = fullName;
        this.title = title;
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; } // Returns the full name cleanly
    public String getTitle() { return title; }
}
