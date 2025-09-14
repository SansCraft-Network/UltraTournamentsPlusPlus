package top.sanscraft.ultratournamentsplusplus.utils;

import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.Round;
import top.sanscraft.ultratournamentsplusplus.events.RoundCompleteEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles round scheduling and notifications
 */
public class RoundScheduler {
    
    private final UltraTournamentsPlusPlus plugin;
    private final Map<String, Integer> pendingNotifications;
    
    public RoundScheduler(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
        this.pendingNotifications = new ConcurrentHashMap<>();
    }
    
    /**
     * Schedule notifications for players before their round
     * @param round The round that will start
     * @param roundsUntilStart Number of rounds until this round starts
     */
    public void scheduleRoundNotifications(Round round, int roundsUntilStart) {
        if (roundsUntilStart == 3) {
            // Send Discord notifications 3 rounds before
            notifyPlayersOnDiscord(round, roundsUntilStart);
        }
        
        // Schedule task to check player presence when round starts
        scheduleRoundStartCheck(round, roundsUntilStart);
    }
    
    /**
     * Notify players on Discord about upcoming round
     */
    private void notifyPlayersOnDiscord(Round round, int roundsUntil) {
        if (plugin.getDiscordBot() == null || !plugin.getDiscordBot().isReady()) {
            return;
        }
        
        List<UUID> participants = round.getParticipants();
        String tournamentName = "Tournament"; // You'd get this from the tournament manager
        
        plugin.getDiscordBot().notifyUpcomingRound(participants, tournamentName, roundsUntil);
        
        plugin.getLogger().info("Sent Discord notifications for round " + round.getId() + 
                               " to " + participants.size() + " players");
    }
    
    /**
     * Schedule a check when the round is supposed to start
     */
    private void scheduleRoundStartCheck(Round round, int roundsUntilStart) {
        // Calculate delay based on average round time (estimate 5 minutes per round)
        long delayTicks = roundsUntilStart * 5 * 60 * 20L; // 5 minutes in ticks
        
        new BukkitRunnable() {
            @Override
            public void run() {
                checkRoundStart(round);
            }
        }.runTaskLater(plugin, delayTicks);
        
        String taskId = round.getId() + "_start_check";
        pendingNotifications.put(taskId, roundsUntilStart);
    }
    
    /**
     * Check if players are online when their round starts
     */
    private void checkRoundStart(Round round) {
        List<UUID> participants = round.getParticipants();
        List<UUID> onlineParticipants = participants.stream()
                .filter(this::isPlayerOnline)
                .toList();
        
        List<UUID> offlineParticipants = participants.stream()
                .filter(uuid -> !isPlayerOnline(uuid))
                .toList();
        
        plugin.getLogger().info("Round " + round.getId() + " starting check: " + 
                               onlineParticipants.size() + " online, " + 
                               offlineParticipants.size() + " offline");
        
        // Handle offline players
        for (UUID offlinePlayer : offlineParticipants) {
            handlePlayerAbsent(round, offlinePlayer, onlineParticipants);
        }
        
        // If round can still proceed with remaining players, start it
        if (onlineParticipants.size() >= 1 && round.getStatus() == Round.RoundStatus.PENDING) {
            startRoundWithOnlinePlayers(round, onlineParticipants);
        }
    }
    
    /**
     * Handle a player who is absent when their round starts
     */
    private void handlePlayerAbsent(Round round, UUID absentPlayer, List<UUID> onlineParticipants) {
        String playerName = getPlayerName(absentPlayer);
        
        plugin.getLogger().info("Player " + playerName + " is absent for round " + round.getId());
        
        // Remove absent player from round
        round.removeParticipant(absentPlayer);
        
        // If this was a 1v1 round and one player is absent, declare the other winner
        if (round.getParticipants().size() == 1 && onlineParticipants.size() == 1) {
            UUID winner = onlineParticipants.get(0);
            String winnerName = getPlayerName(winner);
            
            // Declare winner by forfeit
            round.setWinner(winner);
            
            plugin.getLogger().info("Round " + round.getId() + " won by forfeit: " + winnerName);
            
            // Broadcast forfeit message
            String message = "§e" + winnerName + " §awins round " + round.getId() + " by forfeit! " +
                           "§c" + playerName + " §cwas not online.";
            Bukkit.broadcastMessage(message);
            
            // Fire round complete event
            RoundCompleteEvent event = new RoundCompleteEvent(round, winner, winnerName, 
                                                             absentPlayer, playerName);
            Bukkit.getPluginManager().callEvent(event);
        }
        
        // Notify absent player on Discord if linked
        notifyAbsentPlayer(absentPlayer, round);
    }
    
    /**
     * Notify an absent player via Discord
     */
    private void notifyAbsentPlayer(UUID absentPlayer, Round round) {
        if (plugin.getDiscordBot() == null || !plugin.getDiscordBot().isReady()) {
            return;
        }
        
        String discordId = plugin.getDiscordLinkManager().getDiscordId(absentPlayer);
        if (discordId != null) {
            String message = "⚠️ **You missed your tournament round!**\n" +
                           "You were not online when round " + round.getId() + " started.\n" +
                           "Make sure to be online for future tournament rounds!";
            
            plugin.getDiscordBot().sendDirectMessage(discordId, message);
        }
    }
    
    /**
     * Start a round with only the online players
     */
    private void startRoundWithOnlinePlayers(Round round, List<UUID> onlinePlayers) {
        if (onlinePlayers.isEmpty()) {
            plugin.getLogger().warning("Cannot start round " + round.getId() + " - no players online");
            return;
        }
        
        if (onlinePlayers.size() == 1) {
            // Only one player online, they win by default
            UUID winner = onlinePlayers.get(0);
            String winnerName = getPlayerName(winner);
            
            round.setWinner(winner);
            
            plugin.getLogger().info("Round " + round.getId() + " won by default: " + winnerName);
            
            String message = "§e" + winnerName + " §awins round " + round.getId() + " by default!";
            Bukkit.broadcastMessage(message);
            
            return;
        }
        
        // Multiple players online, start the round normally
        round.startRound();
        
        plugin.getLogger().info("Starting round " + round.getId() + " with " + onlinePlayers.size() + " players");
        
        // Record round start for stats
        if (plugin.getPlayerStatsManager() != null) {
            for (UUID playerId : onlinePlayers) {
                plugin.getPlayerStatsManager().recordRoundStart(playerId);
            }
        }
        
        // Teleport players to arena, apply kits, etc.
        // This would be handled by the round manager
        if (plugin.getRoundManager() != null) {
            // plugin.getRoundManager().startRound(round);
        }
    }
    
    /**
     * Check if a player is online
     */
    private boolean isPlayerOnline(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        return player != null && player.isOnline();
    }
    
    /**
     * Get player name by UUID
     */
    private String getPlayerName(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            return player.getName();
        }
        
        // Try to get from offline player
        return Bukkit.getOfflinePlayer(playerId).getName();
    }
    
    /**
     * Cancel all pending notifications
     */
    public void cancelAllNotifications() {
        pendingNotifications.clear();
        plugin.getLogger().info("Cancelled all pending round notifications");
    }
    
    /**
     * Cancel notifications for a specific round
     */
    public void cancelRoundNotifications(String roundId) {
        pendingNotifications.entrySet().removeIf(entry -> entry.getKey().startsWith(roundId));
        plugin.getLogger().info("Cancelled notifications for round " + roundId);
    }
}