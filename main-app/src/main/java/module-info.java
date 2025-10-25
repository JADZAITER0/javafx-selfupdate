module com.dynamic.mainapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.dynamic.mainapp to javafx.fxml;
    exports com.dynamic.mainapp;
}