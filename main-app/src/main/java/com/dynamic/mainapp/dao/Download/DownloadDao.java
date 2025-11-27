package com.example.selfupdate.testjavafxmvci.dao.Download;

import com.example.selfupdate.testjavafxmvci.models.Download;

import java.util.List;
import java.util.Optional;

public interface DownloadDao {
    Download insert(Download d) throws Exception;
    Optional<Download> findById(long id) throws Exception;
    List<Download> findAll() throws Exception;
    void update(Download d) throws Exception;
    void delete(long id) throws Exception;
}