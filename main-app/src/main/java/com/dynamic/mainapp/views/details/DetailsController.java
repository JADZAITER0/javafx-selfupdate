package com.example.selfupdate.testjavafxmvci.views.details;

import atlantafx.base.controls.Tab;
import atlantafx.base.controls.TabLine;
import atlantafx.base.theme.Styles;
import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.models.Episode;
import com.example.selfupdate.testjavafxmvci.core.AnimeScrappers.impl.AnimePahe;
import com.example.selfupdate.testjavafxmvci.services.AnimeService;
import com.example.selfupdate.testjavafxmvci.views.ViewManager;
import com.example.selfupdate.testjavafxmvci.views.main.BaseApplicationModel;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class DetailsController implements Initializable {

    @FXML
    private VBox root_anime_details_vbox;


    @FXML private ImageView posterView;
    @FXML private Label titleLabel, typeLabel, statusLabel, seasonLabel;
    @FXML private TextArea descriptionArea;
    @FXML private ListView<Episode> episodesList;


    @FXML
    private Button fav_btn;

    @FXML
    private GridView<Episode> episodesGrid;





    private  AnimePahe animePahe;
    private  AnimeService animeService = AnimeService.getInstance();
    private TabLine tabLine;


    private static final FontIcon HEART = new FontIcon(MaterialDesignH.HEART);
    private static final FontIcon HEART_OUTLINE = new FontIcon(MaterialDesignH.HEART_OUTLINE);



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        initializeLabels();


        fav_btn.getStyleClass().addAll(Styles.FLAT,Styles.BUTTON_CIRCLE);


        tabLine = new TabLine();
        tabLine.getStyleClass().addAll(Styles.TABS_FLOATING);
        tabLine.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        tabLine.setTabDragPolicy(Tab.DragPolicy.REORDER);
        tabLine.setTabResizePolicy(Tab.ResizePolicy.FIXED_WIDTH);
        tabLine.setTabClosingPolicy(Tab.ClosingPolicy.SELECTED_TAB);


        root_anime_details_vbox.getChildren().addFirst(tabLine);

        showComponents(false);
        tabLine.getSelectionModel().selectedItemProperty().subscribe(tab ->{
            showComponents(tab != null);
        });

    }

    public void addAnimeDetails(Anime anime) {
        Tab detailsTab = new Tab(anime.getProviderId(),anime.getTitle(),null);
        detailsTab.setOnSelectionChanged(event -> {
            if (detailsTab.isSelected()) {
                loadDetails(anime);
            }
        });

        //TODO need a better wat to seperate concerns
        detailsTab.setOnClosed(event -> {
            BaseApplicationModel.getInstance().getAnime_in_details_view().remove(anime);
            System.gc();
        });

        tabLine.getTabs().addLast(detailsTab);
        tabLine.getSelectionModel().select(detailsTab);
    }

    public void removeAnimeDetails(Anime anime) {
        tabLine.getTabs().removeIf(tab -> tab.getText().equals(anime.getTitle()));
    }

    private void loadDetails(Anime anime) {

        if (anime.isDetailsComplete()){
            setComponentsDetails(anime);
            return;
        }else {
            Consumer<Throwable> onerr = error -> {
                System.err.println("Failed to load anime details: " + error.getMessage());
            };
            Consumer<Anime> onsuc = this::setComponentsDetails;
            animeService.getAnimeDetails(anime, onsuc, onerr);
        }

    }

    private void setComponentsDetails(Anime anime) {
        statusLabel.setText(anime.getStatus());
        seasonLabel.setText(anime.getSeason());
        descriptionArea.setText(anime.getDescription());
        titleLabel.setText(anime.getTitle());
        typeLabel.setText(anime.getAnimeType());
        //fav_btn.setStyle((anime.isFavorite())? "-fx-background-color: #8d0606":"-fx-background-color:#555555");
        updateFavoriteButton(anime.isFavorite());


        fav_btn.setOnAction((actionEvent)-> {
            animeService.toggleFavorites(anime,result -> {
                updateFavoriteButton(result.isFavorite());
                viewMangerUpdateFavoriteAnime(result);
            }, error -> {
                System.err.println("Failed to toggle favorite: " + error.getMessage());
            });
        });

        if (anime.getImageURL() != null && !anime.getImageURL().isEmpty() && anime.getImage() != null) {
            Image image = anime.getImage();
            posterView.setImage(image);

            // Wait for image to load before creating pixelated version
            if (image.getProgress() < 1.0) {
                image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                    System.out.println("Image loading progress: " + newProgress);
                    if (newProgress.doubleValue() >= 1.0 && !image.isError()) {
                        setBackgroundImage(image);
                    }
                });
            } else if (!image.isError()) {
                setBackgroundImage(image);
            }
        }

        loadEpisodes(anime);
    }

    private void setBackgroundImage(Image image) {
        try {
            // Create pixelated background
            Image pixelated = createPixelatedImage(image, 5); // Smaller = more pixelated

        } catch (Exception e) {
            System.err.println("Failed to set background image: " + e.getMessage());
        }
    }


    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite){
            fav_btn.setGraphic(HEART);
            fav_btn.getStyleClass().add(Styles.DANGER);
            fav_btn.layout();
        }else{
            fav_btn.setGraphic(HEART_OUTLINE);
            fav_btn.getStyleClass().remove(Styles.DANGER);
            fav_btn.layout();
        }
    }

    private void viewMangerUpdateFavoriteAnime(Anime anime){
        if (anime.isFavorite()) {
            ViewManager.getInstance().addAnimeToFavorites(anime);
        } else {
            ViewManager.getInstance().removeAnimeFromFavorites(anime);
        }
    }

    private void initializeLabels(){
        seasonLabel.setGraphic(new FontIcon(MaterialDesignC.CALENDAR_BLANK));
        statusLabel.setGraphic(new FontIcon(MaterialDesignP.PULSE));
        typeLabel.setGraphic(new FontIcon(MaterialDesignC.COMMA_CIRCLE_OUTLINE));

        seasonLabel.getStyleClass().addAll(Styles.ELEVATED_2);
        statusLabel.getStyleClass().addAll(Styles.ELEVATED_2);
        typeLabel.getStyleClass().addAll(Styles.ELEVATED_2);
    }

    private Image createPixelatedImage(Image original, int gridSize) {
        if (original == null || original.isError() || original.getWidth() == 0 || original.getHeight() == 0) {
            throw new IllegalArgumentException("Invalid image");
        }

        int width = (int) original.getWidth();
        int height = (int) original.getHeight();

        // Calculate grid dimensions
        int cols = Math.max(1, gridSize);
        int rows = Math.max(1, (int)(gridSize * (height / (double)width)));

        // Create pixelated version
        WritableImage pixelated = new WritableImage(cols, rows);
        PixelWriter writer = pixelated.getPixelWriter();

        int blockWidth = width / cols;
        int blockHeight = height / rows;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // Sample from center of each block
                int srcX = Math.min((col * blockWidth) + (blockWidth / 2), width - 1);
                int srcY = Math.min((row * blockHeight) + (blockHeight / 2), height - 1);
                Color color = original.getPixelReader().getColor(srcX, srcY);
                writer.setColor(col, row, color);
            }
        }

        return pixelated;
    }

    private void showComponents(boolean show){
        titleLabel.setVisible(show);
        descriptionArea.setVisible(show);
        posterView.setVisible(show);
        typeLabel.setVisible(show);
        statusLabel.setVisible(show);
        fav_btn.setVisible(show);
        seasonLabel.setVisible(show);
        tabLine.setVisible(show);
        episodesGrid.setVisible(show);
    }

    private void loadEpisodes(Anime anime) {
        Consumer<List<Episode>> onsuc = episodes -> {

            episodesGrid.setItems(FXCollections.observableArrayList(episodes));
            episodesGrid.setCellFactory(grid -> new GridCell<>() {
                private final Button downloadBtn = new Button();
                private final VBox container = new VBox(8);

                {
                    container.setAlignment(Pos.CENTER);
                    container.getChildren().addAll(downloadBtn);

                    downloadBtn.setMinWidth(60);
                    downloadBtn.getStyleClass().addAll(Styles.ACCENT, Styles.BUTTON_OUTLINED);
                    downloadBtn.setOnAction(event -> {
                        Episode ep = getItem();
                        if (ep != null) {
                            getDownloadLink(ep);
                        }
                    });

                    // Optional size tuning for grid layout:
                    setMaxSize(10, 10);   // control cell size
                }

                @Override
                protected void updateItem(Episode ep, boolean empty) {
                    super.updateItem(ep, empty);

                    if (empty || ep == null) {
                        setGraphic(null);
                    } else {
                        downloadBtn.setText(ep.getEpisodeNumber()+"");
                        setGraphic(container);
                    }
                }
            });


        };

        animeService.getEpisodesList(anime, onsuc, error -> {
            System.err.println("Failed to load episodes: " + error.getMessage());
        });
    }


    private void getDownloadLink(Episode ep) {
        System.out.println("Fetching download link for Episode " + ep.getEpisodeNumber());
        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() {
                System.out.println("Inside download link task for Episode " + ep.getEpisodeNumber());
                Consumer<Map.Entry<String,String>> onSuccess = episodes -> {
                    System.out.println("Download link fetched: " + episodes.getKey());
                    ViewManager.getInstance().setVideoInVideoPlayerView(episodes.getKey());
                };
                Consumer<Throwable> onError = error -> {
                    System.err.println("Failed to fetch download link: " + error.getMessage());
                };

                animeService.getEpisodesVideoLinks(ep,onSuccess,onError);
                return null;
            }
        };

//        downloadTask.setOnSucceeded(e -> {
//            String link = downloadTask.getValue();
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            alert.setHeaderText("Download Link");
//            alert.setContentText(link != null ? link : "No link found");
//            alert.show();
//        });

        new Thread(downloadTask).start();
    }


}