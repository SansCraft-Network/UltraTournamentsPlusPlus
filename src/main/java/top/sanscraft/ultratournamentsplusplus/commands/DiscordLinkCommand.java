package top.sanscraft.ultratournamentsplusplus.commands;

import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.DiscordLink;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command for Discord account linking operations
 */
public class DiscordLinkCommand implements CommandExecutor, TabCompleter {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public DiscordLinkCommand(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "verify" -> handleVerify(player, args);
            case "unlink" -> handleUnlink(player);
            case "status" -> handleStatus(player);
            case "help" -> sendHelp(player);
            default -> {
                player.sendMessage("§cUnknown subcommand: " + subCommand);
                sendHelp(player);
            }
        }
        
        return true;
    }
    
    /**
     * Handle /discord verify <code> command
     */
    private void handleVerify(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage("§cUsage: /discord verify <verification-code>");
            return;
        }
        
        String verificationCode = args[1];
        
        // Check if already linked
        if (plugin.getDiscordLinkManager().isMinecraftLinked(player.getUniqueId())) {
            player.sendMessage("§cYour Minecraft account is already linked to Discord!");
            player.sendMessage("§7Use §e/discord unlink §7to unlink first.");
            return;
        }
        
        // Verify the code
        boolean success = plugin.getDiscordLinkManager().verifyLink(verificationCode, player.getUniqueId());
        
        if (success) {
            DiscordLink link = plugin.getDiscordLinkManager().getLinkByMinecraftUuid(player.getUniqueId());
            player.sendMessage("§a§l✓ Successfully linked your accounts!");
            player.sendMessage("§7Your Minecraft account is now linked to Discord.");
            player.sendMessage("§7You can now use Discord commands and receive tournament notifications!");
            
            plugin.getLogger().info("Player " + player.getName() + " successfully linked their Discord account");
        } else {
            player.sendMessage("§c§l✗ Verification failed!");
            player.sendMessage("§7The verification code is invalid, expired, or doesn't match your account.");
            player.sendMessage("§7Start the linking process again in Discord with §e/link §7command.");
        }
    }
    
    /**
     * Handle /discord unlink command
     */
    private void handleUnlink(Player player) {
        if (!plugin.getDiscordLinkManager().isMinecraftLinked(player.getUniqueId())) {
            player.sendMessage("§cYour Minecraft account is not linked to Discord!");
            return;
        }
        
        DiscordLink link = plugin.getDiscordLinkManager().getLinkByMinecraftUuid(player.getUniqueId());
        boolean success = plugin.getDiscordLinkManager().unlinkMinecraft(player.getUniqueId());
        
        if (success) {
            player.sendMessage("§e§l⚠ Successfully unlinked your accounts!");
            player.sendMessage("§7Your Minecraft account is no longer linked to Discord.");
            player.sendMessage("§7You will no longer receive Discord notifications.");
            
            plugin.getLogger().info("Player " + player.getName() + " unlinked their Discord account");
        } else {
            player.sendMessage("§cFailed to unlink your account. Please try again later.");
        }
    }
    
    /**
     * Handle /discord status command
     */
    private void handleStatus(Player player) {
        boolean isLinked = plugin.getDiscordLinkManager().isMinecraftLinked(player.getUniqueId());
        
        player.sendMessage("§6§l--- Discord Link Status ---");
        
        if (isLinked) {
            DiscordLink link = plugin.getDiscordLinkManager().getLinkByMinecraftUuid(player.getUniqueId());
            long linkedDays = (System.currentTimeMillis() - link.getLinkedTimestamp()) / (1000 * 60 * 60 * 24);
            
            player.sendMessage("§a§l✓ Status: §aLinked");
            player.sendMessage("§7Discord ID: §f" + link.getDiscordId());
            player.sendMessage("§7Linked: §f" + linkedDays + " days ago");
            player.sendMessage("§7Notifications: " + (link.isNotificationsEnabled() ? "§aEnabled" : "§cDisabled"));
            player.sendMessage("");
            player.sendMessage("§7You can use Discord commands like §e/stats§7, §e/tournaments§7, and §e/join§7!");
            player.sendMessage("§7Use §e/discord unlink §7to unlink your account.");
        } else {
            player.sendMessage("§c§l✗ Status: §cNot Linked");
            player.sendMessage("§7Your Minecraft account is not linked to Discord.");
            player.sendMessage("");
            player.sendMessage("§7To link your account:");
            player.sendMessage("§71. Use §e/link <your-minecraft-username> §7in Discord");
            player.sendMessage("§72. Use §e/discord verify <code> §7in Minecraft");
        }
        
        player.sendMessage("§6§l------------------------");
    }
    
    /**
     * Send help message
     */
    private void sendHelp(Player player) {
        player.sendMessage("§6§l--- Discord Link Commands ---");
        player.sendMessage("§e/discord verify <code> §7- Verify your Discord link");
        player.sendMessage("§e/discord unlink §7- Unlink your Discord account");
        player.sendMessage("§e/discord status §7- Check your link status");
        player.sendMessage("§e/discord help §7- Show this help message");
        player.sendMessage("");
        player.sendMessage("§7To start linking: Use §e/link <username> §7in Discord first!");
        player.sendMessage("§6§l-----------------------------");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("verify", "unlink", "status", "help");
            return subCommands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2 && "verify".equals(args[0].toLowerCase())) {
            // For verify command, we could show a placeholder but verification codes are private
            return List.of("<verification-code>");
        }
        
        return new ArrayList<>();
    }
}