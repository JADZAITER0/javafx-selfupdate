package com.example.selfupdate.testjavafxmvci.views.main;

import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.models.AnimeProvider;
import com.example.selfupdate.testjavafxmvci.services.AnimeService;
import com.example.selfupdate.testjavafxmvci.views.Views;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class BaseApplicationModel{
    private static BaseApplicationModel instance;

    private AnimeService animeService;

    private static SimpleObjectProperty<Views>  view;
    private  SimpleBooleanProperty isLoading = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<Node> currentViewNode;
    private final SimpleObjectProperty<AnimeProvider> animeProvider;
    private ObservableList<Anime> anime_in_details_view;
    private SimpleBooleanProperty isFullscreen = new SimpleBooleanProperty(false);

    private BaseApplicationModel(){
        view = new SimpleObjectProperty<>(Views.DASHBOARD);
        animeProvider = new SimpleObjectProperty<>(AnimeProvider.ANIME_PAHE);
        currentViewNode = new SimpleObjectProperty<>();
        anime_in_details_view =  FXCollections.observableArrayList();
    }

    public AnimeProvider getAnimeProvider() {
        return animeProvider.get();
    }

    public static BaseApplicationModel getInstance() {
        if (instance == null) {
            instance = new BaseApplicationModel();
        }
        return instance;
    }

    public AnimeService getAnimeService() {
        return animeService;
    }

    public SimpleObjectProperty<Views> currentViewProperty() {
        if (view == null) {
            view = new SimpleObjectProperty<>(Views.DASHBOARD);
        }
        return view;
    }

    public void addAimeInDetailsView(Anime anime){
        anime_in_details_view.remove(anime);
        anime_in_details_view.addLast(anime);
    }
    public void removeAimeInDetailsView(Anime anime){
        anime_in_details_view.remove(anime);
    }

    public ObservableList<Anime> getAnime_in_details_view() {
        return anime_in_details_view;
    }

    public void setCurrentView(Views view) {
        BaseApplicationModel.view.set(view);
    }

    public SimpleObjectProperty<Node>  currentViewNodeProperty(){
        return currentViewNode;
    }



    public Node getCurrentViewNode() {
        return currentViewNode.get();
    }

    public void setCurrentViewNode(Node node) {
        this.currentViewNode.set(node);
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading.set(isLoading);
    }

    public boolean isIsLoading() {
        return isLoading.get();
    }

    public SimpleBooleanProperty getFullscreenProperty() {
        return isFullscreen;
    }

    public void setIsFullscreen(boolean isFullscreen) {
        this.isFullscreen.set(isFullscreen);
    }

    public boolean isFullscreen() {
        return isFullscreen.get();
    }
}
