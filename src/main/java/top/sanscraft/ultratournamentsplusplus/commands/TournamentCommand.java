package top.sanscraft.ultratournamentsplusplus.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.Tournament;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main tournament command handler
 */
public class TournamentCommand implements CommandExecutor, TabCompleter {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public TournamentCommand(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "spectate":
                return handleSpectateCommand(sender, args);
            case "stopspectate":
            case "stopspec":
                return handleStopSpectateCommand(sender, args);
            case "spectatorregion":
            case "specregion":
                return handleSpectatorRegionCommand(sender, args);
            case "spectatorarea":
            case "specarea":
                return handleSpectatorAreaCommand(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /tournament help for available commands.");
                return true;
        }
    }
    
    /**
     * Handle the spectate command
     */
    private boolean handleSpectateCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check spectate permission
        if (!player.hasPermission("ultratournaments.spectate")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to spectate tournaments!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /tournament spectate <tournament>");
            return true;
        }
        
        // Check if player is in a spectator region
        if (!plugin.getSpectatorManager().isPlayerInSpectatorRegion(player)) {
            sender.sendMessage(ChatColor.RED + "You must be in a spectator region to use this command!");
            return true;
        }
        
        String tournamentId = args[1];
        
        // Check if tournament exists
        Tournament tournament = plugin.getTournamentManager().getTournament(tournamentId);
        if (tournament == null) {
            sender.sendMessage(ChatColor.RED + "Tournament '" + tournamentId + "' not found!");
            return true;
        }
        
        // Start spectating
        boolean success = plugin.getSpectatorManager().startSpectating(player, tournamentId);
        if (success) {
            player.sendMessage(ChatColor.GREEN + "You are now spectating tournament '" + ChatColor.YELLOW + tournament.getName() + ChatColor.GREEN + "'!");
            player.sendMessage(ChatColor.GRAY + "Status: " + tournament.getStatus());
            player.sendMessage(ChatColor.GRAY + "Participants: " + tournament.getParticipantCount() + "/" + tournament.getMaxParticipants());
        } else {
            player.sendMessage(ChatColor.RED + "Failed to start spectating tournament!");
        }
        
        return true;
    }
    
    /**
     * Handle the stop spectate command
     */
    private boolean handleStopSpectateCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player is currently spectating
        if (!plugin.getSpectatorManager().isSpectating(player)) {
            player.sendMessage(ChatColor.RED + "You are not currently spectating any tournament!");
            return true;
        }
        
        String tournamentId = plugin.getSpectatorManager().getSpectatingTournament(player);
        
        // Stop spectating
        boolean success = plugin.getSpectatorManager().stopSpectating(player);
        if (success) {
            player.sendMessage(ChatColor.GREEN + "You have stopped spectating tournament '" + ChatColor.YELLOW + tournamentId + ChatColor.GREEN + "'!");
            player.sendMessage(ChatColor.GRAY + "You have been returned to your original location.");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to stop spectating!");
        }
        
        return true;
    }
    
    /**
     * Handle spectator region management commands (admin only)
     */
    private boolean handleSpectatorRegionCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultratournaments.admin.spectator")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to manage spectator regions!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /tournament spectatorregion <set|remove|list> [world] [region]");
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "set":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tournament spectatorregion set <world> <region>");
                    return true;
                }
                
                String worldName = args[2];
                String regionName = args[3];
                
                boolean setSuccess = plugin.getSpectatorManager().setSpectatorRegion(worldName, regionName);
                if (setSuccess) {
                    sender.sendMessage(ChatColor.GREEN + "Set spectator region for world '" + worldName + "' to '" + regionName + "'");
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to set spectator region. Make sure the world and region exist!");
                }
                break;
                
            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tournament spectatorregion remove <world>");
                    return true;
                }
                
                String removeWorld = args[2];
                boolean removeSuccess = plugin.getSpectatorManager().removeSpectatorRegion(removeWorld);
                if (removeSuccess) {
                    sender.sendMessage(ChatColor.GREEN + "Removed spectator region for world '" + removeWorld + "'");
                } else {
                    sender.sendMessage(ChatColor.RED + "No spectator region found for world '" + removeWorld + "'");
                }
                break;
                
            case "list":
                sender.sendMessage(ChatColor.YELLOW + "Configured spectator regions:");
                for (String world : plugin.getSpectatorManager().getSpectatorRegions().keySet()) {
                    String region = plugin.getSpectatorManager().getSpectatorRegion(world);
                    Location teleportLoc = plugin.getSpectatorManager().getSpectatorTeleportLocation(world);
                    
                    String teleportInfo = teleportLoc != null ? " (teleport: " + 
                        Math.round(teleportLoc.getX()) + ", " + 
                        Math.round(teleportLoc.getY()) + ", " + 
                        Math.round(teleportLoc.getZ()) + ")" : " (no teleport location)";
                    
                    sender.sendMessage(ChatColor.GRAY + "- " + world + ": " + region + teleportInfo);
                }
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action. Use: set, remove, or list");
                break;
        }
        
        return true;
    }
    
    /**
     * Handle spectator area teleport command
     */
    private boolean handleSpectatorAreaCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            // Teleport to spectator area
            boolean success = plugin.getSpectatorManager().teleportToSpectatorArea(player);
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Teleported to spectator area!");
            } else {
                player.sendMessage(ChatColor.RED + "No spectator area configured for this world!");
            }
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        if (action.equals("set")) {
            // Admin command to set spectator teleport location
            if (!player.hasPermission("ultratournaments.admin.spectator")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to set spectator areas!");
                return true;
            }
            
            String worldName = player.getWorld().getName();
            
            // Check if world has a spectator region
            if (!plugin.getSpectatorManager().hasSpectatorRegion(worldName)) {
                player.sendMessage(ChatColor.RED + "This world doesn't have a spectator region configured!");
                player.sendMessage(ChatColor.YELLOW + "Use /tournament spectatorregion set " + worldName + " <region> first");
                return true;
            }
            
            // Set teleport location to player's current location
            Location location = player.getLocation();
            boolean success = plugin.getSpectatorManager().setSpectatorTeleportLocation(worldName, location);
            
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Set spectator teleport location for world '" + worldName + "' to your current location!");
                player.sendMessage(ChatColor.GRAY + "Location: " + 
                    Math.round(location.getX()) + ", " + 
                    Math.round(location.getY()) + ", " + 
                    Math.round(location.getZ()));
            } else {
                player.sendMessage(ChatColor.RED + "Failed to set spectator teleport location!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /tournament spectatorarea [set]");
        }
        
        return true;
    }
    
    /**
     * Send help message
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "=== UltraTournaments++ Commands ===");
        sender.sendMessage(ChatColor.GRAY + "/tournament spectate <tournament> - Spectate a tournament (requires spectator region)");
        sender.sendMessage(ChatColor.GRAY + "/tournament spectatorarea - Teleport to spectator area");
        
        if (sender.hasPermission("ultratournaments.admin.spectator")) {
            sender.sendMessage(ChatColor.YELLOW + "=== Admin Commands ===");
            sender.sendMessage(ChatColor.GRAY + "/tournament spectatorregion set <world> <region> - Set spectator region");
            sender.sendMessage(ChatColor.GRAY + "/tournament spectatorregion remove <world> - Remove spectator region");
            sender.sendMessage(ChatColor.GRAY + "/tournament spectatorregion list - List spectator regions");
            sender.sendMessage(ChatColor.GRAY + "/tournament spectatorarea set - Set spectator teleport location");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("spectate", "stopspectate", "spectatorregion", "spectatorarea");
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("spectate")) {
                // Tab complete tournament names
                for (Tournament tournament : plugin.getTournamentManager().getTournaments()) {
                    if (tournament.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(tournament.getName());
                    }
                }
            } else if (subCommand.equals("spectatorregion")) {
                List<String> actions = Arrays.asList("set", "remove", "list");
                for (String action : actions) {
                    if (action.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(action);
                    }
                }
            } else if (subCommand.equals("spectatorarea")) {
                if ("set".startsWith(args[1].toLowerCase())) {
                    completions.add("set");
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("spectatorregion")) {
            String action = args[1].toLowerCase();
            if (action.equals("set") || action.equals("remove")) {
                // Tab complete world names
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    if (world.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(world.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}