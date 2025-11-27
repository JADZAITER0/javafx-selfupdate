package com.example.selfupdate.testjavafxmvci.dao.Anime;

import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.models.AnimeProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class AnimeDaoRepo implements AnimeDao{
    private final DataSource ds;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public AnimeDaoRepo(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Anime insert(Anime anime) throws SQLException, JsonProcessingException {
        String sql = "INSERT INTO anime (title, mal_id, providers, score, episode_count, current_episode, watched_episodes, description, genre, anime_type, rating, image_url, status, release_date, season, created_at, updated_at, last_watched_at, is_favorite, user_rating, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, anime.getTitle());
            ps.setString(2, anime.getMALId());

            ps.setString(3, objectMapper.writeValueAsString(stringifyProviderMap(anime.getProvidersIds())));
            ps.setDouble(4, anime.getScore());
            ps.setInt(5, anime.getEpisodeCount());
            ps.setInt(6, anime.getCurrentEpisode());
            ps.setInt(7, anime.getWatchedEpisodes());
            ps.setString(8, anime.getDescription());
            ps.setString(9, objectMapper.writeValueAsString(anime.getGenre()));
            ps.setString(10, anime.getAnimeType());
            ps.setString(11, anime.getRating());
            ps.setString(12, anime.getImageURL());
            ps.setString(13, anime.getStatus());
            ps.setString(14, anime.getReleaseDate());
            ps.setString(15, anime.getSeason());
            ps.setString(16, toStringSafe(anime.getCreatedAt())); // private field access; if fields are private you might need getters
            ps.setString(17, toStringSafe(anime.getUpdatedAt()));
            ps.setString(18, toStringSafe(anime.getLastWatchedAt()));
            ps.setInt(19, anime.isFavorite() ? 1 : 0);
            ps.setInt(20, anime.getUserRating());
            ps.setString(21, anime.getNotes());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    anime.setId(id);
                }
            }
            return anime;
        }
    }

    @Override
    public Optional<Anime> findById(long id) throws SQLException {
        String sql = "SELECT * FROM anime WHERE id = ?";
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
    public List<Anime> findAll() throws SQLException {
        String sql = "SELECT * FROM anime";
        List<Anime> results = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    @Override
    public void update(Anime anime) throws SQLException, JsonProcessingException {
        String sql = "UPDATE anime SET title=?, mal_id=?, providers=?, score=?, episode_count=?, current_episode=?, watched_episodes=?, description=?, genre=?, anime_type=?, rating=?, image_url=?, status=?, release_date=?, season=?, created_at=?, updated_at=?, last_watched_at=?, is_favorite=?, user_rating=?, notes=? WHERE id=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, anime.getTitle());
            ps.setString(2, anime.getMALId());
            ps.setString(3, objectMapper.writeValueAsString(stringifyProviderMap(anime.getProvidersIds())));
            ps.setDouble(4, anime.getScore());
            ps.setInt(5, anime.getEpisodeCount());
            ps.setInt(6, anime.getCurrentEpisode());
            ps.setInt(7, anime.getWatchedEpisodes());
            ps.setString(8, anime.getDescription());
            ps.setString(9, objectMapper.writeValueAsString(anime.getGenre()));
            ps.setString(10, anime.getAnimeType());
            ps.setString(11, anime.getRating());
            ps.setString(12, anime.getImageURL());
            ps.setString(13, anime.getStatus());
            ps.setString(14, anime.getReleaseDate());
            ps.setString(15, anime.getSeason());
            ps.setString(16, toStringSafe(anime.getCreatedAt()));
            ps.setString(17, toStringSafe(anime.getUpdatedAt()));
            ps.setString(18, toStringSafe(anime.getLastWatchedAt()));
            ps.setInt(19, anime.isFavorite() ? 1 : 0);
            ps.setInt(20, anime.getUserRating());
            ps.setString(21, anime.getNotes());
            ps.setLong(22, anime.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(long id) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM anime WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // helpers
    private Anime mapRow(ResultSet rs) throws SQLException {
        Anime a = new Anime(rs.getString("providers") != null ? "" : "", rs.getString("title"));
        // we used the constructor which expects providerId + title; we then set fields from DB
        a.setId(rs.getLong("id"));
        a.setMALId(rs.getString("mal_id"));
        String providersJson = rs.getString("providers");
        if (providersJson != null) {
            // providers map stored as Map<String,String>, convert back to Map<AnimeProvider,String>
            Map<String,String> map = null;
            try {
                map = objectMapper.readValue(providersJson, Map.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (map != null) {
                Map<AnimeProvider,String> providerMap = new HashMap<>();
                for (Map.Entry<String,String> e : map.entrySet()) {
                    try {
                        AnimeProvider p = AnimeProvider.valueOf(e.getKey());
                        providerMap.put(p, e.getValue());
                    } catch (IllegalArgumentException ignore) {}
                }
                a.setProvidersIds(providerMap);
            }
        }
        a.setScore(rs.getDouble("score"));
        a.setEpisodeCount(rs.getInt("episode_count"));
        a.setCurrentEpisode(rs.getInt("current_episode"));
        a.setWatchedEpisodes(rs.getInt("watched_episodes"));
        a.setDescription(rs.getString("description"));
        //TODO improve code structure
        a.setUpdatedAt(rs.getString("updated_at") != null ? LocalDateTime.parse(rs.getString("updated_at")) : null);

        String genreJson = rs.getString("genre");
        if (genreJson != null && !genreJson.isBlank()) {
            String[] genres = null;
            try {
                genres = objectMapper.readValue(genreJson, String[].class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            a.setGenre(genres);
        }
        a.setAnimeType(rs.getString("anime_type"));
        a.setRating(rs.getString("rating"));
        a.setImageURL(rs.getString("image_url"));
        a.setStatus(rs.getString("status"));
        a.setReleaseDate(rs.getString("release_date"));
        a.setSeason(rs.getString("season"));
        // parse timestamps if needed (omitted here for brevity)
        a.setFavorite(rs.getInt("is_favorite") == 1);
        a.setUserRating(rs.getInt("user_rating"));
        a.setNotes(rs.getString("notes"));

        return a;
    }


    @Override
    public Optional<Anime> findByTitle(String title) throws SQLException {
        String sql = "SELECT * FROM anime WHERE LOWER(title) = LOWER(?) LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }


    @Override
    public Optional<Anime> findByProvider(AnimeProvider provider, String providerId) throws Exception {
        String jsonKey = "\"" + provider.name() + "\":\"" + providerId + "\"";
        String sql = "SELECT * FROM anime WHERE providers LIKE ? LIMIT 1";

        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + jsonKey + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    // convert Map<AnimeProvider,String> to Map<String,String> for JSON
    private Map<String,String> stringifyProviderMap(Map<AnimeProvider,String> map) {
        if (map == null) return null;
        Map<String,String> out = new HashMap<>();
        for (Map.Entry<AnimeProvider,String> e : map.entrySet()) {
            out.put(e.getKey().name(), e.getValue());
        }
        return out;
    }

    private String toStringSafe(LocalDateTime t) {
        return (t == null) ? null : t.toString();
    }
}
