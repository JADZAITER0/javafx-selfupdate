package com.example.selfupdate.testjavafxmvci.core.AnimeScrappers;

import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.models.Episode;

import java.util.List;
import java.util.Map;

public interface AnimeScrapper {
    List<Anime> searchAnimeByName(String animeName, int pageNumber);
    Anime getAnimeDetails(Anime anime);
    List<Episode> getEpisodesList(Anime anime);
    Map.Entry<String,String>  getEpisodeDownloadLinks(Episode episode, String quality,String subOrDub);
    List<Anime> getNewReleases(int pageNumber);


}
