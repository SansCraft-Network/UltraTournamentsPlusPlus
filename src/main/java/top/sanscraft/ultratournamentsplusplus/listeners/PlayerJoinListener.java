package top.sanscraft.ultratournamentsplusplus.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;

/**
 * Handles player join events
 */
public class PlayerJoinListener implements Listener {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public PlayerJoinListener(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // TODO: Implement player join logic (tournament notifications, etc.)
    }
}