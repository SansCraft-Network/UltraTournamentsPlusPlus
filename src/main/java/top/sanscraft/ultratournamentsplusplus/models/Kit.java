package top.sanscraft.ultratournamentsplusplus.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a kit that can be applied to players
 */
public class Kit {
    
    private String name;
    private String description;
    private UUID creator;
    private long createdTime;
    
    // Inventory contents
    private ItemStack[] items; // 36 slots for main inventory
    private ItemStack[] armor; // 4 slots for armor (boots, leggings, chestplate, helmet)
    private ItemStack offHand; // Off-hand item
    
    // Player effects
    private List<PotionEffect> potionEffects;
    
    // Kit settings
    private boolean clearInventory;
    private boolean resetHealth;
    private boolean resetHunger;
    private boolean resetExperience;
    private int experienceLevel;
    
    public Kit(String name, UUID creator) {
        this.name = name;
        this.creator = creator;
        this.createdTime = System.currentTimeMillis();
        this.items = new ItemStack[36];
        this.armor = new ItemStack[4];
        this.potionEffects = new ArrayList<>();
        this.clearInventory = true;
        this.resetHealth = true;
        this.resetHunger = true;
        this.resetExperience = true;
        this.experienceLevel = 0;
    }
    
    // Basic getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public UUID getCreator() { return creator; }
    public void setCreator(UUID creator) { this.creator = creator; }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    // Inventory management
    public ItemStack[] getItems() { return items.clone(); }
    public void setItems(ItemStack[] items) { 
        this.items = new ItemStack[36];
        System.arraycopy(items, 0, this.items, 0, Math.min(items.length, 36));
    }
    
    public ItemStack[] getArmor() { return armor.clone(); }
    public void setArmor(ItemStack[] armor) { 
        this.armor = new ItemStack[4];
        System.arraycopy(armor, 0, this.armor, 0, Math.min(armor.length, 4));
    }
    
    public ItemStack getOffHand() { return offHand != null ? offHand.clone() : null; }
    public void setOffHand(ItemStack offHand) { this.offHand = offHand != null ? offHand.clone() : null; }
    
    // Individual item access
    public void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot < 36) {
            items[slot] = item != null ? item.clone() : null;
        }
    }
    
    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < 36 && items[slot] != null) {
            return items[slot].clone();
        }
        return null;
    }
    
    // Armor specific methods
    public void setHelmet(ItemStack helmet) { armor[3] = helmet != null ? helmet.clone() : null; }
    public void setChestplate(ItemStack chestplate) { armor[2] = chestplate != null ? chestplate.clone() : null; }
    public void setLeggings(ItemStack leggings) { armor[1] = leggings != null ? leggings.clone() : null; }
    public void setBoots(ItemStack boots) { armor[0] = boots != null ? boots.clone() : null; }
    
    public ItemStack getHelmet() { return armor[3] != null ? armor[3].clone() : null; }
    public ItemStack getChestplate() { return armor[2] != null ? armor[2].clone() : null; }
    public ItemStack getLeggings() { return armor[1] != null ? armor[1].clone() : null; }
    public ItemStack getBoots() { return armor[0] != null ? armor[0].clone() : null; }
    
    // Potion effects
    public List<PotionEffect> getPotionEffects() { return new ArrayList<>(potionEffects); }
    public void setPotionEffects(List<PotionEffect> potionEffects) { 
        this.potionEffects = new ArrayList<>(potionEffects); 
    }
    
    public void addPotionEffect(PotionEffect effect) {
        potionEffects.add(effect);
    }
    
    public boolean removePotionEffect(PotionEffect effect) {
        return potionEffects.remove(effect);
    }
    
    public void clearPotionEffects() {
        potionEffects.clear();
    }
    
    // Kit settings
    public boolean isClearInventory() { return clearInventory; }
    public void setClearInventory(boolean clearInventory) { this.clearInventory = clearInventory; }
    
    public boolean isResetHealth() { return resetHealth; }
    public void setResetHealth(boolean resetHealth) { this.resetHealth = resetHealth; }
    
    public boolean isResetHunger() { return resetHunger; }
    public void setResetHunger(boolean resetHunger) { this.resetHunger = resetHunger; }
    
    public boolean isResetExperience() { return resetExperience; }
    public void setResetExperience(boolean resetExperience) { this.resetExperience = resetExperience; }
    
    public int getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(int experienceLevel) { this.experienceLevel = experienceLevel; }
    
    /**
     * Create a copy of this kit
     * @return Cloned kit
     */
    public Kit clone() {
        Kit cloned = new Kit(name + "_copy", creator);
        cloned.setDescription(description);
        cloned.setItems(items);
        cloned.setArmor(armor);
        cloned.setOffHand(offHand);
        cloned.setPotionEffects(potionEffects);
        cloned.setClearInventory(clearInventory);
        cloned.setResetHealth(resetHealth);
        cloned.setResetHunger(resetHunger);
        cloned.setResetExperience(resetExperience);
        cloned.setExperienceLevel(experienceLevel);
        return cloned;
    }
    
    /**
     * Count non-null items in the kit
     * @return Number of items
     */
    public int getItemCount() {
        int count = 0;
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                count++;
            }
        }
        for (ItemStack armor : armor) {
            if (armor != null && armor.getType() != Material.AIR) {
                count++;
            }
        }
        if (offHand != null && offHand.getType() != Material.AIR) {
            count++;
        }
        return count;
    }
    
    /**
     * Check if the kit has any items
     * @return True if kit has items
     */
    public boolean hasItems() {
        return getItemCount() > 0;
    }
    
    /**
     * Get a summary of the kit contents
     * @return Map of material to count
     */
    public Map<Material, Integer> getItemSummary() {
        Map<Material, Integer> summary = new HashMap<>();
        
        // Count main inventory items
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                summary.merge(item.getType(), item.getAmount(), Integer::sum);
            }
        }
        
        // Count armor items
        for (ItemStack armor : armor) {
            if (armor != null && armor.getType() != Material.AIR) {
                summary.merge(armor.getType(), armor.getAmount(), Integer::sum);
            }
        }
        
        // Count off-hand item
        if (offHand != null && offHand.getType() != Material.AIR) {
            summary.merge(offHand.getType(), offHand.getAmount(), Integer::sum);
        }
        
        return summary;
    }
    
    /**
     * Validate kit configuration
     * @return List of validation errors (empty if valid)
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        
        if (name == null || name.trim().isEmpty()) {
            errors.add("Kit name cannot be empty");
        }
        
        if (experienceLevel < 0) {
            errors.add("Experience level cannot be negative");
        }
        
        // Validate potion effects
        for (PotionEffect effect : potionEffects) {
            if (effect.getDuration() < 0) {
                errors.add("Potion effect duration cannot be negative");
            }
        }
        
        return errors;
    }
    
    /**
     * Check if the kit is ready for use
     * @return True if kit is properly configured
     */
    public boolean isReady() {
        return validate().isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("Kit{name='%s', items=%d, effects=%d, ready=%s}", 
            name, getItemCount(), potionEffects.size(), isReady());
    }
}