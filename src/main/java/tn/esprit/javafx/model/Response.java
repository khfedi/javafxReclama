package tn.esprit.javafx.model;

import java.util.Date;

public class Response {
    private int id;
    private int reclamationId;
    private String message;
    private Date date;

    public Response(int id, int reclamationId, String message, Date date) {
        this.id = id;
        this.reclamationId = reclamationId;
        this.message = message;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReclamationId() {
        return reclamationId;
    }

    public void setReclamationId(int reclamationId) {
        this.reclamationId = reclamationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Response{" +
                "id=" + id +
                ", reclamationId=" + reclamationId +
                ", message='" + message + '\'' +
                ", date=" + date +
                '}';
    }
}
