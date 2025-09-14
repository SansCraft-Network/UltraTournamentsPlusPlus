package top.sanscraft.ultratournamentsplusplus.models;

/**
 * Represents the type of tournament bracket
 */
public enum TournamentType {
    SINGLE_ELIMINATION("Single Elimination", "Players are eliminated after one loss"),
    DOUBLE_ELIMINATION("Double Elimination", "Players are eliminated after two losses"),
    ROUND_ROBIN("Round Robin", "Every player plays every other player"),
    SWISS("Swiss System", "Players are paired based on similar performance"),
    CUSTOM("Custom", "Custom tournament format");
    
    private final String displayName;
    private final String description;
    
    TournamentType(String displayName, String description) {
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