package top.sanscraft.ultratournamentsplusplus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;

/**
 * Command to leave a tournament
 */
public class LeaveTournamentCommand implements CommandExecutor {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public LeaveTournamentCommand(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: Implement leave tournament logic
        sender.sendMessage("§6[UltraTournaments++] §cCommand not yet implemented!");
        return true;
    }
}