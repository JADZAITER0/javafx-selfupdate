package com.example.selfupdate.testjavafxmvci;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.example.selfupdate.testjavafxmvci.utils.database.DatabaseManager;
import com.example.selfupdate.testjavafxmvci.views.ViewManager;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainApp extends Application {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void start(Stage stage) throws Exception {
        LOGGER.debug("################################################");
        LOGGER.debug("Starting AnimePahe Browser Application");
        LOGGER.debug("################################################");
        LOGGER.info("Initializing Database");
        initDatabase();
        LOGGER.info("Database initialized");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/selfupdate/testjavafxmvci/views/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        LOGGER.debug("Main View loaded successfully");
        //scene.setUserAgentStylesheet(getClass().getResource("/com/example/selfupdate/testjavafxmvci/hello.css").toExternalForm());
        stage.setTitle("AnimePahe Browser");
        stage.setScene(scene);
        stage.show();
        ThemeUtil.switchThemeAnimated(scene, ThemeUtil.ThemeMode.DARK);
        scene.getRoot().getStylesheets().add(getClass().getResource("/com/example/selfupdate/testjavafxmvci/sample-theme.css").toExternalForm());
        scene.getRoot().getStylesheets().add(getClass().getResource("/com/example/selfupdate/testjavafxmvci/hello.css").toExternalForm());
        PseudoClass JAD_PURPLE = PseudoClass.getPseudoClass("coral");
        LOGGER.debug("Set Theme Accent to {}", JAD_PURPLE.getPseudoClassName());

        scene.getRoot().pseudoClassStateChanged(JAD_PURPLE, true);
        scene.getWindow().setOnCloseRequest(e ->{
            ViewManager.getInstance().clean();
        });




        var tsk = new Task<Void>(){
            @Override
            protected Void call() throws Exception {
                // Simulate some background work
                Thread.sleep(10000);
                ThemeUtil.switchThemeAnimated(scene, ThemeUtil.ThemeMode.LIGHT);
                return null;
            }
        };
        //new Thread(tsk).start();

    }

    private void initDatabase() {
        String dbPath = System.getProperty("user.home") + "/.anime-pahe/anime.db";
        new java.io.File(dbPath).getParentFile().mkdirs();
        DatabaseManager.init(dbPath);
    }


    public class ThemeUtil {

        public enum ThemeMode {
            LIGHT, DARK
        }

        public static void switchThemeAnimated(Scene scene, ThemeMode mode) {

            // 1. Fade out
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), scene.getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            // 2. After fade out → switch theme → fade in
            fadeOut.setOnFinished(ev -> {
                scene.getStylesheets().clear();

                switch (mode) {
                    case LIGHT -> Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                    case DARK  -> Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                }


                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), scene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            fadeOut.play();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
