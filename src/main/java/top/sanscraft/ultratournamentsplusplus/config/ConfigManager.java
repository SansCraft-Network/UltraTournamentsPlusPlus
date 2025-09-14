package top.sanscraft.ultratournamentsplusplus.config;

import org.bukkit.configuration.file.FileConfiguration;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;

/**
 * Manages plugin configuration
 */
public class ConfigManager {
    
    private final UltraTournamentsPlusPlus plugin;
    private FileConfiguration config;
    
    public ConfigManager(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Load configuration from file
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        plugin.getLogger().info("Configuration loaded successfully!");
    }
    
    /**
     * Reload configuration
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        plugin.getLogger().info("Configuration reloaded!");
    }
    
    /**
     * Check if Discord integration is enabled
     * @return True if Discord is enabled
     */
    public boolean isDiscordEnabled() {
        return config.getBoolean("discord.enabled", false);
    }
    
    /**
     * Get Discord bot token
     * @return Discord bot token
     */
    public String getDiscordToken() {
        return config.getString("discord.token", "");
    }
    
    /**
     * Get Discord guild ID
     * @return Discord guild ID
     */
    public String getDiscordGuildId() {
        return config.getString("discord.guild-id", "");
    }
    
    /**
     * Get Discord announcement channel ID
     * @return Discord channel ID
     */
    public String getDiscordAnnouncementChannel() {
        return config.getString("discord.announcement-channel", "");
    }
    
    /**
     * Get Discord results channel ID
     * @return Discord results channel ID
     */
    public String getDiscordResultsChannel() {
        return config.getString("discord.results-channel", "");
    }
    
    /**
     * Check if Challonge integration is enabled
     * @return True if Challonge is enabled
     */
    public boolean isChallongeEnabled() {
        return config.getBoolean("challonge.enabled", false);
    }
    
    /**
     * Get Challonge API key
     * @return Challonge API key
     */
    public String getChallongeApiKey() {
        return config.getString("challonge.api-key", "");
    }
    
    /**
     * Get database type
     * @return Database type (sqlite, mysql, etc.)
     */
    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }
    
    /**
     * Get database connection string
     * @return Database connection string
     */
    public String getDatabaseUrl() {
        return config.getString("database.url", "");
    }
}