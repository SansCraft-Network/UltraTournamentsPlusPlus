package top.sanscraft.ultratournamentsplusplus.models;

import java.util.UUID;

/**
 * Represents a link between a Discord user and a Minecraft player
 */
public class DiscordLink {
    
    private String discordId;
    private UUID minecraftUuid;
    private String minecraftUsername;
    private long linkedTimestamp;
    private String verificationCode;
    private boolean verified;
    private boolean notificationsEnabled;
    
    /**
     * Constructor for new Discord link
     * @param discordId Discord user ID
     * @param minecraftUuid Minecraft player UUID
     * @param minecraftUsername Minecraft username
     */
    public DiscordLink(String discordId, UUID minecraftUuid, String minecraftUsername) {
        this.discordId = discordId;
        this.minecraftUuid = minecraftUuid;
        this.minecraftUsername = minecraftUsername;
        this.linkedTimestamp = System.currentTimeMillis();
        this.verified = false;
        this.notificationsEnabled = true;
        this.verificationCode = generateVerificationCode();
    }
    
    /**
     * Constructor for loading from database
     */
    public DiscordLink(String discordId, UUID minecraftUuid, String minecraftUsername, 
                      long linkedTimestamp, boolean verified, boolean notificationsEnabled) {
        this.discordId = discordId;
        this.minecraftUuid = minecraftUuid;
        this.minecraftUsername = minecraftUsername;
        this.linkedTimestamp = linkedTimestamp;
        this.verified = verified;
        this.notificationsEnabled = notificationsEnabled;
    }
    
    /**
     * Generate a random verification code
     * @return 6-digit verification code
     */
    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 999999));
    }
    
    // Getters and setters
    public String getDiscordId() {
        return discordId;
    }
    
    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }
    
    public UUID getMinecraftUuid() {
        return minecraftUuid;
    }
    
    public void setMinecraftUuid(UUID minecraftUuid) {
        this.minecraftUuid = minecraftUuid;
    }
    
    public String getMinecraftUsername() {
        return minecraftUsername;
    }
    
    public void setMinecraftUsername(String minecraftUsername) {
        this.minecraftUsername = minecraftUsername;
    }
    
    public long getLinkedTimestamp() {
        return linkedTimestamp;
    }
    
    public void setLinkedTimestamp(long linkedTimestamp) {
        this.linkedTimestamp = linkedTimestamp;
    }
    
    public String getVerificationCode() {
        return verificationCode;
    }
    
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    
    /**
     * Check if the link is expired (unverified for more than 24 hours)
     * @return True if expired
     */
    public boolean isExpired() {
        if (verified) {
            return false;
        }
        long expirationTime = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
        return System.currentTimeMillis() - linkedTimestamp > expirationTime;
    }
    
    @Override
    public String toString() {
        return "DiscordLink{" +
                "discordId='" + discordId + '\'' +
                ", minecraftUuid=" + minecraftUuid +
                ", minecraftUsername='" + minecraftUsername + '\'' +
                ", verified=" + verified +
                ", notificationsEnabled=" + notificationsEnabled +
                '}';
    }
}