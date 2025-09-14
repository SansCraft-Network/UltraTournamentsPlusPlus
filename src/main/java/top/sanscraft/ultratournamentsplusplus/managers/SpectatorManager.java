package top.sanscraft.ultratournamentsplusplus.managers;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.Round;
import top.sanscraft.ultratournamentsplusplus.models.Tournament;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages spectator functionality including spectator regions and tournament viewing
 */
public class SpectatorManager {
    
    private final UltraTournamentsPlusPlus plugin;
    private final Map<String, String> spectatorRegions; // World name -> Region name
    private final Map<String, Location> spectatorTeleportLocations; // World name -> Teleport location
    private final Map<UUID, String> spectatingPlayers; // Player UUID -> Tournament ID
    private final Map<UUID, SpectatorData> spectatorData; // Player UUID -> Original state data
    
    /**
     * Stores original player data for restoration after spectating
     */
    private static class SpectatorData {
        public final Location originalLocation;
        public final GameMode originalGameMode;
        public final String tournamentId;
        
        public SpectatorData(Location originalLocation, GameMode originalGameMode, String tournamentId) {
            this.originalLocation = originalLocation.clone();
            this.originalGameMode = originalGameMode;
            this.tournamentId = tournamentId;
        }
    }
    
    public SpectatorManager(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
        this.spectatorRegions = new HashMap<>();
        this.spectatorTeleportLocations = new HashMap<>();
        this.spectatingPlayers = new HashMap<>();
        this.spectatorData = new HashMap<>();
        
        loadSpectatorConfiguration();
    }
    
    /**
     * Load spectator configuration from plugin config
     */
    private void loadSpectatorConfiguration() {
        // Load spectator regions from config
        if (plugin.getConfig().contains("spectator.regions")) {
            for (String worldName : plugin.getConfig().getConfigurationSection("spectator.regions").getKeys(false)) {
                String regionName = plugin.getConfig().getString("spectator.regions." + worldName + ".region");
                spectatorRegions.put(worldName, regionName);
                
                // Load teleport location if configured
                if (plugin.getConfig().contains("spectator.regions." + worldName + ".teleport")) {
                    double x = plugin.getConfig().getDouble("spectator.regions." + worldName + ".teleport.x");
                    double y = plugin.getConfig().getDouble("spectator.regions." + worldName + ".teleport.y");
                    double z = plugin.getConfig().getDouble("spectator.regions." + worldName + ".teleport.z");
                    float yaw = (float) plugin.getConfig().getDouble("spectator.regions." + worldName + ".teleport.yaw", 0.0);
                    float pitch = (float) plugin.getConfig().getDouble("spectator.regions." + worldName + ".teleport.pitch", 0.0);
                    
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        Location teleportLoc = new Location(world, x, y, z, yaw, pitch);
                        spectatorTeleportLocations.put(worldName, teleportLoc);
                    }
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + spectatorRegions.size() + " spectator regions");
    }
    
    /**
     * Save spectator configuration to plugin config
     */
    public void saveSpectatorConfiguration() {
        // Clear existing config
        plugin.getConfig().set("spectator.regions", null);
        
        // Save spectator regions
        for (Map.Entry<String, String> entry : spectatorRegions.entrySet()) {
            String worldName = entry.getKey();
            String regionName = entry.getValue();
            
            plugin.getConfig().set("spectator.regions." + worldName + ".region", regionName);
            
            // Save teleport location if configured
            Location teleportLoc = spectatorTeleportLocations.get(worldName);
            if (teleportLoc != null) {
                plugin.getConfig().set("spectator.regions." + worldName + ".teleport.x", teleportLoc.getX());
                plugin.getConfig().set("spectator.regions." + worldName + ".teleport.y", teleportLoc.getY());
                plugin.getConfig().set("spectator.regions." + worldName + ".teleport.z", teleportLoc.getZ());
                plugin.getConfig().set("spectator.regions." + worldName + ".teleport.yaw", teleportLoc.getYaw());
                plugin.getConfig().set("spectator.regions." + worldName + ".teleport.pitch", teleportLoc.getPitch());
            }
        }
        
        plugin.saveConfig();
    }
    
    /**
     * Check if a player is in a spectator region
     * @param player The player to check
     * @return True if player is in a spectator region
     */
    public boolean isPlayerInSpectatorRegion(Player player) {
        String worldName = player.getWorld().getName();
        String regionName = spectatorRegions.get(worldName);
        
        if (regionName == null || regionName.isEmpty()) {
            return false;
        }
        
        return isPlayerInRegion(player, regionName);
    }
    
    /**
     * Check if a player is in a specific WorldGuard region
     * @param player The player to check
     * @param regionName The region name
     * @return True if player is in the region
     */
    private boolean isPlayerInRegion(Player player, String regionName) {
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(player.getWorld()));
        
        if (regionManager == null) {
            return false;
        }
        
        ProtectedRegion region = regionManager.getRegion(regionName);
        if (region == null) {
            return false;
        }
        
        com.sk89q.worldedit.util.Location weLocation = 
            com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(player.getLocation());
        
        return region.contains(weLocation.toVector().toBlockPoint());
    }
    
    /**
     * Set a spectator region for a world
     * @param worldName World name
     * @param regionName WorldGuard region name
     * @return True if set successfully
     */
    public boolean setSpectatorRegion(String worldName, String regionName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return false;
        }
        
        // Verify region exists
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world));
        
        if (regionManager == null) {
            return false;
        }
        
        ProtectedRegion region = regionManager.getRegion(regionName);
        if (region == null) {
            return false;
        }
        
        spectatorRegions.put(worldName, regionName);
        saveSpectatorConfiguration();
        
        plugin.getLogger().info("Set spectator region for world " + worldName + " to " + regionName);
        return true;
    }
    
    /**
     * Set the spectator teleport location for a world
     * @param worldName World name
     * @param location Teleport location
     * @return True if set successfully
     */
    public boolean setSpectatorTeleportLocation(String worldName, Location location) {
        if (!spectatorRegions.containsKey(worldName)) {
            return false; // Must have a spectator region first
        }
        
        spectatorTeleportLocations.put(worldName, location.clone());
        saveSpectatorConfiguration();
        
        plugin.getLogger().info("Set spectator teleport location for world " + worldName + 
                              " to " + location.getX() + ", " + location.getY() + ", " + location.getZ());
        return true;
    }
    
    /**
     * Remove spectator region for a world
     * @param worldName World name
     * @return True if removed successfully
     */
    public boolean removeSpectatorRegion(String worldName) {
        boolean hadRegion = spectatorRegions.remove(worldName) != null;
        spectatorTeleportLocations.remove(worldName);
        
        if (hadRegion) {
            saveSpectatorConfiguration();
            plugin.getLogger().info("Removed spectator region for world " + worldName);
        }
        
        return hadRegion;
    }
    
    /**
     * Start spectating a tournament
     * @param player The player who wants to spectate
     * @param tournamentId Tournament to spectate
     * @return True if spectating started successfully
     */
    public boolean startSpectating(Player player, String tournamentId) {
        // Check if player is in a spectator region
        if (!isPlayerInSpectatorRegion(player)) {
            return false;
        }
        
        // Check if tournament exists
        Tournament tournament = plugin.getTournamentManager().getTournament(tournamentId);
        if (tournament == null) {
            return false;
        }
        
        // Save original player state
        SpectatorData originalData = new SpectatorData(
            player.getLocation(),
            player.getGameMode(),
            tournamentId
        );
        spectatorData.put(player.getUniqueId(), originalData);
        
        // Add player to spectating list
        spectatingPlayers.put(player.getUniqueId(), tournamentId);
        
        // Set player to spectator mode
        player.setGameMode(GameMode.SPECTATOR);
        
        // Try to teleport to a competitor
        if (!teleportToCompetitor(player, tournamentId)) {
            // Fallback to spectator area if no competitors available
            teleportToSpectatorArea(player);
        }
        
        plugin.getLogger().info("Player " + player.getName() + " started spectating tournament " + tournamentId);
        return true;
    }
    
    /**
     * Stop spectating
     * @param player The player to stop spectating
     * @return True if stopped successfully
     */
    public boolean stopSpectating(Player player) {
        String previousTournament = spectatingPlayers.remove(player.getUniqueId());
        SpectatorData originalData = spectatorData.remove(player.getUniqueId());
        
        if (previousTournament != null && originalData != null) {
            // Restore original player state
            player.setGameMode(originalData.originalGameMode);
            player.teleport(originalData.originalLocation);
            
            plugin.getLogger().info("Player " + player.getName() + " stopped spectating tournament " + previousTournament);
            return true;
        }
        
        return false;
    }
    
    /**
     * Teleport a spectator to one of the tournament competitors
     * @param player The spectator player
     * @param tournamentId The tournament ID
     * @return True if teleported successfully
     */
    private boolean teleportToCompetitor(Player player, String tournamentId) {
        Tournament tournament = plugin.getTournamentManager().getTournament(tournamentId);
        if (tournament == null) {
            return false;
        }
        
        // Check if tournament has an active round
        Round activeRound = plugin.getRoundManager().getActiveRoundForTournament(tournamentId);
        if (activeRound != null) {
            // Get first available participant from the active round
            List<UUID> participants = activeRound.getParticipants();
            for (UUID participantId : participants) {
                Player participant = Bukkit.getPlayer(participantId);
                if (participant != null && participant.isOnline()) {
                    player.teleport(participant.getLocation());
                    return true;
                }
            }
        }
        
        // Fallback: teleport to any tournament participant
        List<UUID> tournamentParticipants = tournament.getParticipants();
        for (UUID participantId : tournamentParticipants) {
            Player participant = Bukkit.getPlayer(participantId);
            if (participant != null && participant.isOnline()) {
                player.teleport(participant.getLocation());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Stop spectating for all players when a round ends
     * @param tournamentId The tournament ID
     */
    public void stopSpectatingForTournament(String tournamentId) {
        List<UUID> playersToStop = new ArrayList<>();
        
        for (Map.Entry<UUID, String> entry : spectatingPlayers.entrySet()) {
            if (entry.getValue().equals(tournamentId)) {
                playersToStop.add(entry.getKey());
            }
        }
        
        for (UUID playerId : playersToStop) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                stopSpectating(player);
                player.sendMessage("§6Tournament has ended. You have been returned to your original location.");
            }
        }
    }
    
    /**
     * Check if a player is in an arena region (for boundary checking)
     * @param player The player to check
     * @param arenaName The arena name
     * @return True if player is in the arena region
     */
    public boolean isPlayerInArenaRegion(Player player, String arenaName) {
        if (arenaName == null || arenaName.isEmpty()) {
            return false;
        }
        
        return isPlayerInRegion(player, arenaName);
    }
    
    /**
     * Check if a player is currently spectating
     * @param player The player to check
     * @return True if player is spectating
     */
    public boolean isSpectating(Player player) {
        return spectatingPlayers.containsKey(player.getUniqueId());
    }
    
    /**
     * Get the tournament a player is spectating
     * @param player The player
     * @return Tournament ID or null if not spectating
     */
    public String getSpectatingTournament(Player player) {
        return spectatingPlayers.get(player.getUniqueId());
    }
    
    /**
     * Teleport a player to the spectator area
     * @param player The player to teleport
     * @return True if teleported successfully
     */
    public boolean teleportToSpectatorArea(Player player) {
        String worldName = player.getWorld().getName();
        Location teleportLoc = spectatorTeleportLocations.get(worldName);
        
        if (teleportLoc == null) {
            // Try to find a default location in the spectator region
            String regionName = spectatorRegions.get(worldName);
            if (regionName != null) {
                Location defaultLoc = getRegionCenter(player.getWorld(), regionName);
                if (defaultLoc != null) {
                    player.teleport(defaultLoc);
                    return true;
                }
            }
            return false;
        }
        
        player.teleport(teleportLoc);
        return true;
    }
    
    /**
     * Get the center of a WorldGuard region
     * @param world The world
     * @param regionName Region name
     * @return Center location or null if not found
     */
    private Location getRegionCenter(World world, String regionName) {
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world));
        
        if (regionManager == null) {
            return null;
        }
        
        ProtectedRegion region = regionManager.getRegion(regionName);
        if (region == null) {
            return null;
        }
        
        com.sk89q.worldedit.math.BlockVector3 min = region.getMinimumPoint();
        com.sk89q.worldedit.math.BlockVector3 max = region.getMaximumPoint();
        
        double x = (min.getX() + max.getX()) / 2.0;
        double y = (min.getY() + max.getY()) / 2.0;
        double z = (min.getZ() + max.getZ()) / 2.0;
        
        return new Location(world, x, y, z);
    }
    
    /**
     * Get all spectator regions
     * @return Map of world name to region name
     */
    public Map<String, String> getSpectatorRegions() {
        return new HashMap<>(spectatorRegions);
    }
    
    /**
     * Get spectator teleport locations
     * @return Map of world name to teleport location
     */
    public Map<String, Location> getSpectatorTeleportLocations() {
        return new HashMap<>(spectatorTeleportLocations);
    }
    
    /**
     * Get the spectator region for a world
     * @param worldName World name
     * @return Region name or null if not set
     */
    public String getSpectatorRegion(String worldName) {
        return spectatorRegions.get(worldName);
    }
    
    /**
     * Get the spectator teleport location for a world
     * @param worldName World name
     * @return Teleport location or null if not set
     */
    public Location getSpectatorTeleportLocation(String worldName) {
        return spectatorTeleportLocations.get(worldName);
    }
    
    /**
     * Check if a world has a spectator region configured
     * @param worldName World name
     * @return True if spectator region is configured
     */
    public boolean hasSpectatorRegion(String worldName) {
        return spectatorRegions.containsKey(worldName);
    }
    
    /**
     * Shutdown the spectator manager
     */
    public void shutdown() {
        // Stop all spectating players
        List<UUID> playersToStop = new ArrayList<>(spectatingPlayers.keySet());
        for (UUID playerId : playersToStop) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                stopSpectating(player);
            }
        }
        
        spectatingPlayers.clear();
        spectatorData.clear();
        plugin.getLogger().info("SpectatorManager shut down");
    }
}