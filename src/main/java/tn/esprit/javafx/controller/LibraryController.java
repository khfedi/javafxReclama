package tn.esprit.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class LibraryController implements Initializable {
    @FXML
    private ListView<Label> reclamationListView;

    @FXML
    private Text userName;

    @FXML
    private ImageView userAvatar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (userName != null) {
            userName.setText("Benkhalifa Fedi");
        }
    }

    @FXML
    private void handleCreateShelf() {
        System.out.println("Create shelf button clicked");
    }

    @FXML
    private void handleBrowseBooks() {
        System.out.println("Browse books clicked");
    }

    @FXML
    private void handleFavorites() {
        System.out.println("Favorites clicked");
    }

    @FXML
    private void handleCategories() {
        System.out.println("Categories clicked");
    }

    @FXML
    private void handleBookClick(String bookId) {
        System.out.println("Book clicked: " + bookId);
    }
}