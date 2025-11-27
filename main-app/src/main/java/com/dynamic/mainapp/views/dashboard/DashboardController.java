package com.example.selfupdate.testjavafxmvci.views.dashboard;

import atlantafx.base.theme.Styles;
import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.services.AnimeService;
import com.example.selfupdate.testjavafxmvci.views.animeCard.AnimeCard;
import com.example.selfupdate.testjavafxmvci.views.main.BaseApplicationModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Controller for the Dashboard view.
 * Handles anime browsing, search functionality, and section display.
 */
public class DashboardController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger(DashboardController.class);

    // Constants
    private static final int HEIGHT_OVERHEAD = 20;
    private static final int WIDTH_OVERHEAD = 10;
    private static final int SECTION_SPACING = 10;
    private static final int LISTVIEW_PADDING = 30;
    private static final double SEARCH_WIDTH_MULTIPLIER = 1.33; // (1 + 1/3)

    private static final String SECTION_NEW_RELEASES = "New Releases";
    private static final String SECTION_POPULAR = "Popular Anime";
    private static final String SECTION_UPCOMING = "Upcoming Anime";

    // FXML Components
    @FXML private ScrollPane main_dashboard_scroll_pane;
    @FXML private HBox search_hbox;
    @FXML private TextField search_input_field;
    @FXML private Button search_btn;
    @FXML private ListView<Anime> search_results_listview;
    @FXML private HBox search_input_container;

    // Services and Data
    private final AnimeService animeService = AnimeService.getInstance();
    private final ObservableList<Anime> animeResults = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initializing DashboardController");

        if (!checkInternetConnection()) {
            displayNoInternetMessage();
            return;
        }

        initializeDashboardContent();
        initializeSearchComponents();
    }

    // ==================== Initialization Methods ====================

    /**
     * Checks if internet connection is available
     */
    private boolean checkInternetConnection() {
        // TODO: Implement actual internet connection check
        return true;
    }

    /**
     * Displays a message when no internet connection is available
     */
    private void displayNoInternetMessage() {
        LOGGER.warn("No internet connection detected");

        Label label = new Label("No internet connection");
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add(Styles.TEXT_MUTED);

        StackPane stackPane = new StackPane(label);
        StackPane.setAlignment(label, Pos.CENTER);

        main_dashboard_scroll_pane.setContent(stackPane);
    }

    /**
     * Initializes the main dashboard content with sections
     */
    private void initializeDashboardContent() {
        LOGGER.debug("Creating dashboard sections");

        VBox container = new VBox(SECTION_SPACING);
        container.getChildren().addAll(
                createSection(SECTION_NEW_RELEASES),
                createSection(SECTION_POPULAR),
                createSection(SECTION_UPCOMING)
        );

        main_dashboard_scroll_pane.setContent(container);
    }

    /**
     * Initializes all search-related components
     */
    private void initializeSearchComponents() {
        initializeSearchButton();
        initializeSearchInput();
        initializeSearchResultsList();
        setupSearchEventHandlers();
    }

    /**
     * Configures the search button
     */
    private void initializeSearchButton() {
        search_btn.setGraphic(new FontIcon(MaterialDesignS.SEARCH_WEB));
    }

    /**
     * Configures the search input field
     */
    private void initializeSearchInput() {
        search_input_field.setPromptText("Search for anime...");
        search_input_field.setOnAction(e -> search_btn.fire());
    }

    /**
     * Configures the search results ListView
     */
    private void initializeSearchResultsList() {
        // TODO: Move styling to CSS
        search_results_listview.setStyle("-fx-border-radius: 10; -fx-background-radius: 10;");
        search_results_listview.setItems(animeResults);

        bindSearchResultsWidth();
        configureSearchResultsCellFactory();
    }

    /**
     * Binds the search results ListView width to the search input container
     */
    private void bindSearchResultsWidth() {
        search_results_listview.maxWidthProperty().bind(
                search_input_container.widthProperty().multiply(SEARCH_WIDTH_MULTIPLIER)
        );
        search_results_listview.prefWidthProperty().bind(
                search_input_container.widthProperty().multiply(SEARCH_WIDTH_MULTIPLIER)
        );
    }

    /**
     * Configures the cell factory for search results with text wrapping
     */
    private void configureSearchResultsCellFactory() {
        search_results_listview.setCellFactory(lv -> new SearchResultCell());
    }

    /**
     * Sets up event handlers for search functionality
     */
    private void setupSearchEventHandlers() {
        search_btn.setOnAction(event -> performSearch());
        search_results_listview.setOnMouseClicked(event -> handleSearchResultSelection());
        main_dashboard_scroll_pane.setOnMouseClicked(event -> hideSearchResults());
    }

    // ==================== Search Functionality ====================

    /**
     * Performs anime search based on user input
     */
    private void performSearch() {
        String query = search_input_field.getText().trim();

        if (query.isEmpty()) {
            hideSearchResults();
            return;
        }

        LOGGER.debug("Performing search for: {}", query);
        animeService.searchAnimeByName(query, 1, this::handleSearchSuccess, this::handleSearchError);
    }

    /**
     * Handles successful search results
     */
    private void handleSearchSuccess(List<Anime> animeList) {
        Platform.runLater(() -> {
            animeResults.clear();

            if (animeList != null && !animeList.isEmpty()) {
                LOGGER.debug("Found {} search results", animeList.size());
                animeResults.addAll(animeList);
                showSearchResults();
            } else {
                LOGGER.debug("No search results found");
                hideSearchResults();
            }
        });
    }

    /**
     * Handles search errors
     */
    private void handleSearchError(Throwable throwable) {
        Platform.runLater(() -> {
            LOGGER.error("Error fetching anime: {}", throwable.getMessage(), throwable);
            hideSearchResults();
        });
    }

    /**
     * Handles selection of a search result
     */
    private void handleSearchResultSelection() {
        Anime selectedAnime = search_results_listview.getSelectionModel().getSelectedItem();

        if (selectedAnime != null) {
            LOGGER.debug("Selected anime: {}", selectedAnime.getTitle());
            BaseApplicationModel.getInstance().addAimeInDetailsView(selectedAnime);
            hideSearchResults();
        }
    }

    /**
     * Shows the search results ListView
     */
    private void showSearchResults() {
        search_results_listview.setVisible(true);
        search_results_listview.setManaged(true);
    }

    /**
     * Hides the search results ListView
     */
    private void hideSearchResults() {
        search_results_listview.setVisible(false);
        search_results_listview.setManaged(false);
    }

    // ==================== Section Creation ====================

    /**
     * Creates a dashboard section with the given title
     *
     * @param title The section title
     * @return The created section node
     */
    private Node createSection(String title) {
        LOGGER.debug("Creating dashboard section: {}", title);

        Label sectionTitle = createSectionTitle(title);
        ScrollPane sectionScrollPane = createSectionScrollPane();
        ListView<Anime> animeList = createAnimeListView();

        sectionScrollPane.setContent(animeList);

        VBox container = new VBox(sectionTitle, sectionScrollPane);
        container.setMaxHeight(AnimeCard.CARD_HEIGHT + HEIGHT_OVERHEAD * 4);

        loadSectionData(title, animeList);

        return container;
    }

    /**
     * Creates a styled section title label
     */
    private Label createSectionTitle(String title) {
        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().addAll(Styles.TEXT_BOLDER, Styles.TITLE_3, Styles.ACCENT);
        return sectionTitle;
    }

    /**
     * Creates and configures a ScrollPane for a section
     */
    private ScrollPane createSectionScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }

    /**
     * Creates and configures a horizontal ListView for anime cards
     */
    private ListView<Anime> createAnimeListView() {
        ListView<Anime> animeList = new ListView<>();
        animeList.setOrientation(Orientation.HORIZONTAL);
        animeList.setStyle("-fx-alignment: CENTER; -fx-border-color: transparent;");
        animeList.getStyleClass().add(Styles.DENSE);
        animeList.setFixedCellSize(AnimeCard.CARD_WIDTH + WIDTH_OVERHEAD);
        animeList.setMaxHeight(AnimeCard.CARD_HEIGHT + HEIGHT_OVERHEAD);

        configureScrollBehavior(animeList);
        configureCellFactory(animeList);

        return animeList;
    }

    /**
     * Configures scroll behavior to prevent ListView from hijacking vertical scrolls
     */
    private void configureScrollBehavior(ListView<Anime> animeList) {
        animeList.addEventFilter(ScrollEvent.SCROLL, event -> {
            // Allow vertical scrolling to bubble up to parent ScrollPane
            if (Math.abs(event.getDeltaY()) > Math.abs(event.getDeltaX())) {
                event.consume();
                Node parent = animeList.getParent();
                parent.fireEvent(event.copyFor(parent, parent));
            }
        });
    }

    /**
     * Configures the cell factory for anime cards
     */
    private void configureCellFactory(ListView<Anime> animeList) {
        animeList.setCellFactory(lv -> new AnimeCardCell());
    }

    /**
     * Loads data for a specific section
     */
    private void loadSectionData(String sectionTitle, ListView<Anime> animeList) {
        switch (sectionTitle) {
            case SECTION_NEW_RELEASES:
                loadNewReleases(animeList);
                break;
            case SECTION_POPULAR:
                loadPopularAnime(animeList);
                break;
            case SECTION_UPCOMING:
                loadUpcomingAnime(animeList);
                break;
            default:
                LOGGER.warn("Unknown section title: {}", sectionTitle);
        }
    }

    /**
     * Loads new releases into the ListView
     */
    private void loadNewReleases(ListView<Anime> animeList) {
        LOGGER.debug("Loading new releases");
        animeService.getNewReleases(1,
                animes -> handleSectionDataSuccess(animeList, animes),
                error -> handleSectionDataError(SECTION_NEW_RELEASES, error)
        );
    }

    /**
     * Loads popular anime into the ListView
     */
    private void loadPopularAnime(ListView<Anime> animeList) {
        LOGGER.debug("Loading popular anime");
        // TODO: Implement popular anime loading
    }

    /**
     * Loads upcoming anime into the ListView
     */
    private void loadUpcomingAnime(ListView<Anime> animeList) {
        LOGGER.debug("Loading upcoming anime");
        // TODO: Implement upcoming anime loading
    }

    /**
     * Handles successful section data loading
     */
    private void handleSectionDataSuccess(ListView<Anime> animeList, List<Anime> animes) {
        Platform.runLater(() -> {
            LOGGER.debug("Loaded {} anime items", animes.size());
            animeList.getItems().addAll(animes);
        });
    }

    /**
     * Handles section data loading errors
     */
    private void handleSectionDataError(String sectionTitle, Throwable error) {
        LOGGER.error("Error fetching {}: {}", sectionTitle, error.getMessage(), error);
    }

    // ==================== Inner Classes ====================

    /**
     * Custom ListCell for displaying search results with text wrapping
     */
    private class SearchResultCell extends ListCell<Anime> {
        private final Label label = new Label();

        public SearchResultCell() {
            label.setWrapText(true);
            label.setMaxWidth(Double.MAX_VALUE);
            setPrefWidth(0); // Important for text wrapping
        }

        @Override
        protected void updateItem(Anime anime, boolean empty) {
            super.updateItem(anime, empty);
            setText(null);

            if (empty || anime == null) {
                setGraphic(null);
            } else {
                label.setText(anime.getTitle());
                label.prefWidthProperty().bind(
                        search_results_listview.widthProperty().subtract(LISTVIEW_PADDING)
                );
                setGraphic(label);
            }
        }
    }

    /**
     * Custom ListCell for displaying anime cards
     */
    private class AnimeCardCell extends ListCell<Anime> {
        private AnimeCard animeCard;

        @Override
        protected void updateItem(Anime anime, boolean empty) {
            super.updateItem(anime, empty);

            if (empty || anime == null) {
                setGraphic(null);
                animeCard = null;
                return;
            }

            // TODO: Optimize AnimeCard creation (consider caching)
            animeCard = new AnimeCard(anime);
            setGraphic(animeCard.build());

            // Update border based on selection state
            animeCard.updateBorder(isSelected());
            selectedProperty().addListener((obs, wasSelected, isNowSelected) ->
                    animeCard.updateBorder(isNowSelected)
            );

            // Handle click to show details
            setOnMouseClicked(event -> {
                LOGGER.debug("Anime card clicked: {}", anime.getTitle());
                BaseApplicationModel.getInstance().addAimeInDetailsView(anime);
            });
        }
    }
}