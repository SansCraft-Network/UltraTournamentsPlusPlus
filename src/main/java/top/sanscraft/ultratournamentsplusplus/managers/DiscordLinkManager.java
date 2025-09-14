package top.sanscraft.ultratournamentsplusplus.managers;

import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.DiscordLink;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages Discord account linking operations
 */
public class DiscordLinkManager {
    
    private final UltraTournamentsPlusPlus plugin;
    private final Map<String, DiscordLink> linksByDiscordId;
    private final Map<UUID, DiscordLink> linksByMinecraftUuid;
    private final Map<String, DiscordLink> pendingVerifications;
    
    public DiscordLinkManager(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
        this.linksByDiscordId = new ConcurrentHashMap<>();
        this.linksByMinecraftUuid = new ConcurrentHashMap<>();
        this.pendingVerifications = new ConcurrentHashMap<>();
        
        // Start cleanup task for expired verifications
        startCleanupTask();
    }
    
    /**
     * Load Discord links from storage
     */
    public void loadLinks() {
        plugin.getLogger().info("Loading Discord links from storage...");
        // TODO: Load from database via DatabaseManager
        // For now, this is a placeholder
    }
    
    /**
     * Save Discord links to storage
     */
    public void saveLinks() {
        plugin.getLogger().info("Saving Discord links to storage...");
        // TODO: Save to database via DatabaseManager
    }
    
    /**
     * Start a new Discord account linking process
     * @param discordId Discord user ID
     * @param player Minecraft player
     * @return Generated verification code
     */
    public String startLinking(String discordId, Player player) {
        return startLinking(discordId, player.getUniqueId(), player.getName());
    }
    
    /**
     * Start a new Discord account linking process
     * @param discordId Discord user ID
     * @param minecraftUuid Minecraft player UUID
     * @param minecraftUsername Minecraft player name
     * @return Generated verification code
     */
    public String startLinking(String discordId, UUID minecraftUuid, String minecraftUsername) {
        // Check if Discord account is already linked
        if (isDiscordLinked(discordId)) {
            return null; // Already linked
        }
        
        // Check if Minecraft account is already linked
        if (isMinecraftLinked(minecraftUuid)) {
            return null; // Already linked
        }
        
        // Remove any existing pending verification for this Discord ID
        pendingVerifications.entrySet().removeIf(entry -> 
            entry.getValue().getDiscordId().equals(discordId));
        
        // Create new link
        DiscordLink newLink = new DiscordLink(discordId, minecraftUuid, minecraftUsername);
        pendingVerifications.put(newLink.getVerificationCode(), newLink);
        
        plugin.getLogger().info("Started linking process for Discord ID " + discordId + 
                               " with Minecraft account " + minecraftUsername);
        
        return newLink.getVerificationCode();
    }
    
    /**
     * Verify a Discord link using verification code
     * @param verificationCode 6-digit verification code
     * @param minecraftUuid Player UUID who is verifying
     * @return True if verification successful
     */
    public boolean verifyLink(String verificationCode, UUID minecraftUuid) {
        DiscordLink pendingLink = pendingVerifications.get(verificationCode);
        
        if (pendingLink == null) {
            return false; // Invalid or expired code
        }
        
        if (!pendingLink.getMinecraftUuid().equals(minecraftUuid)) {
            return false; // Wrong player trying to verify
        }
        
        if (pendingLink.isExpired()) {
            pendingVerifications.remove(verificationCode);
            return false; // Expired
        }
        
        // Complete the verification
        pendingLink.setVerified(true);
        linksByDiscordId.put(pendingLink.getDiscordId(), pendingLink);
        linksByMinecraftUuid.put(pendingLink.getMinecraftUuid(), pendingLink);
        pendingVerifications.remove(verificationCode);
        
        plugin.getLogger().info("Successfully verified Discord link for " + 
                               pendingLink.getMinecraftUsername() + " (" + pendingLink.getDiscordId() + ")");
        
        // Save to database
        saveLinks();
        
        return true;
    }
    
    /**
     * Unlink a Discord account
     * @param discordId Discord user ID
     * @return True if unlinked successfully
     */
    public boolean unlinkDiscord(String discordId) {
        DiscordLink link = linksByDiscordId.remove(discordId);
        if (link != null) {
            linksByMinecraftUuid.remove(link.getMinecraftUuid());
            plugin.getLogger().info("Unlinked Discord account " + discordId + 
                                   " from " + link.getMinecraftUsername());
            saveLinks();
            return true;
        }
        return false;
    }
    
    /**
     * Unlink a Minecraft account
     * @param minecraftUuid Minecraft player UUID
     * @return True if unlinked successfully
     */
    public boolean unlinkMinecraft(UUID minecraftUuid) {
        DiscordLink link = linksByMinecraftUuid.remove(minecraftUuid);
        if (link != null) {
            linksByDiscordId.remove(link.getDiscordId());
            plugin.getLogger().info("Unlinked Minecraft account " + link.getMinecraftUsername() + 
                                   " from Discord " + link.getDiscordId());
            saveLinks();
            return true;
        }
        return false;
    }
    
    /**
     * Get Discord link by Discord ID
     * @param discordId Discord user ID
     * @return DiscordLink or null if not found
     */
    public DiscordLink getLinkByDiscordId(String discordId) {
        return linksByDiscordId.get(discordId);
    }
    
    /**
     * Get Discord link by Minecraft UUID
     * @param minecraftUuid Minecraft player UUID
     * @return DiscordLink or null if not found
     */
    public DiscordLink getLinkByMinecraftUuid(UUID minecraftUuid) {
        return linksByMinecraftUuid.get(minecraftUuid);
    }
    
    /**
     * Check if a Discord account is linked
     * @param discordId Discord user ID
     * @return True if linked
     */
    public boolean isDiscordLinked(String discordId) {
        return linksByDiscordId.containsKey(discordId);
    }
    
    /**
     * Check if a Minecraft account is linked
     * @param minecraftUuid Minecraft player UUID
     * @return True if linked
     */
    public boolean isMinecraftLinked(UUID minecraftUuid) {
        return linksByMinecraftUuid.containsKey(minecraftUuid);
    }
    
    /**
     * Get Discord ID from Minecraft UUID
     * @param minecraftUuid Minecraft player UUID
     * @return Discord ID or null if not linked
     */
    public String getDiscordId(UUID minecraftUuid) {
        DiscordLink link = getLinkByMinecraftUuid(minecraftUuid);
        return link != null ? link.getDiscordId() : null;
    }
    
    /**
     * Get Minecraft UUID from Discord ID
     * @param discordId Discord user ID
     * @return Minecraft UUID or null if not linked
     */
    public UUID getMinecraftUuid(String discordId) {
        DiscordLink link = getLinkByDiscordId(discordId);
        return link != null ? link.getMinecraftUuid() : null;
    }
    
    /**
     * Get Minecraft username from Discord ID
     * @param discordId Discord user ID
     * @return Minecraft username or null if not linked
     */
    public String getMinecraftUsername(String discordId) {
        DiscordLink link = getLinkByDiscordId(discordId);
        return link != null ? link.getMinecraftUsername() : null;
    }
    
    /**
     * Update notification settings for a Discord link
     * @param discordId Discord user ID
     * @param enabled Whether notifications should be enabled
     * @return True if updated successfully
     */
    public boolean updateNotificationSettings(String discordId, boolean enabled) {
        DiscordLink link = getLinkByDiscordId(discordId);
        if (link != null) {
            link.setNotificationsEnabled(enabled);
            saveLinks();
            return true;
        }
        return false;
    }
    
    /**
     * Get all linked accounts with notifications enabled
     * @return List of DiscordLinks with notifications enabled
     */
    public List<DiscordLink> getLinksWithNotificationsEnabled() {
        return linksByDiscordId.values().stream()
                .filter(DiscordLink::isNotificationsEnabled)
                .toList();
    }
    
    /**
     * Get all linked Discord IDs for a list of Minecraft UUIDs
     * @param minecraftUuids List of Minecraft UUIDs
     * @return List of Discord IDs
     */
    public List<String> getDiscordIds(List<UUID> minecraftUuids) {
        List<String> discordIds = new ArrayList<>();
        for (UUID uuid : minecraftUuids) {
            String discordId = getDiscordId(uuid);
            if (discordId != null) {
                discordIds.add(discordId);
            }
        }
        return discordIds;
    }
    
    /**
     * Check if a player is online in Minecraft
     * @param minecraftUuid Player UUID
     * @return True if player is online
     */
    public boolean isPlayerOnline(UUID minecraftUuid) {
        Player player = Bukkit.getPlayer(minecraftUuid);
        return player != null && player.isOnline();
    }
    
    /**
     * Get total number of linked accounts
     * @return Number of verified links
     */
    public int getLinkedAccountCount() {
        return linksByDiscordId.size();
    }
    
    /**
     * Get pending verification by code
     * @param verificationCode Verification code
     * @return DiscordLink or null if not found
     */
    public DiscordLink getPendingVerification(String verificationCode) {
        return pendingVerifications.get(verificationCode);
    }
    
    /**
     * Clean up expired pending verifications
     */
    private void cleanupExpiredVerifications() {
        int removedCount = 0;
        var iterator = pendingVerifications.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            plugin.getLogger().info("Cleaned up " + removedCount + " expired verification codes");
        }
    }
    
    /**
     * Start cleanup task for expired verifications
     */
    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, 
            this::cleanupExpiredVerifications, 
            20 * 60 * 15, // Initial delay: 15 minutes
            20 * 60 * 30  // Repeat every 30 minutes
        );
    }
    
    /**
     * Reload the Discord link manager
     */
    public void reload() {
        linksByDiscordId.clear();
        linksByMinecraftUuid.clear();
        pendingVerifications.clear();
        loadLinks();
        plugin.getLogger().info("Discord link manager reloaded");
    }
    
    /**
     * Shutdown the Discord link manager
     */
    public void shutdown() {
        saveLinks();
        plugin.getLogger().info("Discord link manager shutdown");
    }
}