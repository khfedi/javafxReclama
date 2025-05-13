package tn.esprit.javafx.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.javafx.model.Reclamation;
import tn.esprit.javafx.model.Response;
import tn.esprit.javafx.service.ReclamationService;
import tn.esprit.javafx.service.ResponseService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public class AdminController {

    private final ReclamationService reclamationService = new ReclamationService();
    private final ResponseService responseService = new ResponseService();

    @FXML
    private Button allReclamationsBtn;

    @FXML
    private Button allResponsesBtn;

    @FXML
    private Button backToDashboardBtn;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private FlowPane reclamationsContainer;

    @FXML
    private Text reclamationDetailsTitle;

    @FXML
    private VBox detailsContainer;

    @FXML
    private VBox responseContainer;

    @FXML
    private TextArea responseField;

    @FXML
    private Button rejectBtn;

    @FXML
    private Button sendResponseBtn;

    private Reclamation selectedReclamation;

    private ObservableList<Reclamation> reclamationsList = FXCollections.observableArrayList();
    private FilteredList<Reclamation> filteredReclamations;

    @FXML
    public void initialize() {
        try {
            reclamationsList = FXCollections.observableArrayList();
            statusFilterComboBox.setItems(FXCollections.observableArrayList(
                    "All", "Pending", "Answered", "Rejected"
            ));
            statusFilterComboBox.setValue("All");
            statusFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                filterReclamationsByStatus();
            });
            responseContainer.setVisible(false);
            responseContainer.setManaged(false);
            loadAllReclamations();
            System.out.println("Loaded " + (reclamationsList != null ? reclamationsList.size() : 0) + " reclamations");
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Initialization Error", "Failed to initialize admin panel: " + ex.getMessage());
        }
    }

    private void filterReclamationsByStatus() {
        String status = statusFilterComboBox.getValue();
        if (status == null || status.equals("All")) {
            filteredReclamations.setPredicate(reclamation -> true);
        } else {
            filteredReclamations.setPredicate(reclamation -> reclamation.getStatus().equalsIgnoreCase(status));
        }
        updateReclamationsView();
    }

    private void updateReclamationsView() {
        reclamationsContainer.getChildren().clear();
        for (Reclamation reclamation : filteredReclamations) {
            VBox card = createReclamationCard(reclamation);
            reclamationsContainer.getChildren().add(card);
        }
    }

    private VBox createReclamationCard(Reclamation reclamation) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(200);
        card.setMaxWidth(200);
        Label titleLabel = new Label(reclamation.getTitle());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);
        Label emailLabel = new Label(reclamation.getEmail());
        emailLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Label dateLabel = new Label(sdf.format(reclamation.getDate()));
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        String statusText = reclamation.getStatus().toUpperCase();
        String statusTextColor;
        String statusBgColor;
        switch (reclamation.getStatus().toLowerCase()) {
            case "pending":
                statusTextColor = "#f39c12";
                statusBgColor = "#f39c1233";
                break;
            case "answered":
                statusTextColor = "#2ecc71";
                statusBgColor = "#2ecc7133";
                break;
            case "rejected":
                statusTextColor = "#e74c3c";
                statusBgColor = "#e74c3c33";
                break;
            default:
                statusTextColor = "#3498db";
                statusBgColor = "#3498db33";
                break;
        }

        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-background-radius: 3; " +
                "-fx-background-color: " + statusBgColor + "; " +
                "-fx-text-fill: " + statusTextColor + "; -fx-padding: 3 8;");

        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #00145b; -fx-text-fill: #fff; -fx-font-size: 12; -fx-padding: 3 8; -fx-background-radius: 3;");
        viewBtn.setOnAction(e -> {
            selectedReclamation = reclamation;
            showReclamationDetails(reclamation);
        });

        card.getChildren().addAll( titleLabel, emailLabel, dateLabel, statusLabel, viewBtn);
        card.setAlignment(Pos.TOP_LEFT);
        card.setOnMouseClicked(e -> {
            selectedReclamation = reclamation;
            showReclamationDetails(reclamation);
            reclamationsContainer.getChildren().forEach(node -> node.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"));
            card.setStyle("-fx-background-color: #e6f3ff; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        });

        return card;
    }

    @FXML
    private void showAllReclamations() {
        loadAllReclamations();
        statusFilterComboBox.setValue("All");
    }

    @FXML
    private void showAllResponses() {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("All Responses");
            dialog.setHeaderText("List of All Responses");
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setPrefWidth(600);
            content.setPrefHeight(500);
            List<Response> responses = responseService.getAllResponses();

            if (responses.isEmpty()) {
                Label noResponses = new Label("No responses found.");
                noResponses.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");
                content.getChildren().add(noResponses);
            } else {
                for (Response response : responses) {
                    Reclamation reclamation = reclamationService.getReclamationById(response.getReclamationId());
                    VBox responseBox = new VBox(5);
                    responseBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5;");
                    HBox headerBox = new HBox();
                    headerBox.setAlignment(Pos.CENTER_LEFT);
                    Label idLabel = new Label("Response #" + response.getId());
                    idLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    String formattedDate = sdf.format(response.getDate());
                    Label dateLabel = new Label(formattedDate);
                    dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
                    headerBox.getChildren().addAll(idLabel, spacer, dateLabel);
                    Label reclamationLabel = new Label("For reclamation: #" + reclamation.getId() + " - " + reclamation.getTitle());
                    reclamationLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #3498db;");
                    Label messageLabel = new Label(response.getMessage());
                    messageLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50;");
                    messageLabel.setWrapText(true);
                    HBox actionsBox = new HBox(10);
                    actionsBox.setAlignment(Pos.CENTER_RIGHT);
                    Button editBtn = new Button("Edit");
                    editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00145b; -fx-border-color: #000; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10;");
                    editBtn.setOnAction(e -> {
                        editResponse(response);
                        dialog.close();
                    });

                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 5 10;");
                    deleteBtn.setOnAction(e -> {
                        if (confirmDeleteResponse(response)) {
                            dialog.close();
                            showAllResponses();
                        }
                    });

                    actionsBox.getChildren().addAll(editBtn, deleteBtn);
                    responseBox.getChildren().addAll(headerBox, reclamationLabel, messageLabel, actionsBox);
                    content.getChildren().add(responseBox);
                }
            }

            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (Exception e) {
            showError("Error", "Could not load responses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editResponse(Response response) {
        try {
            TextInputDialog dialog = new TextInputDialog(response.getMessage());
            dialog.setTitle("Edit Response");
            dialog.setHeaderText("Edit Response #" + response.getId());
            dialog.setContentText("Message:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(message -> {
                if (!message.trim().isEmpty()) {
                    if (responseService.updateResponse(response.getId(), message)) {
                        showInfo("Success", "Response updated successfully");

                        if (selectedReclamation != null && selectedReclamation.getId() == response.getReclamationId()) {
                            showReclamationDetails(selectedReclamation);
                        }
                    } else {
                        showError("Error", "Failed to update response");
                    }
                } else {
                    showError("Error", "Response message cannot be empty");
                }
            });

        } catch (Exception e) {
            showError("Error", "Could not edit response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean confirmDeleteResponse(Response response) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Response #" + response.getId());
        alert.setContentText("Are you sure you want to delete this response?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (responseService.deleteResponse(response.getId())) {
                showInfo("Success", "Response deleted successfully");

                if (selectedReclamation != null && selectedReclamation.getId() == response.getReclamationId()) {
                    showReclamationDetails(selectedReclamation);
                }

                return true;
            } else {
                showError("Error", "Failed to delete response");
            }
        }

        return false;
    }

    @FXML
    private void backToDashboard() {
        try {
            Stage stage = (Stage) backToDashboardBtn.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showError("Error", "Could not return to dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAllReclamations() {
        try {
            List<Reclamation> reclamations = reclamationService.getAllReclamations();
            System.out.println("Retrieved " + reclamations.size() + " reclamations from database");
            for (Reclamation rec : reclamations) {
                System.out.println("Reclamation: ID=" + rec.getId() + ", Title=" + rec.getTitle() + ", Status=" + rec.getStatus());
            }

            javafx.application.Platform.runLater(() -> {
                try {
                    reclamationsList.clear();
                    reclamationsList.addAll(reclamations);
                    filteredReclamations = new FilteredList<>(reclamationsList, p -> true);
                    updateReclamationsView();
                    selectedReclamation = null;
                    resetDetailsView();

                    System.out.println("View updated with " + reclamationsList.size() + " reclamations");
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("UI Update Error", "Error updating view: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            showError("Error", "Could not load reclamations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetDetailsView() {
        detailsContainer.getChildren().clear();
        Label label = new Label("Select a reclamation to view details");
        label.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");
        detailsContainer.getChildren().add(label);
        responseContainer.setVisible(false);
        responseContainer.setManaged(false);
    }

    private void showReclamationDetails(Reclamation reclamation) {
        detailsContainer.getChildren().clear();
        reclamationDetailsTitle.setText("Reclamation Details");
        VBox detailsBox = new VBox(15);
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(reclamation.getTitle());
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        String statusText = reclamation.getStatus().toUpperCase();
        String statusTextColor;
        String statusBgColor;

        switch (reclamation.getStatus().toLowerCase()) {
            case "pending":
                statusTextColor = "#f39c12";
                statusBgColor = "#f39c1233";
                break;
            case "answered":
                statusTextColor = "#2ecc71";
                statusBgColor = "#2ecc7133";
                break;
            case "rejected":
                statusTextColor = "#e74c3c";
                statusBgColor = "#e74c3c33";
                break;
            default:
                statusTextColor = "#3498db";
                statusBgColor = "#3498db33";
                break;
        }

        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 3; " +
                "-fx-background-color: " + statusBgColor + "; " +
                "-fx-text-fill: " + statusTextColor + "; -fx-padding: 5 10;");

        headerBox.getChildren().addAll(titleLabel, spacer, statusLabel);

        VBox userInfoBox = new VBox(5);
        userInfoBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");
        Label emailLabel = new Label(reclamation.getEmail());
        emailLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String formattedDate = sdf.format(reclamation.getDate());
        Label dateLabel = new Label("Submitted on: " + formattedDate);
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        userInfoBox.getChildren().addAll(emailLabel, dateLabel);
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        TextArea descArea = new TextArea(reclamation.getDescription());
        descArea.setStyle("-fx-background-color: #f5f7fa; -fx-border-color: #e0e0e0; -fx-background-radius: 5; -fx-border-radius: 5;");
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefHeight(120);
        detailsBox.getChildren().addAll(headerBox, userInfoBox, descLabel, descArea);
        List<Response> responses = responseService.getResponsesByReclamationId(reclamation.getId());

        if (!responses.isEmpty()) {
            Label responsesLabel = new Label("Responses:");
            responsesLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            responsesLabel.setPadding(new Insets(10, 0, 0, 0));

            detailsBox.getChildren().add(responsesLabel);

            for (Response response : responses) {
                VBox responseBox = new VBox(5);
                responseBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");
                HBox responseHeaderBox = new HBox();
                responseHeaderBox.setAlignment(Pos.CENTER_LEFT);
                Label responseDate = new Label(sdf.format(response.getDate()));
                responseDate.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
                Region responseSpacer = new Region();
                HBox.setHgrow(responseSpacer, Priority.ALWAYS);
                Button editBtn = new Button("Edit");
                editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00145b; -fx-border-color: #000; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 2 8;");
                editBtn.setOnAction(e -> editResponse(response));
                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-background-radius: 5; -fx-padding: 2 8;");
                deleteBtn.setOnAction(e -> confirmDeleteResponse(response));
                responseHeaderBox.getChildren().addAll(responseDate, responseSpacer, editBtn, deleteBtn);
                Label messageLabel = new Label(response.getMessage());
                messageLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50;");
                messageLabel.setWrapText(true);
                responseBox.getChildren().addAll(responseHeaderBox, messageLabel);
                detailsBox.getChildren().add(responseBox);
            }
        }
        detailsContainer.getChildren().add(detailsBox);

        boolean isPending = "pending".equalsIgnoreCase(reclamation.getStatus());
        responseContainer.setVisible(isPending);
        responseContainer.setManaged(isPending);
        responseField.clear();

        if (isPending) {
            boolean aiButtonExists = false;
            for (javafx.scene.Node node : responseContainer.getChildren()) {
                if (node instanceof HBox && node.getId() != null && node.getId().equals("aiButtonBox")) {
                    aiButtonExists = true;
                    break;
                }
            }

            if (!aiButtonExists) {
                Button generateAIBtn = new Button("Generate AI Response");
                generateAIBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
                generateAIBtn.setOnAction(e -> generateAIResponse(reclamation));
                HBox aiButtonBox = new HBox(generateAIBtn);
                aiButtonBox.setId("aiButtonBox");
                aiButtonBox.setAlignment(Pos.CENTER_RIGHT);
                responseContainer.getChildren().add(1, aiButtonBox);
            }
        }
    }

    @FXML
    private void sendResponse() {
        if (selectedReclamation == null) {
            showError("Error", "No reclamation selected");
            return;
        }

        String message = responseField.getText();
        if (message == null || message.trim().isEmpty()) {
            showError("Error", "Response message cannot be empty");
            return;
        }

        try {
            responseField.setDisable(true);
            sendResponseBtn.setDisable(true);
            rejectBtn.setDisable(true);
            sendResponseBtn.setText("Sending...");

            new Thread(() -> {
                try {
                    boolean responseSaved = responseService.addResponse(selectedReclamation.getId(), message);
                    boolean statusUpdated = reclamationService.updateReclamationStatus(selectedReclamation.getId(), "answered");

                    javafx.application.Platform.runLater(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        responseField.setDisable(false);
                        sendResponseBtn.setDisable(false);
                        rejectBtn.setDisable(false);
                        sendResponseBtn.setText("Send Response");

                        if (responseSaved && statusUpdated) {
                            showInfo("Success", "Response sent successfully");

                            loadAllReclamations();

                            Reclamation updatedReclamation = reclamationService.getReclamationById(selectedReclamation.getId());
                            if (updatedReclamation != null) {
                                selectedReclamation = updatedReclamation;
                                showReclamationDetails(updatedReclamation);
                            }

                            responseField.clear();
                        } else {
                            showError("Error", "Failed to save response");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        showError("Error", "Could not send response: " + e.getMessage());
                        responseField.setDisable(false);
                        sendResponseBtn.setDisable(false);
                        rejectBtn.setDisable(false);
                        sendResponseBtn.setText("Send Response");
                    });
                }
            }).start();
        } catch (Exception e) {
            showError("Error", "Could not prepare response: " + e.getMessage());
            e.printStackTrace();
            responseField.setDisable(false);
            sendResponseBtn.setDisable(false);
            rejectBtn.setDisable(false);
            sendResponseBtn.setText("Send Response");
        }
    }

    @FXML
    private void rejectReclamation() {
        if (selectedReclamation == null) {
            showError("Error", "No reclamation selected");
            return;
        }

        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Confirm Rejection");
            dialog.setHeaderText("Reject Reclamation");
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            Label warningLabel = new Label("Are you sure you want to reject this reclamation?");
            warningLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            Label reclamationTitle = new Label("Reclamation: " + selectedReclamation.getTitle());
            reclamationTitle.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50;");
            Label reclamationEmail = new Label("From: " + selectedReclamation.getEmail());
            reclamationEmail.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50;");
            TextArea reasonField = new TextArea();
            reasonField.setPromptText("Enter a reason for rejection (optional)");
            reasonField.setPrefHeight(100);
            reasonField.setWrapText(true);
            reasonField.setStyle("-fx-background-color: #f5f7fa; -fx-border-color: #e0e0e0; -fx-background-radius: 5; -fx-border-radius: 5;");
            content.getChildren().addAll(warningLabel, reclamationTitle, reclamationEmail, reasonField);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            okButton.setText("Reject");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                rejectBtn.setDisable(true);
                sendResponseBtn.setDisable(true);
                responseField.setDisable(true);
                rejectBtn.setText("Rejecting...");
                rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 8 15; -fx-cursor: wait;");
                String reason = reasonField.getText().trim();

                if (!reason.isEmpty()) {
                    responseService.addResponse(selectedReclamation.getId(), "Rejection reason: " + reason);
                }

                if (reclamationService.updateReclamationStatus(selectedReclamation.getId(), "rejected")) {
                    showInfo("Success", "Reclamation rejected successfully");

                    javafx.application.Platform.runLater(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        rejectBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
                        rejectBtn.setText("Reject Reclamation");
                        rejectBtn.setDisable(false);
                        sendResponseBtn.setDisable(false);
                        responseField.setDisable(false);
                        loadAllReclamations();

                        Reclamation updatedReclamation = reclamationService.getReclamationById(selectedReclamation.getId());
                        if (updatedReclamation != null) {
                            selectedReclamation = updatedReclamation;
                            showReclamationDetails(updatedReclamation);
                        }
                    });
                } else {
                    showError("Error", "Failed to reject reclamation");
                    rejectBtn.setDisable(false);
                    sendResponseBtn.setDisable(false);
                    responseField.setDisable(false);
                    rejectBtn.setText("Reject Reclamation");
                    rejectBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
                }
            }
        } catch (Exception e) {
            showError("Error", "Could not reject reclamation: " + e.getMessage());
            e.printStackTrace();
            rejectBtn.setDisable(false);
            sendResponseBtn.setDisable(false);
            responseField.setDisable(false);
            rejectBtn.setText("Reject Reclamation");
            rejectBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
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

    private void generateAIResponse(Reclamation reclamation) {
        if (reclamation == null) {
            showError("Error", "No reclamation selected");
            return;
        }

        String apiKey = "AIzaSyBwukLHQLv7Wz47p5m2ZeLbCJupbADfryg";
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        String prompt = "Response only with small text on this reclamation in desktop application named Plannify for events plannification. " +
                "Be professional, concise and helpful. Here are the details of the reclamation: " +
                "Title: " + reclamation.getTitle() + ", " +
                "Email: " + reclamation.getEmail() + ", " +
                "Description: " + reclamation.getDescription();

        String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"") + "\"}]}]}";

        System.out.println("AI Request Body: " + requestBody);
        responseField.setDisable(true);
        if (sendResponseBtn != null) sendResponseBtn.setDisable(true);
        if (rejectBtn != null) rejectBtn.setDisable(true);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(30, 30);
        Label loadingLabel = new Label("Generating AI response...");
        loadingLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #7f8c8d;");
        HBox loadingBox = new HBox(10, progressIndicator, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);

        int responseFieldIndex = responseContainer.getChildren().indexOf(responseField);
        if (responseFieldIndex >= 0) {
            responseContainer.getChildren().add(responseFieldIndex + 1, loadingBox);
        }

        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(apiUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                try (java.io.OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                if (responseCode == 200) {
                    StringBuilder response = new StringBuilder();
                    try (java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                    }

                    String jsonResponse = response.toString();
                    System.out.println("Gemini API Response: " + jsonResponse);
                    String aiText = extractTextFromJsonResponse(jsonResponse);
                    javafx.application.Platform.runLater(() -> {
                        responseContainer.getChildren().remove(loadingBox);
                        responseField.setText(aiText);
                        responseField.setDisable(false);
                        if (sendResponseBtn != null) sendResponseBtn.setDisable(false);
                        if (rejectBtn != null) rejectBtn.setDisable(false);
                        showInfo("AI Response", "Response generated successfully.");
                    });
                } else {
                    StringBuilder errorResponse = new StringBuilder();
                    try (java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            errorResponse.append(responseLine.trim());
                        }
                    }

                    String errorMessage = "Error: " + responseCode + " - " + errorResponse.toString();
                    System.err.println(errorMessage);
                    javafx.application.Platform.runLater(() -> {
                        responseContainer.getChildren().remove(loadingBox);
                        responseField.setDisable(false);
                        if (sendResponseBtn != null) sendResponseBtn.setDisable(false);
                        if (rejectBtn != null) rejectBtn.setDisable(false);
                        showError("AI Response Error", "Failed to generate AI response. Error: " + responseCode);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    responseContainer.getChildren().remove(loadingBox);
                    responseField.setDisable(false);
                    if (sendResponseBtn != null) sendResponseBtn.setDisable(false);
                    if (rejectBtn != null) rejectBtn.setDisable(false);
                    showError("AI Response Error", "Failed to generate AI response: " + e.getMessage());
                });
            }
        }).start();
    }


    private String extractTextFromJsonResponse(String jsonResponse) {
        try {
            System.out.println("Processing Gemini response: " + jsonResponse);

            if (jsonResponse.contains("\"text\":")) {
                int textIndex = jsonResponse.indexOf("\"text\":");
                if (textIndex != -1) {
                    int startQuote = jsonResponse.indexOf('"', textIndex + 7);

                    if (startQuote != -1) {
                        int endQuote = -1;
                        boolean isEscaped = false;

                        int currentPos = startQuote + 1;
                        StringBuilder result = new StringBuilder();

                        while (currentPos < jsonResponse.length()) {
                            char c = jsonResponse.charAt(currentPos);

                            if (isEscaped) {
                                if (c == 'n') {
                                    result.append('\n');
                                } else if (c == 'r') {
                                } else if (c == '"') {
                                    result.append('"');
                                } else if (c == '\\') {
                                    result.append('\\');
                                } else {
                                    result.append(c);
                                }
                                isEscaped = false;
                            } else if (c == '\\') {
                                isEscaped = true;
                            } else if (c == '"') {
                                endQuote = currentPos;
                                break;
                            } else {
                                result.append(c);
                            }

                            currentPos++;
                        }

                        if (endQuote != -1) {
                            String extractedText = result.toString();
                            System.out.println("Successfully extracted AI text: [" + extractedText + "]");
                            return extractedText;
                        }
                    }
                }
            }

            String rawText = getTextBetween(jsonResponse, "\"text\":\"", "\"");
            if (rawText != null && !rawText.isEmpty()) {
                rawText = rawText.replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\r", "");
                System.out.println("Extracted with direct method: [" + rawText + "]");
                return rawText;
            }

            if (jsonResponse.contains("Gemini API Response")) {
                String responseMarker = "text\":\"";
                int respStart = jsonResponse.indexOf(responseMarker);
                if (respStart >= 0) {
                    respStart += responseMarker.length();
                    int respEnd = jsonResponse.indexOf("\"", respStart);
                    if (respEnd > respStart) {
                        String text = jsonResponse.substring(respStart, respEnd);
                        text = text.replace("\\n", "\n").replace("\\\"", "\"");
                        System.out.println("Extracted from console output: [" + text + "]");
                        return text;
                    }
                }
            }

            String subjectText = getSubjectFromResponse(jsonResponse);
            if (subjectText != null) {
                return subjectText;
            }

            return "We have received your reclamation and will process it promptly.";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception while parsing JSON: " + e.getMessage());
            String directExtract = getDirectText(jsonResponse);
            if (directExtract != null) {
                return directExtract;
            }
            return "We've received your reclamation and will address it promptly.";
        }
    }

    private String getTextBetween(String source, String startMarker, String endMarker) {
        try {
            int start = source.indexOf(startMarker);
            if (start >= 0) {
                start += startMarker.length();
                int end = start;
                boolean isEscaped = false;

                while (end < source.length()) {
                    char c = source.charAt(end);
                    if (isEscaped) {
                        isEscaped = false;
                    } else if (c == '\\') {
                        isEscaped = true;
                    } else if (c == '"' && !isEscaped) {
                        break;
                    }
                    end++;
                }

                if (end > start) {
                    return source.substring(start, end);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getSubjectFromResponse(String jsonResponse) {
        try {
            if (jsonResponse.contains("Subject:") || jsonResponse.contains("**Subject:**")) {
                int start = Math.max(jsonResponse.indexOf("Subject:"), jsonResponse.indexOf("**Subject:**"));
                if (start >= 0) {
                    start = jsonResponse.indexOf(":", start) + 1;
                    int end = -1;

                    int newlinePos = jsonResponse.indexOf("\n", start);
                    int periodPos = jsonResponse.indexOf(".", start);

                    if (newlinePos >= 0 && (periodPos < 0 || newlinePos < periodPos)) {
                        end = newlinePos;
                    } else if (periodPos >= 0) {
                        end = periodPos + 1;
                    }

                    if (end > start) {
                        String subject = jsonResponse.substring(start, end).trim();
                        if (!subject.isEmpty()) {
                            return subject;
                        }
                    }
                }
            }

            int boldStart = jsonResponse.indexOf("**");
            if (boldStart >= 0) {
                boldStart += 2;
                int boldEnd = jsonResponse.indexOf("**", boldStart);
                if (boldEnd > boldStart) {
                    return jsonResponse.substring(boldStart, boldEnd).trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getDirectText(String jsonResponse) {
        try {
            if (jsonResponse.contains("Event Creation Issue")) {
                return "Subject: Event Creation Issue\n\nWe received your reclamation. Please ensure all required fields are completed and valid. If the issue persists, contact support with specific error details.";
            }
            if (jsonResponse.contains("Reclamation Received")) {
                return "Reclamation Received. We will review it and get back to you shortly.";
            }
            if (jsonResponse.contains("Billing")) {
                return "Billing Inquiry Received. We've received your billing question and will contact you shortly.";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}