package tn.esprit.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.javafx.controller.DashboardController;
import tn.esprit.javafx.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        connectToDatabase();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/javafx/view/dashboard.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("My Reclamation Center");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void connectToDatabase() {
        try {
            Connection connection = DatabaseConnection.getConnection();

            if (connection != null) {
                System.out.println("Successfully connected to the database.");
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}