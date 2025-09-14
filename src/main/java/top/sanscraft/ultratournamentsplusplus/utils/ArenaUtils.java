package top.sanscraft.ultratournamentsplusplus.utils;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import top.sanscraft.ultratournamentsplusplus.models.Arena;
import top.sanscraft.ultratournamentsplusplus.models.ArenaType;
import top.sanscraft.ultratournamentsplusplus.models.SpawnPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for arena-related operations
 * Provides helper methods for arena validation, region management, and player teleportation
 */
public class ArenaUtils {
    
    /**
     * Validate if an arena is properly configured
     * @param arena The arena to validate
     * @return True if arena is valid and ready for use
     */
    public static boolean isArenaValid(Arena arena) {
        if (arena == null) {
            return false;
        }
        
        // Check if arena has spawn points
        if (arena.getSpawnPoints().isEmpty()) {
            return false;
        }
        
        // Check arena-specific requirements
        switch (arena.getType()) {
            case PVP:
                // PvP arenas need at least 2 spawn points (for teams)
                return arena.getSpawnPoints().size() >= 2;
            case PARKOUR:
                // Parkour arenas need at least 1 spawn point and a goal location
                return !arena.getSpawnPoints().isEmpty() && arena.getGoalLocation() != null;
            default:
                return false;
        }
    }
    
    /**
     * Check if a WorldGuard region exists for the arena
     * @param arena The arena to check
     * @return True if the region exists
     */
    public static boolean doesArenaRegionExist(Arena arena) {
        if (arena.getWorldGuardRegion() == null || arena.getWorldGuardRegion().isEmpty()) {
            return false;
        }
        
        // Get the world from the first spawn point
        if (arena.getSpawnPoints().isEmpty()) {
            return false;
        }
        
        Location firstSpawn = arena.getSpawnPoints().get(0).getLocation();
        if (firstSpawn.getWorld() == null) {
            return false;
        }
        
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(firstSpawn.getWorld()));
        
        if (regionManager == null) {
            return false;
        }
        
        ProtectedRegion region = regionManager.getRegion(arena.getWorldGuardRegion());
        return region != null;
    }
    
    /**
     * Get all players currently in an arena region
     * @param arena The arena to check
     * @return List of players in the arena region
     */
    public static List<Player> getPlayersInArena(Arena arena) {
        List<Player> playersInArena = new ArrayList<>();
        
        if (!doesArenaRegionExist(arena)) {
            return playersInArena;
        }
        
        // Get the world from the first spawn point
        if (arena.getSpawnPoints().isEmpty()) {
            return playersInArena;
        }
        
        Location firstSpawn = arena.getSpawnPoints().get(0).getLocation();
        World world = firstSpawn.getWorld();
        if (world == null) {
            return playersInArena;
        }
        
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world));
        
        if (regionManager == null) {
            return playersInArena;
        }
        
        ProtectedRegion region = regionManager.getRegion(arena.getWorldGuardRegion());
        if (region == null) {
            return playersInArena;
        }
        
        // Check all players in the world
        for (Player player : world.getPlayers()) {
            com.sk89q.worldedit.util.Location weLocation = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(player.getLocation());
            if (region.contains(weLocation.toVector().toBlockPoint())) {
                playersInArena.add(player);
            }
        }
        
        return playersInArena;
    }
    
    /**
     * Teleport a player to a specific spawn point in an arena
     * @param player The player to teleport
     * @param arena The arena containing the spawn point
     * @param spawnIndex The index of the spawn point (0-based)
     * @return True if teleportation was successful
     */
    public static boolean teleportToSpawn(Player player, Arena arena, int spawnIndex) {
        if (arena == null || player == null) {
            return false;
        }
        
        List<SpawnPoint> spawnPoints = arena.getSpawnPoints();
        if (spawnIndex < 0 || spawnIndex >= spawnPoints.size()) {
            return false;
        }
        
        SpawnPoint spawnPoint = spawnPoints.get(spawnIndex);
        Location location = spawnPoint.getLocation();
        
        if (location.getWorld() == null) {
            return false;
        }
        
        player.teleport(location);
        return true;
    }
    
    /**
     * Get spawn points for a specific team in a PvP arena
     * @param arena The PvP arena
     * @param teamName The team name
     * @return List of spawn points for the team
     */
    public static List<SpawnPoint> getTeamSpawnPoints(Arena arena, String teamName) {
        if (arena.getType() != ArenaType.PVP) {
            return Collections.emptyList();
        }
        
        List<SpawnPoint> teamSpawns = new ArrayList<>();
        for (SpawnPoint spawn : arena.getSpawnPoints()) {
            if (teamName == null && !spawn.hasTeam()) {
                // Include spawns with no team if looking for null team
                teamSpawns.add(spawn);
            } else if (teamName != null && teamName.equals(spawn.getTeam())) {
                teamSpawns.add(spawn);
            }
        }
        
        return teamSpawns;
    }
    
    /**
     * Get a random spawn point for a specific team
     * @param arena The PvP arena
     * @param teamName The team name
     * @return A random spawn point for the team, or null if none available
     */
    public static SpawnPoint getRandomTeamSpawn(Arena arena, String teamName) {
        List<SpawnPoint> teamSpawns = getTeamSpawnPoints(arena, teamName);
        if (teamSpawns.isEmpty()) {
            return null;
        }
        
        return teamSpawns.get((int) (Math.random() * teamSpawns.size()));
    }
    
    /**
     * Get all unique team names in an arena
     * @param arena The arena to check
     * @return List of unique team names
     */
    public static List<String> getAvailableTeams(Arena arena) {
        List<String> teams = new ArrayList<>();
        for (SpawnPoint spawn : arena.getSpawnPoints()) {
            if (spawn.hasTeam() && !teams.contains(spawn.getTeam())) {
                teams.add(spawn.getTeam());
            }
        }
        return teams;
    }
    
    /**
     * Check if a player has reached the goal in a parkour arena
     * @param player The player to check
     * @param arena The parkour arena
     * @param tolerance The distance tolerance for reaching the goal
     * @return True if player is within tolerance of the goal
     */
    public static boolean hasPlayerReachedGoal(Player player, Arena arena, double tolerance) {
        if (arena.getType() != ArenaType.PARKOUR || arena.getGoalLocation() == null) {
            return false;
        }
        
        Location playerLocation = player.getLocation();
        Location goalLocation = arena.getGoalLocation();
        
        // Check if they're in the same world
        if (!playerLocation.getWorld().equals(goalLocation.getWorld())) {
            return false;
        }
        
        // Check distance
        return playerLocation.distance(goalLocation) <= tolerance;
    }
    
    /**
     * Clear all players from an arena region
     * @param arena The arena to clear
     * @param fallbackLocation Location to teleport players to (spawn, lobby, etc.)
     */
    public static void clearArena(Arena arena, Location fallbackLocation) {
        List<Player> playersInArena = getPlayersInArena(arena);
        
        for (Player player : playersInArena) {
            if (fallbackLocation != null && fallbackLocation.getWorld() != null) {
                player.teleport(fallbackLocation);
            } else {
                // Teleport to world spawn as fallback
                World world = Bukkit.getWorlds().get(0); // Main world
                player.teleport(world.getSpawnLocation());
            }
        }
    }
    
    /**
     * Get the distance between two spawn points
     * @param spawn1 First spawn point
     * @param spawn2 Second spawn point
     * @return Distance between spawn points, or -1 if they're in different worlds
     */
    public static double getSpawnPointDistance(SpawnPoint spawn1, SpawnPoint spawn2) {
        Location loc1 = spawn1.getLocation();
        Location loc2 = spawn2.getLocation();
        
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return -1;
        }
        
        return loc1.distance(loc2);
    }
    
    /**
     * Check if an arena is currently in use by any tournament
     * @param arenaName The name of the arena to check
     * @return True if arena is in use (this would need integration with tournament manager)
     */
    public static boolean isArenaInUse(String arenaName) {
        // This method would need to be implemented with tournament manager integration
        // For now, return false as a placeholder
        return false;
    }
}