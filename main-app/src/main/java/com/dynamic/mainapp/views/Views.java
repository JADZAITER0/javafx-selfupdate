package com.example.selfupdate.testjavafxmvci.views;

public enum Views {
    DASHBOARD("views/dashboard.fxml","stylesheet/css",true),
    SETTINGS("views/search-view.fxml","hello.css",true),
    LOGIN("views/login.fxml","stylesheet/css",false),
    DETAILS("views/details-view.fxml","stylesheet/css",true),
    ANIME_CARD("views/anime-card.fxml","anime_card.css",false),
    FAVORITES("views/favorites-view.fxml","stylesheet/css",true),
    VIDEO_PLAYER("views/video-player.fxml","stylesheet/css",true);


    private final String path;
    private final String cssPath;
    private final boolean preload;

    Views(String path, String cssPath, boolean preload) {
        this.path = path;
        this.cssPath = cssPath;
        this.preload = preload;
    }

    public String getPath() {
        return path;
    }

    public String getCssPath() {
        return cssPath;
    }

    public boolean isPreloaded(){
        return preload;
    }

}
