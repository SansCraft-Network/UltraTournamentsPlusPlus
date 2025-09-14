package top.sanscraft.ultratournamentsplusplus;

import org.bukkit.plugin.java.JavaPlugin;
import top.sanscraft.ultratournamentsplusplus.commands.*;
import top.sanscraft.ultratournamentsplusplus.config.ConfigManager;
import top.sanscraft.ultratournamentsplusplus.discord.DiscordBot;
import top.sanscraft.ultratournamentsplusplus.listeners.PlayerJoinListener;
import top.sanscraft.ultratournamentsplusplus.listeners.PlayerQuitListener;
import top.sanscraft.ultratournamentsplusplus.listeners.RoundListener;
import top.sanscraft.ultratournamentsplusplus.listeners.SpectatorListener;
import top.sanscraft.ultratournamentsplusplus.listeners.DiscordPlayerListener;
import top.sanscraft.ultratournamentsplusplus.listeners.TournamentEventListener;
import top.sanscraft.ultratournamentsplusplus.managers.ArenaManager;
import top.sanscraft.ultratournamentsplusplus.managers.KitManager;
import top.sanscraft.ultratournamentsplusplus.managers.RoundManager;
import top.sanscraft.ultratournamentsplusplus.managers.SpectatorManager;
import top.sanscraft.ultratournamentsplusplus.managers.TournamentManager;
import top.sanscraft.ultratournamentsplusplus.managers.DiscordLinkManager;
import top.sanscraft.ultratournamentsplusplus.managers.PlayerStatsManager;
import top.sanscraft.ultratournamentsplusplus.storage.DatabaseManager;
import top.sanscraft.ultratournamentsplusplus.utils.MessageUtils;
import java.util.logging.Level;

/**
 * Main plugin class for UltraTournamentsPlusPlus
 * 
 * This plugin provides tournament management functionality for Minecraft servers
 * with support for Challonge integration and Discord announcements.
 * 
 * @author SansCraft-Network
 * @version 1.0.0
 */
public class UltraTournamentsPlusPlus extends JavaPlugin {
    
    private static UltraTournamentsPlusPlus instance;
    
    // Core managers
    private ConfigManager configManager;
    private TournamentManager tournamentManager;
    private DatabaseManager databaseManager;
    private DiscordBot discordBot;
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private RoundManager roundManager;
    private SpectatorManager spectatorManager;
    private DiscordLinkManager discordLinkManager;
    private PlayerStatsManager playerStatsManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Plugin startup message
        getLogger().info("Starting UltraTournamentsPlusPlus v" + getDescription().getVersion());
        
        try {
            // Initialize configuration
            initializeConfig();
            
            // Initialize managers
            initializeManagers();
            
            // Register commands
            registerCommands();
            
            // Register event listeners
            registerListeners();
            
            // Initialize Discord bot (if enabled)
            initializeDiscord();
            
            getLogger().info("UltraTournamentsPlusPlus has been enabled successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable UltraTournamentsPlusPlus!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Disabling UltraTournamentsPlusPlus...");
        
        try {
            // Shutdown Discord bot
            if (discordBot != null) {
                discordBot.shutdown();
            }
            
            // Shutdown managers
            if (playerStatsManager != null) {
                playerStatsManager.shutdown();
            }
            
            if (discordLinkManager != null) {
                discordLinkManager.shutdown();
            }
            
            if (roundManager != null) {
                roundManager.shutdown();
            }
            
            if (spectatorManager != null) {
                spectatorManager.shutdown();
            }
            
            // Save any pending data
            if (tournamentManager != null) {
                tournamentManager.saveTournaments();
            }
            
            // Close database connections
            if (databaseManager != null) {
                databaseManager.close();
            }
            
            getLogger().info("UltraTournamentsPlusPlus has been disabled successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error while disabling UltraTournamentsPlusPlus", e);
        }
        
        instance = null;
    }
    
    /**
     * Initialize plugin configuration
     */
    private void initializeConfig() {
        getLogger().info("Initializing configuration...");
        configManager = new ConfigManager(this);
        configManager.loadConfig();
    }
    
    /**
     * Initialize core managers
     */
    private void initializeManagers() {
        getLogger().info("Initializing managers...");
        
        // Initialize database manager
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        // Initialize Discord link manager
        discordLinkManager = new DiscordLinkManager(this);
        discordLinkManager.loadLinks();
        
        // Initialize player stats manager
        playerStatsManager = new PlayerStatsManager(this);
        playerStatsManager.loadStats();
        
        // Initialize tournament manager
        tournamentManager = new TournamentManager(this);
        tournamentManager.loadTournaments();
        
        // Initialize arena manager
        arenaManager = new ArenaManager(this);
        arenaManager.initialize();
        
        // Initialize kit manager
        kitManager = new KitManager(this);
        kitManager.initialize();
        
        // Initialize round manager
        roundManager = new RoundManager(this);
        
        // Initialize spectator manager
        spectatorManager = new SpectatorManager(this);
    }
    
    /**
     * Register plugin commands
     */
    private void registerCommands() {
        getLogger().info("Registering commands...");
        
        // Main tournament command
        TournamentCommand tournamentCommand = new TournamentCommand(this);
        getCommand("tournament").setExecutor(tournamentCommand);
        getCommand("tournament").setTabCompleter(tournamentCommand);
        
        // Player commands
        getCommand("tournjoin").setExecutor(new JoinTournamentCommand(this));
        getCommand("tournleave").setExecutor(new LeaveTournamentCommand(this));
        getCommand("tournlist").setExecutor(new ListTournamentsCommand(this));
        getCommand("tourninfo").setExecutor(new TournamentInfoCommand(this));
        
        // Discord link command
        DiscordLinkCommand discordLinkCommand = new DiscordLinkCommand(this);
        getCommand("discord").setExecutor(discordLinkCommand);
        getCommand("discord").setTabCompleter(discordLinkCommand);
        
        // UTP Arena commands
        getCommand("utparena").setExecutor(new UtpArenaCommand(this));
        getCommand("utparena").setTabCompleter(new UtpArenaCommand(this));
        
        // UTP Kit commands
        getCommand("utpkit").setExecutor(new UtpKitCommand(this));
        getCommand("utpkit").setTabCompleter(new UtpKitCommand(this));
    }
    
    /**
     * Register event listeners
     */
    private void registerListeners() {
        getLogger().info("Registering event listeners...");
        
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new RoundListener(this), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(this), this);
        getServer().getPluginManager().registerEvents(new DiscordPlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new TournamentEventListener(this), this);
    }
    
    /**
     * Initialize Discord bot if enabled
     */
    private void initializeDiscord() {
        if (configManager.isDiscordEnabled()) {
            getLogger().info("Initializing Discord bot...");
            try {
                discordBot = new DiscordBot(this);
                discordBot.initialize();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to initialize Discord bot", e);
            }
        } else {
            getLogger().info("Discord integration is disabled in config");
        }
    }
    
    /**
     * Get the plugin instance
     * @return Plugin instance
     */
    public static UltraTournamentsPlusPlus getInstance() {
        return instance;
    }
    
    /**
     * Get the configuration manager
     * @return Configuration manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Get the tournament manager
     * @return Tournament manager
     */
    public TournamentManager getTournamentManager() {
        return tournamentManager;
    }
    
    /**
     * Get the database manager
     * @return Database manager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    /**
     * Get the Discord bot
     * @return Discord bot instance
     */
    public DiscordBot getDiscordBot() {
        return discordBot;
    }
    
    /**
     * Get the arena manager
     * @return Arena manager
     */
    public ArenaManager getArenaManager() {
        return arenaManager;
    }
    
    /**
     * Get the kit manager
     * @return Kit manager
     */
    public KitManager getKitManager() {
        return kitManager;
    }
    
    /**
     * Get the round manager
     * @return Round manager
     */
    public RoundManager getRoundManager() {
        return roundManager;
    }
    
    /**
     * Get the spectator manager
     * @return Spectator manager
     */
    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }
    
    /**
     * Get the Discord link manager
     * @return Discord link manager
     */
    public DiscordLinkManager getDiscordLinkManager() {
        return discordLinkManager;
    }
    
    /**
     * Get the player stats manager
     * @return Player stats manager
     */
    public PlayerStatsManager getPlayerStatsManager() {
        return playerStatsManager;
    }
    
    /**
     * Reload the plugin configuration and managers
     */
    public void reloadPlugin() {
        getLogger().info("Reloading UltraTournamentsPlusPlus...");
        
        try {
            // Reload configuration
            configManager.reloadConfig();
            
            // Reload tournament manager
            tournamentManager.reload();
            
            // Restart Discord bot if configuration changed
            if (discordBot != null) {
                discordBot.shutdown();
            }
            if (configManager.isDiscordEnabled()) {
                discordBot = new DiscordBot(this);
                discordBot.initialize();
            }
            
            MessageUtils.sendConsoleMessage("Plugin reloaded successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to reload plugin!", e);
        }
    }
}