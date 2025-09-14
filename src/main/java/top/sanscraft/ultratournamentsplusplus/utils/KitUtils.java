package top.sanscraft.ultratournamentsplusplus.utils;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import top.sanscraft.ultratournamentsplusplus.models.Kit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility class for kit-related operations
 * Provides helper methods for kit validation, application, and player state management
 */
public class KitUtils {
    
    /**
     * Validate if a kit is properly configured
     * @param kit The kit to validate
     * @return True if kit is valid and ready for use
     */
    public static boolean isKitValid(Kit kit) {
        if (kit == null) {
            return false;
        }
        
        return kit.isReady();
    }
    
    /**
     * Save a player's current state (inventory, armor, effects, etc.)
     * @param player The player whose state to save
     * @param stateName Name for the saved state
     * @return A kit representing the player's current state
     */
    public static Kit savePlayerState(Player player, String stateName) {
        Kit playerState = new Kit(stateName, player.getUniqueId());
        playerState.setDescription("Saved player state");
        
        // Save inventory
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = new ItemStack[36]; // Main inventory
        ItemStack[] inventoryContents = inventory.getContents();
        
        // Copy main inventory (slots 0-35)
        System.arraycopy(inventoryContents, 0, contents, 0, Math.min(36, inventoryContents.length));
        playerState.setItems(contents);
        
        // Save armor
        ItemStack[] armor = new ItemStack[4];
        armor[0] = inventory.getBoots();
        armor[1] = inventory.getLeggings();
        armor[2] = inventory.getChestplate();
        armor[3] = inventory.getHelmet();
        playerState.setArmor(armor);
        
        // Save off-hand
        playerState.setOffHand(inventory.getItemInOffHand());
        
        // Save potion effects
        if (!player.getActivePotionEffects().isEmpty()) {
            playerState.setPotionEffects(new ArrayList<>(player.getActivePotionEffects()));
        }
        
        // Save experience
        playerState.setExperienceLevel(player.getLevel());
        
        return playerState;
    }
    
    /**
     * Restore a player's state from a saved kit
     * @param player The player to restore
     * @param savedState The saved state kit
     */
    public static void restorePlayerState(Player player, Kit savedState) {
        if (savedState == null) {
            return;
        }
        
        applyKitToPlayer(player, savedState);
    }
    
    /**
     * Apply a kit to a player with all safety checks
     * @param player The player to apply the kit to
     * @param kit The kit to apply
     * @return True if kit was successfully applied
     */
    public static boolean applyKitToPlayer(Player player, Kit kit) {
        if (!isKitValid(kit)) {
            return false;
        }
        
        try {
            // Clear existing effects
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            
            // Clear inventory if specified
            if (kit.isClearInventory()) {
                player.getInventory().clear();
            }
            
            // Apply inventory items
            ItemStack[] items = kit.getItems();
            for (int i = 0; i < items.length && i < 36; i++) {
                if (items[i] != null && !items[i].getType().isAir()) {
                    player.getInventory().setItem(i, items[i].clone());
                }
            }
            
            // Apply armor
            ItemStack[] armor = kit.getArmor();
            if (armor.length >= 1 && armor[0] != null) {
                player.getInventory().setBoots(armor[0].clone());
            }
            if (armor.length >= 2 && armor[1] != null) {
                player.getInventory().setLeggings(armor[1].clone());
            }
            if (armor.length >= 3 && armor[2] != null) {
                player.getInventory().setChestplate(armor[2].clone());
            }
            if (armor.length >= 4 && armor[3] != null) {
                player.getInventory().setHelmet(armor[3].clone());
            }
            
            // Apply off-hand item
            if (kit.getOffHand() != null) {
                player.getInventory().setItemInOffHand(kit.getOffHand().clone());
            }
            
            // Apply potion effects
            for (PotionEffect effect : kit.getPotionEffects()) {
                player.addPotionEffect(effect);
            }
            
            // Reset health if specified
            if (kit.isResetHealth()) {
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH))
                       .setBaseValue(20.0);
                player.setHealth(20.0);
            }
            
            // Reset hunger if specified
            if (kit.isResetHunger()) {
                player.setFoodLevel(20);
                player.setSaturation(20f);
            }
            
            // Reset experience if specified
            if (kit.isResetExperience()) {
                player.setLevel(kit.getExperienceLevel());
                player.setExp(0f);
            }
            
            return true;
            
        } catch (Exception e) {
            // Log error and return false
            return false;
        }
    }
    
    /**
     * Compare two kits for equality (excluding timestamps and IDs)
     * @param kit1 First kit
     * @param kit2 Second kit
     * @return True if kits have the same contents
     */
    public static boolean areKitsEqual(Kit kit1, Kit kit2) {
        if (kit1 == null && kit2 == null) {
            return true;
        }
        if (kit1 == null || kit2 == null) {
            return false;
        }
        
        // Compare basic properties
        if (!Objects.equals(kit1.getName(), kit2.getName()) ||
            !Objects.equals(kit1.getDescription(), kit2.getDescription()) ||
            kit1.getExperienceLevel() != kit2.getExperienceLevel() ||
            kit1.isResetHealth() != kit2.isResetHealth() ||
            kit1.isResetHunger() != kit2.isResetHunger() ||
            kit1.isResetExperience() != kit2.isResetExperience() ||
            kit1.isClearInventory() != kit2.isClearInventory()) {
            return false;
        }
        
        // Compare inventories
        if (!compareItemArrays(kit1.getItems(), kit2.getItems())) {
            return false;
        }
        
        // Compare armor
        if (!compareItemArrays(kit1.getArmor(), kit2.getArmor())) {
            return false;
        }
        
        // Compare off-hand
        if (!Objects.equals(kit1.getOffHand(), kit2.getOffHand())) {
            return false;
        }
        
        // Compare potion effects
        return comparePotionEffects(kit1.getPotionEffects(), kit2.getPotionEffects());
    }
    
    /**
     * Compare two ItemStack arrays for equality
     * @param array1 First array
     * @param array2 Second array
     * @return True if arrays contain the same items
     */
    private static boolean compareItemArrays(ItemStack[] array1, ItemStack[] array2) {
        if (array1 == null && array2 == null) {
            return true;
        }
        if (array1 == null || array2 == null) {
            return false;
        }
        if (array1.length != array2.length) {
            return false;
        }
        
        for (int i = 0; i < array1.length; i++) {
            ItemStack item1 = array1[i];
            ItemStack item2 = array2[i];
            
            if (item1 == null && item2 == null) {
                continue;
            }
            if (item1 == null || item2 == null) {
                return false;
            }
            if (!item1.equals(item2)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Compare two PotionEffect lists for equality
     * @param effects1 First list
     * @param effects2 Second list
     * @return True if lists contain the same effects
     */
    private static boolean comparePotionEffects(List<PotionEffect> effects1, List<PotionEffect> effects2) {
        if (effects1 == null && effects2 == null) {
            return true;
        }
        if (effects1 == null || effects2 == null) {
            return false;
        }
        if (effects1.size() != effects2.size()) {
            return false;
        }
        
        // Since order might not matter, check if all effects in list1 are in list2
        for (PotionEffect effect1 : effects1) {
            boolean found = false;
            for (PotionEffect effect2 : effects2) {
                if (effect1.equals(effect2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Count the number of non-null items in a kit's inventory
     * @param kit The kit to count items for
     * @return Number of non-null items
     */
    public static int countKitItems(Kit kit) {
        if (kit == null) {
            return 0;
        }
        
        return kit.getItemCount();
    }
    
    /**
     * Count the number of non-null armor pieces in a kit
     * @param kit The kit to count armor for
     * @return Number of non-null armor pieces
     */
    public static int countKitArmor(Kit kit) {
        if (kit == null || kit.getArmor() == null) {
            return 0;
        }
        
        int count = 0;
        for (ItemStack armor : kit.getArmor()) {
            if (armor != null && !armor.getType().isAir()) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Get a summary string of a kit's contents
     * @param kit The kit to summarize
     * @return A human-readable summary
     */
    public static String getKitSummary(Kit kit) {
        if (kit == null) {
            return "No kit";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Kit '").append(kit.getName()).append("': ");
        
        int itemCount = kit.getItemCount();
        int armorCount = countKitArmor(kit);
        int effectCount = kit.getPotionEffects().size();
        
        summary.append(itemCount).append(" total items (");
        summary.append(armorCount).append(" armor pieces), ");
        summary.append(effectCount).append(" effects");
        
        if (kit.isResetHealth()) {
            summary.append(", resets health");
        }
        
        if (kit.isResetHunger()) {
            summary.append(", resets hunger");
        }
        
        if (kit.isResetExperience()) {
            summary.append(", sets experience to ").append(kit.getExperienceLevel());
        }
        
        return summary.toString();
    }
    
    /**
     * Create a basic PvP kit with common items
     * @param name Kit name
     * @param creator Kit creator
     * @return A basic PvP kit
     */
    public static Kit createBasicPvPKit(String name, UUID creator) {
        Kit kit = new Kit(name, creator);
        kit.setDescription("Basic PvP kit with sword, bow, and armor");
        
        // Add basic items (this is just an example structure)
        // The actual items would be set by the kit creation command
        kit.setClearInventory(true);
        kit.setResetHealth(true);
        kit.setResetHunger(true);
        kit.setResetExperience(true);
        
        return kit;
    }
    
    /**
     * Create a basic parkour kit
     * @param name Kit name
     * @param creator Kit creator
     * @return A basic parkour kit
     */
    public static Kit createBasicParkourKit(String name, UUID creator) {
        Kit kit = new Kit(name, creator);
        kit.setDescription("Basic parkour kit with minimal items");
        
        kit.setClearInventory(true);
        kit.setResetHealth(true);
        kit.setResetHunger(true);
        kit.setResetExperience(true);
        
        return kit;
    }
}