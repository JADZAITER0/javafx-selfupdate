package com.example.selfupdate.testjavafxmvci.views.main;

import atlantafx.base.theme.Styles;
import com.example.selfupdate.testjavafxmvci.MainApp;
import com.example.selfupdate.testjavafxmvci.views.ViewManager;
import com.example.selfupdate.testjavafxmvci.views.Views;
import javafx.animation.FadeTransition;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignN;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;
import org.kordamp.ikonli.materialdesign2.MaterialDesignV;


import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {


    private static final Logger LOGGER = LogManager.getLogger();

    private final BaseApplicationModel MODEL = BaseApplicationModel.getInstance();
    private final ViewManager viewManager = ViewManager.getInstance();
    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");




    @FXML
    private Button dashboard_btn;

    @FXML
    private Button settings_btn;

    @FXML
    private Button details_btn;

    @FXML
    private Button favorite_btn;

    @FXML
    private Button video_player_btn;

    @FXML
    private AnchorPane view_anchor_pane;

    @FXML
    private BorderPane root_border_pane;

    @FXML
    private StackPane root_stack_pane;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private VBox navigation_menu_bar_vbox;






    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.debug("--Initializing Main Controller--");
        initializeNavigationButtons();
        initializeStyles();
        initializeBindingsToBaseApplicationModel();

    }

    private void initializeBindingsToBaseApplicationModel() {
        bindDashboardToCurrentViewProperty();
        bindMeuBarToFullscreenProperty();
    }

    private void initializeNavigationButtons() {
        initializeButtonBindings();
        initializeButtonIcons();
    }

    private void bindMeuBarToFullscreenProperty() {
        LOGGER.debug("Binding the fullscreen property to adjust navigation menu bar visibility");
        MODEL.getFullscreenProperty().addListener((observable, oldValue, isFullscreen) -> {
            if (isFullscreen) {
                root_border_pane.getChildren().remove(navigation_menu_bar_vbox);
            }else{
                System.out.println("Exiting fullscreen mode, restoring navigation menu bar.");
                root_border_pane.setLeft(navigation_menu_bar_vbox);
            }
        });
    }

    private void bindDashboardToCurrentViewProperty() {
        LOGGER.debug("Binding the view port, to BaseApplicationModel current view node property");
        MODEL.currentViewNodeProperty().addListener((observable, oldNode, newNode) -> {
            if (newNode != null) {
                displayView(newNode);
            }
        });

        // Display the initial view if available
        LOGGER.debug("Displaying the initial view if already set in the model");
        if (MODEL.getCurrentViewNode() != null) {
            displayView(MODEL.getCurrentViewNode());
        }
    }

    private void initializeStyles() {
        LOGGER.debug("Applying styles to main layout components");
        root_stack_pane.getStyleClass().addAll(Styles.BG_NEUTRAL_SUBTLE);
        navigation_menu_bar_vbox.getStyleClass().addAll(Styles.BG_NEUTRAL_SUBTLE);
        view_anchor_pane.getStyleClass().addAll(Styles.BORDER_MUTED, Styles.BG_DEFAULT);
    }

    private void initializeButtonIcons() {
        LOGGER.debug("Initializing button icons");
        dashboard_btn.setGraphic(new FontIcon(MaterialDesignV.VIEW_DASHBOARD_OUTLINE));
        details_btn.setGraphic(new FontIcon(MaterialDesignT.TAB_SEARCH));
        favorite_btn.setGraphic(new FontIcon(MaterialDesignC.CARDS_HEART_OUTLINE));
        settings_btn.setGraphic(new FontIcon(MaterialDesignN.NUT));
        video_player_btn.setGraphic(new FontIcon(MaterialDesignV.VIDEO_OUTLINE));
    }

    private void initializeButtonBindings() {
        LOGGER.debug("Binding navigation buttons to views");
        bindButtonToView(dashboard_btn, Views.DASHBOARD);
        bindButtonToView(details_btn, Views.DETAILS);
        bindButtonToView(settings_btn, Views.SETTINGS);
        bindButtonToView(favorite_btn, Views.FAVORITES);
        bindButtonToView(video_player_btn, Views.VIDEO_PLAYER);
    }


    private void displayView(Node view) {
        LOGGER.debug("Displaying view {}", view);

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
        AnchorPane.setTopAnchor(view, 0.0);
        view_anchor_pane.getChildren().setAll(view);
    }







    private void bindButtonToView(Button btn, Views view) {
        LOGGER.debug("Bind button {} to view {}", btn, view);
        btn.setOnAction(event -> {
            if (MODEL.currentViewProperty().get() != view)
                MODEL.setCurrentView(view);
        });

        MODEL.currentViewProperty().addListener((_, _, newVal) ->
                btn.pseudoClassStateChanged(ACTIVE, newVal == view)
        );
        // initial state
        btn.pseudoClassStateChanged(ACTIVE, MODEL.currentViewProperty().get() == view);
    }


    public static boolean hasInternetConnection() {
        try{
            URL url = new URL("https://www.google.com");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("HEAD");
            con.setConnectTimeout(2000);
            con.setReadTimeout(2000);
            int responseCode = con.getResponseCode();
            return (responseCode >= 200  && responseCode <= 399);
        }catch (Exception e){
            return false;
        }
    }

}
