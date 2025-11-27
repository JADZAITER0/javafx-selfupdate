package com.example.selfupdate.testjavafxmvci.views;

import com.example.selfupdate.testjavafxmvci.MainApp;
import com.example.selfupdate.testjavafxmvci.views.details.DetailsController;
import com.example.selfupdate.testjavafxmvci.errors.ViewLoadException;
import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.views.favorite.FavoritesController;
import com.example.selfupdate.testjavafxmvci.views.main.BaseApplicationModel;
import com.example.selfupdate.testjavafxmvci.views.videoPlayer.VideoPlayerController;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewManager {

    private static ViewManager instance;

    private final Map<Views, Node> cache = new HashMap<>();
    private final Map<Views, Object> controllersCache = new HashMap<>();
    private final Set<Views> preloadedViews = new HashSet<>();

    private static final BaseApplicationModel MODEL = BaseApplicationModel.getInstance();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private boolean isPreloading = false;

    private ViewManager() {

        preloadStaticViews();

        MODEL.currentViewProperty().addListener((_, old, newView) -> {
            if (old != newView) {
                loadViewAsync(newView);
            }
        });

        //bind to observable list in model if needed
        MODEL.getAnime_in_details_view().addListener((ListChangeListener<Anime>) change ->{
            while (change.next()) {
                if (change.wasRemoved()) {
                    System.out.println("removed");
                    removeAnimeInDetailsView(change.getRemoved().getFirst());
                }
                if (change.wasAdded()) {
                    System.out.println("Anime added to details view: " + change.getAddedSubList().getFirst().getTitle());
                    addAnimeInDetailsView(change.getAddedSubList().getFirst());
                }
            }
        });
        // Load initial view
        loadViewAsync(MODEL.currentViewProperty().get());

    }

    public static ViewManager getInstance() {
        if (instance == null) {
            return instance = new ViewManager();
        }
        return instance;
    }





    public void addAnimeInDetailsView(Anime anime){
        DetailsController detailsController = (DetailsController) controllersCache.get(Views.DETAILS);
        if (detailsController != null) {
            detailsController.addAnimeDetails(anime);
        }
    }

    public void removeAnimeInDetailsView(Anime anime){
        DetailsController detailsController = (DetailsController) controllersCache.get(Views.DETAILS);
        if (detailsController != null) {
            detailsController.removeAnimeDetails(anime);
        }
    }

    public void removeAnimeFromFavorites(Anime anime){
        FavoritesController favoritesController = (FavoritesController) controllersCache.get(Views.FAVORITES);
        if (favoritesController != null) {
            favoritesController.removeFavoriteAnime(anime);
        }
    }

    public void addAnimeToFavorites(Anime anime){
        FavoritesController favoritesController = (FavoritesController) controllersCache.get(Views.FAVORITES);
        if (favoritesController != null) {
            favoritesController.addFavoriteAnime(anime);
        }
    }

    public void setVideoInVideoPlayerView(String videoUrl){
        VideoPlayerController videoPlayerController = (VideoPlayerController) controllersCache.get(Views.VIDEO_PLAYER);
        if (videoPlayerController != null) {
            videoPlayerController.setVideoSource(videoUrl);
        }

    }

    public void setFullScreenInVideoPlayerView(boolean isFullScreen){

    }



    private void loadViewAsync(Views requestedView) {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                Node node = loadView(requestedView);
                Platform.runLater(() -> MODEL.setCurrentViewNode(node));
                return null;
            }
        };
        executor.submit(loadTask);
    }


    private void preloadStaticViews() throws ViewLoadException{
        isPreloading = true;
        for (Views view : Views.values()) {
            if (view.isPreloaded()) {
                loadView(view);
                preloadedViews.add(view);
            }
        }
//        new Thread(() -> {
//            System.out.println("Starting view preloading...");
//
//            isPreloading = false;
//        }, "ViewPreloader").start();
    }



    private Node loadView(Views requestedView){
        if (cache.containsKey(requestedView)) {
            return cache.get(requestedView);
        }
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(requestedView.getPath()));
            Node view = loader.load();
            cache.put(requestedView, view);
            controllersCache.put(requestedView, loader.getController());
            return view;
        } catch (IOException e) {
            throw new ViewLoadException("Failed to load view " + requestedView, e);
        }
    }

    private Node loadView(Views requestedView, Object controller) {
        if (cache.containsKey(requestedView)) {
            return cache.get(requestedView);
        }
        try {
            String path = requestedView.getPath();
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(requestedView.getPath()));
            loader.setController(controller);
            Node view = loader.load();
            cache.put(requestedView, view);
            return view;
        } catch (IOException e) {
            throw new ViewLoadException("Failed to load view " + requestedView, e);
        }
    }

    public void clearDynamicViews() {
        cache.entrySet().removeIf(entry -> !entry.getKey().isPreloaded());
        System.out.println("Cleared all dynamic views from cache");
    }


    public void clearCache() {
        cache.clear();
        preloadedViews.clear();
    }

    public void clearCache(Views view) {
        cache.remove(view);
    }

    public void clean() {
        VideoPlayerController videoPlayerController = (VideoPlayerController) controllersCache.get(Views.VIDEO_PLAYER);
        if (videoPlayerController != null) {
            videoPlayerController.dispose();
        }
    }
}
