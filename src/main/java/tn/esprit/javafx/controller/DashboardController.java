package tn.esprit.javafx.controller;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import tn.esprit.javafx.model.Reclamation;
import tn.esprit.javafx.model.Response;
import tn.esprit.javafx.service.ReclamationService;
import tn.esprit.javafx.service.ResponseService;
import tn.esprit.javafx.service.UserService;
import tn.esprit.javafx.database.ReclamationDAO;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class DashboardController {
    private final ReclamationService reclamationService = new ReclamationService();
    private final ResponseService responseService = new ResponseService();
    private final UserService userService = new UserService();
    
    @FXML
    private TextField myEmailField;

    private Reclamation reclamationBeingEdited = null;
    
    @FXML
    private Button adminPanelBtn;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private VBox myReclamationsContainer;

    @FXML
    private Text userName;

    @FXML
    private ListView<Label> reclamationListView;

    @FXML
    private VBox allReclamationsView;

    @FXML
    private VBox allReclamationsContainer;

    @FXML
    private VBox myReclamationsView;

    @FXML
    private VBox addReclamationView;

    @FXML
    private TextField emailField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> allReclamationsSortComboBox;

    @FXML
    private ComboBox<String> myReclamationsSortComboBox;

    @FXML
    private Label emailErrorLabel;

    @FXML
    private Label descriptionErrorLabel;

    private enum SortOrder {
        NEWEST_FIRST("Plus récentes d'abord"),
        OLDEST_FIRST("Plus anciennes d'abord");

        private final String displayText;

        SortOrder(String displayText) {
            this.displayText = displayText;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }

    public void initialize() {
        setupSortComboBoxes();

        if (emailErrorLabel != null) {
            emailErrorLabel.setVisible(false);
            emailErrorLabel.setStyle("-fx-text-fill: #e74c3c;");
        }

        if (descriptionErrorLabel != null) {
            descriptionErrorLabel.setVisible(false);
            descriptionErrorLabel.setStyle("-fx-text-fill: #e74c3c;");
        }

        if (emailField != null) {
            emailField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    validateEmail();
                }
            });
        }

        if (descriptionField != null) {
            descriptionField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    validateDescription();
                }
            });
        }
        
        if (adminPanelBtn != null) {
            adminPanelBtn.setOnAction(event -> openAdminPanel());
        }
        loadUserReclamations(1);
    }
    
    private void openAdminPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/javafx/view/admin.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Admin Panel");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error", "Could not open admin panel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSortComboBoxes() {
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
                SortOrder.NEWEST_FIRST.toString(),
                SortOrder.OLDEST_FIRST.toString()
        );

        if (allReclamationsSortComboBox != null) {
            allReclamationsSortComboBox.setItems(sortOptions);
            allReclamationsSortComboBox.setValue(SortOrder.NEWEST_FIRST.toString());

            allReclamationsSortComboBox.setOnAction(event -> {
                if (allReclamationsView.isVisible()) {
                    loadAllReclamations();
                }
            });
        }

        if (myReclamationsSortComboBox != null) {
            myReclamationsSortComboBox.setItems(sortOptions);
            myReclamationsSortComboBox.setValue(SortOrder.NEWEST_FIRST.toString());
            myReclamationsSortComboBox.setOnAction(event -> {
                if (myReclamationsView.isVisible()) {
                    loadMyReclamations();
                }
            });
        }
    }

    private void deleteReclamation(Reclamation reclamation) {
        try {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation de suppression");
            confirmAlert.setHeaderText("Supprimer la réclamation");
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (reclamationService.deleteReclamation(reclamation.getId())) {
                    loadAllReclamations();
                    if (myReclamationsView.isVisible()) {
                        loadMyReclamations();
                    }
                    showInfo("Suppression réussie", "La réclamation a été supprimée avec succès.");
                } else {
                    showError("Erreur de suppression", "Impossible de supprimer la réclamation.");
                }
            }
        } catch (Exception e) {
            showError("Erreur de suppression", "Impossible de supprimer la réclamation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showAllReclamations() {
        allReclamationsView.setVisible(true);
        myReclamationsView.setVisible(false);
        addReclamationView.setVisible(false);
        loadAllReclamations();
    }

    private List<Reclamation> sortAllReclamations(List<Reclamation> reclamations) {
        if (allReclamationsSortComboBox != null && allReclamationsSortComboBox.getValue() != null) {
            String selectedSortOrder = allReclamationsSortComboBox.getValue();

            if (selectedSortOrder.equals(SortOrder.NEWEST_FIRST.toString())) {
                return reclamations.stream()
                        .sorted(Comparator.comparing(Reclamation::getDate).reversed())
                        .collect(Collectors.toList());
            } else if (selectedSortOrder.equals(SortOrder.OLDEST_FIRST.toString())) {
                return reclamations.stream()
                        .sorted(Comparator.comparing(Reclamation::getDate))
                        .collect(Collectors.toList());
            }
        }

        return reclamations.stream()
                .sorted(Comparator.comparing(Reclamation::getDate).reversed())
                .collect(Collectors.toList());
    }

    private List<Reclamation> sortMyReclamations(List<Reclamation> reclamations) {
        if (myReclamationsSortComboBox != null && myReclamationsSortComboBox.getValue() != null) {
            String selectedSortOrder = myReclamationsSortComboBox.getValue();

            if (selectedSortOrder.equals(SortOrder.NEWEST_FIRST.toString())) {
                return reclamations.stream()
                        .sorted(Comparator.comparing(Reclamation::getDate).reversed())
                        .collect(Collectors.toList());
            } else if (selectedSortOrder.equals(SortOrder.OLDEST_FIRST.toString())) {
                return reclamations.stream()
                        .sorted(Comparator.comparing(Reclamation::getDate))
                        .collect(Collectors.toList());
            }
        }

        return reclamations.stream()
                .sorted(Comparator.comparing(Reclamation::getDate).reversed())
                .collect(Collectors.toList());
    }

    public void loadAllReclamations() {
        allReclamationsContainer.getChildren().clear();

        FlowPane cardContainer = new FlowPane();
        cardContainer.setHgap(15);
        cardContainer.setVgap(15);

        try {
            List<Reclamation> sortedReclamations = sortAllReclamations(reclamationService.getAllReclamations());

            for (Reclamation reclamation : sortedReclamations) {
                VBox card = createReclamationCard(reclamation, false);
                cardContainer.getChildren().add(card);
            }

            allReclamationsContainer.getChildren().add(cardContainer);

            if (sortedReclamations.isEmpty()) {
                Label noReclamations = new Label("Aucune réclamation trouvée.");
                noReclamations.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");
                allReclamationsContainer.getChildren().add(noReclamations);
            }
        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les réclamations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showMyReclamations() {
        myReclamationsView.setVisible(true);
        addReclamationView.setVisible(false);
        loadMyReclamations();
    }

    @FXML
    private void showAddReclamationForm() {
        myReclamationsView.setVisible(false);
        addReclamationView.setVisible(true);
        if (emailErrorLabel != null) emailErrorLabel.setVisible(false);
        if (descriptionErrorLabel != null) descriptionErrorLabel.setVisible(false);

        resetFieldStyles();
    }

    private void resetFieldStyles() {
        String normalStyle = "-fx-background-color: #f5f7fa; -fx-border-color: #e0e0e0; -fx-background-radius: 5; -fx-border-radius: 5;";
        if (emailField != null) emailField.setStyle(normalStyle);
        if (descriptionField != null) descriptionField.setStyle(normalStyle);
    }

    @FXML
    private void resetReclamation() {
        emailField.clear();
        descriptionField.clear();
    }

    @FXML
    private void submitReclamation() {
        if (!validateForm()) {
            return;
        }

        try {
            String title = titleField != null ? titleField.getText() : "Reclamation";
            String email = emailField.getText();
            String description = descriptionField.getText();
            Date date = new Date();
            if (reclamationBeingEdited != null) {
                reclamationBeingEdited.setTitle(title);
                reclamationBeingEdited.setEmail(email);
                reclamationBeingEdited.setDescription(description);
                reclamationBeingEdited.setDate(date);
                reclamationService.updateReclamation(reclamationBeingEdited.getId(), title, email, description);
                reclamationBeingEdited = null;

                showInfo("Modification réussie", "La réclamation a été mise à jour avec succès.");
            } else {
                reclamationService.addReclamation(title, email, description);
                showInfo("Ajout réussi", "La réclamation a été ajoutée avec succès.");
            }

            if (titleField != null) titleField.clear();
            emailField.clear();
            descriptionField.clear();
            showMyReclamations();
        } catch (Exception e) {
            showError("Erreur de soumission", "Une erreur est survenue lors de la soumission: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean isEmailValid = validateEmail();
        boolean isDescriptionValid = validateDescription();

        return isEmailValid && isDescriptionValid;
    }

    private boolean validateEmail() {
        String email = emailField.getText();
        String errorStyle = "-fx-background-color: #ffeaea; -fx-border-color: #e74c3c; -fx-background-radius: 5; -fx-border-radius: 5;";
        String normalStyle = "-fx-background-color: #f5f7fa; -fx-border-color: #e0e0e0; -fx-background-radius: 5; -fx-border-radius: 5;";

        if (email == null || email.trim().isEmpty()) {
            emailField.setStyle(errorStyle);
            if (emailErrorLabel != null) {
                emailErrorLabel.setText("L'email ne peut pas être vide");
                emailErrorLabel.setVisible(true);
            } else {
                showError("Validation email", "L'email ne peut pas être vide");
            }
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!email.matches(emailRegex)) {
            emailField.setStyle(errorStyle);
            if (emailErrorLabel != null) {
                emailErrorLabel.setText("Format d'email invalide");
                emailErrorLabel.setVisible(true);
            } else {
                showError("Validation email", "Format d'email invalide");
            }
            return false;
        }

        emailField.setStyle(normalStyle);
        if (emailErrorLabel != null) {
            emailErrorLabel.setVisible(false);
        }
        return true;
    }

    private boolean validateDescription() {
        String description = descriptionField.getText();
        String errorStyle = "-fx-background-color: #ffeaea; -fx-border-color: #e74c3c; -fx-background-radius: 5; -fx-border-radius: 5;";
        String normalStyle = "-fx-background-color: #f5f7fa; -fx-border-color: #e0e0e0; -fx-background-radius: 5; -fx-border-radius: 5;";

        if (description == null || description.trim().isEmpty()) {
            descriptionField.setStyle(errorStyle);
            if (descriptionErrorLabel != null) {
                descriptionErrorLabel.setText("La description ne peut pas être vide");
                descriptionErrorLabel.setVisible(true);
            } else {
                showError("Validation description", "La description ne peut pas être vide");
            }
            return false;
        }

        if (description.trim().length() < 10) {
            descriptionField.setStyle(errorStyle);
            if (descriptionErrorLabel != null) {
                descriptionErrorLabel.setText("La description doit contenir au moins 10 caractères");
                descriptionErrorLabel.setVisible(true);
            } else {
                showError("Validation description", "La description doit contenir au moins 10 caractères");
            }
            return false;
        }

        descriptionField.setStyle(normalStyle);
        if (descriptionErrorLabel != null) {
            descriptionErrorLabel.setVisible(false);
        }
        return true;
    }

    public void loadMyReclamations() {
        loadUserReclamations(1);
    }

    private void loadUserReclamations(int userId) {
        myReclamationsContainer.getChildren().clear();
        
        try {
            List<Reclamation> reclamations = reclamationService.getReclamationsByUser();
            
            if (reclamations.isEmpty()) {
                Label noReclamationsLabel = new Label("Aucune réclamation trouvée.");
                noReclamationsLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16; -fx-font-style: italic;");
                
                myReclamationsContainer.getChildren().add(noReclamationsLabel);
            } else {
                reclamations = sortMyReclamations(reclamations);
                for (Reclamation reclamation : reclamations) {
                    VBox card = createReclamationCard(reclamation, true); // true indicates this is in the My Reclamations view
                    myReclamationsContainer.getChildren().add(card);
                }
            }
            
        } catch (Exception e) {
            showError("Error", "Failed to load reclamations: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String getStatusColor(String status) {
        if (status == null) return "#3498db"; // Default blue color
        
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
    
    private VBox createReclamationCard(Reclamation reclamation, boolean isMyReclamation) {
        VBox card = new VBox(10);
        card.setPrefWidth(670);
        card.setPrefHeight(isMyReclamation ? 220 : 190);
        card.setStyle("-fx-background-color: white; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 15;");

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 15, 0, 0, 8); " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 15; " +
                    "-fx-scale-x: 1.01; " +
                    "-fx-scale-y: 1.01;");
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 15; " +
                    "-fx-scale-x: 1.0; " +
                    "-fx-scale-y: 1.0;");
        });

        HBox statusBar = new HBox();
        statusBar.setPrefHeight(5);
        statusBar.setPrefWidth(Double.MAX_VALUE);
        statusBar.setStyle("-fx-background-color: " + getStatusColor(reclamation.getStatus()) + "; -fx-background-radius: 8 8 0 0;");
        
        HBox titleDateBox = new HBox();
        titleDateBox.setAlignment(Pos.CENTER_LEFT);
        titleDateBox.setSpacing(10);
        
        Label titleLabel = new Label(reclamation.getTitle());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label statusLabel = new Label(reclamation.getStatus().toUpperCase());
        String statusStyle = "-fx-font-size: 12; -fx-font-weight: bold; -fx-background-radius: 3; " +
                             "-fx-background-color: " + getStatusColor(reclamation.getStatus()) + "33; " + // 33 adds transparency
                             "-fx-text-fill: " + getStatusColor(reclamation.getStatus()) + "; -fx-padding: 3 8;"; 
        statusLabel.setStyle(statusStyle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String formattedDate = sdf.format(reclamation.getDate());
        Label dateLabel = new Label(formattedDate);
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        titleDateBox.getChildren().addAll(titleLabel, spacer, statusLabel, dateLabel);
        Label emailLabel = new Label("From: " + reclamation.getEmail());
        emailLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        Label descLabel = new Label(reclamation.getDescription());
        descLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40); // Approx 2 lines
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(10, 0, 0, 0));
        if (isMyReclamation) {
            if ("pending".equalsIgnoreCase(reclamation.getStatus())) {
                Button editBtn = new Button("Edit");
                editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00145b; -fx-border-color: #000; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-size: 14px;");
                editBtn.setOnMouseEntered(e -> {
                    editBtn.setStyle("-fx-background-color: #00145b; -fx-text-fill: #fff; -fx-border-color: #000; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0.0, 0, 1); -fx-cursor: hand; -fx-font-size: 14px;");
                });
                editBtn.setOnMouseExited(e -> {
                    editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00145b; -fx-border-color: #000; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-size: 14px;");
                });
                editBtn.setOnAction(e -> {
                    if (titleField != null) titleField.setText(reclamation.getTitle());
                    emailField.setText(reclamation.getEmail());
                    descriptionField.setText(reclamation.getDescription());
                    reclamationBeingEdited = reclamation;
                    showAddReclamationForm();
                });
                actions.getChildren().add(editBtn);
            }
            
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-size: 14px;");
            deleteBtn.setOnMouseEntered(e -> {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0.0, 0, 1); -fx-cursor: hand; -fx-font-size: 14px;");
            });
            deleteBtn.setOnMouseExited(e -> {
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-size: 14px;");
            });
            deleteBtn.setOnAction(e -> deleteReclamation(reclamation));
            actions.getChildren().add(deleteBtn);
            
            if ("answered".equalsIgnoreCase(reclamation.getStatus())) {
                Button viewResponseBtn = new Button("View Response");
                viewResponseBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2ecc71; -fx-border-color: #2ecc71; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-size: 14px;");
                viewResponseBtn.setOnMouseEntered(e -> {
                    viewResponseBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-border-color: #2ecc71; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0.0, 0, 1); -fx-cursor: hand; -fx-font-size: 14px;");
                });
                viewResponseBtn.setOnMouseExited(e -> {
                    viewResponseBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2ecc71; -fx-border-color: #2ecc71; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-size: 14px;");
                });
                viewResponseBtn.setOnAction(e -> showReclamationResponses(reclamation));
                actions.getChildren().add(viewResponseBtn);
            }
        } else {
            Button detailsBtn = new Button("Details");
            detailsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00145b; -fx-border-color: #000; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-size: 14px;");
            detailsBtn.setOnMouseEntered(e -> {
                detailsBtn.setStyle("-fx-background-color: #00145b; -fx-text-fill: #fff; -fx-border-color: #000; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0.0, 0, 1); -fx-cursor: hand; -fx-font-size: 14px;");
            });
            detailsBtn.setOnMouseExited(e -> {
                detailsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00145b; -fx-border-color: #000; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-size: 14px;");
            });
            detailsBtn.setOnAction(e -> showReclamationDetails(reclamation));
            actions.getChildren().add(detailsBtn);
        }

        card.getChildren().addAll(statusBar, titleDateBox, emailLabel, descLabel, actions);

        return card;
    }

    private void showReclamationResponses(Reclamation reclamation) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Responses for Reclamation #" + reclamation.getId());
        dialog.setHeaderText(reclamation.getTitle());
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label descriptionLabel = new Label("Your Reclamation:");
        descriptionLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label descriptionContent = new Label(reclamation.getDescription());
        descriptionContent.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50; -fx-wrap-text: true;");
        descriptionContent.setWrapText(true);
        descriptionContent.setMaxWidth(450);
        
        content.getChildren().addAll(descriptionLabel, descriptionContent);
        
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        content.getChildren().add(separator);
        
        List<Response> responses = responseService.getResponsesByReclamationId(reclamation.getId());
        
        if (responses.isEmpty()) {
            Label noResponseLabel = new Label("No responses yet.");
            noResponseLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            content.getChildren().add(noResponseLabel);
        } else {
            Label responsesLabel = new Label("Admin Responses:");
            responsesLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            content.getChildren().add(responsesLabel);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            
            for (Response response : responses) {
                VBox responseBox = new VBox(10);
                responseBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
                
                HBox headerBox = new HBox();
                headerBox.setAlignment(Pos.CENTER_LEFT);
                
                Label adminLabel = new Label("Admin Response");
                adminLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #00145b; -fx-font-weight: bold;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                String respFormattedDate = sdf.format(response.getDate());
                Label respDateLabel = new Label(respFormattedDate);
                respDateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
                
                headerBox.getChildren().addAll(adminLabel, spacer, respDateLabel);
                
                Label messageLabel = new Label(response.getMessage());
                messageLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50;");
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(450);
                
                responseBox.getChildren().addAll(headerBox, messageLabel);
                content.getChildren().add(responseBox);
            }
        }
        
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().setContent(content);
        
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/tn/esprit/javafx/styles/dialog.css").toExternalForm());
        
        dialog.showAndWait();
    }

    private void showReclamationDetails(Reclamation reclamation) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Reclamation Details");
            dialog.setHeaderText("Details for: " + reclamation.getTitle());
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setPrefWidth(500);
            Label userLabel = new Label("From: " + reclamation.getEmail());
            userLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            String formattedDate = sdf.format(reclamation.getDate());
            Label dateLabel = new Label("Submitted on: " + formattedDate);
            dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
            Label statusLabel = new Label("Status: " + reclamation.getStatus().toUpperCase());
            statusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: " + getStatusColor(reclamation.getStatus()) + "; -fx-font-weight: bold;");
            Label descLabel = new Label("Description:\n" + reclamation.getDescription());
            descLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50;");
            descLabel.setWrapText(true);
            content.getChildren().addAll(userLabel, dateLabel, statusLabel, descLabel);
            content.getChildren().add(new Separator());
            List<Response> responses = responseService.getResponsesByReclamationId(reclamation.getId());
            
            if (!responses.isEmpty()) {
                Label responsesTitle = new Label("Responses:");
                responsesTitle.setStyle("-fx-font-size: 16; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                content.getChildren().add(responsesTitle);
                
                for (Response response : responses) {
                    VBox responseBox = new VBox(10);
                    responseBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
                    
                    HBox respHeaderBox = new HBox();
                    respHeaderBox.setAlignment(Pos.CENTER_LEFT);
                    
                    Label adminLabel = new Label("Admin Response");
                    adminLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #00145b; -fx-font-weight: bold;");
                    
                    Region respSpacer = new Region();
                    HBox.setHgrow(respSpacer, Priority.ALWAYS);
                    
                    String respFormattedDate = sdf.format(response.getDate());
                    Label respDateLabel = new Label(respFormattedDate);
                    respDateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
                    
                    respHeaderBox.getChildren().addAll(adminLabel, respSpacer, respDateLabel);
                    
                    Label messageLabel = new Label(response.getMessage());
                    messageLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50;");
                    messageLabel.setWrapText(true);
                    
                    responseBox.getChildren().addAll(respHeaderBox, messageLabel);
                    content.getChildren().add(responseBox);
                }
            } else {
                Label noResponsesLabel = new Label("No responses yet");
                noResponsesLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                content.getChildren().add(noResponsesLabel);
            }
            
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(new ScrollPane(content));
            dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            showError("Error", "Could not load reclamation details: " + e.getMessage());
            e.printStackTrace();
        }
    }
}