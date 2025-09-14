package top.sanscraft.ultratournamentsplusplus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;

/**
 * Command to get tournament information
 */
public class TournamentInfoCommand implements CommandExecutor {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public TournamentInfoCommand(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: Implement tournament info logic
        sender.sendMessage("§6[UltraTournaments++] §cCommand not yet implemented!");
        return true;
    }
}