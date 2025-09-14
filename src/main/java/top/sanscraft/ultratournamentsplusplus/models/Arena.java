package top.sanscraft.ultratournamentsplusplus.models;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an arena for tournaments
 */
public class Arena {
    
    private String name;
    private String description;
    private ArenaType type;
    private World world;
    private UUID creator;
    private long createdTime;
    
    // Arena settings
    private boolean buildingEnabled;
    private int maxPlayers;
    private String worldGuardRegion;
    
    // Spawn points
    private List<SpawnPoint> spawnPoints;
    
    // Parkour specific
    private Location goalLocation;
    private long timeLimit; // in seconds, 0 = no limit
    
    // PvP specific
    private boolean teamBased;
    private List<String> teams;
    
    public Arena(String name, ArenaType type, World world, UUID creator) {
        this.name = name;
        this.type = type;
        this.world = world;
        this.creator = creator;
        this.createdTime = System.currentTimeMillis();
        this.spawnPoints = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.maxPlayers = 16; // Default
        this.buildingEnabled = type == ArenaType.PVP; // Default: PvP allows building, Parkour doesn't
    }
    
    // Basic getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ArenaType getType() { return type; }
    public void setType(ArenaType type) { this.type = type; }
    
    public World getWorld() { return world; }
    public void setWorld(World world) { this.world = world; }
    
    public UUID getCreator() { return creator; }
    public void setCreator(UUID creator) { this.creator = creator; }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    public boolean isBuildingEnabled() { return buildingEnabled; }
    public void setBuildingEnabled(boolean buildingEnabled) { this.buildingEnabled = buildingEnabled; }
    
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    
    public String getWorldGuardRegion() { return worldGuardRegion; }
    public void setWorldGuardRegion(String worldGuardRegion) { this.worldGuardRegion = worldGuardRegion; }
    
    // Spawn point management
    public List<SpawnPoint> getSpawnPoints() { return new ArrayList<>(spawnPoints); }
    public void setSpawnPoints(List<SpawnPoint> spawnPoints) { this.spawnPoints = new ArrayList<>(spawnPoints); }
    
    public void addSpawnPoint(SpawnPoint spawnPoint) {
        spawnPoints.add(spawnPoint);
    }
    
    public boolean removeSpawnPoint(String name) {
        return spawnPoints.removeIf(sp -> sp.getName().equalsIgnoreCase(name));
    }
    
    public SpawnPoint getSpawnPoint(String name) {
        return spawnPoints.stream()
            .filter(sp -> sp.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    public int getSpawnPointCount() {
        return spawnPoints.size();
    }
    
    // Parkour specific methods
    public Location getGoalLocation() { return goalLocation != null ? goalLocation.clone() : null; }
    public void setGoalLocation(Location goalLocation) { this.goalLocation = goalLocation != null ? goalLocation.clone() : null; }
    
    public long getTimeLimit() { return timeLimit; }
    public void setTimeLimit(long timeLimit) { this.timeLimit = timeLimit; }
    
    public boolean hasTimeLimit() { return timeLimit > 0; }
    
    // PvP specific methods
    public boolean isTeamBased() { return teamBased; }
    public void setTeamBased(boolean teamBased) { this.teamBased = teamBased; }
    
    public List<String> getTeams() { return new ArrayList<>(teams); }
    public void setTeams(List<String> teams) { this.teams = new ArrayList<>(teams); }
    
    public void addTeam(String team) {
        if (!teams.contains(team)) {
            teams.add(team);
        }
    }
    
    public boolean removeTeam(String team) {
        return teams.remove(team);
    }
    
    /**
     * Get spawn points for a specific team
     * @param team Team name
     * @return List of spawn points for the team
     */
    public List<SpawnPoint> getTeamSpawnPoints(String team) {
        return spawnPoints.stream()
            .filter(sp -> team.equals(sp.getTeam()))
            .toList();
    }
    
    /**
     * Get spawn points without team assignment
     * @return List of neutral spawn points
     */
    public List<SpawnPoint> getNeutralSpawnPoints() {
        return spawnPoints.stream()
            .filter(sp -> !sp.hasTeam())
            .toList();
    }
    
    /**
     * Validate arena configuration
     * @return List of validation errors (empty if valid)
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        
        if (name == null || name.trim().isEmpty()) {
            errors.add("Arena name cannot be empty");
        }
        
        if (world == null) {
            errors.add("Arena world is not set");
        }
        
        if (spawnPoints.isEmpty()) {
            errors.add("Arena must have at least one spawn point");
        }
        
        switch (type) {
            case PVP:
                if (teamBased && teams.size() < 2) {
                    errors.add("Team-based PvP arena must have at least 2 teams");
                }
                if (teamBased) {
                    for (String team : teams) {
                        if (getTeamSpawnPoints(team).isEmpty()) {
                            errors.add("Team '" + team + "' has no spawn points");
                        }
                    }
                }
                break;
                
            case PARKOUR:
                if (goalLocation == null) {
                    errors.add("Parkour arena must have a goal location");
                }
                if (spawnPoints.size() > 1) {
                    errors.add("Parkour arena should have only one spawn point");
                }
                break;
        }
        
        return errors;
    }
    
    /**
     * Check if the arena is ready for use
     * @return True if arena is properly configured
     */
    public boolean isReady() {
        return validate().isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("Arena{name='%s', type=%s, world='%s', spawns=%d, ready=%s}", 
            name, type, world != null ? world.getName() : "null", spawnPoints.size(), isReady());
    }
}