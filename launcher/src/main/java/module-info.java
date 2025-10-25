module com.example.selfupdate.launcher {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;

    opens com.example.selfupdate.launcher to javafx.fxml;
    exports com.example.selfupdate.launcher;
}