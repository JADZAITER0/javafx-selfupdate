package com.dynamic.mainapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        Label label = new Label("Hello from dynamically loaded Main App!");
        Button btn = new Button("Say hi");
        btn.setOnAction(e -> label.setText("Hi! Updated at: " + System.currentTimeMillis()));

        VBox root = new VBox(10, label, btn);
        root.setStyle("-fx-padding:20");
        Scene scene = new Scene(root, 400, 200);
        stage.setTitle("Main App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
