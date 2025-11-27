package com.example.selfupdate.testjavafxmvci.controllers;

import com.example.selfupdate.testjavafxmvci.dao.Anime.AnimeDaoRepo;
import com.example.selfupdate.testjavafxmvci.dao.Episode.EpisodeDaoJdbc;
import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.core.AnimeScrappers.impl.AnimePahe;
import com.example.selfupdate.testjavafxmvci.services.AnimeService;
import com.example.selfupdate.testjavafxmvci.utils.database.DatabaseManager;
import com.example.selfupdate.testjavafxmvci.views.ViewManager;
import com.example.selfupdate.testjavafxmvci.views.Views;
import com.example.selfupdate.testjavafxmvci.views.main.BaseApplicationModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class SearchController {

    @FXML private TextField searchField;
    @FXML private ListView<Anime> resultsList;


    private AnimeService animeService = AnimeService.getInstance();


    @FXML
    public void initialize() {

        resultsList.setPlaceholder(new Label("No results"));
        resultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Anime anime, boolean empty) {
                super.updateItem(anime, empty);
                setText(empty || anime == null ? null : anime.getTitle());
            }
        });

        resultsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && resultsList.getSelectionModel().getSelectedItem() != null) {
                Anime selected = resultsList.getSelectionModel().getSelectedItem();
                openDetailsView(selected);
            }
        });
    }

    @FXML
    private void onSearchClicked() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        resultsList.getItems().clear();
        resultsList.setPlaceholder(new Label("Searching..."));


        Consumer<List<Anime>> onSuccess = animes -> {
            resultsList.getItems().setAll(animes);
            Platform.runLater(() -> {
                resultsList.setPlaceholder(new Label("No results found"));

            });
        };

        Consumer<Throwable> onError = e -> {
            Platform.runLater(() -> {
                resultsList.setPlaceholder(new Label("Error: " + e.getMessage()));
            });
        };



        animeService.searchAnimeByName(query,1,onSuccess, onError);

    }

    @FXML
    private void openDetailsView(Anime anime) {
        BaseApplicationModel.getInstance().addAimeInDetailsView(anime);
        //DetailsController controller = new DetailsController();
        //ViewManager.getInstance().loadViewAsync(Views.DETAILS, controller);
        //controller.setAnime(anime);
    }

}
