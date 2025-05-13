package tn.esprit.javafx.database;
import tn.esprit.javafx.model.Reclamation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationDAO {

    public static List<Reclamation> getAllReclamations() {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.*, u.email FROM reclamation r JOIN user u ON r.user_id = u.id";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int userId = resultSet.getInt("user_id");
                String title = resultSet.getString("title");
                String email = resultSet.getString("email");
                String description = resultSet.getString("description");
                Timestamp date = resultSet.getTimestamp("date");
                String status = resultSet.getString("status");

                reclamations.add(new Reclamation(id, userId, title, email, description, date, status));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reclamations;
    }

    public static List<Reclamation> getReclamationsByUserId(int userId) {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.*, u.email FROM reclamation r JOIN user u ON r.user_id = u.id WHERE r.user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String email = resultSet.getString("email");
                String description = resultSet.getString("description");
                Timestamp date = resultSet.getTimestamp("date");
                String status = resultSet.getString("status");

                reclamations.add(new Reclamation(id, userId, title, email, description, date, status));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reclamations;
    }

    public static Reclamation getReclamationById(int id) {
        String query = "SELECT r.*, u.email FROM reclamation r JOIN user u ON r.user_id = u.id WHERE r.id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String title = resultSet.getString("title");
                String email = resultSet.getString("email");
                String description = resultSet.getString("description");
                Timestamp date = resultSet.getTimestamp("date");
                String status = resultSet.getString("status");

                return new Reclamation(id, userId, title, email, description, date, status);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean addReclamation(Reclamation reclamation) {
        String query = "INSERT INTO reclamation (user_id, title, description, date, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, reclamation.getUserId());
            preparedStatement.setString(2, reclamation.getTitle());
            preparedStatement.setString(3, reclamation.getDescription());
            preparedStatement.setTimestamp(4, new Timestamp(reclamation.getDate().getTime()));
            preparedStatement.setString(5, reclamation.getStatus());

            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    reclamation.setId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteReclamation(int id) {
        String query = "DELETE FROM reclamation WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateReclamation(Reclamation reclamation) {
        String query = "UPDATE reclamation SET user_id = ?, title = ?, description = ?, date = ?, status = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, reclamation.getUserId());
            preparedStatement.setString(2, reclamation.getTitle());
            preparedStatement.setString(3, reclamation.getDescription());
            preparedStatement.setTimestamp(4, new Timestamp(reclamation.getDate().getTime()));
            preparedStatement.setString(5, reclamation.getStatus());
            preparedStatement.setInt(6, reclamation.getId());
            
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateReclamationStatus(int id, String status) {
        String query = "UPDATE reclamation SET status = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, id);
            
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Reclamation> getReclamationsByEmail(String email) {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.* FROM reclamation r JOIN user u ON r.user_id = u.id WHERE u.email = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int userId = resultSet.getInt("user_id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                Timestamp date = resultSet.getTimestamp("date");
                String status = resultSet.getString("status");

                reclamations.add(new Reclamation(id, userId, title, email, description, date, status));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reclamations;
    }
}