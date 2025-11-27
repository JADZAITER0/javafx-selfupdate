package com.example.selfupdate.testjavafxmvci.utils.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database connection manager with connection pooling for SQLite.
 * Thread-safe singleton implementation.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private final HikariDataSource dataSource;

    private DatabaseManager(String dbFilePath) {
        HikariConfig cfg = new HikariConfig();

        // ✅ SQLite JDBC URL
        cfg.setJdbcUrl("jdbc:sqlite:" + dbFilePath);

        // ✅ Basic pool configuration
        cfg.setMaximumPoolSize(4);
        cfg.setPoolName("sqlite-pool");

        // ✅ For SQLite, connectionTestQuery is unnecessary — remove to avoid warnings
        cfg.setConnectionTestQuery("SELECT 1");

        this.dataSource = new HikariDataSource(cfg);

        // ✅ Run DB schema migrations
        runMigrations();
    }

    public static synchronized void init(String dbFilePath) {
        if (instance == null) {
            instance = new DatabaseManager(dbFilePath);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseManager not initialized. Call init(dbPath) first.");
        }
        return instance;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Creates all necessary tables if they do not exist.
     */
    private void runMigrations() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");

            // anime table (provider_id/provider_type removed)
            s.execute(
                    "CREATE TABLE IF NOT EXISTS anime (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "title TEXT NOT NULL," +
                            "mal_id TEXT," +
                            "providers TEXT," + // JSON map of provider->id
                            "score REAL," +
                            "episode_count INTEGER," +
                            "current_episode INTEGER," +
                            "watched_episodes INTEGER," +
                            "description TEXT," +
                            "genre TEXT," + // JSON array
                            "anime_type TEXT," +
                            "rating TEXT," +
                            "image_url TEXT," +
                            "status TEXT," +
                            "release_date TEXT," +
                            "season TEXT," +
                            "created_at TEXT," +
                            "updated_at TEXT," +
                            "last_watched_at TEXT," +
                            "is_favorite INTEGER DEFAULT 0," +
                            "user_rating INTEGER," +
                            "notes TEXT" +
                            ");"
            );

            // episodes table
            s.execute(
                    "CREATE TABLE IF NOT EXISTS episode (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "anime_id TEXT NOT NULL," +
                            "episode_number INTEGER NOT NULL," +
                            "episode_identifier TEXT," +
                            "provider TEXT," +
                            "watch_progress INTEGER," +
                            "duration INTEGER," +
                            "created_at TEXT," +
                            "updated_at TEXT" +
                            ");"
            );

            // download table
            s.execute(
                    "CREATE TABLE IF NOT EXISTS download (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "episode_id INTEGER," +
                            "file_name TEXT," +
                            "url TEXT," +
                            "download_quality TEXT," +
                            "file_path TEXT," +
                            "file_size INTEGER," +
                            "downloaded_bytes INTEGER," +
                            "status TEXT," +
                            "error_message TEXT," +
                            "progress REAL," +
                            "created_at TEXT," +
                            "updated_at TEXT," +
                            "completed_at TEXT" +
                            ");"
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to run DB migrations", e);
        }
    }





    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
