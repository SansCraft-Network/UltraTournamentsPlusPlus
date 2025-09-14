package top.sanscraft.ultratournamentsplusplus.discord;

import top.sanscraft.ultratournamentsplusplus.UltraTournamentsPlusPlus;
import top.sanscraft.ultratournamentsplusplus.models.DiscordLink;
import top.sanscraft.ultratournamentsplusplus.models.PlayerStats;
import top.sanscraft.ultratournamentsplusplus.models.Tournament;
import top.sanscraft.ultratournamentsplusplus.models.Round;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages Discord bot integration with full JDA implementation
 */
public class DiscordBot extends ListenerAdapter {
    
    private final UltraTournamentsPlusPlus plugin;
    private JDA jda;
    private String guildId;
    private String announcementChannelId;
    private String resultsChannelId;
    
    public DiscordBot(UltraTournamentsPlusPlus plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initialize the Discord bot
     */
    public void initialize() {
        plugin.getLogger().info("Initializing Discord bot...");
        
        String token = plugin.getConfigManager().getDiscordToken();
        if (token == null || token.isEmpty() || "YOUR_BOT_TOKEN_HERE".equals(token)) {
            plugin.getLogger().warning("Discord token not configured!");
            return;
        }
        
        this.guildId = plugin.getConfigManager().getDiscordGuildId();
        this.announcementChannelId = plugin.getConfigManager().getDiscordAnnouncementChannel();
        this.resultsChannelId = plugin.getConfigManager().getDiscordResultsChannel();
        
        try {
            jda = JDABuilder.createDefault(token)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES)
                    .addEventListeners(this)
                    .build();
            
            jda.awaitReady();
            plugin.getLogger().info("Discord bot initialized successfully!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize Discord bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        plugin.getLogger().info("Discord bot is ready! Logged in as: " + event.getJDA().getSelfUser().getName());
        
        // Register slash commands
        registerSlashCommands();
    }
    
    /**
     * Register Discord slash commands
     */
    private void registerSlashCommands() {
        if (jda.getGuildById(guildId) == null) {
            plugin.getLogger().warning("Could not find Discord guild with ID: " + guildId);
            return;
        }
        
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            plugin.getLogger().warning("Guild is null, cannot register commands");
            return;
        }
        
        // Register commands
        guild.updateCommands().addCommands(
            Commands.slash("link", "Link your Discord account with your Minecraft account")
                .addOption(OptionType.STRING, "username", "Your Minecraft username", true),
                
            Commands.slash("unlink", "Unlink your Discord account from Minecraft"),
            
            Commands.slash("stats", "View tournament statistics")
                .addOptions(
                    new OptionData(OptionType.USER, "player", "View stats for another player", false),
                    new OptionData(OptionType.STRING, "username", "View stats by Minecraft username", false)
                ),
                
            Commands.slash("tournaments", "View active tournaments"),
            
            Commands.slash("join", "Join a tournament")
                .addOption(OptionType.STRING, "tournament", "Tournament name", true),
                
            Commands.slash("leave", "Leave a tournament")
                .addOption(OptionType.STRING, "tournament", "Tournament name", true),
                
            Commands.slash("profile", "View your tournament profile"),
            
            Commands.slash("leaderboard", "View tournament leaderboard")
                .addOption(OptionType.INTEGER, "limit", "Number of players to show (default: 10)", false),
                
            Commands.slash("notifications", "Toggle tournament notifications")
                .addOption(OptionType.BOOLEAN, "enabled", "Enable or disable notifications", true)
        ).queue(
            success -> plugin.getLogger().info("Successfully registered Discord slash commands"),
            error -> plugin.getLogger().severe("Failed to register Discord commands: " + error.getMessage())
        );
    }
    
    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        
        try {
            switch (commandName) {
                case "link" -> handleLinkCommand(event);
                case "unlink" -> handleUnlinkCommand(event);
                case "stats" -> handleStatsCommand(event);
                case "tournaments" -> handleTournamentsCommand(event);
                case "join" -> handleJoinCommand(event);
                case "leave" -> handleLeaveCommand(event);
                case "profile" -> handleProfileCommand(event);
                case "leaderboard" -> handleLeaderboardCommand(event);
                case "notifications" -> handleNotificationsCommand(event);
                default -> event.reply("Unknown command!").setEphemeral(true).queue();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error handling Discord command '" + commandName + "': " + e.getMessage());
            event.reply("An error occurred while processing your command. Please try again later.")
                 .setEphemeral(true).queue();
        }
    }
    
    /**
     * Handle /link command
     */
    private void handleLinkCommand(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        
        if (event.getOption("username") == null) {
            event.reply("Username parameter is required!").setEphemeral(true).queue();
            return;
        }
        
        
        OptionMapping usernameOption = event.getOption("username");
        if (usernameOption == null) {
            event.getHook().editOriginal("Username parameter is required!")
                 .queue();
            return;
        }
        String minecraftUsername = usernameOption.getAsString();
        
        // Check if already linked
        if (plugin.getDiscordLinkManager().isDiscordLinked(discordId)) {
            event.reply("Your Discord account is already linked! Use `/unlink` first if you want to link a different account.")
                 .setEphemeral(true).queue();
            return;
        }
        
        // Find player by username
        Player player = Bukkit.getPlayerExact(minecraftUsername);
        if (player == null) {
            event.reply("Player '" + minecraftUsername + "' is not online. Please make sure you're in-game and try again.")
                 .setEphemeral(true).queue();
            return;
        }
        
        // Check if Minecraft account is already linked
        if (plugin.getDiscordLinkManager().isMinecraftLinked(player.getUniqueId())) {
            event.reply("That Minecraft account is already linked to another Discord account!")
                 .setEphemeral(true).queue();
            return;
        }
        
        // Start linking process
        String verificationCode = plugin.getDiscordLinkManager().startLinking(discordId, player);
        if (verificationCode == null) {
            event.reply("Failed to start linking process. Please try again later.")
                 .setEphemeral(true).queue();
            return;
        }
        
        // Send verification code to Discord user
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🔗 Account Linking")
            .setDescription("To complete the linking process, type this command in Minecraft:")
            .addField("Command", "`/tournament verify " + verificationCode + "`", false)
            .addField("⏰ Expires", "24 hours", true)
            .addField("🎮 Minecraft Account", minecraftUsername, true)
            .setColor(Color.GREEN)
            .setTimestamp(Instant.now());
        
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        
        // Also notify in-game
        player.sendMessage("§a[Tournament] §fDiscord linking started! Use §e/tournament verify " + 
                          verificationCode + " §fto complete the link.");
    }
    
    /**
     * Handle /unlink command
     */
    private void handleUnlinkCommand(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        
        if (!plugin.getDiscordLinkManager().isDiscordLinked(discordId)) {
            event.reply("Your Discord account is not linked to any Minecraft account!")
                 .setEphemeral(true).queue();
            return;
        }
        
        DiscordLink link = plugin.getDiscordLinkManager().getLinkByDiscordId(discordId);
        boolean success = plugin.getDiscordLinkManager().unlinkDiscord(discordId);
        
        if (success) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔓 Account Unlinked")
                .setDescription("Successfully unlinked your Discord account from: **" + link.getMinecraftUsername() + "**")
                .setColor(Color.ORANGE)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            event.reply("Failed to unlink your account. Please try again later.")
                 .setEphemeral(true).queue();
        }
    }
    
    /**
     * Handle /stats command
     */
    private void handleStatsCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        String discordId = event.getUser().getId();
        UUID targetPlayerId = null;
        String targetUsername = null;
        
        // Check if looking up another user
        if (event.getOption("player") != null) {
            OptionMapping playerOption = event.getOption("player");
            if (playerOption != null) {
                User targetUser = playerOption.getAsUser();
                if (targetUser != null) {
                    targetPlayerId = plugin.getDiscordLinkManager().getMinecraftUuid(targetUser.getId());
                    if (targetPlayerId == null) {
                        event.getHook().editOriginal("That Discord user is not linked to a Minecraft account!")
                             .queue();
                        return;
                    }
                    targetUsername = plugin.getDiscordLinkManager().getMinecraftUsername(targetUser.getId());
                }
            }
        } else if (event.getOption("username") != null) {
            OptionMapping usernameOption = event.getOption("username");
            if (usernameOption != null) {
                String usernameOptionValue = usernameOption.getAsString();
                if (usernameOptionValue != null) {
                    // TODO: Look up UUID by username from database or Bukkit
                    Player player = Bukkit.getPlayerExact(usernameOptionValue);
                    if (player != null) {
                        targetPlayerId = player.getUniqueId();
                        targetUsername = usernameOptionValue;
                    } else {
                        event.getHook().editOriginal("Player '" + usernameOptionValue + "' not found!")
                             .queue();
                        return;
                    }
                }
            }
        } else {
            // Look up own stats
            UUID ownPlayerId = plugin.getDiscordLinkManager().getMinecraftUuid(discordId);
            if (ownPlayerId == null) {
                event.getHook().editOriginal("You need to link your Discord account first! Use `/link`")
                     .queue();
                return;
            }
            targetPlayerId = ownPlayerId;
            targetUsername = plugin.getDiscordLinkManager().getMinecraftUsername(discordId);
        }
        
        if (targetPlayerId == null || targetUsername == null) {
            event.getHook().editOriginal("Failed to resolve player information!")
                 .queue();
            return;
        }
        
        final UUID finalTargetPlayerId = targetPlayerId;
        final String finalTargetUsername = targetUsername;
        
        // Load stats from database
        CompletableFuture.supplyAsync(() -> {
            try {
                return plugin.getDatabaseManager().loadPlayerStats(finalTargetPlayerId);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load stats for " + finalTargetUsername + ": " + e.getMessage());
                return null;
            }
        }).thenAccept(stats -> {
            if (stats == null) {
                // Create empty stats for display
                stats = new PlayerStats(finalTargetPlayerId, finalTargetUsername);
            }
            
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 Tournament Statistics")
                .setDescription(stats.getDiscordStatsSummary())
                .setColor(Color.BLUE)
                .setTimestamp(Instant.now());
            
            event.getHook().editOriginalEmbeds(embed.build()).queue();
        });
    }
    
    /**
     * Handle /tournaments command
     */
    private void handleTournamentsCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        var tournaments = plugin.getTournamentManager().getTournaments();
        
        if (tournaments.isEmpty()) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Active Tournaments")
                .setDescription("No tournaments are currently active.")
                .setColor(Color.GRAY)
                .setTimestamp(Instant.now());
            
            event.getHook().editOriginalEmbeds(embed.build()).queue();
            return;
        }
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🏆 Active Tournaments")
            .setColor(Color.GREEN)
            .setTimestamp(Instant.now());
        
        for (Tournament tournament : tournaments) {
            String status = tournament.getStatus().toString();
            String participants = tournament.getParticipantCount() + "/" + tournament.getMaxParticipants();
            String value = String.format("**Status:** %s\n**Players:** %s\n**Type:** %s", 
                                        status, participants, tournament.getType());
            embed.addField(tournament.getName(), value, true);
        }
        
        event.getHook().editOriginalEmbeds(embed.build()).queue();
    }
    
    /**
     * Handle /join command
     */
    private void handleJoinCommand(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        
        if (event.getOption("tournament") == null) {
            event.reply("Tournament parameter is required!").setEphemeral(true).queue();
            return;
        }
        
        OptionMapping tournamentOption = event.getOption("tournament");
        if (tournamentOption == null) {
            event.reply("Tournament parameter is required!").setEphemeral(true).queue();
            return;
        }
        String tournamentName = tournamentOption.getAsString();
        
        // Check if linked
        UUID playerId = plugin.getDiscordLinkManager().getMinecraftUuid(discordId);
        if (playerId == null) {
            event.reply("You need to link your Discord account first! Use `/link`")
                 .setEphemeral(true).queue();
            return;
        }
        
        // Check if player is online
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            event.reply("You need to be online in Minecraft to join tournaments!")
                 .setEphemeral(true).queue();
            return;
        }
        
        Tournament tournament = plugin.getTournamentManager().getTournament(tournamentName);
        if (tournament == null) {
            event.reply("Tournament '" + tournamentName + "' not found!")
                 .setEphemeral(true).queue();
            return;
        }
        
        // Try to join tournament
        boolean success = tournament.addParticipant(playerId);
        if (success) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("✅ Tournament Joined")
                .setDescription("Successfully joined tournament: **" + tournament.getName() + "**")
                .addField("Participants", tournament.getParticipantCount() + "/" + tournament.getMaxParticipants(), true)
                .setColor(Color.GREEN)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            
            // Notify in-game
            player.sendMessage("§a[Tournament] §fSuccessfully joined tournament: §e" + tournament.getName());
        } else {
            String reason = tournament.isFull() ? "Tournament is full!" : "You are already in this tournament!";
            event.reply(reason).setEphemeral(true).queue();
        }
    }
    
    /**
     * Handle /leave command
     */
    private void handleLeaveCommand(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        
        if (event.getOption("tournament") == null) {
            event.reply("Tournament parameter is required!").setEphemeral(true).queue();
            return;
        }
        
        OptionMapping tournamentOption = event.getOption("tournament");
        if (tournamentOption == null) {
            event.reply("Tournament parameter is required!").setEphemeral(true).queue();
            return;
        }
        String tournamentName = tournamentOption.getAsString();
        
        UUID playerId = plugin.getDiscordLinkManager().getMinecraftUuid(discordId);
        if (playerId == null) {
            event.reply("You need to link your Discord account first!")
                 .setEphemeral(true).queue();
            return;
        }
        
        Tournament tournament = plugin.getTournamentManager().getTournament(tournamentName);
        if (tournament == null) {
            event.reply("Tournament '" + tournamentName + "' not found!")
                 .setEphemeral(true).queue();
            return;
        }
        
        boolean success = tournament.removeParticipant(playerId);
        if (success) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🚪 Left Tournament")
                .setDescription("Successfully left tournament: **" + tournament.getName() + "**")
                .setColor(Color.ORANGE)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            event.reply("You are not in that tournament!").setEphemeral(true).queue();
        }
    }
    
    /**
     * Handle /profile command
     */
    private void handleProfileCommand(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        
        DiscordLink link = plugin.getDiscordLinkManager().getLinkByDiscordId(discordId);
        if (link == null) {
            event.reply("You need to link your Discord account first! Use `/link`")
                 .setEphemeral(true).queue();
            return;
        }
        
        event.deferReply().setEphemeral(true).queue();
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return plugin.getDatabaseManager().loadPlayerStats(link.getMinecraftUuid());
            } catch (Exception e) {
                return null;
            }
        }).thenAccept(stats -> {
            if (stats == null) {
                stats = new PlayerStats(link.getMinecraftUuid(), link.getMinecraftUsername());
            }
            
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("👤 Your Tournament Profile")
                .setDescription("**Minecraft:** " + link.getMinecraftUsername())
                .addField("🏆 Tournament Record", 
                         stats.getTournamentsWon() + " wins / " + stats.getTournamentsPlayed() + " played", true)
                .addField("⚔️ Round Record", 
                         stats.getRoundsWon() + " wins / " + stats.getRoundsPlayed() + " played", true)
                .addField("📈 Current Rank", stats.getRank(), true)
                .addField("🔥 Win Streak", String.valueOf(stats.getWinStreak()), true)
                .addField("💀 K/D Ratio", String.format("%.2f", stats.getKillDeathRatio()), true)
                .addField("🔔 Notifications", link.isNotificationsEnabled() ? "Enabled" : "Disabled", true)
                .setColor(Color.CYAN)
                .setTimestamp(Instant.now());
            
            event.getHook().editOriginalEmbeds(embed.build()).queue();
        });
    }
    
    /**
     * Handle /leaderboard command
     */
    private void handleLeaderboardCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        OptionMapping limitOption = event.getOption("limit");
        final int limit = limitOption != null ? limitOption.getAsInt() : 10;
        final int clampedLimit = Math.min(Math.max(limit, 1), 25); // Clamp between 1-25
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return plugin.getDatabaseManager().getTopPlayersByTournaments(clampedLimit);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load leaderboard: " + e.getMessage());
                return List.<PlayerStats>of();
            }
        }).thenAccept(topPlayers -> {
            if (topPlayers.isEmpty()) {
                EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🏆 Tournament Leaderboard")
                    .setDescription("No tournament data available yet.")
                    .setColor(Color.GRAY);
                
                event.getHook().editOriginalEmbeds(embed.build()).queue();
                return;
            }
            
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Tournament Leaderboard")
                .setColor(Color.YELLOW)
                .setTimestamp(Instant.now());
            
            StringBuilder description = new StringBuilder();
            for (int i = 0; i < topPlayers.size(); i++) {
                PlayerStats stats = topPlayers.get(i);
                String medal = switch (i) {
                    case 0 -> "🥇";
                    case 1 -> "🥈";
                    case 2 -> "🥉";
                    default -> String.format("%d.", i + 1);
                };
                
                description.append(String.format("%s **%s** - %d wins (%s)\n", 
                    medal, stats.getPlayerName(), stats.getTournamentsWon(), stats.getRank()));
            }
            
            embed.setDescription(description.toString());
            event.getHook().editOriginalEmbeds(embed.build()).queue();
        });
    }
    
    /**
     * Handle /notifications command
     */
    private void handleNotificationsCommand(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        
        if (event.getOption("enabled") == null) {
            event.reply("Enabled parameter is required!").setEphemeral(true).queue();
            return;
        }
        
        OptionMapping enabledOption = event.getOption("enabled");
        if (enabledOption == null) {
            event.reply("Enabled parameter is required!").setEphemeral(true).queue();
            return;
        }
        boolean enabled = enabledOption.getAsBoolean();
        
        if (!plugin.getDiscordLinkManager().isDiscordLinked(discordId)) {
            event.reply("You need to link your Discord account first! Use `/link`")
                 .setEphemeral(true).queue();
            return;
        }
        
        boolean success = plugin.getDiscordLinkManager().updateNotificationSettings(discordId, enabled);
        if (success) {
            String status = enabled ? "enabled" : "disabled";
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔔 Notifications " + (enabled ? "Enabled" : "Disabled"))
                .setDescription("Tournament notifications have been " + status + ".")
                .setColor(enabled ? Color.GREEN : Color.ORANGE)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            event.reply("Failed to update notification settings. Please try again later.")
                 .setEphemeral(true).queue();
        }
    }
    
    /**
     * Send announcement to Discord
     */
    public void sendAnnouncement(String message) {
        if (jda == null || announcementChannelId == null) return;
        
        TextChannel channel = jda.getTextChannelById(announcementChannelId);
        if (channel != null) {
            channel.sendMessage(message).queue();
        }
    }
    
    /**
     * Send tournament result to Discord
     */
    public void sendTournamentResult(Tournament tournament, String winner) {
        if (jda == null || resultsChannelId == null) return;
        
        TextChannel channel = jda.getTextChannelById(resultsChannelId);
        if (channel != null) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Tournament Complete!")
                .setDescription("**" + tournament.getName() + "** has finished!")
                .addField("Winner", winner, true)
                .addField("Participants", String.valueOf(tournament.getParticipantCount()), true)
                .addField("Type", tournament.getType().toString(), true)
                .setColor(Color.YELLOW)
                .setTimestamp(Instant.now());
            
            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }
    
    /**
     * Send round result to Discord
     */
    public void sendRoundResult(Round round, String winner, String loser) {
        if (jda == null || resultsChannelId == null) return;
        
        TextChannel channel = jda.getTextChannelById(resultsChannelId);
        if (channel != null) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⚔️ Round Complete")
                .addField("Winner", winner, true)
                .addField("Opponent", loser, true)
                .addField("Duration", round.getFormattedDuration(), true)
                .setColor(Color.GREEN)
                .setTimestamp(Instant.now());
            
            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }
    
    /**
     * Send DM to a Discord user
     */
    public void sendDirectMessage(String discordId, String message) {
        if (jda == null) return;
        
        User user = jda.getUserById(discordId);
        if (user != null) {
            user.openPrivateChannel().queue(channel -> {
                channel.sendMessage(message).queue(
                    success -> {},
                    error -> plugin.getLogger().warning("Failed to send DM to " + discordId + ": " + error.getMessage())
                );
            });
        }
    }
    
    /**
     * Send DM with embed to a Discord user
     */
    public void sendDirectMessageEmbed(String discordId, MessageEmbed embed) {
        if (jda == null) return;
        
        User user = jda.getUserById(discordId);
        if (user != null) {
            user.openPrivateChannel().queue(channel -> {
                channel.sendMessageEmbeds(embed).queue(
                    success -> {},
                    error -> plugin.getLogger().warning("Failed to send DM to " + discordId + ": " + error.getMessage())
                );
            });
        }
    }
    
    /**
     * Notify players about upcoming rounds
     */
    public void notifyUpcomingRound(List<UUID> participants, String tournamentName, int roundsUntil) {
        for (UUID playerId : participants) {
            String discordId = plugin.getDiscordLinkManager().getDiscordId(playerId);
            if (discordId != null) {
                DiscordLink link = plugin.getDiscordLinkManager().getLinkByDiscordId(discordId);
                if (link != null && link.isNotificationsEnabled()) {
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("⚠️ Upcoming Tournament Round")
                        .setDescription("Your round in **" + tournamentName + "** is starting in " + roundsUntil + " rounds!")
                        .addField("⏰ Reminder", "Make sure you're online in Minecraft, or you'll automatically lose!", false)
                        .setColor(Color.YELLOW)
                        .setTimestamp(Instant.now());
                    
                    sendDirectMessageEmbed(discordId, embed.build());
                }
            }
        }
    }
    
    /**
     * Check if the bot is ready
     */
    public boolean isReady() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }
    
    /**
     * Shutdown the Discord bot
     */
    public void shutdown() {
        if (jda != null) {
            plugin.getLogger().info("Shutting down Discord bot...");
            jda.shutdown();
            try {
                if (!jda.awaitShutdown(10, TimeUnit.SECONDS)) {
                    jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                jda.shutdownNow();
                Thread.currentThread().interrupt();
            }
            plugin.getLogger().info("Discord bot shutdown complete");
        }
    }
}