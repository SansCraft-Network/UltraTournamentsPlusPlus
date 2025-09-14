package top.sanscraft.ultratournamentsplusplus.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.Kit;
import top.sanscraft.ultratournamentsplusplus.utils.MessageUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Command handler for kit management
 */
public class KitCommand implements CommandExecutor, TabCompleter {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public KitCommand(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
            case "delete":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "apply":
                return handleApply(sender, args);
            case "save":
                return handleSave(sender, args);
            case "clone":
                return handleClone(sender, args);
            case "edit":
                return handleEdit(sender, args);
            default:
                MessageUtils.sendError(sender, "Unknown subcommand: " + subCommand);
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendError(sender, "Usage: /kit create <name>");
            return true;
        }
        
        String name = args[1];
        
        Kit kit = plugin.getKitManager().createKit(name, 
            sender instanceof Player ? ((Player) sender).getUniqueId() : null);
        
        if (kit == null) {
            MessageUtils.sendError(sender, "Kit '" + name + "' already exists!");
            return true;
        }
        
        MessageUtils.sendSuccess(sender, "Created empty kit '" + name + "'");
        MessageUtils.sendInfo(sender, "Use /kit save " + name + " while holding items to populate it");
        
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendError(sender, "Usage: /kit delete <name>");
            return true;
        }
        
        String name = args[1];
        
        if (!plugin.getKitManager().deleteKit(name)) {
            MessageUtils.sendError(sender, "Kit '" + name + "' not found!");
            return true;
        }
        
        MessageUtils.sendSuccess(sender, "Deleted kit '" + name + "'");
        return true;
    }
    
    private boolean handleList(CommandSender sender, String[] args) {
        var kitNames = plugin.getKitManager().getKitNames();
        
        if (kitNames.isEmpty()) {
            MessageUtils.sendInfo(sender, "No kits found");
            return true;
        }
        
        MessageUtils.sendInfo(sender, "§6§lKits (" + kitNames.size() + "):");
        for (String kitName : kitNames) {
            Kit kit = plugin.getKitManager().getKit(kitName);
            String status = kit.isReady() ? "§aReady" : "§cIncomplete";
            int itemCount = kit.getItemCount();
            MessageUtils.sendMessage(sender, "§7- §e" + kit.getName() + " §7(" + itemCount + " items) " + status);
        }
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendError(sender, "Usage: /kit info <name>");
            return true;
        }
        
        String name = args[1];
        Kit kit = plugin.getKitManager().getKit(name);
        
        if (kit == null) {
            MessageUtils.sendError(sender, "Kit '" + name + "' not found!");
            return true;
        }
        
        MessageUtils.sendMessage(sender, "§6§m----§r §e" + kit.getName() + " §6§m----§r");
        
        if (kit.getDescription() != null) {
            MessageUtils.sendMessage(sender, "§6Description: §f" + kit.getDescription());
        }
        
        MessageUtils.sendMessage(sender, "§6Total Items: §f" + kit.getItemCount());
        MessageUtils.sendMessage(sender, "§6Potion Effects: §f" + kit.getPotionEffects().size());
        MessageUtils.sendMessage(sender, "§6Experience Level: §f" + kit.getExperienceLevel());
        
        MessageUtils.sendMessage(sender, "§6Settings:");
        MessageUtils.sendMessage(sender, "§7- Clear Inventory: §f" + (kit.isClearInventory() ? "Yes" : "No"));
        MessageUtils.sendMessage(sender, "§7- Reset Health: §f" + (kit.isResetHealth() ? "Yes" : "No"));
        MessageUtils.sendMessage(sender, "§7- Reset Hunger: §f" + (kit.isResetHunger() ? "Yes" : "No"));
        MessageUtils.sendMessage(sender, "§7- Reset Experience: §f" + (kit.isResetExperience() ? "Yes" : "No"));
        
        // Show item summary
        Map<Material, Integer> itemSummary = kit.getItemSummary();
        if (!itemSummary.isEmpty()) {
            MessageUtils.sendMessage(sender, "§6Items:");
            for (Map.Entry<Material, Integer> entry : itemSummary.entrySet()) {
                String materialName = entry.getKey().name().toLowerCase().replace("_", " ");
                MessageUtils.sendMessage(sender, "§7- §f" + entry.getValue() + "x " + materialName);
            }
        }
        
        var errors = kit.validate();
        if (!errors.isEmpty()) {
            MessageUtils.sendMessage(sender, "§cConfiguration Issues:");
            for (String error : errors) {
                MessageUtils.sendMessage(sender, "§7- §c" + error);
            }
        } else {
            MessageUtils.sendMessage(sender, "§aKit is ready for use!");
        }
        
        return true;
    }
    
    private boolean handleApply(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendError(sender, "Usage: /kit apply <name> [player]");
            return true;
        }
        
        String kitName = args[1];
        Kit kit = plugin.getKitManager().getKit(kitName);
        
        if (kit == null) {
            MessageUtils.sendError(sender, "Kit '" + kitName + "' not found!");
            return true;
        }
        
        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                MessageUtils.sendError(sender, "Player '" + args[2] + "' not found!");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                MessageUtils.sendError(sender, "Console must specify a player");
                return true;
            }
            target = (Player) sender;
        }
        
        if (!plugin.getKitManager().applyKit(kit, target)) {
            MessageUtils.sendError(sender, "Failed to apply kit '" + kitName + "'");
            return true;
        }
        
        MessageUtils.sendSuccess(sender, "Applied kit '" + kitName + "' to " + target.getName());
        if (!sender.equals(target)) {
            MessageUtils.sendInfo(target, "Kit '" + kitName + "' has been applied to you");
        }
        
        return true;
    }
    
    private boolean handleSave(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendError(sender, "Only players can save kits");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtils.sendError(sender, "Usage: /kit save <name>");
            return true;
        }
        
        Player player = (Player) sender;
        String kitName = args[1];
        
        if (!plugin.getKitManager().savePlayerAsKit(kitName, player)) {
            MessageUtils.sendError(sender, "Failed to save kit '" + kitName + "'");
            return true;
        }
        
        Kit kit = plugin.getKitManager().getKit(kitName);
        MessageUtils.sendSuccess(sender, "Saved your current state as kit '" + kitName + "' (" + kit.getItemCount() + " items)");
        
        return true;
    }
    
    private boolean handleClone(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtils.sendError(sender, "Usage: /kit clone <original> <new_name>");
            return true;
        }
        
        String originalName = args[1];
        String newName = args[2];
        
        Kit cloned = plugin.getKitManager().cloneKit(originalName, newName, 
            sender instanceof Player ? ((Player) sender).getUniqueId() : null);
        
        if (cloned == null) {
            MessageUtils.sendError(sender, "Failed to clone kit (original not found or new name already exists)");
            return true;
        }
        
        MessageUtils.sendSuccess(sender, "Cloned kit '" + originalName + "' as '" + newName + "'");
        return true;
    }
    
    private boolean handleEdit(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtils.sendError(sender, "Usage: /kit edit <name> <setting> <value>");
            MessageUtils.sendInfo(sender, "Settings: description, clearinv, resethealth, resethunger, resetxp, xplevel");
            return true;
        }
        
        String kitName = args[1];
        String setting = args[2].toLowerCase();
        
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            MessageUtils.sendError(sender, "Kit '" + kitName + "' not found!");
            return true;
        }
        
        switch (setting) {
            case "description":
                if (args.length < 4) {
                    MessageUtils.sendError(sender, "Usage: /kit edit " + kitName + " description <text>");
                    return true;
                }
                String description = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                kit.setDescription(description);
                MessageUtils.sendSuccess(sender, "Updated kit description");
                break;
                
            case "clearinv":
                if (args.length < 4) {
                    MessageUtils.sendError(sender, "Usage: /kit edit " + kitName + " clearinv <true/false>");
                    return true;
                }
                boolean clearInv = Boolean.parseBoolean(args[3]);
                kit.setClearInventory(clearInv);
                MessageUtils.sendSuccess(sender, "Set clear inventory to " + clearInv);
                break;
                
            case "resethealth":
                if (args.length < 4) {
                    MessageUtils.sendError(sender, "Usage: /kit edit " + kitName + " resethealth <true/false>");
                    return true;
                }
                boolean resetHealth = Boolean.parseBoolean(args[3]);
                kit.setResetHealth(resetHealth);
                MessageUtils.sendSuccess(sender, "Set reset health to " + resetHealth);
                break;
                
            case "resethunger":
                if (args.length < 4) {
                    MessageUtils.sendError(sender, "Usage: /kit edit " + kitName + " resethunger <true/false>");
                    return true;
                }
                boolean resetHunger = Boolean.parseBoolean(args[3]);
                kit.setResetHunger(resetHunger);
                MessageUtils.sendSuccess(sender, "Set reset hunger to " + resetHunger);
                break;
                
            case "resetxp":
                if (args.length < 4) {
                    MessageUtils.sendError(sender, "Usage: /kit edit " + kitName + " resetxp <true/false>");
                    return true;
                }
                boolean resetXp = Boolean.parseBoolean(args[3]);
                kit.setResetExperience(resetXp);
                MessageUtils.sendSuccess(sender, "Set reset experience to " + resetXp);
                break;
                
            case "xplevel":
                if (args.length < 4) {
                    MessageUtils.sendError(sender, "Usage: /kit edit " + kitName + " xplevel <number>");
                    return true;
                }
                try {
                    int xpLevel = Integer.parseInt(args[3]);
                    if (xpLevel < 0) {
                        MessageUtils.sendError(sender, "Experience level cannot be negative");
                        return true;
                    }
                    kit.setExperienceLevel(xpLevel);
                    MessageUtils.sendSuccess(sender, "Set experience level to " + xpLevel);
                } catch (NumberFormatException e) {
                    MessageUtils.sendError(sender, "Invalid number: " + args[3]);
                }
                break;
                
            default:
                MessageUtils.sendError(sender, "Unknown setting: " + setting);
                MessageUtils.sendInfo(sender, "Settings: description, clearinv, resethealth, resethunger, resetxp, xplevel");
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "§6§lKit Commands:");
        MessageUtils.sendMessage(sender, "§e/kit create <name> §7- Create new empty kit");
        MessageUtils.sendMessage(sender, "§e/kit delete <name> §7- Delete kit");
        MessageUtils.sendMessage(sender, "§e/kit list §7- List all kits");
        MessageUtils.sendMessage(sender, "§e/kit info <name> §7- Show kit information");
        MessageUtils.sendMessage(sender, "§e/kit apply <name> [player] §7- Apply kit to player");
        MessageUtils.sendMessage(sender, "§e/kit save <name> §7- Save current inventory as kit");
        MessageUtils.sendMessage(sender, "§e/kit clone <original> <new> §7- Clone existing kit");
        MessageUtils.sendMessage(sender, "§e/kit edit <name> <setting> <value> §7- Edit kit settings");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Subcommands
            List<String> subCommands = Arrays.asList("create", "delete", "list", "info", "apply", "save", "clone", "edit");
            return subCommands.stream()
                .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "delete":
                case "info":
                case "apply":
                case "save":
                case "clone":
                case "edit":
                    // Kit names
                    return plugin.getKitManager().getKitNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            
            if ("apply".equals(subCommand)) {
                // Player names
                return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            } else if ("clone".equals(subCommand)) {
                // For cloning, don't suggest existing kit names for the new name
                return new ArrayList<>();
            } else if ("edit".equals(subCommand)) {
                // Kit settings
                List<String> settings = Arrays.asList("description", "clearinv", "resethealth", "resethunger", "resetxp", "xplevel");
                return settings.stream()
                    .filter(setting -> setting.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        if (args.length == 4 && "edit".equals(args[0].toLowerCase())) {
            String setting = args[2].toLowerCase();
            
            switch (setting) {
                case "clearinv":
                case "resethealth":
                case "resethunger":
                case "resetxp":
                    return Arrays.asList("true", "false").stream()
                        .filter(bool -> bool.startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}