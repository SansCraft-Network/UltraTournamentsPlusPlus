package top.sanscraft.ultratournamentsplusplus.listeners;

import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.events.TournamentCompleteEvent;
import top.sanscraft.ultratournamentsplusplus.events.RoundCompleteEvent;
import top.sanscraft.ultratournamentsplusplus.models.Tournament;
import top.sanscraft.ultratournamentsplusplus.models.Round;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;

/**
 * Handles tournament events for Discord integration and stats tracking
 */
public class TournamentEventListener implements Listener {
    
    private final UltraTournamentsPlusPlus plugin;
    
    public TournamentEventListener(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTournamentComplete(TournamentCompleteEvent event) {
        Tournament tournament = event.getTournament();
        String winnerName = event.getWinnerName();
        
        plugin.getLogger().info("Tournament '" + tournament.getName() + "' completed! Winner: " + winnerName);
        
        // Send Discord announcement
        if (plugin.getDiscordBot() != null && plugin.getDiscordBot().isReady()) {
            plugin.getDiscordBot().sendTournamentResult(tournament, winnerName);
        }
        
        // Update player stats
        if (plugin.getPlayerStatsManager() != null) {
            // Record tournament win for winner
            plugin.getPlayerStatsManager().recordTournamentParticipation(event.getWinner(), true);
            
            // Record tournament participation for all other participants
            for (java.util.UUID participantId : tournament.getParticipants()) {
                if (!participantId.equals(event.getWinner())) {
                    plugin.getPlayerStatsManager().recordTournamentParticipation(participantId, false);
                }
            }
        }
        
        // Broadcast in-game announcement
        String announcement = "§6§l[TOURNAMENT] §e" + tournament.getName() + " §ahas been won by §e" + winnerName + "§a!";
        Bukkit.broadcastMessage(announcement);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRoundComplete(RoundCompleteEvent event) {
        Round round = event.getRound();
        String winnerName = event.getWinnerName();
        String loserName = event.getLoserName();
        
        plugin.getLogger().info("Round " + round.getId() + " completed! Winner: " + winnerName + ", Loser: " + loserName);
        
        // Send Discord announcement for important rounds (finals, semi-finals, etc.)
        if (plugin.getDiscordBot() != null && plugin.getDiscordBot().isReady()) {
            // Only announce certain rounds to avoid spam
            boolean shouldAnnounce = isImportantRound(round);
            if (shouldAnnounce) {
                plugin.getDiscordBot().sendRoundResult(round, winnerName, loserName);
            }
        }
        
        // Update player stats
        if (plugin.getPlayerStatsManager() != null) {
            // Record round win for winner
            plugin.getPlayerStatsManager().recordRoundResult(event.getWinner(), true);
            
            // Record round loss for loser
            plugin.getPlayerStatsManager().recordRoundResult(event.getLoser(), false);
            
            // Record kills/deaths if available (this would need to be tracked during the round)
            // For now, we'll assume 1 kill for winner, 1 death for loser in PvP rounds
            if (round.getAssignedArena() != null && isPvPArena(round.getAssignedArena())) {
                plugin.getPlayerStatsManager().recordKill(event.getWinner());
                plugin.getPlayerStatsManager().recordDeath(event.getLoser());
            }
        }
    }
    
    /**
     * Determine if a round is important enough to announce in Discord
     */
    private boolean isImportantRound(Round round) {
        // This is a simplified check - in practice you'd want to check the tournament bracket
        // to determine if it's a final, semi-final, etc.
        
        String bracketPosition = round.getBracketPosition();
        if (bracketPosition != null) {
            String lower = bracketPosition.toLowerCase();
            return lower.contains("final") || lower.contains("championship") || 
                   lower.contains("semi") || lower.contains("quarter");
        }
        
        // Default to announcing if we're not sure
        return round.getRoundNumber() >= 3; // Announce rounds 3 and higher
    }
    
    /**
     * Check if an arena is a PvP arena (simplified check)
     */
    private boolean isPvPArena(String arenaName) {
        // This is a simplified check - in practice you'd check the arena type
        return arenaName != null && !arenaName.toLowerCase().contains("parkour");
    }
}