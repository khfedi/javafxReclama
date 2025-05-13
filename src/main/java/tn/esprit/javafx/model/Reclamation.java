package tn.esprit.javafx.model;
import java.util.Date;

public class Reclamation {
    private int id;
    private int userId;
    private String title;
    private String email;
    private String description;
    private Date date;
    private String status; // 'pending', 'answered', 'rejected'

    public Reclamation(int id, int userId, String title, String email, String description, Date date, String status) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.email = email;
        this.description = description;
        this.date = date;
        this.status = status;
    }

    public Reclamation(int id, String email, String description, Date date) {
        this.id = id;
        this.userId = 1; // Static user ID as per requirement
        this.title = "Reclamation"; // Default title
        this.email = email;
        this.description = description;
        this.date = date;
        this.status = "pending"; // Default status
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusColor() {
        switch (status.toLowerCase()) {
            case "pending":
                return "#f39c12";
            case "answered":
                return "#2ecc71";
            case "rejected":
                return "#e74c3c";
            default:
                return "#3498db";
        }
    }
}
