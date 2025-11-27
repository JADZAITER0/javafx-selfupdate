package com.example.selfupdate.testjavafxmvci.views.favorite;

import atlantafx.base.theme.Styles;
import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.services.AnimeService;
import com.example.selfupdate.testjavafxmvci.views.animeCard.AnimeCard;
import com.example.selfupdate.testjavafxmvci.views.main.BaseApplicationModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Favorites view.
 * Manages the display and manipulation of favorite anime in a grid layout.
 */
public class FavoritesController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger(FavoritesController.class);

    // Constants
    private static final int MIN_HEIGHT = 300;
    private static final int MIN_WIDTH = 300;
    private static final int CELL_HEIGHT_OVERHEAD = 10;
    private static final int CELL_WIDTH_OVERHEAD = 10;

    // FXML Components
    @FXML private GridView<Anime> favorite_grid_view;
    @FXML private Label favorites_label;

    // Services and Data
    private final AnimeService animeService = AnimeService.getInstance();
    private final ObservableList<Anime> favoriteAnimes = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("--Initializing FavoritesController--");
        initializeComponents();
        setupGridView();
        setupObservableListListener();
        loadFavoriteAnimes();
    }

    // ==================== Initialization Methods ====================

    /**
     * Initializes UI components styling and properties
     */
    private void initializeComponents() {
        favorites_label.getStyleClass().addAll(Styles.TITLE_1, Styles.ACCENT);

        // TODO: Move styling to CSS
        favorite_grid_view.setStyle("-fx-border-color: transparent;");
        favorite_grid_view.setMinHeight(MIN_HEIGHT);
        favorite_grid_view.setMinWidth(MIN_WIDTH);
    }

    /**
     * Sets up the GridView configuration and cell factory
     */
    private void setupGridView() {
        favorite_grid_view.setCellFactory(FavoriteAnimeCell::new);
    }

    /**
     * Sets up listener to sync favoriteAnimes list with GridView
     */
    private void setupObservableListListener() {
        favoriteAnimes.addListener((ListChangeListener<Anime>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    handleAnimesAdded(change.getAddedSubList());
                }
                if (change.wasRemoved()) {
                    handleAnimesRemoved(change.getRemoved());
                }
            }
        });
    }

    /**
     * Loads favorite animes from the service
     */
    private void loadFavoriteAnimes() {
        LOGGER.debug("Loading favorite animes");
        animeService.getFavorite(this::handleLoadSuccess, this::handleLoadError);
    }

    // ==================== Public API Methods ====================

    /**
     * Adds an anime to the favorites list if not already present
     *
     * @param anime The anime to add to favorites
     */
    public void addFavoriteAnime(Anime anime) {
        if (anime == null) {
            LOGGER.warn("Attempted to add null anime to favorites");
            return;
        }

        if (isAnimeAlreadyInFavorites(anime)) {
            LOGGER.debug("Anime already in favorites: {}", anime.getTitle());
            return;
        }

        LOGGER.info("Adding anime to favorites: {}", anime.getTitle());
        favoriteAnimes.add(anime);
    }

    /**
     * Removes an anime from the favorites list
     *
     * @param anime The anime to remove from favorites
     */
    public void removeFavoriteAnime(Anime anime) {
        if (anime == null) {
            LOGGER.warn("Attempted to remove null anime from favorites");
            return;
        }

        boolean removed = favoriteAnimes.removeIf(fav ->
                fav.getId().equals(anime.getId())
        );

        if (removed) {
            LOGGER.info("Removed anime from favorites: {}", anime.getTitle());
        } else {
            LOGGER.debug("Anime not found in favorites: {}", anime.getTitle());
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Checks if an anime is already in the favorites list
     *
     * @param anime The anime to check
     * @return true if anime is already in favorites, false otherwise
     */
    private boolean isAnimeAlreadyInFavorites(Anime anime) {
        return favoriteAnimes.stream()
                .anyMatch(fav -> fav.getId().equals(anime.getId()));
    }

    /**
     * Handles added animes to the observable list
     *
     * @param addedAnimes List of newly added animes
     */
    private void handleAnimesAdded(List<? extends Anime> addedAnimes) {
        for (Anime anime : addedAnimes) {
            if (!favorite_grid_view.getItems().contains(anime)) {
                LOGGER.debug("Adding anime to grid view: {}", anime.getTitle());
                favorite_grid_view.getItems().add(anime);
            }
        }
    }

    /**
     * Handles removed animes from the observable list
     *
     * @param removedAnimes List of removed animes
     */
    private void handleAnimesRemoved(List<? extends Anime> removedAnimes) {
        for (Anime anime : removedAnimes) {
            LOGGER.debug("Removing anime from grid view: {}", anime.getTitle());
            favorite_grid_view.getItems().remove(anime);
        }
    }

    /**
     * Handles successful loading of favorite animes
     *
     * @param animes List of favorite animes from the service
     */
    private void handleLoadSuccess(List<Anime> animes) {
        Platform.runLater(() -> {
            LOGGER.info("Loaded {} favorite animes", animes.size());
            favoriteAnimes.addAll(animes);
        });
    }

    /**
     * Handles errors during favorite animes loading
     *
     * @param throwable The error that occurred
     */
    private void handleLoadError(Throwable throwable) {
        LOGGER.error("Error fetching favorite animes: {}", throwable.getMessage(), throwable);

        Platform.runLater(() -> {
            // TODO: Show error message to user
            // Could use a notification or dialog
        });
    }

    // ==================== Inner Classes ====================

    /**
     * Custom GridCell for displaying favorite anime cards
     */
    private class FavoriteAnimeCell extends GridCell<Anime> {
        private AnimeCard animeCard;

        public FavoriteAnimeCell(GridView<Anime> grid) {
            configureCellDimensions(grid);
        }

        /**
         * Configures the cell dimensions for the grid
         */
        private void configureCellDimensions(GridView<Anime> grid) {
            grid.setCellHeight(AnimeCard.CARD_HEIGHT + CELL_HEIGHT_OVERHEAD);
            grid.setCellWidth(AnimeCard.CARD_WIDTH + CELL_WIDTH_OVERHEAD);
        }

        @Override
        protected void updateItem(Anime anime, boolean empty) {
            super.updateItem(anime, empty);

            if (empty || anime == null) {
                setGraphic(null);
                return;
            }

            updateAnimeCard(anime);
            setupSelectionListener();
            setupClickHandler(anime);
        }

        /**
         * Updates or creates the anime card for this cell
         */
        private void updateAnimeCard(Anime anime) {
            if (animeCard == null) {
                animeCard = new AnimeCard(anime);
                LOGGER.trace("Created new AnimeCard for: {}", anime.getTitle());
            } else {
                animeCard.update(anime);
                LOGGER.trace("Updated existing AnimeCard for: {}", anime.getTitle());
            }

            setGraphic(animeCard.build());
            animeCard.updateBorder(isSelected());
        }

        /**
         * Sets up listener for cell selection changes
         */
        private void setupSelectionListener() {
            selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
                if (animeCard != null) {
                    animeCard.updateBorder(isNowSelected);
                }
            });
        }

        /**
         * Sets up click handler to show anime details
         */
        private void setupClickHandler(Anime anime) {
            setOnMouseClicked(event -> {
                LOGGER.debug("Favorite anime card clicked: {}", anime.getTitle());
                BaseApplicationModel.getInstance().addAimeInDetailsView(anime);
            });
        }
    }
}