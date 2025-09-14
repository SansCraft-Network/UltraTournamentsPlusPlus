package top.sanscraft.ultratournamentsplusplus.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;

/**
 * Handles player quit events
 */
public class PlayerQuitListener implements Listener {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public PlayerQuitListener(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // TODO: Implement player quit logic (tournament cleanup, etc.)
    }
}