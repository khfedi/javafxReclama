package tn.esprit.javafx.database;

import tn.esprit.javafx.model.Response;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResponseDAO {

    public static List<Response> getAllResponses() {
        List<Response> responses = new ArrayList<>();
        String query = "SELECT * FROM response";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int reclamationId = resultSet.getInt("reclamation_id");
                String message = resultSet.getString("message");
                Timestamp date = resultSet.getTimestamp("date");

                responses.add(new Response(id, reclamationId, message, date));
            }

        } catch (SQLException e) {
            System.out.println("Error getting all responses: " + e.getMessage());
            e.printStackTrace();
        }

        return responses;
    }

    public static List<Response> getResponsesByReclamationId(int reclamationId) {
        List<Response> responses = new ArrayList<>();
        String query = "SELECT * FROM response WHERE reclamation_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, reclamationId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String message = resultSet.getString("message");
                Timestamp date = resultSet.getTimestamp("date");

                responses.add(new Response(id, reclamationId, message, date));
            }

        } catch (SQLException e) {
            System.out.println("Error getting responses by reclamation ID: " + e.getMessage());
            e.printStackTrace();
        }

        return responses;
    }

    public static Response getResponseById(int id) {
        String query = "SELECT * FROM response WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int reclamationId = resultSet.getInt("reclamation_id");
                String message = resultSet.getString("message");
                Timestamp date = resultSet.getTimestamp("date");

                return new Response(id, reclamationId, message, date);
            }

        } catch (SQLException e) {
            System.out.println("Error getting response by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static boolean addResponse(Response response) {
        String query = "INSERT INTO response (reclamation_id, message, date) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, response.getReclamationId());
            preparedStatement.setString(2, response.getMessage());
            preparedStatement.setTimestamp(3, new Timestamp(response.getDate().getTime()));

            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                ReclamationDAO.updateReclamationStatus(response.getReclamationId(), "answered");
                
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    response.setId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error adding response: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateResponse(Response response) {
        String query = "UPDATE response SET reclamation_id = ?, message = ?, date = ? WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, response.getReclamationId());
            preparedStatement.setString(2, response.getMessage());
            preparedStatement.setTimestamp(3, new Timestamp(response.getDate().getTime()));
            preparedStatement.setInt(4, response.getId());
            
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.out.println("Error updating response: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    public static boolean deleteResponse(int id) {
        String query = "DELETE FROM response WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            Response response = getResponseById(id);
            
            preparedStatement.setInt(1, id);
            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0 && response != null) {
                List<Response> otherResponses = getResponsesByReclamationId(response.getReclamationId());
                
                if (otherResponses.isEmpty()) {
                    ReclamationDAO.updateReclamationStatus(response.getReclamationId(), "pending");
                }
                
                return true;
            }
            
        } catch (SQLException e) {
            System.out.println("Error deleting response: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    public static boolean deleteResponsesByReclamationId(int reclamationId) {
        String query = "DELETE FROM response WHERE reclamation_id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, reclamationId);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.out.println("Error deleting responses by reclamation ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}
