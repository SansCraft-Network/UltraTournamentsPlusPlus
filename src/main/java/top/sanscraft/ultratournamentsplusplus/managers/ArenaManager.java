package top.sanscraft.ultratournamentsplusplus.managers;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.Arena;
import top.sanscraft.ultratournamentsplusplus.models.ArenaType;
import top.sanscraft.ultratournamentsplusplus.models.SpawnPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages all arena operations and WorldGuard integration
 */
public class ArenaManager {
    
    private final UltraTournamentsPlusPlus plugin;
    private final Map<String, Arena> arenas;
    private boolean worldGuardEnabled;
    
    public ArenaManager(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.worldGuardEnabled = checkWorldGuardAvailability();
    }
    
    /**
     * Initialize the arena manager
     */
    public void initialize() {
        plugin.getLogger().info("Initializing ArenaManager...");
        loadArenas();
        
        if (worldGuardEnabled) {
            plugin.getLogger().info("WorldGuard integration enabled");
        } else {
            plugin.getLogger().warning("WorldGuard not found - region protection features disabled");
        }
    }
    
    /**
     * Check if WorldGuard is available
     * @return True if WorldGuard is loaded and available
     */
    private boolean checkWorldGuardAvailability() {
        try {
            return Bukkit.getPluginManager().getPlugin("WorldGuard") != null && 
                   Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check WorldGuard availability", e);
            return false;
        }
    }
    
    /**
     * Load arenas from storage
     */
    public void loadArenas() {
        // TODO: Implement arena loading from database/files
        plugin.getLogger().info("Loading arenas from storage...");
    }
    
    /**
     * Save arenas to storage
     */
    public void saveArenas() {
        // TODO: Implement arena saving to database/files
        plugin.getLogger().info("Saving arenas to storage...");
    }
    
    /**
     * Reload arena manager
     */
    public void reload() {
        arenas.clear();
        loadArenas();
        plugin.getLogger().info("Arena manager reloaded");
    }
    
    /**
     * Get all arena names
     * @return Set of arena names
     */
    public Set<String> getArenaNames() {
        return arenas.keySet();
    }
    
    /**
     * Get an arena by name
     * @param name Arena name
     * @return Arena or null if not found
     */
    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }
    
    /**
     * Create a new arena
     * @param name Arena name
     * @param type Arena type
     * @param world World for the arena
     * @param creator Creator UUID
     * @return Created arena or null if failed
     */
    public Arena createArena(String name, ArenaType type, World world, UUID creator) {
        if (arenas.containsKey(name.toLowerCase())) {
            return null; // Arena already exists
        }
        
        Arena arena = new Arena(name, type, world, creator);
        arenas.put(name.toLowerCase(), arena);
        
        plugin.getLogger().info("Created arena: " + name + " (Type: " + type + ", World: " + world.getName() + ")");
        return arena;
    }
    
    /**
     * Delete an arena
     * @param name Arena name
     * @return True if deleted, false if not found
     */
    public boolean deleteArena(String name) {
        Arena arena = arenas.remove(name.toLowerCase());
        if (arena != null) {
            // Clean up WorldGuard region if it exists
            if (worldGuardEnabled && arena.getWorldGuardRegion() != null) {
                removeWorldGuardRegion(arena);
            }
            plugin.getLogger().info("Deleted arena: " + name);
            return true;
        }
        return false;
    }
    
    /**
     * Add a spawn point to an arena
     * @param arenaName Arena name
     * @param spawnName Spawn point name
     * @param location Spawn location
     * @param team Team name (optional, for PvP arenas)
     * @return True if added successfully
     */
    public boolean addSpawnPoint(String arenaName, String spawnName, Location location, String team) {
        Arena arena = getArena(arenaName);
        if (arena == null) {
            return false;
        }
        
        // Check if spawn point already exists
        if (arena.getSpawnPoint(spawnName) != null) {
            return false;
        }
        
        // Validate location is in the same world
        if (!arena.getWorld().equals(location.getWorld())) {
            return false;
        }
        
        SpawnPoint spawnPoint = new SpawnPoint(spawnName, location, team);
        arena.addSpawnPoint(spawnPoint);
        
        plugin.getLogger().info("Added spawn point '" + spawnName + "' to arena '" + arenaName + "'");
        return true;
    }
    
    /**
     * Remove a spawn point from an arena
     * @param arenaName Arena name
     * @param spawnName Spawn point name
     * @return True if removed successfully
     */
    public boolean removeSpawnPoint(String arenaName, String spawnName) {
        Arena arena = getArena(arenaName);
        if (arena == null) {
            return false;
        }
        
        boolean removed = arena.removeSpawnPoint(spawnName);
        if (removed) {
            plugin.getLogger().info("Removed spawn point '" + spawnName + "' from arena '" + arenaName + "'");
        }
        return removed;
    }
    
    /**
     * Set the goal location for a parkour arena
     * @param arenaName Arena name
     * @param location Goal location
     * @return True if set successfully
     */
    public boolean setGoalLocation(String arenaName, Location location) {
        Arena arena = getArena(arenaName);
        if (arena == null || arena.getType() != ArenaType.PARKOUR) {
            return false;
        }
        
        // Validate location is in the same world
        if (!arena.getWorld().equals(location.getWorld())) {
            return false;
        }
        
        arena.setGoalLocation(location);
        plugin.getLogger().info("Set goal location for parkour arena '" + arenaName + "'");
        return true;
    }
    
    /**
     * Create or update WorldGuard region for an arena
     * @param arena Arena to create region for
     * @param regionName Region name
     * @return True if successful
     */
    public boolean createWorldGuardRegion(Arena arena, String regionName) {
        if (!worldGuardEnabled) {
            return false;
        }
        
        try {
            RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(arena.getWorld()));
            
            if (regionManager == null) {
                return false;
            }
            
            // Check if region exists
            ProtectedRegion region = regionManager.getRegion(regionName);
            if (region != null) {
                arena.setWorldGuardRegion(regionName);
                plugin.getLogger().info("Associated arena '" + arena.getName() + "' with existing WorldGuard region '" + regionName + "'");
                return true;
            }
            
            return false; // Region doesn't exist - would need to be created manually
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create WorldGuard region for arena " + arena.getName(), e);
            return false;
        }
    }
    
    /**
     * Remove WorldGuard region association
     * @param arena Arena to remove region from
     * @return True if successful
     */
    public boolean removeWorldGuardRegion(Arena arena) {
        if (!worldGuardEnabled || arena.getWorldGuardRegion() == null) {
            return false;
        }
        
        arena.setWorldGuardRegion(null);
        plugin.getLogger().info("Removed WorldGuard region association from arena '" + arena.getName() + "'");
        return true;
    }
    
    /**
     * Check if a location is within an arena's WorldGuard region
     * @param arena Arena to check
     * @param location Location to check
     * @return True if location is within arena region
     */
    public boolean isLocationInArena(Arena arena, Location location) {
        if (!worldGuardEnabled || arena.getWorldGuardRegion() == null) {
            return false;
        }
        
        try {
            RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(arena.getWorld()));
            
            if (regionManager == null) {
                return false;
            }
            
            ProtectedRegion region = regionManager.getRegion(arena.getWorldGuardRegion());
            if (region == null) {
                return false;
            }
            
            return region.contains(
                com.sk89q.worldedit.bukkit.BukkitAdapter.asBlockVector(location)
            );
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check location in arena region", e);
            return false;
        }
    }
    
    /**
     * Teleport player to arena spawn point
     * @param player Player to teleport
     * @param arena Arena to teleport to
     * @param team Team name (optional)
     * @return True if teleported successfully
     */
    public boolean teleportToArena(Player player, Arena arena, String team) {
        SpawnPoint spawnPoint = null;
        
        switch (arena.getType()) {
            case PVP:
                if (team != null && arena.isTeamBased()) {
                    var teamSpawns = arena.getTeamSpawnPoints(team);
                    if (!teamSpawns.isEmpty()) {
                        spawnPoint = teamSpawns.get(0); // Use first available team spawn
                    }
                } else {
                    var neutralSpawns = arena.getNeutralSpawnPoints();
                    if (!neutralSpawns.isEmpty()) {
                        spawnPoint = neutralSpawns.get(0); // Use first available neutral spawn
                    }
                }
                break;
                
            case PARKOUR:
                var spawns = arena.getSpawnPoints();
                if (!spawns.isEmpty()) {
                    spawnPoint = spawns.get(0); // Use the single spawn point
                }
                break;
        }
        
        if (spawnPoint == null) {
            return false;
        }
        
        player.teleport(spawnPoint.getLocation());
        plugin.getLogger().info("Teleported player " + player.getName() + " to arena " + arena.getName());
        return true;
    }
    
    /**
     * Check if WorldGuard is enabled
     * @return True if WorldGuard integration is available
     */
    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }
}