package top.sanscraft.ultratournamentsplusplus.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single round/match in a tournament
 */
public class Round {
    
    public enum RoundStatus {
        PENDING,      // Round created but not started
        PREPARING,    // Players being teleported, countdown starting
        ACTIVE,       // Round in progress
        COMPLETED,    // Round finished with winner
        CANCELLED     // Round was cancelled
    }
    
    private final String id;
    private final String tournamentId;
    private final List<UUID> participants;
    private String assignedArena;
    private String assignedKit;
    private RoundStatus status;
    private UUID winner;
    private long startTime;
    private long endTime;
    private int roundNumber;
    private String bracketPosition; // For tracking position in tournament bracket
    
    // Round settings
    private int preparationTime; // Seconds before round starts
    private boolean autoDetectWinner;
    
    public Round(String id, String tournamentId, List<UUID> participants) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.participants = new ArrayList<>(participants);
        this.status = RoundStatus.PENDING;
        this.preparationTime = 3; // Default 3 seconds
        this.autoDetectWinner = true;
    }
    
    // Basic getters and setters
    public String getId() { return id; }
    public String getTournamentId() { return tournamentId; }
    public List<UUID> getParticipants() { return new ArrayList<>(participants); }
    public String getAssignedArena() { return assignedArena; }
    public void setAssignedArena(String assignedArena) { this.assignedArena = assignedArena; }
    
    public String getAssignedKit() { return assignedKit; }
    public void setAssignedKit(String assignedKit) { this.assignedKit = assignedKit; }
    
    public RoundStatus getStatus() { return status; }
    public void setStatus(RoundStatus status) { this.status = status; }
    
    public UUID getWinner() { return winner; }
    public void setWinner(UUID winner) { 
        this.winner = winner;
        if (winner != null) {
            this.status = RoundStatus.COMPLETED;
            this.endTime = System.currentTimeMillis();
        }
    }
    
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    
    public String getBracketPosition() { return bracketPosition; }
    public void setBracketPosition(String bracketPosition) { this.bracketPosition = bracketPosition; }
    
    public int getPreparationTime() { return preparationTime; }
    public void setPreparationTime(int preparationTime) { this.preparationTime = preparationTime; }
    
    public boolean isAutoDetectWinner() { return autoDetectWinner; }
    public void setAutoDetectWinner(boolean autoDetectWinner) { this.autoDetectWinner = autoDetectWinner; }
    
    // Utility methods
    
    /**
     * Check if a player is participating in this round
     * @param playerId The player's UUID
     * @return True if player is in this round
     */
    public boolean hasParticipant(UUID playerId) {
        return participants.contains(playerId);
    }
    
    /**
     * Add a participant to the round
     * @param playerId The player's UUID
     * @return True if added successfully
     */
    public boolean addParticipant(UUID playerId) {
        if (!participants.contains(playerId)) {
            participants.add(playerId);
            return true;
        }
        return false;
    }
    
    /**
     * Remove a participant from the round
     * @param playerId The player's UUID
     * @return True if removed successfully
     */
    public boolean removeParticipant(UUID playerId) {
        return participants.remove(playerId);
    }
    
    /**
     * Get the opponent of a specific player (for 1v1 rounds)
     * @param playerId The player's UUID
     * @return The opponent's UUID, or null if not found or not 1v1
     */
    public UUID getOpponent(UUID playerId) {
        if (participants.size() == 2 && participants.contains(playerId)) {
            for (UUID participant : participants) {
                if (!participant.equals(playerId)) {
                    return participant;
                }
            }
        }
        return null;
    }
    
    /**
     * Get all participants except the specified player
     * @param playerId The player to exclude
     * @return List of other participants
     */
    public List<UUID> getOtherParticipants(UUID playerId) {
        List<UUID> others = new ArrayList<>(participants);
        others.remove(playerId);
        return others;
    }
    
    /**
     * Check if the round has started
     * @return True if round is active or completed
     */
    public boolean hasStarted() {
        return status == RoundStatus.ACTIVE || status == RoundStatus.COMPLETED;
    }
    
    /**
     * Check if the round is finished
     * @return True if round is completed or cancelled
     */
    public boolean isFinished() {
        return status == RoundStatus.COMPLETED || status == RoundStatus.CANCELLED;
    }
    
    /**
     * Check if the round is currently active
     * @return True if round is in progress
     */
    public boolean isActive() {
        return status == RoundStatus.ACTIVE;
    }
    
    /**
     * Check if the round is in preparation phase
     * @return True if round is preparing to start
     */
    public boolean isPreparing() {
        return status == RoundStatus.PREPARING;
    }
    
    /**
     * Start the round preparation phase
     */
    public void startPreparation() {
        if (status == RoundStatus.PENDING) {
            this.status = RoundStatus.PREPARING;
        }
    }
    
    /**
     * Start the actual round
     */
    public void startRound() {
        if (status == RoundStatus.PREPARING) {
            this.status = RoundStatus.ACTIVE;
            this.startTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Cancel the round
     */
    public void cancel() {
        this.status = RoundStatus.CANCELLED;
        this.endTime = System.currentTimeMillis();
    }
    
    /**
     * Get the duration of the round in milliseconds
     * @return Duration, or 0 if not completed
     */
    public long getDuration() {
        if (startTime > 0 && endTime > 0) {
            return endTime - startTime;
        }
        return 0;
    }
    
    /**
     * Get a formatted duration string
     * @return Duration in MM:SS format
     */
    public String getFormattedDuration() {
        long duration = getDuration();
        if (duration <= 0) {
            return "00:00";
        }
        
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Check if the round has an assigned arena
     * @return True if arena is assigned
     */
    public boolean hasAssignedArena() {
        return assignedArena != null && !assignedArena.isEmpty();
    }
    
    /**
     * Check if the round has an assigned kit
     * @return True if kit is assigned
     */
    public boolean hasAssignedKit() {
        return assignedKit != null && !assignedKit.isEmpty();
    }
    
    /**
     * Check if this is a 1v1 round
     * @return True if exactly 2 participants
     */
    public boolean is1v1() {
        return participants.size() == 2;
    }
    
    /**
     * Check if this is a team/multi-player round
     * @return True if more than 2 participants
     */
    public boolean isMultiPlayer() {
        return participants.size() > 2;
    }
    
    /**
     * Validate that the round is properly configured
     * @return List of validation errors (empty if valid)
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        
        if (participants.isEmpty()) {
            errors.add("Round must have at least one participant");
        }
        
        if (tournamentId == null || tournamentId.isEmpty()) {
            errors.add("Round must be associated with a tournament");
        }
        
        if (status == RoundStatus.ACTIVE && !hasAssignedArena()) {
            errors.add("Active rounds must have an assigned arena");
        }
        
        if (preparationTime < 0) {
            errors.add("Preparation time cannot be negative");
        }
        
        return errors;
    }
    
    /**
     * Check if the round is ready to start
     * @return True if round can be started
     */
    public boolean isReadyToStart() {
        List<String> errors = validate();
        return errors.isEmpty() && status == RoundStatus.PENDING;
    }
    
    @Override
    public String toString() {
        return String.format("Round{id='%s', tournament='%s', participants=%d, status=%s, winner=%s}", 
            id, tournamentId, participants.size(), status, winner);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Round round = (Round) obj;
        return id.equals(round.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}