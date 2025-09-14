package top.sanscraft.ultratournamentsplusplus.events;

import top.sanscraft.ultratournamentsplusplus.models.Round;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event fired when a round is completed
 */
public class RoundCompleteEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final Round round;
    private final UUID winner;
    private final String winnerName;
    private final UUID loser;
    private final String loserName;
    
    public RoundCompleteEvent(Round round, UUID winner, String winnerName, UUID loser, String loserName) {
        this.round = round;
        this.winner = winner;
        this.winnerName = winnerName;
        this.loser = loser;
        this.loserName = loserName;
    }
    
    public Round getRound() {
        return round;
    }
    
    public UUID getWinner() {
        return winner;
    }
    
    public String getWinnerName() {
        return winnerName;
    }
    
    public UUID getLoser() {
        return loser;
    }
    
    public String getLoserName() {
        return loserName;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}