package com.example.selfupdate.testjavafxmvci.models;


import com.example.selfupdate.testjavafxmvci.utils.cache.ImageCache;
import com.example.selfupdate.testjavafxmvci.views.main.BaseApplicationModel;
import javafx.scene.image.Image;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Anime {

    private Long id;

    private Map<AnimeProvider,String> providersIds;
    private String title;


    private String MALId;
    private double score;


    private int episodeCount;
    private int currentEpisode;
    private int watchedEpisodes;



    private String description;



    private String[] genre;
    private String animeType;
    private String rating;

    private String imageURL;


    private String status;

    private String releaseDate;
    private String season;


    //auditing fileds
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastWatchedAt;



    private boolean isFavorite;
    private int userRating;
    private String notes;


    public Anime(String providerId,String title) {
        if (providersIds == null) {
            providersIds = new HashMap<>();
        }
        providersIds.put(AnimeProvider.ANIME_PAHE, providerId);
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }

    public Anime setMALId(String MALId) {
        this.MALId = MALId;
        return this;
    }

    public Anime setDescription(String description) {
        this.description = description;
        return this;
    }

    public Anime setGenre(String[] genre) {
        this.genre = genre;
        return this;
    }

    public Anime setAnimeType(String animeType) {
        this.animeType = animeType;
        return this;
    }

    public Anime setRating(String rating) {
        this.rating = rating;
        return this;
    }

    public Anime setImageURL(String imageURL) {
        this.imageURL = imageURL;
        return this;
    }



    public Anime setStatus(String status) {
        this.status = status;
        return this;
    }

    public Anime setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public Anime setSeason(String season) {
        this.season = season;
        return this;
    }

    public Anime setScore(double score) {
        this.score = score;
        return this;
    }

    public Anime setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
        return this;
    }

    public Anime setCurrentEpisode(int currentEpisode) {
        this.currentEpisode = currentEpisode;
        return this;
    }

    public Anime setWatchedEpisodes(int watchedEpisodes) {
        this.watchedEpisodes = watchedEpisodes;
        return this;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    public void setUserRating(int userRating) {
        this.userRating = userRating;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastWatchedAt(LocalDateTime lastWatchedAt) {
        this.lastWatchedAt = lastWatchedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setProvidersIds(Map<AnimeProvider, String> providersIds) {
        this.providersIds = providersIds;
    }

    public void refreshUpdatedAt(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.updatedAt = LocalDateTime.parse(date, formatter);
    }

    public void addProviderId(AnimeProvider provider, String id) {
        if (providersIds == null) {
            providersIds = new HashMap<>();
        }
        providersIds.put(provider, id);
    }

    public String getProviderId(AnimeProvider provider) {
        if (providersIds != null) {
            return providersIds.get(provider);
        }
        return null;
    }

    public String getTitle() {
        return title;
    }

    public String getMALId() {
        return MALId;
    }

    public String getDescription() {
        return description;
    }

    public String[] getGenre() {
        return genre;
    }

    public String getAnimeType() {
        return animeType;
    }

    public String getRating() {
        return rating;
    }

    public String getImageURL() {
        return imageURL;
    }

    public Image getImage() {
        return ImageCache.get(imageURL);
    }

    public String getStatus() {
        return status;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getSeason() {
        return season;
    }

    public double getScore() {
        return score;
    }

    public int getEpisodeCount() {
        return episodeCount;
    }

    public int getCurrentEpisode() {
        return currentEpisode;
    }

    public int getWatchedEpisodes() {
        return watchedEpisodes;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public int getUserRating() {
        return userRating;
    }

    public String getNotes() {
        return notes;
    }

    public Long getId() {
        return id;
    }



    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastWatchedAt() {
        return lastWatchedAt;
    }

    public Map<AnimeProvider, String> getProvidersIds() {
        return providersIds;
    }

    //TODO make base application model private and use singleton pattern
    public String getProviderId(){
        if(providersIds != null && !providersIds.isEmpty()){
            return providersIds.get(BaseApplicationModel.getInstance().getAnimeProvider());
        }
        return null;
    }

    public boolean isDetailsComplete() {
        return description != null && !description.isBlank();
    }

    public void update(Anime anime) {
        this.setMALId(anime.getMALId());
        this.setDescription(anime.getDescription());
        this.setGenre(anime.getGenre());
        this.setAnimeType(anime.getAnimeType());
        this.setRating(anime.getRating());
        this.setImageURL(anime.getImageURL());
        this.setStatus(anime.getStatus());
        this.setReleaseDate(anime.getReleaseDate());
        this.setSeason(anime.getSeason());
        this.setScore(anime.getScore());
        this.setEpisodeCount(anime.getEpisodeCount());
        this.setCurrentEpisode(anime.getCurrentEpisode());
        this.setWatchedEpisodes(anime.getWatchedEpisodes());
        this.setFavorite(anime.isFavorite());
        this.setUpdatedAt(anime.getUpdatedAt());
        this.setId(anime.getId());
    }

    @Override
    public String toString() {
        return "Anime{" +
                "providersIds=" + providersIds +
                ", title='" + title + '\'' +
                ", MALId='" + MALId + '\'' +
                ", score=" + score +
                ", episodeCount=" + episodeCount +
                ", currentEpisode=" + currentEpisode +
                ", watchedEpisodes=" + watchedEpisodes +
                ", description='" + description + '\'' +
                ", genre=" + Arrays.toString(genre) +
                ", animeType='" + animeType + '\'' +
                ", rating='" + rating + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", status='" + status + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", season='" + season + '\'' +
                '}';
    }

    public boolean isFinishedAiring() {
        String normalizedStatus = status != null ? status.trim().toLowerCase() : "";
        return status != null && (normalizedStatus.contains("completed") || normalizedStatus.contains("finished"));
    }
}
