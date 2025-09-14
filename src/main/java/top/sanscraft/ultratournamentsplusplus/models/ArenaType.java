package top.sanscraft.ultratournamentsplusplus.models;

/**
 * Represents the type of arena
 */
public enum ArenaType {
    PVP("PvP Arena", "Player vs Player combat arena with multiple spawn points"),
    PARKOUR("Parkour Arena", "Parkour challenge arena with start and finish points");
    
    private final String displayName;
    private final String description;
    
    ArenaType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}