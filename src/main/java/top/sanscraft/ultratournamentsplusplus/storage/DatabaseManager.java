package top.sanscraft.ultratournamentsplusplus.storage;

import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.DiscordLink;
import top.sanscraft.ultratournamentsplusplus.models.PlayerStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages database connections and operations
 */
public class DatabaseManager {
    
    private final UltraTournamentsPlusPlus plugin;
    private Connection connection;
    private String databaseType;
    
    public DatabaseManager(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initialize the database connection
     */
    public void initialize() {
        plugin.getLogger().info("Initializing database connection...");
        
        databaseType = plugin.getConfigManager().getDatabaseType();
        plugin.getLogger().info("Using database type: " + databaseType);
        
        try {
            if ("sqlite".equalsIgnoreCase(databaseType)) {
                initializeSQLite();
            } else if ("mysql".equalsIgnoreCase(databaseType)) {
                initializeMySQL();
            } else {
                plugin.getLogger().warning("Unsupported database type: " + databaseType + ". Falling back to SQLite.");
                initializeSQLite();
            }
            
            createTables();
            plugin.getLogger().info("Database initialized successfully!");
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize SQLite connection
     */
    private void initializeSQLite() throws SQLException {
        String dbPath = plugin.getDataFolder().getAbsolutePath() + "/tournaments.db";
        String url = "jdbc:sqlite:" + dbPath;
        connection = DriverManager.getConnection(url);
        connection.setAutoCommit(true);
    }
    
    /**
     * Initialize MySQL connection
     */
    private void initializeMySQL() throws SQLException {
        // TODO: Implement MySQL connection from config
        String host = "localhost"; // Get from config
        int port = 3306; // Get from config
        String database = "ultratournaments"; // Get from config
        String username = "username"; // Get from config
        String password = "password"; // Get from config
        
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true", 
                                 host, port, database);
        connection = DriverManager.getConnection(url, username, password);
        connection.setAutoCommit(true);
    }
    
    /**
     * Create necessary database tables
     */
    private void createTables() throws SQLException {
        createDiscordLinksTable();
        createPlayerStatsTable();
        // TODO: Add other tournament-related tables
    }
    
    /**
     * Create Discord links table
     */
    private void createDiscordLinksTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS discord_links (
                discord_id VARCHAR(20) PRIMARY KEY,
                minecraft_uuid VARCHAR(36) NOT NULL UNIQUE,
                minecraft_username VARCHAR(16) NOT NULL,
                linked_timestamp BIGINT NOT NULL,
                verified BOOLEAN NOT NULL DEFAULT FALSE,
                notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                INDEX idx_minecraft_uuid (minecraft_uuid)
            )
            """;
        
        if ("sqlite".equalsIgnoreCase(databaseType)) {
            sql = sql.replace("VARCHAR(20)", "TEXT")
                   .replace("VARCHAR(36)", "TEXT")
                   .replace("VARCHAR(16)", "TEXT")
                   .replace("BOOLEAN", "INTEGER")
                   .replace("BIGINT", "INTEGER")
                   .replaceAll("INDEX.*\\)", "");
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create player stats table
     */
    private void createPlayerStatsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS player_stats (
                player_uuid VARCHAR(36) PRIMARY KEY,
                player_name VARCHAR(16) NOT NULL,
                tournaments_played INT NOT NULL DEFAULT 0,
                tournaments_won INT NOT NULL DEFAULT 0,
                rounds_played INT NOT NULL DEFAULT 0,
                rounds_won INT NOT NULL DEFAULT 0,
                total_kills INT NOT NULL DEFAULT 0,
                total_deaths INT NOT NULL DEFAULT 0,
                total_playtime BIGINT NOT NULL DEFAULT 0,
                first_played BIGINT NOT NULL,
                last_played BIGINT NOT NULL,
                win_streak INT NOT NULL DEFAULT 0,
                best_win_streak INT NOT NULL DEFAULT 0,
                loss_streak INT NOT NULL DEFAULT 0,
                average_round_duration DOUBLE NOT NULL DEFAULT 0.0,
                favorite_kit VARCHAR(50),
                favorite_arena VARCHAR(50)
            )
            """;
        
        if ("sqlite".equalsIgnoreCase(databaseType)) {
            sql = sql.replace("VARCHAR(36)", "TEXT")
                   .replace("VARCHAR(16)", "TEXT")
                   .replace("VARCHAR(50)", "TEXT")
                   .replace("INT", "INTEGER")
                   .replace("BIGINT", "INTEGER");
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    // Discord Link Operations
    
    /**
     * Save a Discord link to the database
     */
    public void saveDiscordLink(DiscordLink link) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO discord_links 
            (discord_id, minecraft_uuid, minecraft_username, linked_timestamp, verified, notifications_enabled)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        if ("mysql".equalsIgnoreCase(databaseType)) {
            sql = sql.replace("INSERT OR REPLACE", "INSERT INTO ... ON DUPLICATE KEY UPDATE");
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, link.getDiscordId());
            stmt.setString(2, link.getMinecraftUuid().toString());
            stmt.setString(3, link.getMinecraftUsername());
            stmt.setLong(4, link.getLinkedTimestamp());
            stmt.setBoolean(5, link.isVerified());
            stmt.setBoolean(6, link.isNotificationsEnabled());
            stmt.executeUpdate();
        }
    }
    
    /**
     * Load all Discord links from the database
     */
    public List<DiscordLink> loadDiscordLinks() throws SQLException {
        List<DiscordLink> links = new ArrayList<>();
        String sql = "SELECT * FROM discord_links WHERE verified = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, true);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DiscordLink link = new DiscordLink(
                        rs.getString("discord_id"),
                        UUID.fromString(rs.getString("minecraft_uuid")),
                        rs.getString("minecraft_username"),
                        rs.getLong("linked_timestamp"),
                        rs.getBoolean("verified"),
                        rs.getBoolean("notifications_enabled")
                    );
                    links.add(link);
                }
            }
        }
        
        return links;
    }
    
    /**
     * Delete a Discord link from the database
     */
    public void deleteDiscordLink(String discordId) throws SQLException {
        String sql = "DELETE FROM discord_links WHERE discord_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, discordId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete a Discord link by Minecraft UUID
     */
    public void deleteDiscordLinkByMinecraft(UUID minecraftUuid) throws SQLException {
        String sql = "DELETE FROM discord_links WHERE minecraft_uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, minecraftUuid.toString());
            stmt.executeUpdate();
        }
    }
    
    // Player Stats Operations
    
    /**
     * Save player stats to the database
     */
    public void savePlayerStats(PlayerStats stats) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO player_stats 
            (player_uuid, player_name, tournaments_played, tournaments_won, rounds_played, rounds_won,
             total_kills, total_deaths, total_playtime, first_played, last_played, win_streak,
             best_win_streak, loss_streak, average_round_duration, favorite_kit, favorite_arena)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, stats.getPlayerId().toString());
            stmt.setString(2, stats.getPlayerName());
            stmt.setInt(3, stats.getTournamentsPlayed());
            stmt.setInt(4, stats.getTournamentsWon());
            stmt.setInt(5, stats.getRoundsPlayed());
            stmt.setInt(6, stats.getRoundsWon());
            stmt.setInt(7, stats.getTotalKills());
            stmt.setInt(8, stats.getTotalDeaths());
            stmt.setLong(9, stats.getTotalPlayTime());
            stmt.setLong(10, stats.getFirstPlayed());
            stmt.setLong(11, stats.getLastPlayed());
            stmt.setInt(12, stats.getWinStreak());
            stmt.setInt(13, stats.getBestWinStreak());
            stmt.setInt(14, stats.getLossStreak());
            stmt.setDouble(15, stats.getAverageRoundDuration());
            stmt.setString(16, stats.getFavoriteKit());
            stmt.setString(17, stats.getFavoriteArena());
            stmt.executeUpdate();
        }
    }
    
    /**
     * Load player stats from the database
     */
    public PlayerStats loadPlayerStats(UUID playerId) throws SQLException {
        String sql = "SELECT * FROM player_stats WHERE player_uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PlayerStats(
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("player_name"),
                        rs.getInt("tournaments_played"),
                        rs.getInt("tournaments_won"),
                        rs.getInt("rounds_played"),
                        rs.getInt("rounds_won"),
                        rs.getInt("total_kills"),
                        rs.getInt("total_deaths"),
                        rs.getLong("total_playtime"),
                        rs.getLong("first_played"),
                        rs.getLong("last_played"),
                        rs.getInt("win_streak"),
                        rs.getInt("best_win_streak"),
                        rs.getInt("loss_streak"),
                        rs.getDouble("average_round_duration"),
                        rs.getString("favorite_kit"),
                        rs.getString("favorite_arena")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Load all player stats from the database
     */
    public List<PlayerStats> loadAllPlayerStats() throws SQLException {
        List<PlayerStats> statsList = new ArrayList<>();
        String sql = "SELECT * FROM player_stats ORDER BY tournaments_won DESC, rounds_won DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                PlayerStats stats = new PlayerStats(
                    UUID.fromString(rs.getString("player_uuid")),
                    rs.getString("player_name"),
                    rs.getInt("tournaments_played"),
                    rs.getInt("tournaments_won"),
                    rs.getInt("rounds_played"),
                    rs.getInt("rounds_won"),
                    rs.getInt("total_kills"),
                    rs.getInt("total_deaths"),
                    rs.getLong("total_playtime"),
                    rs.getLong("first_played"),
                    rs.getLong("last_played"),
                    rs.getInt("win_streak"),
                    rs.getInt("best_win_streak"),
                    rs.getInt("loss_streak"),
                    rs.getDouble("average_round_duration"),
                    rs.getString("favorite_kit"),
                    rs.getString("favorite_arena")
                );
                statsList.add(stats);
            }
        }
        
        return statsList;
    }
    
    /**
     * Get top players by tournaments won
     */
    public List<PlayerStats> getTopPlayersByTournaments(int limit) throws SQLException {
        List<PlayerStats> topPlayers = new ArrayList<>();
        String sql = "SELECT * FROM player_stats ORDER BY tournaments_won DESC, rounds_won DESC LIMIT ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PlayerStats stats = new PlayerStats(
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("player_name"),
                        rs.getInt("tournaments_played"),
                        rs.getInt("tournaments_won"),
                        rs.getInt("rounds_played"),
                        rs.getInt("rounds_won"),
                        rs.getInt("total_kills"),
                        rs.getInt("total_deaths"),
                        rs.getLong("total_playtime"),
                        rs.getLong("first_played"),
                        rs.getLong("last_played"),
                        rs.getInt("win_streak"),
                        rs.getInt("best_win_streak"),
                        rs.getInt("loss_streak"),
                        rs.getDouble("average_round_duration"),
                        rs.getString("favorite_kit"),
                        rs.getString("favorite_arena")
                    );
                    topPlayers.add(stats);
                }
            }
        }
        
        return topPlayers;
    }
    
    /**
     * Check if the database connection is valid
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Close database connections
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connections closed successfully");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
        }
    }
}