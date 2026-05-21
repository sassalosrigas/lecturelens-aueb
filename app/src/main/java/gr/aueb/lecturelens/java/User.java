package gr.aueb.lecturelens.java;

public class User {

    private String username;
    private String email;
    private String passwordHash;
    private String role; // 'student' or 'admin'

    private String createdAt;
    // Default Constructor
    public User() {}

    public User(String username, String email, String passwordHash, String role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getCreatedAt(){
        return this.createdAt;
    }
    public void setCreatedAt(String creationDate){
        this.createdAt = createdAt;
    }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getUsername(){
        return username;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
