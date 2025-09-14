package top.sanscraft.ultratournamentsplusplus.managers;

import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.PlayerStats;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages player tournament statistics
 */
public class PlayerStatsManager {
    
    private final UltraTournamentsPlusPlus plugin;
    private final Map<UUID, PlayerStats> playerStatsCache;
    private final Map<UUID, Long> roundStartTimes; // Track when players start rounds
    
    public PlayerStatsManager(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
        this.playerStatsCache = new ConcurrentHashMap<>();
        this.roundStartTimes = new ConcurrentHashMap<>();
    }
    
    /**
     * Load player stats from database
     */
    public void loadStats() {
        plugin.getLogger().info("Loading player statistics from database...");
        
        try {
            List<PlayerStats> allStats = plugin.getDatabaseManager().loadAllPlayerStats();
            for (PlayerStats stats : allStats) {
                playerStatsCache.put(stats.getPlayerId(), stats);
            }
            plugin.getLogger().info("Loaded " + allStats.size() + " player statistics");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load player statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Save all cached stats to database
     */
    public void saveAllStats() {
        plugin.getLogger().info("Saving all player statistics to database...");
        
        for (PlayerStats stats : playerStatsCache.values()) {
            try {
                plugin.getDatabaseManager().savePlayerStats(stats);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save stats for " + stats.getPlayerName() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Get or create player stats
     */
    public PlayerStats getPlayerStats(UUID playerId) {
        PlayerStats stats = playerStatsCache.get(playerId);
        if (stats == null) {
            Player player = Bukkit.getPlayer(playerId);
            String playerName = player != null ? player.getName() : "Unknown";
            
            // Try loading from database first
            try {
                stats = plugin.getDatabaseManager().loadPlayerStats(playerId);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load stats for " + playerId + ": " + e.getMessage());
            }
            
            // Create new stats if not found
            if (stats == null) {
                stats = new PlayerStats(playerId, playerName);
            }
            
            playerStatsCache.put(playerId, stats);
        }
        return stats;
    }
    
    /**
     * Get player stats by player object
     */
    public PlayerStats getPlayerStats(Player player) {
        return getPlayerStats(player.getUniqueId());
    }
    
    /**
     * Record a tournament participation
     */
    public void recordTournamentParticipation(UUID playerId, boolean won) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.recordTournament(won);
        savePlayerStatsAsync(stats);
        
        plugin.getLogger().info("Recorded tournament " + (won ? "win" : "participation") + 
                               " for " + stats.getPlayerName());
    }
    
    /**
     * Record a round start
     */
    public void recordRoundStart(UUID playerId) {
        roundStartTimes.put(playerId, System.currentTimeMillis());
    }
    
    /**
     * Record a round result
     */
    public void recordRoundResult(UUID playerId, boolean won) {
        PlayerStats stats = getPlayerStats(playerId);
        
        // Calculate round duration
        long duration = 0;
        Long startTime = roundStartTimes.remove(playerId);
        if (startTime != null) {
            duration = System.currentTimeMillis() - startTime;
        }
        
        stats.recordRound(won, duration);
        savePlayerStatsAsync(stats);
        
        plugin.getLogger().info("Recorded round " + (won ? "win" : "loss") + 
                               " for " + stats.getPlayerName() + 
                               (duration > 0 ? " (duration: " + (duration / 1000) + "s)" : ""));
    }
    
    /**
     * Record kills and deaths for a player
     */
    public void recordKillsDeaths(UUID playerId, int kills, int deaths) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.recordKillsDeaths(kills, deaths);
        savePlayerStatsAsync(stats);
        
        if (kills > 0 || deaths > 0) {
            plugin.getLogger().info("Recorded " + kills + " kills, " + deaths + 
                                   " deaths for " + stats.getPlayerName());
        }
    }
    
    /**
     * Record a single kill
     */
    public void recordKill(UUID killerId) {
        recordKillsDeaths(killerId, 1, 0);
    }
    
    /**
     * Record a single death
     */
    public void recordDeath(UUID deadPlayerId) {
        recordKillsDeaths(deadPlayerId, 0, 1);
    }
    
    /**
     * Add playtime to a player's stats
     */
    public void addPlaytime(UUID playerId, long playtimeMs) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.addPlaytime(playtimeMs);
        savePlayerStatsAsync(stats);
    }
    
    /**
     * Update favorite kit for a player
     */
    public void updateFavoriteKit(UUID playerId, String kitName) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.updateFavoriteKit(kitName);
        savePlayerStatsAsync(stats);
    }
    
    /**
     * Update favorite arena for a player
     */
    public void updateFavoriteArena(UUID playerId, String arenaName) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.updateFavoriteArena(arenaName);
        savePlayerStatsAsync(stats);
    }
    
    /**
     * Reset win/loss streaks for all players (e.g., at start of new tournament)
     */
    public void resetAllStreaks() {
        for (PlayerStats stats : playerStatsCache.values()) {
            stats.resetStreaks();
        }
        plugin.getLogger().info("Reset win/loss streaks for all players");
    }
    
    /**
     * Reset win/loss streaks for specific players
     */
    public void resetStreaks(List<UUID> playerIds) {
        for (UUID playerId : playerIds) {
            PlayerStats stats = getPlayerStats(playerId);
            stats.resetStreaks();
        }
        plugin.getLogger().info("Reset win/loss streaks for " + playerIds.size() + " players");
    }
    
    /**
     * Get top players by tournaments won
     */
    public List<PlayerStats> getTopPlayersByTournaments(int limit) {
        try {
            return plugin.getDatabaseManager().getTopPlayersByTournaments(limit);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get top players from database: " + e.getMessage());
            
            // Fall back to cached data
            return playerStatsCache.values().stream()
                    .sorted((a, b) -> {
                        int tournamentCompare = Integer.compare(b.getTournamentsWon(), a.getTournamentsWon());
                        if (tournamentCompare != 0) return tournamentCompare;
                        return Integer.compare(b.getRoundsWon(), a.getRoundsWon());
                    })
                    .limit(limit)
                    .toList();
        }
    }
    
    /**
     * Get stats for multiple players
     */
    public List<PlayerStats> getPlayerStats(List<UUID> playerIds) {
        List<PlayerStats> statsList = new ArrayList<>();
        for (UUID playerId : playerIds) {
            statsList.add(getPlayerStats(playerId));
        }
        return statsList;
    }
    
    /**
     * Update player name (in case of name change)
     */
    public void updatePlayerName(UUID playerId, String newName) {
        PlayerStats stats = getPlayerStats(playerId);
        if (!stats.getPlayerName().equals(newName)) {
            stats.setPlayerName(newName);
            savePlayerStatsAsync(stats);
            plugin.getLogger().info("Updated player name for " + playerId + " to " + newName);
        }
    }
    
    /**
     * Get total statistics summary
     */
    public String getStatsSummary() {
        int totalPlayers = playerStatsCache.size();
        int totalTournaments = playerStatsCache.values().stream()
                .mapToInt(PlayerStats::getTournamentsPlayed)
                .sum();
        int totalRounds = playerStatsCache.values().stream()
                .mapToInt(PlayerStats::getRoundsPlayed)
                .sum();
        
        return String.format("Total Players: %d, Total Tournaments: %d, Total Rounds: %d", 
                           totalPlayers, totalTournaments, totalRounds);
    }
    
    /**
     * Check if a player has any recorded stats
     */
    public boolean hasStats(UUID playerId) {
        PlayerStats stats = playerStatsCache.get(playerId);
        if (stats == null) {
            try {
                stats = plugin.getDatabaseManager().loadPlayerStats(playerId);
                return stats != null;
            } catch (Exception e) {
                return false;
            }
        }
        return stats.getTournamentsPlayed() > 0 || stats.getRoundsPlayed() > 0;
    }
    
    /**
     * Save player stats asynchronously
     */
    private void savePlayerStatsAsync(PlayerStats stats) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getDatabaseManager().savePlayerStats(stats);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save stats for " + stats.getPlayerName() + ": " + e.getMessage());
            }
        });
    }
    
    /**
     * Handle player joining the server
     */
    public void onPlayerJoin(Player player) {
        // Ensure player has stats entry and update name if changed
        updatePlayerName(player.getUniqueId(), player.getName());
    }
    
    /**
     * Handle player leaving the server
     */
    public void onPlayerQuit(Player player) {
        // Clean up round start time tracking
        roundStartTimes.remove(player.getUniqueId());
        
        // Save stats before player leaves
        PlayerStats stats = playerStatsCache.get(player.getUniqueId());
        if (stats != null) {
            savePlayerStatsAsync(stats);
        }
    }
    
    /**
     * Reload the stats manager
     */
    public void reload() {
        playerStatsCache.clear();
        roundStartTimes.clear();
        loadStats();
        plugin.getLogger().info("Player stats manager reloaded");
    }
    
    /**
     * Shutdown the stats manager
     */
    public void shutdown() {
        saveAllStats();
        playerStatsCache.clear();
        roundStartTimes.clear();
        plugin.getLogger().info("Player stats manager shutdown");
    }
}