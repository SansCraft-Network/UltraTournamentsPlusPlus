package top.sanscraft.ultratournamentsplusplus.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.Arena;
import top.sanscraft.ultratournamentsplusplus.models.ArenaType;
import top.sanscraft.ultratournamentsplusplus.utils.MessageUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for UTP arena management (/utparena)
 */
public class UtpArenaCommand implements CommandExecutor, TabCompleter {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public UtpArenaCommand(UltraTournamentsPlusPlus plugin) {
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
            case "setspawn":
                return handleSetSpawn(sender, args);
            case "removespawn":
                return handleRemoveSpawn(sender, args);
            case "setgoal":
                return handleSetGoal(sender, args);
            case "teleport":
            case "tp":
                return handleTeleport(sender, args);
            case "region":
                return handleRegion(sender, args);
            default:
                MessageUtils.sendError(sender, "Unknown subcommand: " + subCommand);
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtils.sendError(sender, "Usage: /arena create <name> <type> [world]");
            MessageUtils.sendInfo(sender, "Types: PVP, PARKOUR");
            return true;
        }
        
        String name = args[1];
        String typeStr = args[2].toUpperCase();
        
        // Parse arena type
        ArenaType type;
        try {
            type = ArenaType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            MessageUtils.sendError(sender, "Invalid arena type: " + typeStr);
            MessageUtils.sendInfo(sender, "Valid types: " + Arrays.toString(ArenaType.values()));
            return true;
        }
        
        // Determine world
        World world;
        if (args.length >= 4) {
            world = Bukkit.getWorld(args[3]);
            if (world == null) {
                MessageUtils.sendError(sender, "World not found: " + args[3]);
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                MessageUtils.sendError(sender, "Console must specify a world");
                return true;
            }
            world = ((Player) sender).getWorld();
        }
        
        // Create arena
        Arena arena = plugin.getArenaManager().createArena(name, type, world, 
            sender instanceof Player ? ((Player) sender).getUniqueId() : null);
        
        if (arena == null) {
            MessageUtils.sendError(sender, "Arena '" + name + "' already exists!");
            return true;
        }
        
        MessageUtils.sendSuccess(sender, "Created " + type.getDisplayName() + " arena '" + name + "' in world '" + world.getName() + "'");
        
        if (type == ArenaType.PVP) {
            MessageUtils.sendInfo(sender, "Use /arena setspawn to add spawn points");
        } else if (type == ArenaType.PARKOUR) {
            MessageUtils.sendInfo(sender, "Use /arena setspawn to set the start point and /arena setgoal to set the finish");
        }
        
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendError(sender, "Usage: /arena delete <name>");
            return true;
        }
        
        String name = args[1];
        
        if (!plugin.getArenaManager().deleteArena(name)) {
            MessageUtils.sendError(sender, "Arena '" + name + "' not found!");
            return true;
        }
        
        MessageUtils.sendSuccess(sender, "Deleted arena '" + name + "'");
        return true;
    }
    
    private boolean handleList(CommandSender sender, String[] args) {
        var arenaNames = plugin.getArenaManager().getArenaNames();
        
        if (arenaNames.isEmpty()) {
            MessageUtils.sendInfo(sender, "No arenas found");
            return true;
        }
        
        MessageUtils.sendInfo(sender, "§6§lArenas (" + arenaNames.size() + "):");
        for (String arenaName : arenaNames) {
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            String status = arena.isReady() ? "§aReady" : "§cIncomplete";
            MessageUtils.sendMessage(sender, "§7- §e" + arena.getName() + " §7(" + arena.getType().getDisplayName() + ") " + status);
        }
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendError(sender, "Usage: /arena info <name>");
            return true;
        }
        
        String name = args[1];
        Arena arena = plugin.getArenaManager().getArena(name);
        
        if (arena == null) {
            MessageUtils.sendError(sender, "Arena '" + name + "' not found!");
            return true;
        }
        
        MessageUtils.sendMessage(sender, "§6§m----§r §e" + arena.getName() + " §6§m----§r");
        MessageUtils.sendMessage(sender, "§6Type: §f" + arena.getType().getDisplayName());
        MessageUtils.sendMessage(sender, "§6World: §f" + arena.getWorld().getName());
        MessageUtils.sendMessage(sender, "§6Max Players: §f" + arena.getMaxPlayers());
        MessageUtils.sendMessage(sender, "§6Building: §f" + (arena.isBuildingEnabled() ? "Enabled" : "Disabled"));
        MessageUtils.sendMessage(sender, "§6Spawn Points: §f" + arena.getSpawnPointCount());
        
        if (arena.getType() == ArenaType.PARKOUR) {
            MessageUtils.sendMessage(sender, "§6Goal Set: §f" + (arena.getGoalLocation() != null ? "Yes" : "No"));
            if (arena.hasTimeLimit()) {
                MessageUtils.sendMessage(sender, "§6Time Limit: §f" + arena.getTimeLimit() + "s");
            }
        }
        
        if (arena.getType() == ArenaType.PVP && arena.isTeamBased()) {
            MessageUtils.sendMessage(sender, "§6Teams: §f" + String.join(", ", arena.getTeams()));
        }
        
        if (arena.getWorldGuardRegion() != null) {
            MessageUtils.sendMessage(sender, "§6WorldGuard Region: §f" + arena.getWorldGuardRegion());
        }
        
        var errors = arena.validate();
        if (!errors.isEmpty()) {
            MessageUtils.sendMessage(sender, "§cConfiguration Issues:");
            for (String error : errors) {
                MessageUtils.sendMessage(sender, "§7- §c" + error);
            }
        } else {
            MessageUtils.sendMessage(sender, "§aArena is ready for use!");
        }
        
        return true;
    }
    
    private boolean handleSetSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendError(sender, "Only players can set spawn points");
            return true;
        }
        
        if (args.length < 3) {
            MessageUtils.sendError(sender, "Usage: /arena setspawn <arena> <spawn_name> [team]");
            return true;
        }
        
        Player player = (Player) sender;
        String arenaName = args[1];
        String spawnName = args[2];
        String team = args.length >= 4 ? args[3] : null;
        
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            MessageUtils.sendError(sender, "Arena '" + arenaName + "' not found!");
            return true;
        }
        
        Location location = player.getLocation();
        
        if (!plugin.getArenaManager().addSpawnPoint(arenaName, spawnName, location, team)) {
            MessageUtils.sendError(sender, "Failed to add spawn point (may already exist or wrong world)");
            return true;
        }
        
        String teamInfo = team != null ? " for team '" + team + "'" : "";
        MessageUtils.sendSuccess(sender, "Added spawn point '" + spawnName + "'" + teamInfo + " to arena '" + arenaName + "'");
        
        return true;
    }
    
    private boolean handleRemoveSpawn(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtils.sendError(sender, "Usage: /arena removespawn <arena> <spawn_name>");
            return true;
        }
        
        String arenaName = args[1];
        String spawnName = args[2];
        
        if (!plugin.getArenaManager().removeSpawnPoint(arenaName, spawnName)) {
            MessageUtils.sendError(sender, "Failed to remove spawn point (arena or spawn not found)");
            return true;
        }
        
        MessageUtils.sendSuccess(sender, "Removed spawn point '" + spawnName + "' from arena '" + arenaName + "'");
        return true;
    }
    
    private boolean handleSetGoal(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendError(sender, "Only players can set goal locations");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtils.sendError(sender, "Usage: /arena setgoal <arena>");
            return true;
        }
        
        Player player = (Player) sender;
        String arenaName = args[1];
        
        Location location = player.getLocation();
        
        if (!plugin.getArenaManager().setGoalLocation(arenaName, location)) {
            MessageUtils.sendError(sender, "Failed to set goal location (arena not found or not parkour type)");
            return true;
        }
        
        MessageUtils.sendSuccess(sender, "Set goal location for parkour arena '" + arenaName + "'");
        return true;
    }
    
    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendError(sender, "Only players can teleport to arenas");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtils.sendError(sender, "Usage: /arena tp <arena> [team]");
            return true;
        }
        
        Player player = (Player) sender;
        String arenaName = args[1];
        String team = args.length >= 3 ? args[2] : null;
        
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            MessageUtils.sendError(sender, "Arena '" + arenaName + "' not found!");
            return true;
        }
        
        if (!plugin.getArenaManager().teleportToArena(player, arena, team)) {
            MessageUtils.sendError(sender, "Failed to teleport to arena (no suitable spawn points)");
            return true;
        }
        
        MessageUtils.sendSuccess(sender, "Teleported to arena '" + arenaName + "'");
        return true;
    }
    
    private boolean handleRegion(CommandSender sender, String[] args) {
        if (!plugin.getArenaManager().isWorldGuardEnabled()) {
            MessageUtils.sendError(sender, "WorldGuard is not available");
            return true;
        }
        
        if (args.length < 3) {
            MessageUtils.sendError(sender, "Usage: /arena region <arena> <region_name>");
            return true;
        }
        
        String arenaName = args[1];
        String regionName = args[2];
        
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            MessageUtils.sendError(sender, "Arena '" + arenaName + "' not found!");
            return true;
        }
        
        if (!plugin.getArenaManager().createWorldGuardRegion(arena, regionName)) {
            MessageUtils.sendError(sender, "Failed to associate arena with WorldGuard region '" + regionName + "'");
            MessageUtils.sendInfo(sender, "Make sure the region exists first");
            return true;
        }
        
        MessageUtils.sendSuccess(sender, "Associated arena '" + arenaName + "' with WorldGuard region '" + regionName + "'");
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "§6§lUTP Arena Commands:");
        MessageUtils.sendMessage(sender, "§e/utparena create <name> <type> [world] §7- Create new arena");
        MessageUtils.sendMessage(sender, "§e/utparena delete <name> §7- Delete arena");
        MessageUtils.sendMessage(sender, "§e/utparena list §7- List all arenas");
        MessageUtils.sendMessage(sender, "§e/utparena info <name> §7- Show arena information");
        MessageUtils.sendMessage(sender, "§e/utparena setspawn <arena> <name> [team] §7- Add spawn point");
        MessageUtils.sendMessage(sender, "§e/utparena removespawn <arena> <name> §7- Remove spawn point");
        MessageUtils.sendMessage(sender, "§e/utparena setgoal <arena> §7- Set parkour goal (parkour only)");
        MessageUtils.sendMessage(sender, "§e/utparena tp <arena> [team] §7- Teleport to arena");
        MessageUtils.sendMessage(sender, "§e/utparena region <arena> <region> §7- Link WorldGuard region");
        MessageUtils.sendMessage(sender, "§7Types: §fPVP, PARKOUR");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Subcommands
            List<String> subCommands = Arrays.asList("create", "delete", "list", "info", "setspawn", "removespawn", "setgoal", "tp", "region");
            return subCommands.stream()
                .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "delete":
                case "info":
                case "setspawn":
                case "removespawn":
                case "setgoal":
                case "tp":
                case "region":
                    // Arena names
                    return plugin.getArenaManager().getArenaNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            
            if ("create".equals(subCommand)) {
                // Arena types
                return Arrays.stream(ArenaType.values())
                    .map(Enum::name)
                    .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        if (args.length == 4 && "create".equals(args[0].toLowerCase())) {
            // World names
            return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(world -> world.toLowerCase().startsWith(args[3].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}