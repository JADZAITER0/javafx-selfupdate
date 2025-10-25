module com.dynamic.mainapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.*;



    opens com.dynamic.mainapp to javafx.fxml;
    exports com.dynamic.mainapp;
}