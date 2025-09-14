package top.sanscraft.ultratournamentsplusplus.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility class for message handling
 */
public class MessageUtils {
    
    public static final String PREFIX = "§6[UltraTournaments++] §r";
    
    /**
     * Send a formatted message to a player
     * @param player Target player
     * @param message Message to send
     */
    public static void sendMessage(Player player, String message) {
        player.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Send a formatted message to a command sender
     * @param sender Target sender
     * @param message Message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Send a success message
     * @param sender Target sender
     * @param message Success message
     */
    public static void sendSuccess(CommandSender sender, String message) {
        sendMessage(sender, "§a" + message);
    }
    
    /**
     * Send an error message
     * @param sender Target sender
     * @param message Error message
     */
    public static void sendError(CommandSender sender, String message) {
        sendMessage(sender, "§c" + message);
    }
    
    /**
     * Send a warning message
     * @param sender Target sender
     * @param message Warning message
     */
    public static void sendWarning(CommandSender sender, String message) {
        sendMessage(sender, "§e" + message);
    }
    
    /**
     * Send an info message
     * @param sender Target sender
     * @param message Info message
     */
    public static void sendInfo(CommandSender sender, String message) {
        sendMessage(sender, "§b" + message);
    }
    
    /**
     * Send a message to console
     * @param message Message to send
     */
    public static void sendConsoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Broadcast a message to all online players
     * @param message Message to broadcast
     */
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Broadcast a message to players with specific permission
     * @param message Message to broadcast
     * @param permission Required permission
     */
    public static void broadcastWithPermission(String message, String permission) {
        String formattedMessage = PREFIX + ChatColor.translateAlternateColorCodes('&', message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(formattedMessage);
            }
        }
    }
}