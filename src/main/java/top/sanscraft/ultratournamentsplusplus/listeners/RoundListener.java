package top.sanscraft.ultratournamentsplusplus.listeners;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.Arena;
import top.sanscraft.ultratournamentsplusplus.models.Round;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;

/**
 * Listener for round-related events including automatic winner detection
 */
public class RoundListener implements Listener {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public RoundListener(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();
        
        // Check if player is in an active round
        Round round = plugin.getRoundManager().getPlayerRound(playerId);
        if (round == null || !round.isActive()) {
            return;
        }
        
        // Check if the death occurred in the assigned arena
        if (!isPlayerInArenaRegion(player, round)) {
            return;
        }
        
        // Only auto-detect winner if enabled for this round
        if (!round.isAutoDetectWinner()) {
            return;
        }
        
        // Determine winner based on round type
        UUID winnerId = determineWinnerAfterDeath(round, playerId);
        if (winnerId != null) {
            // Prevent normal death behavior in tournament rounds
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
            
            // Declare winner
            plugin.getRoundManager().declareWinner(round.getId(), winnerId);
            
            plugin.getLogger().info("Player " + player.getName() + " died in round " + round.getId() + 
                                  ", winner determined: " + winnerId);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check if player is in an active round
        Round round = plugin.getRoundManager().getPlayerRound(playerId);
        if (round == null || !round.isActive()) {
            return;
        }
        
        // Only auto-detect winner if enabled for this round
        if (!round.isAutoDetectWinner()) {
            return;
        }
        
        // Determine winner based on remaining players
        UUID winnerId = determineWinnerAfterDisconnect(round, playerId);
        if (winnerId != null) {
            plugin.getRoundManager().declareWinner(round.getId(), winnerId);
            
            plugin.getLogger().info("Player " + player.getName() + " disconnected from round " + round.getId() + 
                                  ", winner determined: " + winnerId);
        }
    }
    
    /**
     * Check if a player is currently in their assigned arena region
     * @param player The player to check
     * @param round The round the player is in
     * @return True if player is in the arena region
     */
    private boolean isPlayerInArenaRegion(Player player, Round round) {
        if (!round.hasAssignedArena()) {
            // If no arena assigned, consider any location valid
            return true;
        }
        
        Arena arena = plugin.getArenaManager().getArena(round.getAssignedArena());
        if (arena == null || arena.getWorldGuardRegion() == null || arena.getWorldGuardRegion().isEmpty()) {
            // If arena or region not found, consider any location valid
            return true;
        }
        
        // Check if player is in the WorldGuard region
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(player.getWorld()));
        
        if (regionManager == null) {
            return true;
        }
        
        ProtectedRegion region = regionManager.getRegion(arena.getWorldGuardRegion());
        if (region == null) {
            return true;
        }
        
        com.sk89q.worldedit.util.Location weLocation = 
            com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(player.getLocation());
        
        return region.contains(weLocation.toVector().toBlockPoint());
    }
    
    /**
     * Determine the winner after a player death
     * @param round The round
     * @param deadPlayerId The player who died
     * @return Winner's UUID or null if no winner yet
     */
    private UUID determineWinnerAfterDeath(Round round, UUID deadPlayerId) {
        List<UUID> participants = round.getParticipants();
        
        if (round.is1v1()) {
            // In 1v1, the other player wins
            for (UUID participantId : participants) {
                if (!participantId.equals(deadPlayerId)) {
                    return participantId;
                }
            }
        } else if (round.isMultiPlayer()) {
            // In multi-player rounds, check if only one player remains alive
            List<UUID> aliveParticipants = getAliveParticipants(round, deadPlayerId);
            if (aliveParticipants.size() == 1) {
                return aliveParticipants.get(0);
            }
        }
        
        return null;
    }
    
    /**
     * Determine the winner after a player disconnects
     * @param round The round
     * @param disconnectedPlayerId The player who disconnected
     * @return Winner's UUID or null if no winner yet
     */
    private UUID determineWinnerAfterDisconnect(Round round, UUID disconnectedPlayerId) {
        List<UUID> participants = round.getParticipants();
        
        if (round.is1v1()) {
            // In 1v1, the other player wins by forfeit
            for (UUID participantId : participants) {
                if (!participantId.equals(disconnectedPlayerId)) {
                    return participantId;
                }
            }
        } else if (round.isMultiPlayer()) {
            // In multi-player rounds, check if only one player remains online
            List<UUID> onlineParticipants = getOnlineParticipants(round, disconnectedPlayerId);
            if (onlineParticipants.size() == 1) {
                return onlineParticipants.get(0);
            }
        }
        
        return null;
    }
    
    /**
     * Get list of participants who are still alive (not the dead player)
     * @param round The round
     * @param deadPlayerId The player who died
     * @return List of alive participant UUIDs
     */
    private List<UUID> getAliveParticipants(Round round, UUID deadPlayerId) {
        List<UUID> alive = round.getOtherParticipants(deadPlayerId);
        
        // In the future, this could check for other conditions like health, 
        // but for now we assume all other participants are alive
        return alive;
    }
    
    /**
     * Get list of participants who are still online (not the disconnected player)
     * @param round The round
     * @param disconnectedPlayerId The player who disconnected
     * @return List of online participant UUIDs
     */
    private List<UUID> getOnlineParticipants(Round round, UUID disconnectedPlayerId) {
        List<UUID> online = round.getOtherParticipants(disconnectedPlayerId);
        
        // Filter to only include actually online players
        online.removeIf(playerId -> {
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            return player == null || !player.isOnline();
        });
        
        return online;
    }
}