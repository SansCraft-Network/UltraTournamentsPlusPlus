package top.sanscraft.ultratournamentsplusplus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;

/**
 * Command to list all tournaments
 */
public class ListTournamentsCommand implements CommandExecutor {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public ListTournamentsCommand(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: Implement list tournaments logic
        sender.sendMessage("§6[UltraTournaments++] §cCommand not yet implemented!");
        return true;
    }
}