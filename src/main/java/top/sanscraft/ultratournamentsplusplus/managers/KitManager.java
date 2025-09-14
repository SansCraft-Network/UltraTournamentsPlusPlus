package top.sanscraft.ultratournamentsplusplus.managers;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.Kit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manages all kit operations and player kit application
 */
public class KitManager {
    
    private final UltraTournamentsPlusPlus plugin;
    private final Map<String, Kit> kits;
    
    public KitManager(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
        this.kits = new HashMap<>();
    }
    
    /**
     * Initialize the kit manager
     */
    public void initialize() {
        plugin.getLogger().info("Initializing KitManager...");
        loadKits();
    }
    
    /**
     * Load kits from storage
     */
    public void loadKits() {
        // TODO: Implement kit loading from database/files
        plugin.getLogger().info("Loading kits from storage...");
    }
    
    /**
     * Save kits to storage
     */
    public void saveKits() {
        // TODO: Implement kit saving to database/files
        plugin.getLogger().info("Saving kits to storage...");
    }
    
    /**
     * Reload kit manager
     */
    public void reload() {
        kits.clear();
        loadKits();
        plugin.getLogger().info("Kit manager reloaded");
    }
    
    /**
     * Get all kit names
     * @return Set of kit names
     */
    public Set<String> getKitNames() {
        return kits.keySet();
    }
    
    /**
     * Get a kit by name
     * @param name Kit name
     * @return Kit or null if not found
     */
    public Kit getKit(String name) {
        return kits.get(name.toLowerCase());
    }
    
    /**
     * Create a new kit
     * @param name Kit name
     * @param creator Creator UUID
     * @return Created kit or null if failed
     */
    public Kit createKit(String name, UUID creator) {
        if (kits.containsKey(name.toLowerCase())) {
            return null; // Kit already exists
        }
        
        Kit kit = new Kit(name, creator);
        kits.put(name.toLowerCase(), kit);
        
        plugin.getLogger().info("Created kit: " + name);
        return kit;
    }
    
    /**
     * Delete a kit
     * @param name Kit name
     * @return True if deleted, false if not found
     */
    public boolean deleteKit(String name) {
        Kit kit = kits.remove(name.toLowerCase());
        if (kit != null) {
            plugin.getLogger().info("Deleted kit: " + name);
            return true;
        }
        return false;
    }
    
    /**
     * Create a kit from a player's current inventory
     * @param name Kit name
     * @param player Player to copy from
     * @return Created kit or null if failed
     */
    public Kit createKitFromPlayer(String name, Player player) {
        if (kits.containsKey(name.toLowerCase())) {
            return null; // Kit already exists
        }
        
        Kit kit = new Kit(name, player.getUniqueId());
        
        // Copy main inventory
        ItemStack[] inventory = player.getInventory().getContents();
        kit.setItems(inventory);
        
        // Copy armor
        ItemStack[] armor = player.getInventory().getArmorContents();
        kit.setArmor(armor);
        
        // Copy off-hand
        kit.setOffHand(player.getInventory().getItemInOffHand());
        
        // Copy active potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            kit.addPotionEffect(effect);
        }
        
        // Set experience level
        kit.setExperienceLevel(player.getLevel());
        
        kits.put(name.toLowerCase(), kit);
        
        plugin.getLogger().info("Created kit '" + name + "' from player " + player.getName());
        return kit;
    }
    
    /**
     * Apply a kit to a player
     * @param kit Kit to apply
     * @param player Player to apply to
     * @return True if applied successfully
     */
    public boolean applyKit(Kit kit, Player player) {
        if (kit == null || player == null) {
            return false;
        }
        
        try {
            // Clear inventory if specified
            if (kit.isClearInventory()) {
                player.getInventory().clear();
            }
            
            // Apply main inventory items
            ItemStack[] items = kit.getItems();
            for (int i = 0; i < items.length && i < 36; i++) {
                player.getInventory().setItem(i, items[i]);
            }
            
            // Apply armor
            ItemStack[] armor = kit.getArmor();
            player.getInventory().setArmorContents(armor);
            
            // Apply off-hand item
            if (kit.getOffHand() != null) {
                player.getInventory().setItemInOffHand(kit.getOffHand());
            }
            
            // Reset health if specified
            if (kit.isResetHealth()) {
                player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
            }
            
            // Reset hunger if specified
            if (kit.isResetHunger()) {
                player.setFoodLevel(20);
                player.setSaturation(20.0f);
                player.setExhaustion(0.0f);
            }
            
            // Reset/set experience if specified
            if (kit.isResetExperience()) {
                player.setLevel(kit.getExperienceLevel());
                player.setExp(0.0f);
                player.setTotalExperience(0);
            }
            
            // Clear existing potion effects and apply kit effects
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            
            for (PotionEffect effect : kit.getPotionEffects()) {
                player.addPotionEffect(effect);
            }
            
            // Update inventory
            player.updateInventory();
            
            plugin.getLogger().info("Applied kit '" + kit.getName() + "' to player " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply kit '" + kit.getName() + "' to player " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Apply a kit to a player by name
     * @param kitName Kit name
     * @param player Player to apply to
     * @return True if applied successfully
     */
    public boolean applyKit(String kitName, Player player) {
        Kit kit = getKit(kitName);
        return applyKit(kit, player);
    }
    
    /**
     * Save a player's current state as a kit
     * @param kitName Kit name
     * @param player Player to save from
     * @return True if saved successfully
     */
    public boolean savePlayerAsKit(String kitName, Player player) {
        Kit existingKit = getKit(kitName);
        if (existingKit == null) {
            return createKitFromPlayer(kitName, player) != null;
        }
        
        // Update existing kit
        try {
            // Update inventory contents
            existingKit.setItems(player.getInventory().getContents());
            existingKit.setArmor(player.getInventory().getArmorContents());
            existingKit.setOffHand(player.getInventory().getItemInOffHand());
            
            // Update potion effects
            existingKit.clearPotionEffects();
            for (PotionEffect effect : player.getActivePotionEffects()) {
                existingKit.addPotionEffect(effect);
            }
            
            // Update experience level
            existingKit.setExperienceLevel(player.getLevel());
            
            plugin.getLogger().info("Updated kit '" + kitName + "' from player " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update kit '" + kitName + "' from player " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Clone a kit with a new name
     * @param originalName Original kit name
     * @param newName New kit name
     * @param creator Creator UUID for the new kit
     * @return Cloned kit or null if failed
     */
    public Kit cloneKit(String originalName, String newName, UUID creator) {
        Kit original = getKit(originalName);
        if (original == null || kits.containsKey(newName.toLowerCase())) {
            return null;
        }
        
        Kit cloned = original.clone();
        cloned.setName(newName);
        cloned.setCreator(creator);
        cloned.setCreatedTime(System.currentTimeMillis());
        
        kits.put(newName.toLowerCase(), cloned);
        
        plugin.getLogger().info("Cloned kit '" + originalName + "' as '" + newName + "'");
        return cloned;
    }
    
    /**
     * Get the number of kits
     * @return Kit count
     */
    public int getKitCount() {
        return kits.size();
    }
    
    /**
     * Check if a kit exists
     * @param name Kit name
     * @return True if kit exists
     */
    public boolean hasKit(String name) {
        return kits.containsKey(name.toLowerCase());
    }
    
    /**
     * Prepare a player for tournament (clear effects, set gamemode, etc.)
     * @param player Player to prepare
     */
    public void preparePlayerForTournament(Player player) {
        // Set survival mode
        player.setGameMode(GameMode.SURVIVAL);
        
        // Clear inventory
        player.getInventory().clear();
        
        // Reset health and hunger
        player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setExhaustion(0.0f);
        
        // Clear potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // Reset experience
        player.setLevel(0);
        player.setExp(0.0f);
        player.setTotalExperience(0);
        
        // Clear fire ticks
        player.setFireTicks(0);
        
        // Update inventory
        player.updateInventory();
        
        plugin.getLogger().info("Prepared player " + player.getName() + " for tournament");
    }
}