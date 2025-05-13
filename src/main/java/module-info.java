module tn.esprit.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jakarta.mail;

    opens tn.esprit.javafx.controller to javafx.fxml;

    exports tn.esprit.javafx;
}
