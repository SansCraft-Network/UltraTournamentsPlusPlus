package top.sanscraft.ultratournamentsplusplus.models;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents a spawn point in an arena
 */
public class SpawnPoint {
    
    private final String name;
    private final Location location;
    private final String team; // For team-based PvP arenas
    
    public SpawnPoint(String name, Location location) {
        this(name, location, null);
    }
    
    public SpawnPoint(String name, Location location, String team) {
        this.name = name;
        this.location = location.clone();
        this.team = team;
    }
    
    public String getName() {
        return name;
    }
    
    public Location getLocation() {
        return location.clone();
    }
    
    public String getTeam() {
        return team;
    }
    
    public boolean hasTeam() {
        return team != null && !team.isEmpty();
    }
    
    public World getWorld() {
        return location.getWorld();
    }
    
    /**
     * Check if this spawn point is in the same world as another location
     * @param other The other location to check
     * @return True if in same world
     */
    public boolean isInSameWorld(Location other) {
        return location.getWorld().equals(other.getWorld());
    }
    
    /**
     * Get distance to another location
     * @param other The other location
     * @return Distance in blocks
     */
    public double getDistance(Location other) {
        if (!isInSameWorld(other)) {
            return Double.MAX_VALUE;
        }
        return location.distance(other);
    }
    
    @Override
    public String toString() {
        return String.format("SpawnPoint{name='%s', world='%s', x=%.1f, y=%.1f, z=%.1f, team='%s'}", 
            name, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), team);
    }
}