package com.example.selfupdate.testjavafxmvci.services;

import com.example.selfupdate.testjavafxmvci.core.AnimeScrappers.AnimeScrapper;
import com.example.selfupdate.testjavafxmvci.core.AnimeScrappers.impl.AnimePahe;
import com.example.selfupdate.testjavafxmvci.dao.Anime.AnimeDao;
import com.example.selfupdate.testjavafxmvci.dao.Anime.AnimeDaoRepo;
import com.example.selfupdate.testjavafxmvci.dao.Episode.EpisodeDao;
import com.example.selfupdate.testjavafxmvci.dao.Episode.EpisodeDaoJdbc;
import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.models.Episode;
import com.example.selfupdate.testjavafxmvci.utils.database.DatabaseManager;
import com.example.selfupdate.testjavafxmvci.views.main.BaseApplicationModel;
import javafx.concurrent.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AnimeService {

    private static AnimeService instance;

    private final AnimeDao animeDao;
    private final EpisodeDao episodeDao;

    private final AnimeScrapper animeScrapper;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final BaseApplicationModel MODEL = BaseApplicationModel.getInstance();



    private AnimeService() {
        animeDao = new AnimeDaoRepo(DatabaseManager.getInstance().getDataSource());
        episodeDao = new EpisodeDaoJdbc(DatabaseManager.getInstance().getDataSource());
        animeScrapper = new AnimePahe();
    }

    public static AnimeService getInstance() {
        if (instance == null) {
            instance = new AnimeService();
        }
        return instance;
    }


    public void getNewReleases(int pageNumber, Consumer<List<Anime>> onSuccess, Consumer<Throwable> onError){
            Task<List<Anime>> task = new Task<>() {
                @Override
                protected List<Anime> call() throws Exception {
                    List<Anime> animes = animeScrapper.getNewReleases(pageNumber);
                    for (Anime anime : animes) {
                        saveAnimeIfNotExists(anime);
                    }
                    return animes;
                }
            };


            task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
            task.setOnFailed(e -> onError.accept(task.getException()));

            executor.submit(task);
    }


    public void searchAnimeByName(String name, int pageNumber, Consumer<List<Anime>> onSuccess, Consumer<Throwable> onError){
        Task<List<Anime>> task = new Task<>() {
            @Override
            protected List<Anime> call() throws Exception {
                List<Anime> animes = animeScrapper.searchAnimeByName(name, pageNumber);
                for (Anime anime : animes) {
                    saveAnimeIfNotExists(anime);
                }
                return animes;
            }
        };

        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        executor.submit(task);
    }


    public void getAnimeDetails(Anime anime, Consumer<Anime> onSuccess, Consumer<Throwable> onError){
        Task<Anime> task = new Task<>() {
            @Override
            protected Anime call() throws Exception {
                Optional<Anime> dbAnime = animeDao.findByProvider(MODEL.getAnimeProvider(), anime.getProviderId());
                //exists in DB
                if (dbAnime.isPresent() && dbAnime.get().isDetailsComplete()) {
                    anime.update(dbAnime.get());
                    return dbAnime.get();
                }
                // exists but incomplete → refresh details
                Anime baseAnime = dbAnime.orElse(anime);
                Anime detailed = animeScrapper.getAnimeDetails(baseAnime);

                animeDao.update(detailed);
                return detailed;
            }
        };

        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        executor.submit(task);
    }

    public void getEpisodesList(Anime anime, Consumer<List<Episode>> onSuccess, Consumer<Throwable> onError){
        Task<List<Episode>> task = new Task<>() {
            @Override
            protected List<Episode> call() throws Exception {
                //exists in DB and anime status is completed or last aired episode date is less than a week
                System.out.println(anime.getUpdatedAt());
                if (anime.isFinishedAiring() || (anime.getUpdatedAt() != null && !anime.getUpdatedAt().isBefore(LocalDateTime.now().minusWeeks(1)))) {
                    System.out.println("Fetching episodes from DB for anime: " + anime.getTitle());
                    List<Episode> existing = episodeDao.findForAnimeId(anime.getProviderId());
                    if (!existing.isEmpty()) {
                        return existing;
                    }
                }

                // fetch from scrapper and save
                List<Episode> episodes = animeScrapper.getEpisodesList(anime);
                animeDao.update(anime);
                for (Episode episode : episodes) {
                    episodeDao.insert(episode);
                }
                return episodes;
            }
        };

        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        executor.submit(task);
    }

    public void getEpisodesVideoLinks(Episode episode, Consumer<Map.Entry<String,String>> onSuccess, Consumer<Throwable> onError){
        Task<Map.Entry<String,String>> task = new Task<>() {
            @Override
            protected Map.Entry<String,String> call() throws Exception {

                Map.Entry<String, String> detailed = animeScrapper.getEpisodeDownloadLinks(episode,"360p","sub");
                return detailed;
            }
        };

        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        executor.submit(task);
    }

    public void getFavorite(Consumer<List<Anime>> onSuccess, Consumer<Throwable> onError) {
        Task<List<Anime>> task = new Task<>() {
            @Override
            protected List<Anime> call() throws Exception {
                return animeDao.findAll().stream().filter(Anime::isFavorite).toList();
            }
        };

        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));

        executor.submit(task);
    }

    public void toggleFavorites(Anime anime, Consumer<Anime> onSuccess, Consumer<Throwable> onError) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                anime.setFavorite(!anime.isFavorite());
                animeDao.update(anime);
                return null;
            }
        };

        task.setOnSucceeded(e -> onSuccess.accept(anime));
        task.setOnFailed(e -> onError.accept(task.getException()));

        executor.submit(task);
    }

    public void saveAnimeIfNotExists(Anime anime) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Optional<Anime> existing = animeDao.findByProvider(MODEL.getAnimeProvider(), anime.getProviderId());
                if (existing.isEmpty()) {
                    animeDao.insert(anime);
                }
                return null;
            }
        };

        executor.submit(task);
    }
}
