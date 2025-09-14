package top.sanscraft.ultratournamentsplusplus.models;

import java.util.UUID;

/**
 * Represents player tournament statistics
 */
public class PlayerStats {
    
    private UUID playerId;
    private String playerName;
    private int tournamentsPlayed;
    private int tournamentsWon;
    private int roundsPlayed;
    private int roundsWon;
    private int totalKills;
    private int totalDeaths;
    private long totalPlayTime; // in milliseconds
    private long firstPlayed;
    private long lastPlayed;
    private int winStreak;
    private int bestWinStreak;
    private int lossStreak;
    private double averageRoundDuration;
    private String favoriteKit;
    private String favoriteArena;
    
    public PlayerStats(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.tournamentsPlayed = 0;
        this.tournamentsWon = 0;
        this.roundsPlayed = 0;
        this.roundsWon = 0;
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.totalPlayTime = 0;
        this.firstPlayed = System.currentTimeMillis();
        this.lastPlayed = System.currentTimeMillis();
        this.winStreak = 0;
        this.bestWinStreak = 0;
        this.lossStreak = 0;
        this.averageRoundDuration = 0.0;
        this.favoriteKit = "";
        this.favoriteArena = "";
    }
    
    // Constructor for loading from database
    public PlayerStats(UUID playerId, String playerName, int tournamentsPlayed, int tournamentsWon,
                      int roundsPlayed, int roundsWon, int totalKills, int totalDeaths,
                      long totalPlayTime, long firstPlayed, long lastPlayed, int winStreak,
                      int bestWinStreak, int lossStreak, double averageRoundDuration,
                      String favoriteKit, String favoriteArena) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.tournamentsPlayed = tournamentsPlayed;
        this.tournamentsWon = tournamentsWon;
        this.roundsPlayed = roundsPlayed;
        this.roundsWon = roundsWon;
        this.totalKills = totalKills;
        this.totalDeaths = totalDeaths;
        this.totalPlayTime = totalPlayTime;
        this.firstPlayed = firstPlayed;
        this.lastPlayed = lastPlayed;
        this.winStreak = winStreak;
        this.bestWinStreak = bestWinStreak;
        this.lossStreak = lossStreak;
        this.averageRoundDuration = averageRoundDuration;
        this.favoriteKit = favoriteKit != null ? favoriteKit : "";
        this.favoriteArena = favoriteArena != null ? favoriteArena : "";
    }
    
    // Getters and setters
    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public int getTournamentsPlayed() { return tournamentsPlayed; }
    public void setTournamentsPlayed(int tournamentsPlayed) { this.tournamentsPlayed = tournamentsPlayed; }
    
    public int getTournamentsWon() { return tournamentsWon; }
    public void setTournamentsWon(int tournamentsWon) { this.tournamentsWon = tournamentsWon; }
    
    public int getRoundsPlayed() { return roundsPlayed; }
    public void setRoundsPlayed(int roundsPlayed) { this.roundsPlayed = roundsPlayed; }
    
    public int getRoundsWon() { return roundsWon; }
    public void setRoundsWon(int roundsWon) { this.roundsWon = roundsWon; }
    
    public int getTotalKills() { return totalKills; }
    public void setTotalKills(int totalKills) { this.totalKills = totalKills; }
    
    public int getTotalDeaths() { return totalDeaths; }
    public void setTotalDeaths(int totalDeaths) { this.totalDeaths = totalDeaths; }
    
    public long getTotalPlayTime() { return totalPlayTime; }
    public void setTotalPlayTime(long totalPlayTime) { this.totalPlayTime = totalPlayTime; }
    
    public long getFirstPlayed() { return firstPlayed; }
    public void setFirstPlayed(long firstPlayed) { this.firstPlayed = firstPlayed; }
    
    public long getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(long lastPlayed) { this.lastPlayed = lastPlayed; }
    
    public int getWinStreak() { return winStreak; }
    public void setWinStreak(int winStreak) { this.winStreak = winStreak; }
    
    public int getBestWinStreak() { return bestWinStreak; }
    public void setBestWinStreak(int bestWinStreak) { this.bestWinStreak = bestWinStreak; }
    
    public int getLossStreak() { return lossStreak; }
    public void setLossStreak(int lossStreak) { this.lossStreak = lossStreak; }
    
    public double getAverageRoundDuration() { return averageRoundDuration; }
    public void setAverageRoundDuration(double averageRoundDuration) { this.averageRoundDuration = averageRoundDuration; }
    
    public String getFavoriteKit() { return favoriteKit; }
    public void setFavoriteKit(String favoriteKit) { this.favoriteKit = favoriteKit != null ? favoriteKit : ""; }
    
    public String getFavoriteArena() { return favoriteArena; }
    public void setFavoriteArena(String favoriteArena) { this.favoriteArena = favoriteArena != null ? favoriteArena : ""; }
    
    // Calculated stats
    
    /**
     * Get tournament win percentage
     * @return Win percentage (0.0 to 100.0)
     */
    public double getTournamentWinPercentage() {
        if (tournamentsPlayed == 0) return 0.0;
        return (double) tournamentsWon / tournamentsPlayed * 100.0;
    }
    
    /**
     * Get round win percentage
     * @return Win percentage (0.0 to 100.0)
     */
    public double getRoundWinPercentage() {
        if (roundsPlayed == 0) return 0.0;
        return (double) roundsWon / roundsPlayed * 100.0;
    }
    
    /**
     * Get kill/death ratio
     * @return K/D ratio
     */
    public double getKillDeathRatio() {
        if (totalDeaths == 0) {
            return totalKills > 0 ? totalKills : 0.0;
        }
        return (double) totalKills / totalDeaths;
    }
    
    /**
     * Get rounds lost
     * @return Number of rounds lost
     */
    public int getRoundsLost() {
        return roundsPlayed - roundsWon;
    }
    
    /**
     * Get tournaments lost
     * @return Number of tournaments lost
     */
    public int getTournamentsLost() {
        return tournamentsPlayed - tournamentsWon;
    }
    
    /**
     * Get average playtime per tournament in minutes
     * @return Average playtime in minutes
     */
    public double getAveragePlaytimePerTournament() {
        if (tournamentsPlayed == 0) return 0.0;
        return (double) (totalPlayTime / 1000 / 60) / tournamentsPlayed;
    }
    
    /**
     * Get formatted total playtime
     * @return Formatted playtime string (e.g., "2h 30m")
     */
    public String getFormattedTotalPlaytime() {
        long totalMinutes = totalPlayTime / 1000 / 60;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }
    
    /**
     * Get player rank based on tournaments won
     * @return Rank string
     */
    public String getRank() {
        if (tournamentsWon >= 100) return "Legend";
        if (tournamentsWon >= 50) return "Master";
        if (tournamentsWon >= 25) return "Expert";
        if (tournamentsWon >= 10) return "Veteran";
        if (tournamentsWon >= 5) return "Skilled";
        if (tournamentsWon >= 1) return "Fighter";
        return "Rookie";
    }
    
    // Stat modification methods
    
    /**
     * Record a tournament participation
     * @param won Whether the tournament was won
     */
    public void recordTournament(boolean won) {
        tournamentsPlayed++;
        if (won) {
            tournamentsWon++;
        }
        lastPlayed = System.currentTimeMillis();
    }
    
    /**
     * Record a round result
     * @param won Whether the round was won
     * @param duration Round duration in milliseconds
     */
    public void recordRound(boolean won, long duration) {
        roundsPlayed++;
        if (won) {
            roundsWon++;
            winStreak++;
            lossStreak = 0;
            if (winStreak > bestWinStreak) {
                bestWinStreak = winStreak;
            }
        } else {
            winStreak = 0;
            lossStreak++;
        }
        
        // Update average round duration
        if (duration > 0) {
            double newDuration = duration / 1000.0; // Convert to seconds
            if (averageRoundDuration == 0) {
                averageRoundDuration = newDuration;
            } else {
                averageRoundDuration = (averageRoundDuration * (roundsPlayed - 1) + newDuration) / roundsPlayed;
            }
        }
        
        lastPlayed = System.currentTimeMillis();
    }
    
    /**
     * Record kills and deaths
     * @param kills Number of kills
     * @param deaths Number of deaths
     */
    public void recordKillsDeaths(int kills, int deaths) {
        totalKills += kills;
        totalDeaths += deaths;
    }
    
    /**
     * Add playtime
     * @param playtime Playtime in milliseconds
     */
    public void addPlaytime(long playtime) {
        totalPlayTime += playtime;
    }
    
    /**
     * Update favorite kit (most used)
     * @param kitName Kit name
     */
    public void updateFavoriteKit(String kitName) {
        // This is simplified - in practice you'd track usage counts
        this.favoriteKit = kitName;
    }
    
    /**
     * Update favorite arena (most played)
     * @param arenaName Arena name
     */
    public void updateFavoriteArena(String arenaName) {
        // This is simplified - in practice you'd track usage counts
        this.favoriteArena = arenaName;
    }
    
    /**
     * Reset win/loss streaks (useful for new tournaments)
     */
    public void resetStreaks() {
        winStreak = 0;
        lossStreak = 0;
    }
    
    /**
     * Get a formatted stats summary for Discord
     * @return Formatted stats string
     */
    public String getDiscordStatsSummary() {
        return String.format(
            "**%s** (%s)\n" +
            "🏆 **Tournaments:** %d played, %d won (%.1f%%)\n" +
            "⚔️ **Rounds:** %d played, %d won (%.1f%%)\n" +
            "💀 **K/D Ratio:** %.2f (%d kills, %d deaths)\n" +
            "🔥 **Win Streak:** %d (Best: %d)\n" +
            "⏱️ **Total Playtime:** %s\n" +
            "🎯 **Favorite Kit:** %s\n" +
            "🏟️ **Favorite Arena:** %s",
            playerName, getRank(),
            tournamentsPlayed, tournamentsWon, getTournamentWinPercentage(),
            roundsPlayed, roundsWon, getRoundWinPercentage(),
            getKillDeathRatio(), totalKills, totalDeaths,
            winStreak, bestWinStreak,
            getFormattedTotalPlaytime(),
            favoriteKit.isEmpty() ? "None" : favoriteKit,
            favoriteArena.isEmpty() ? "None" : favoriteArena
        );
    }
    
    @Override
    public String toString() {
        return String.format("PlayerStats{player=%s, tournaments=%d/%d, rounds=%d/%d, kdr=%.2f}", 
            playerName, tournamentsWon, tournamentsPlayed, roundsWon, roundsPlayed, getKillDeathRatio());
    }
}