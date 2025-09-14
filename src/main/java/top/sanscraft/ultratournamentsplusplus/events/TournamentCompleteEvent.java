package top.sanscraft.ultratournamentsplusplus.events;

import top.sanscraft.ultratournamentsplusplus.models.Tournament;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event fired when a tournament is completed
 */
public class TournamentCompleteEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final Tournament tournament;
    private final UUID winner;
    private final String winnerName;
    
    public TournamentCompleteEvent(Tournament tournament, UUID winner, String winnerName) {
        this.tournament = tournament;
        this.winner = winner;
        this.winnerName = winnerName;
    }
    
    public Tournament getTournament() {
        return tournament;
    }
    
    public UUID getWinner() {
        return winner;
    }
    
    public String getWinnerName() {
        return winnerName;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}