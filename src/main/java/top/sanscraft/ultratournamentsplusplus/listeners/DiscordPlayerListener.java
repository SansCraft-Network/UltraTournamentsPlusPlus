package top.sanscraft.ultratournamentsplusplus.listeners;

import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.DiscordLink;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

/**
 * Handles Discord-related player events
 */
public class DiscordPlayerListener implements Listener {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public DiscordPlayerListener(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Handle stats manager player join
        if (plugin.getPlayerStatsManager() != null) {
            plugin.getPlayerStatsManager().onPlayerJoin(player);
        }
        
        // Check if player has Discord link and send welcome message
        if (plugin.getDiscordLinkManager() != null && 
            plugin.getDiscordLinkManager().isMinecraftLinked(player.getUniqueId())) {
            
            DiscordLink link = plugin.getDiscordLinkManager().getLinkByMinecraftUuid(player.getUniqueId());
            if (link != null) {
                player.sendMessage("§a[Discord] §fWelcome back! Your Discord account is linked.");
                player.sendMessage("§7Use §e/discord status §7to check your linking status.");
            }
        } else {
            // Send info about Discord linking for new/unlinked players
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§6[Tournament] §fDid you know you can link your Discord account?");
                player.sendMessage("§7Use §e/discord help §7to learn how to link accounts and get notifications!");
            }, 100L); // Send after 5 seconds
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Handle stats manager player quit
        if (plugin.getPlayerStatsManager() != null) {
            plugin.getPlayerStatsManager().onPlayerQuit(player);
        }
    }
}