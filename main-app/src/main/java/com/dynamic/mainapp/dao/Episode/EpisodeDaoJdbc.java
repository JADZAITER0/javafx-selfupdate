package com.example.selfupdate.testjavafxmvci.dao.Episode;

import com.example.selfupdate.testjavafxmvci.models.AnimeProvider;
import com.example.selfupdate.testjavafxmvci.models.Episode;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EpisodeDaoJdbc implements EpisodeDao {

    private final DataSource ds;

    public EpisodeDaoJdbc(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Episode insert(Episode e) throws SQLException {
        String sql = "INSERT INTO episode (anime_id, episode_number, episode_identifier, provider, watch_progress, duration, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getAnimeId());
            ps.setInt(2, e.getEpisodeNumber());
            ps.setString(3, e.getEpisodeId());
            ps.setString(4, e.getProvider() != null ? e.getProvider().name() : null);
            ps.setInt(5, e.getWatchProgress());
            ps.setInt(6, e.getDuration());
            ps.setString(7, LocalDateTime.now().toString());
            ps.setString(8, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getLong(1));
                }
            }
        }
        return e;
    }

    @Override
    public Optional<Episode> findById(long id) throws SQLException {
        String sql = "SELECT * FROM episode WHERE id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Episode> findForAnimeId(String animeProviderId) throws SQLException {
        String sql = "SELECT * FROM episode WHERE anime_id = ? ORDER BY episode_number ASC";
        List<Episode> list = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, animeProviderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public void update(Episode e) throws SQLException {
        String sql = "UPDATE episode SET anime_id=?, episode_number=?, episode_identifier=?, provider=?, watch_progress=?, duration=?, updated_at=? WHERE id=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getAnimeId());
            ps.setInt(2, e.getEpisodeNumber());
            ps.setString(3, e.getEpisodeId());
            ps.setString(4, e.getProvider() != null ? e.getProvider().name() : null);
            ps.setInt(5, e.getWatchProgress());
            ps.setInt(6, e.getDuration());
            ps.setString(7, LocalDateTime.now().toString());
            ps.setLong(8, e.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(long id) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM episode WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Episode mapRow(ResultSet rs) throws SQLException {
        Episode ep = new Episode(rs.getString("anime_id"), rs.getInt("episode_number"), rs.getString("episode_identifier"),
                parseProvider(rs.getString("provider")));
        ep.setId(rs.getLong("id"));
        ep.setDuration(rs.getInt("duration"));
        ep.setWatchProgress(rs.getInt("watch_progress"));
        return ep;
    }

    private AnimeProvider parseProvider(String name) {
        if (name == null) return null;
        try {
            return AnimeProvider.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}