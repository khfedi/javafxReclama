package tn.esprit.javafx.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "integration";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Connecting to MySQL server...");
        createDatabaseIfNotExists(connection);
        connection = DriverManager.getConnection(URL + DB_NAME, USER, PASSWORD);
        createTablesIfNotExist(connection);

        return connection;
    }

    private static void createDatabaseIfNotExists(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        String createDbQuery = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
        try {
            statement.executeUpdate(createDbQuery);
            System.out.println("Database " + DB_NAME + " created or already exists.");
        } catch (SQLException e) {
            System.out.println("Error creating the database.");
            e.printStackTrace();
        }
    }

    private static void createTablesIfNotExist(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        String createUserTableQuery = "CREATE TABLE IF NOT EXISTS user (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL)";

        String createReclamationTableQuery = "CREATE TABLE IF NOT EXISTS reclamation (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "title VARCHAR(100) NOT NULL, " +
                "email VARCHAR(255) NOT NULL, " +
                "description TEXT NOT NULL, " +
                "date DATETIME NOT NULL, " +
                "status ENUM('pending', 'answered', 'rejected') NOT NULL DEFAULT 'pending', " +
                "FOREIGN KEY (user_id) REFERENCES user(id))";

        String createResponseTableQuery = "CREATE TABLE IF NOT EXISTS response (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "reclamation_id INT NOT NULL, " +
                "message TEXT NOT NULL, " +
                "date DATETIME NOT NULL, " +
                "FOREIGN KEY (reclamation_id) REFERENCES reclamation(id) ON DELETE CASCADE)";

        try {
            statement.executeUpdate(createUserTableQuery);
            System.out.println("Table 'user' created or already exists.");
            
            statement.executeUpdate(createReclamationTableQuery);
            System.out.println("Table 'reclamation' created or already exists.");
            
            statement.executeUpdate(createResponseTableQuery);
            System.out.println("Table 'response' created or already exists.");

            checkDefaultUser(connection);
            
        } catch (SQLException e) {
            System.out.println("Error creating tables.");
            e.printStackTrace();
        }
    }

    private static void checkDefaultUser(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            
            var resultSet = statement.executeQuery("SELECT COUNT(*) as count FROM user WHERE id = 1");
            
            if (resultSet.next() && resultSet.getInt("count") == 0) {
                String insertDefaultUser = "INSERT INTO user (id, username, email, password) VALUES " +
                        "(1, 'testuser', 'test@example.com', 'password123')";
                statement.executeUpdate(insertDefaultUser);
                System.out.println("Default user created.");
                
                String insertAdminUser = "INSERT INTO user (id, username, email, password) VALUES " +
                        "(2, 'admin', 'admin@example.com', 'admin123')";
                statement.executeUpdate(insertAdminUser);
                System.out.println("Admin user created.");
            }
        } catch (SQLException e) {
            System.out.println("Error checking default user.");
            e.printStackTrace();
        }
    }
}
