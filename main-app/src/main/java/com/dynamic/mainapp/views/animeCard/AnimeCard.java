package com.example.selfupdate.testjavafxmvci.views.animeCard;

import atlantafx.base.theme.Styles;
import com.example.selfupdate.testjavafxmvci.MainApp;
import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.services.AnimeService;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Builder;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

import java.util.function.Consumer;


public class AnimeCard implements Builder {
    public static final double CARD_WIDTH = 180;
    public static final double CARD_HEIGHT = 260;


    private Anime anime;

    private final StackPane root;
    private Rectangle rectangle;

    private Label anime_title_label;
    private Label anime_season_label;
    private Label anime_type_label;


    private static final PseudoClass hovered = PseudoClass.getPseudoClass("hovered");
    private static final PseudoClass selected = PseudoClass.getPseudoClass("selected");

    public AnimeCard(Anime anime) {
        this.anime = anime;
        root = new StackPane();
        root.getStylesheets().add(MainApp.class.getResource("anime_card.css").toExternalForm());

        root.getStyleClass().addAll(Styles.ELEVATED_2);
        root.setMaxHeight(CARD_HEIGHT);
        root.setMaxWidth(CARD_WIDTH);

        var indicator = new ProgressIndicator();
        indicator.setMaxSize(50, 50);
        indicator.setProgress(-1d);

        rectangle = new Rectangle();
        rectangle.getStyleClass().add("rectangle");
        rectangle.setHeight(CARD_HEIGHT);
        rectangle.setWidth(CARD_WIDTH);
        rectangle.setFill(Color.TRANSPARENT);
        StackPane.setAlignment(rectangle, Pos.CENTER);

        anime_title_label = new Label(anime.getTitle());
        anime_title_label.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD,Styles.TEXT_MUTED);
        anime_title_label.setAlignment(Pos.CENTER);
        anime_title_label.setTextAlignment(TextAlignment.CENTER);
        anime_title_label.setMaxHeight(CARD_HEIGHT);
        anime_title_label.setMaxWidth(CARD_WIDTH);
        anime_title_label.setWrapText(true);

        root.setOnMouseEntered(event ->{
            showInfoOverlay(true);
            rectangle.pseudoClassStateChanged(hovered,true);
            rectangle.setEffect(new ColorAdjust(-1, -1, -1, -1));
        });

        root.setOnMouseExited(event ->{
            showInfoOverlay(false);
            rectangle.pseudoClassStateChanged(hovered,false);
            rectangle.setEffect(null);
        });


        anime_season_label = new Label("",new FontIcon(MaterialDesignC.CALENDAR_BLANK));
        anime_season_label.setAlignment(Pos.CENTER);
        anime_season_label.getStyleClass().add(Styles.TEXT_BOLD);

        anime_type_label = new Label();
        anime_type_label.setAlignment(Pos.CENTER);
        anime_type_label.getStyleClass().add(Styles.TEXT_BOLD);

        showInfoOverlay(false);


        StackPane.setAlignment(indicator, Pos.CENTER);
        StackPane.setAlignment(anime_season_label, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(anime_type_label, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(anime_title_label, Pos.TOP_LEFT);
        StackPane.setMargin(anime_title_label, new javafx.geometry.Insets(10, 10, 10, 10));
        StackPane.setMargin(anime_season_label, new javafx.geometry.Insets(5, 5, 5, 5));
        StackPane.setMargin(anime_type_label, new javafx.geometry.Insets(5, 5, 5, 5));

        root.getChildren().addAll(rectangle,indicator,anime_season_label,anime_type_label,anime_title_label);




        getAnimeDetails(anime);
    }

    //method called when anime details are fetched
    public void update(Anime anime) {
        this.anime = anime;
        anime_type_label.setText(anime.getAnimeType());
        anime_season_label.setText(anime.getSeason());

        Image animeImage = anime.getImage();
        if (animeImage != null && animeImage.progressProperty().get() != 1.0) {
            animeImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() == 1.0) {
                    rectangle.setFill(new ImagePattern(animeImage));
                    root.getChildren().clear();
                    root.getChildren().addAll(rectangle,anime_season_label,anime_type_label,anime_title_label);
                }
            });
        }else {
            rectangle.setFill(new ImagePattern(animeImage));
            root.getChildren().clear();
            root.getChildren().addAll(rectangle,anime_season_label,anime_type_label,anime_title_label);
        }

        root.setOnMouseClicked(event -> {
            System.out.println("clicked");
            //BaseApplicationModel.getInstance().addAimeInDetailsView(anime);
        });


    }


    @Override
    public Node build() {

//        root.getStylesheets().add(MainApp.class.getResource("anime_card.css").toExternalForm());
//
//        root.getStyleClass().addAll(Styles.ELEVATED_2);
//        root.setMaxHeight(CARD_HEIGHT);
//        root.setMaxWidth(CARD_WIDTH);
//
//        var indicator = new ProgressIndicator();
//        indicator.setMaxSize(50, 50);
//        indicator.setProgress(-1d);
//
//        rectangle = new Rectangle();
//        rectangle.getStyleClass().add("rectangle");
//        rectangle.setHeight(CARD_HEIGHT);
//        rectangle.setWidth(CARD_WIDTH);
//        rectangle.setFill(Color.TRANSPARENT);
//        StackPane.setAlignment(rectangle, Pos.CENTER);
//
//        anime_title_label = new Label(anime.getTitle());
//        anime_title_label.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD,Styles.TEXT_MUTED);
//        anime_title_label.setAlignment(Pos.CENTER);
//        anime_title_label.setTextAlignment(TextAlignment.CENTER);
//        anime_title_label.setMaxHeight(CARD_HEIGHT);
//        anime_title_label.setMaxWidth(CARD_WIDTH);
//        anime_title_label.setWrapText(true);
//
//        root.setOnMouseEntered(event ->{
//            showInfoOverlay(true);
//            rectangle.pseudoClassStateChanged(hovered,true);
//            rectangle.setEffect(new ColorAdjust(-1, -1, -1, -1));
//        });
//
//        root.setOnMouseExited(event ->{
//            showInfoOverlay(false);
//            rectangle.pseudoClassStateChanged(hovered,false);
//            rectangle.setEffect(null);
//        });
//
//
//        anime_season_label = new Label("",new FontIcon(MaterialDesignC.CALENDAR_BLANK));
//        anime_season_label.setAlignment(Pos.CENTER);
//        anime_season_label.getStyleClass().add(Styles.TEXT_BOLD);
//
//        anime_type_label = new Label();
//        anime_type_label.setAlignment(Pos.CENTER);
//        anime_type_label.getStyleClass().add(Styles.TEXT_BOLD);
//
//        showInfoOverlay(false);
//
//
//        StackPane.setAlignment(indicator, Pos.CENTER);
//        StackPane.setAlignment(anime_season_label, Pos.BOTTOM_LEFT);
//        StackPane.setAlignment(anime_type_label, Pos.BOTTOM_RIGHT);
//        StackPane.setAlignment(anime_title_label, Pos.TOP_LEFT);
//        StackPane.setMargin(anime_title_label, new javafx.geometry.Insets(10, 10, 10, 10));
//        StackPane.setMargin(anime_season_label, new javafx.geometry.Insets(5, 5, 5, 5));
//        StackPane.setMargin(anime_type_label, new javafx.geometry.Insets(5, 5, 5, 5));
//
//        root.getChildren().addAll(rectangle,indicator,anime_season_label,anime_type_label,anime_title_label);


        return root;
    }

    public void updateBorder(boolean isSelected){
        rectangle.pseudoClassStateChanged(selected,isSelected);
    }


    private void showInfoOverlay(boolean show){
        int opacity = show ? 100 : 0;
        anime_season_label.setOpacity(opacity);
        anime_type_label.setOpacity(opacity);
        anime_title_label.setOpacity(opacity);
    }


    private void getAnimeDetails(Anime anime){

        if (anime.isDetailsComplete()){
            update(anime);
            return;
        }

        Consumer<Anime> onSuccess = this::update;
        Consumer<Throwable> onError = error -> {
            // Handle error (e.g., show an alert)
            error.printStackTrace();
        };

        AnimeService.getInstance().getAnimeDetails(anime, onSuccess, onError);

    }
}
