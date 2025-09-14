package top.sanscraft.ultratournamentsplusplus.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.Arena;
import top.sanscraft.ultratournamentsplusplus.models.Kit;
import top.sanscraft.ultratournamentsplusplus.models.Round;
import top.sanscraft.ultratournamentsplusplus.models.SpawnPoint;
import top.sanscraft.ultratournamentsplusplus.utils.ArenaUtils;
import top.sanscraft.ultratournamentsplusplus.utils.KitUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages tournament rounds including creation, execution, and winner detection
 */
public class RoundManager {
    
    private final UltraTournamentsPlusPlus plugin;
    private final Map<String, Round> activeRounds;
    private final Map<String, BukkitTask> countdownTasks;
    private final Map<UUID, String> playerRounds; // Player UUID -> Round ID
    private final Map<UUID, Kit> savedPlayerStates; // Player UUID -> Saved state before round
    
    public RoundManager(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
        this.activeRounds = new HashMap<>();
        this.countdownTasks = new HashMap<>();
        this.playerRounds = new HashMap<>();
        this.savedPlayerStates = new HashMap<>();
    }
    
    /**
     * Create a new round for a tournament
     * @param tournamentId Tournament ID
     * @param participants List of participant UUIDs
     * @param arena Arena to use (optional)
     * @param kit Kit to use (optional)
     * @return Created round, or null if creation failed
     */
    public Round createRound(String tournamentId, List<UUID> participants, String arena, String kit) {
        String roundId = generateRoundId(tournamentId, participants);
        Round round = new Round(roundId, tournamentId, participants);
        
        if (arena != null) {
            round.setAssignedArena(arena);
        }
        if (kit != null) {
            round.setAssignedKit(kit);
        }
        
        // Validate round
        List<String> errors = round.validate();
        if (!errors.isEmpty()) {
            plugin.getLogger().warning("Failed to create round: " + String.join(", ", errors));
            return null;
        }
        
        activeRounds.put(roundId, round);
        
        // Track players in this round
        for (UUID playerId : participants) {
            playerRounds.put(playerId, roundId);
        }
        
        plugin.getLogger().info("Created round " + roundId + " for tournament " + tournamentId + " with " + participants.size() + " participants");
        return round;
    }
    
    /**
     * Start a round with preparation phase
     * @param roundId Round ID to start
     * @return True if round started successfully
     */
    public boolean startRound(String roundId) {
        Round round = activeRounds.get(roundId);
        if (round == null || !round.isReadyToStart()) {
            return false;
        }
        
        // Start preparation phase
        round.startPreparation();
        
        // Teleport players and apply kits
        if (!preparePlayersForRound(round)) {
            round.cancel();
            return false;
        }
        
        // Start countdown
        startCountdown(round);
        
        return true;
    }
    
    /**
     * Prepare players for the round (teleport, apply kits, save states)
     * @param round Round to prepare for
     * @return True if preparation was successful
     */
    private boolean preparePlayersForRound(Round round) {
        Arena arena = null;
        Kit kit = null;
        
        // Get arena if assigned
        if (round.hasAssignedArena()) {
            arena = plugin.getArenaManager().getArena(round.getAssignedArena());
            if (arena == null) {
                plugin.getLogger().warning("Arena " + round.getAssignedArena() + " not found for round " + round.getId());
                return false;
            }
            
            if (!ArenaUtils.isArenaValid(arena)) {
                plugin.getLogger().warning("Arena " + round.getAssignedArena() + " is not valid for round " + round.getId());
                return false;
            }
        }
        
        // Get kit if assigned
        if (round.hasAssignedKit()) {
            kit = plugin.getKitManager().getKit(round.getAssignedKit());
            if (kit == null) {
                plugin.getLogger().warning("Kit " + round.getAssignedKit() + " not found for round " + round.getId());
                return false;
            }
        }
        
        // Prepare each participant
        List<UUID> participants = round.getParticipants();
        for (int i = 0; i < participants.size(); i++) {
            UUID playerId = participants.get(i);
            Player player = Bukkit.getPlayer(playerId);
            
            if (player == null || !player.isOnline()) {
                plugin.getLogger().warning("Player " + playerId + " is not online for round " + round.getId());
                continue;
            }
            
            // Save player state
            Kit savedState = KitUtils.savePlayerState(player, "round_" + round.getId() + "_state");
            savedPlayerStates.put(playerId, savedState);
            
            // Teleport to arena spawn
            if (arena != null) {
                boolean teleported = false;
                
                if (arena.getType().name().equals("PVP") && round.is1v1()) {
                    // For PvP 1v1, teleport to team spawns
                    List<String> teams = ArenaUtils.getAvailableTeams(arena);
                    if (teams.size() >= 2) {
                        String team = teams.get(i % teams.size());
                        SpawnPoint spawn = ArenaUtils.getRandomTeamSpawn(arena, team);
                        if (spawn != null) {
                            player.teleport(spawn.getLocation());
                            teleported = true;
                        }
                    }
                }
                
                if (!teleported) {
                    // Default: teleport to any spawn point
                    List<SpawnPoint> spawns = arena.getSpawnPoints();
                    if (!spawns.isEmpty()) {
                        SpawnPoint spawn = spawns.get(i % spawns.size());
                        player.teleport(spawn.getLocation());
                        teleported = true;
                    }
                }
                
                if (!teleported) {
                    plugin.getLogger().warning("Failed to teleport player " + player.getName() + " to arena for round " + round.getId());
                }
            }
            
            // Apply kit
            if (kit != null) {
                KitUtils.applyKitToPlayer(player, kit);
            }
            
            // Send preparation message
            player.sendMessage(ChatColor.YELLOW + "Round starting in " + round.getPreparationTime() + " seconds!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }
        
        return true;
    }
    
    /**
     * Start the countdown timer for a round
     * @param round Round to start countdown for
     */
    private void startCountdown(Round round) {
        String roundId = round.getId();
        
        // Cancel existing countdown if any
        BukkitTask existingTask = countdownTasks.get(roundId);
        if (existingTask != null) {
            existingTask.cancel();
        }
        
        BukkitTask countdownTask = new BukkitRunnable() {
            int timeLeft = round.getPreparationTime();
            
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    // Start the actual round
                    round.startRound();
                    announceRoundStart(round);
                    countdownTasks.remove(roundId);
                    this.cancel();
                    return;
                }
                
                // Send countdown message
                for (UUID playerId : round.getParticipants()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        if (timeLeft <= 3) {
                            player.sendMessage(ChatColor.RED + "Round starts in " + ChatColor.BOLD + timeLeft + ChatColor.RED + "!");
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 2.0f);
                        } else if (timeLeft % 5 == 0) {
                            player.sendMessage(ChatColor.YELLOW + "Round starts in " + timeLeft + " seconds!");
                        }
                    }
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
        
        countdownTasks.put(roundId, countdownTask);
    }
    
    /**
     * Announce that a round has started
     * @param round The round that started
     */
    private void announceRoundStart(Round round) {
        for (UUID playerId : round.getParticipants()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "ROUND STARTED! FIGHT!");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
        }
        
        plugin.getLogger().info("Round " + round.getId() + " has started");
    }
    
    /**
     * Declare a winner for a round
     * @param roundId Round ID
     * @param winnerId Winner's UUID
     * @return True if winner was declared successfully
     */
    public boolean declareWinner(String roundId, UUID winnerId) {
        Round round = activeRounds.get(roundId);
        if (round == null || !round.isActive()) {
            return false;
        }
        
        if (!round.hasParticipant(winnerId)) {
            plugin.getLogger().warning("Player " + winnerId + " is not a participant in round " + roundId);
            return false;
        }
        
        round.setWinner(winnerId);
        
        // Announce winner
        Player winner = Bukkit.getPlayer(winnerId);
        String winnerName = winner != null ? winner.getName() : winnerId.toString();
        
        for (UUID playerId : round.getParticipants()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                if (playerId.equals(winnerId)) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "YOU WON THE ROUND!");
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                } else {
                    player.sendMessage(ChatColor.RED + "Round won by " + ChatColor.YELLOW + winnerName + ChatColor.RED + "!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            }
        }
        
        plugin.getLogger().info("Round " + roundId + " won by " + winnerName + " (" + winnerId + ")");
        
        // Stop spectating for this tournament
        plugin.getSpectatorManager().stopSpectatingForTournament(round.getTournamentId());
        
        // Schedule round cleanup
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cleanupRound(roundId);
        }, 60L); // 3 seconds delay
        
        return true;
    }
    
    /**
     * Clean up a finished round
     * @param roundId Round ID to clean up
     */
    public void cleanupRound(String roundId) {
        Round round = activeRounds.get(roundId);
        if (round == null) {
            return;
        }
        
        // Cancel countdown if still running
        BukkitTask countdownTask = countdownTasks.get(roundId);
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTasks.remove(roundId);
        }
        
        // Restore player states
        for (UUID playerId : round.getParticipants()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                Kit savedState = savedPlayerStates.get(playerId);
                if (savedState != null) {
                    KitUtils.restorePlayerState(player, savedState);
                }
                
                player.sendMessage(ChatColor.GREEN + "Round completed! Your state has been restored.");
            }
            
            // Remove from tracking
            playerRounds.remove(playerId);
            savedPlayerStates.remove(playerId);
        }
        
        // Remove round from active rounds
        activeRounds.remove(roundId);
        
        plugin.getLogger().info("Cleaned up round " + roundId);
    }
    
    /**
     * Cancel a round
     * @param roundId Round ID to cancel
     * @return True if cancelled successfully
     */
    public boolean cancelRound(String roundId) {
        Round round = activeRounds.get(roundId);
        if (round == null) {
            return false;
        }
        
        round.cancel();
        
        // Notify participants
        for (UUID playerId : round.getParticipants()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.RED + "Round has been cancelled!");
            }
        }
        
        // Schedule cleanup
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cleanupRound(roundId);
        }, 20L); // 1 second delay
        
        return true;
    }
    
    /**
     * Get a round by ID
     * @param roundId Round ID
     * @return Round or null if not found
     */
    public Round getRound(String roundId) {
        return activeRounds.get(roundId);
    }
    
    /**
     * Get the round a player is currently in
     * @param playerId Player UUID
     * @return Round or null if not in any round
     */
    public Round getPlayerRound(UUID playerId) {
        String roundId = playerRounds.get(playerId);
        return roundId != null ? activeRounds.get(roundId) : null;
    }
    
    /**
     * Check if a player is in an active round
     * @param playerId Player UUID
     * @return True if player is in a round
     */
    public boolean isPlayerInRound(UUID playerId) {
        return getPlayerRound(playerId) != null;
    }
    
    /**
     * Get all active rounds
     * @return List of active rounds
     */
    public List<Round> getActiveRounds() {
        return new ArrayList<>(activeRounds.values());
    }
    
    /**
     * Get active rounds for a specific tournament
     * @param tournamentId Tournament ID
     * @return List of rounds for the tournament
     */
    public List<Round> getTournamentRounds(String tournamentId) {
        List<Round> tournamentRounds = new ArrayList<>();
        for (Round round : activeRounds.values()) {
            if (round.getTournamentId().equals(tournamentId)) {
                tournamentRounds.add(round);
            }
        }
        return tournamentRounds;
    }
    
    /**
     * Get active round for a specific tournament
     * @param tournamentId Tournament ID
     * @return Active round or null if none found
     */
    public Round getActiveRoundForTournament(String tournamentId) {
        for (Round round : activeRounds.values()) {
            if (round.getTournamentId().equals(tournamentId) && 
                (round.getStatus() == Round.RoundStatus.ACTIVE || round.getStatus() == Round.RoundStatus.PREPARING)) {
                return round;
            }
        }
        return null;
    }
    
    /**
     * Generate a unique round ID
     * @param tournamentId Tournament ID
     * @param participants List of participants
     * @return Generated round ID
     */
    private String generateRoundId(String tournamentId, List<UUID> participants) {
        String base = tournamentId + "_round_" + System.currentTimeMillis();
        int hash = participants.hashCode();
        return base + "_" + Math.abs(hash);
    }
    
    /**
     * Shutdown the round manager, cleaning up all active rounds
     */
    public void shutdown() {
        plugin.getLogger().info("Shutting down RoundManager, cleaning up " + activeRounds.size() + " active rounds");
        
        // Cancel all countdown tasks
        for (BukkitTask task : countdownTasks.values()) {
            task.cancel();
        }
        countdownTasks.clear();
        
        // Clean up all rounds
        for (String roundId : new ArrayList<>(activeRounds.keySet())) {
            cleanupRound(roundId);
        }
        
        activeRounds.clear();
        playerRounds.clear();
        savedPlayerStates.clear();
    }
}