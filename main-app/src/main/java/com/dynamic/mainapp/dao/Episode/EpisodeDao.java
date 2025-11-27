package com.example.selfupdate.testjavafxmvci.dao.Episode;

import com.example.selfupdate.testjavafxmvci.models.Episode;

import java.util.List;
import java.util.Optional;

public interface EpisodeDao {
    Episode insert(Episode e) throws Exception;
    Optional<Episode> findById(long id) throws Exception;
    List<Episode> findForAnimeId(String animeProviderId) throws Exception;
    void update(Episode e) throws Exception;
    void delete(long id) throws Exception;
}