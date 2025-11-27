package com.example.selfupdate.testjavafxmvci.dao.Anime;

import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.models.AnimeProvider;

import java.util.List;
import java.util.Optional;

public interface AnimeDao {
    Anime insert(Anime anime) throws Exception;
    Optional<Anime> findById(long id) throws Exception;
    List<Anime> findAll() throws Exception;
    void update(Anime anime) throws Exception;
    void delete(long id) throws Exception;
    Optional<Anime> findByTitle(String title) throws Exception;
    Optional<Anime> findByProvider(AnimeProvider providerType, String providerId) throws Exception;


}
