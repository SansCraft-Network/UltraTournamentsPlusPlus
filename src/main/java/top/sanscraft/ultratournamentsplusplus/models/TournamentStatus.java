package top.sanscraft.ultratournamentsplusplus.models;

/**
 * Represents the status of a tournament
 */
public enum TournamentStatus {
    PREPARING("Preparing", "Tournament is being set up"),
    OPEN("Open for Registration", "Players can join the tournament"),
    STARTING("Starting", "Tournament is about to begin"),
    IN_PROGRESS("In Progress", "Tournament matches are ongoing"),
    PAUSED("Paused", "Tournament is temporarily paused"),
    FINISHED("Finished", "Tournament has completed"),
    CANCELLED("Cancelled", "Tournament was cancelled");
    
    private final String displayName;
    private final String description;
    
    TournamentStatus(String displayName, String description) {
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