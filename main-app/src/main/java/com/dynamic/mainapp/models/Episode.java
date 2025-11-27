package com.example.selfupdate.testjavafxmvci.models;

import java.time.LocalDateTime;

public class Episode {

    private Long id;

    private String animeId;
    private int episodeNumber;
    private String episodeId;
    private AnimeProvider provider;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int watchProgress;
    private int duration;



    public Episode(String animeId, int episodeNumber, String episodeId, AnimeProvider provider) {
        this.animeId = animeId;
        this.episodeNumber = episodeNumber;
        this.episodeId = episodeId;
        this.provider = provider;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getAnimeId(){
        return animeId;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public String getEpisodeId() {
        return episodeId;
    }

    public AnimeProvider getProvider() {
        return provider;
    }

    public int getWatchProgress() {
        return watchProgress;
    }

    public int getDuration() {
        return duration;
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

    public void setWatchProgress(int watchProgress) {
        this.watchProgress = watchProgress;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDuration(int duration) {
        this.duration = duration;
        this.updatedAt = LocalDateTime.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Episode{" +
                "animeId='" + animeId + '\'' +
                ", episodeNumber=" + episodeNumber +
                ", episodeId='" + episodeId + '\'' +
                ", provider=" + provider +
                '}';
    }
}
