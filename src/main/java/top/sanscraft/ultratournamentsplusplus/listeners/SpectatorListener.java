package top.sanscraft.ultratournamentsplusplus.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.Arena;
import top.sanscraft.ultratournamentsplusplus.models.Round;

/**
 * Handles spectator-related events including boundary checking
 */
public class SpectatorListener implements Listener {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public SpectatorListener(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Prevent spectators from leaving arena bounds
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Only check spectators
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }
        
        // Only check if player is spectating a tournament
        if (!plugin.getSpectatorManager().isSpectating(player)) {
            return;
        }
        
        String tournamentId = plugin.getSpectatorManager().getSpectatingTournament(player);
        if (tournamentId == null) {
            return;
        }
        
        // Check if there's an active round for this tournament
        Round activeRound = plugin.getRoundManager().getActiveRoundForTournament(tournamentId);
        if (activeRound == null) {
            return;
        }
        
        // Get the arena for the active round
        Arena arena = plugin.getArenaManager().getArena(activeRound.getAssignedArena());
        if (arena == null) {
            return;
        }
        
        // Check if player is still in the arena region
        if (!plugin.getSpectatorManager().isPlayerInArenaRegion(player, arena.getWorldGuardRegion())) {
            // Player left arena bounds, teleport them back
            event.setCancelled(true);
            
            // Teleport back to arena or a competitor
            if (!teleportToCompetitorInArena(player, activeRound)) {
                // Fallback: teleport to arena center
                if (arena.getSpawnPoints() != null && !arena.getSpawnPoints().isEmpty()) {
                    player.teleport(arena.getSpawnPoints().get(0).getLocation());
                }
            }
            
            player.sendMessage(ChatColor.RED + "You cannot leave the arena while spectating!");
        }
    }
    
    /**
     * Clean up when spectator disconnects
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getSpectatorManager().isSpectating(player)) {
            plugin.getSpectatorManager().stopSpectating(player);
        }
    }
    
    /**
     * Prevent spectators from teleporting outside arena (except for plugin teleports)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // Only check spectators
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }
        
        // Only check if player is spectating a tournament
        if (!plugin.getSpectatorManager().isSpectating(player)) {
            return;
        }
        
        // Allow plugin-initiated teleports (like stop spectating)
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return;
        }
        
        String tournamentId = plugin.getSpectatorManager().getSpectatingTournament(player);
        if (tournamentId == null) {
            return;
        }
        
        // Check if there's an active round for this tournament
        Round activeRound = plugin.getRoundManager().getActiveRoundForTournament(tournamentId);
        if (activeRound == null) {
            return;
        }
        
        // Get the arena for the active round
        Arena arena = plugin.getArenaManager().getArena(activeRound.getAssignedArena());
        if (arena == null) {
            return;
        }
        
        // Check if destination is within arena bounds
        if (!plugin.getSpectatorManager().isPlayerInArenaRegion(player, arena.getWorldGuardRegion())) {
            // Prevent teleport outside arena
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot teleport outside the arena while spectating!");
        }
    }
    
    /**
     * Teleport spectator to a competitor within the arena
     * @param spectator The spectator player
     * @param round The active round
     * @return True if teleported successfully
     */
    private boolean teleportToCompetitorInArena(Player spectator, Round round) {
        for (java.util.UUID participantId : round.getParticipants()) {
            Player participant = org.bukkit.Bukkit.getPlayer(participantId);
            if (participant != null && participant.isOnline()) {
                // Check if participant is in arena
                Arena arena = plugin.getArenaManager().getArena(round.getAssignedArena());
                if (arena != null && plugin.getSpectatorManager().isPlayerInArenaRegion(participant, arena.getWorldGuardRegion())) {
                    spectator.teleport(participant.getLocation());
                    return true;
                }
            }
        }
        return false;
    }
}