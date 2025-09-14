package top.sanscraft.ultratournamentsplusplus.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a tournament
 */
public class Tournament {
    
    private String name;
    private String description;
    private TournamentStatus status;
    private TournamentType type;
    private int maxParticipants;
    private List<UUID> participants;
    private UUID creator;
    private long createdTime;
    private long startTime;
    
    // Challonge integration
    private String challongeId;
    private String challongeUrl;
    
    // Arena and Kit assignments
    private String assignedArena;
    private String assignedKit;
    
    public Tournament(String name, String description, TournamentType type, int maxParticipants, UUID creator) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.maxParticipants = maxParticipants;
        this.creator = creator;
        this.status = TournamentStatus.PREPARING;
        this.createdTime = System.currentTimeMillis();
        this.participants = new ArrayList<>();
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public TournamentStatus getStatus() { return status; }
    public void setStatus(TournamentStatus status) { this.status = status; }
    
    public TournamentType getType() { return type; }
    public void setType(TournamentType type) { this.type = type; }
    
    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    
    public List<UUID> getParticipants() { return participants; }
    public void setParticipants(List<UUID> participants) { this.participants = participants; }
    
    public UUID getCreator() { return creator; }
    public void setCreator(UUID creator) { this.creator = creator; }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    
    public String getChallongeId() { return challongeId; }
    public void setChallongeId(String challongeId) { this.challongeId = challongeId; }
    
    public String getChallongeUrl() { return challongeUrl; }
    public void setChallongeUrl(String challongeUrl) { this.challongeUrl = challongeUrl; }
    
    public String getAssignedArena() { return assignedArena; }
    public void setAssignedArena(String assignedArena) { this.assignedArena = assignedArena; }
    
    public String getAssignedKit() { return assignedKit; }
    public void setAssignedKit(String assignedKit) { this.assignedKit = assignedKit; }
    
    /**
     * Add a participant to the tournament
     * @param playerId Player UUID
     * @return True if added successfully
     */
    public boolean addParticipant(UUID playerId) {
        if (participants.size() >= maxParticipants) {
            return false;
        }
        if (participants.contains(playerId)) {
            return false;
        }
        return participants.add(playerId);
    }
    
    /**
     * Remove a participant from the tournament
     * @param playerId Player UUID
     * @return True if removed successfully
     */
    public boolean removeParticipant(UUID playerId) {
        return participants.remove(playerId);
    }
    
    /**
     * Check if the tournament is full
     * @return True if tournament is at max capacity
     */
    public boolean isFull() {
        return participants.size() >= maxParticipants;
    }
    
    /**
     * Check if the tournament has an assigned arena
     * @return True if arena is assigned
     */
    public boolean hasAssignedArena() {
        return assignedArena != null && !assignedArena.isEmpty();
    }
    
    /**
     * Check if the tournament has an assigned kit
     * @return True if kit is assigned
     */
    public boolean hasAssignedKit() {
        return assignedKit != null && !assignedKit.isEmpty();
    }
    
    /**
     * Get current participant count
     * @return Number of participants
     */
    public int getParticipantCount() {
        return participants.size();
    }
}