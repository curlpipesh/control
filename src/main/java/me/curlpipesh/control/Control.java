package me.curlpipesh.control;

import lombok.Getter;
import me.curlpipesh.control.adblock.Adblocker;
import me.curlpipesh.control.adminchat.PlayerMapperTask;
import me.curlpipesh.control.adminchat.UserMap;
import me.curlpipesh.control.adminchat.UserMap.AdminChatUser;
import me.curlpipesh.control.commands.*;
import me.curlpipesh.control.db.IPunishmentDB;
import me.curlpipesh.control.db.PunishmentDB;
import me.curlpipesh.control.db.PunishmentDB.DBMode;
import me.curlpipesh.control.fixes.Fix;
import me.curlpipesh.control.fixes.NetherTopFix;
import me.curlpipesh.control.fixes.SignHackFix;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.control.punishment.Punishments;
import me.curlpipesh.util.command.SkirtsCommand;
import me.curlpipesh.util.plugin.SkirtsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author audrey
 * @since 8/23/15.
 */
public class Control extends SkirtsPlugin {
    @Getter
    private final IPunishmentDB activePunishments;

    @Getter
    private final IPunishmentDB inactivePunishments;

    @Getter
    private final Collection<String> mutes = new CopyOnWriteArrayList<>();

    @Getter
    private final Collection<String> cmutes = new CopyOnWriteArrayList<>();

    @Getter
    private final Collection<String> bans = new CopyOnWriteArrayList<>();

    @Getter
    private final Collection<String> ipBans = new CopyOnWriteArrayList<>();

    /****************************************************
     * DO NOT EVER CHANGE THIS FOR ANY REASON           *
     * TODO: Should probably not store dates as TEXT... *
     ****************************************************/
    @Getter
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Getter
    private String chatPrefix;

    @Getter
    private String chatHeader;

    private final List<Fix> fixes = Arrays.<Fix>asList(new SignHackFix(), new NetherTopFix());

    private String lastPlayer = "";

    private boolean welcEnabled = true;

    private List<String> cmuteCommandsToBlock;

    public Control() {
        final String sqlmode = getConfig().getString("sql-mode");
        if(sqlmode.equalsIgnoreCase("sqlite")) {
            activePunishments = new PunishmentDB(this, "active_punishments", DBMode.SQLITE);
            inactivePunishments = new PunishmentDB(this, "inactive_punishments", DBMode.SQLITE);
        }/* else if(sqlmode.equalsIgnoreCase("mysql")) {
            activePunishments = new PunishmentDB(this, "active_punishments", PunishmentDB.DBMode.MYSQL);
            inactivePunishments = new PunishmentDB(this, "inactive_punishments", PunishmentDB.DBMode.MYSQL);
        } */ else {
            throw new IllegalArgumentException('\'' + sqlmode + "' is not a valid SQL mode!");
        }
    }

    public void onEnable() {
        if(!getDataFolder().exists()) {
            getLogger().info("Data folder doesn't exist, making...");
            if(getDataFolder().mkdir()) {
                getLogger().info("Data folder made!");
            }
        }
        getLogger().info("Saving default config...");
        saveDefaultConfig();
        chatPrefix = getConfig().getString("chat-prefix");
        chatHeader = getConfig().getString("chat-header");
        cmuteCommandsToBlock = getConfig().getStringList("cmute-blocked-cmds");
        prepDBs();
        scheduleCleanupTask();
        registerEventBlockers();
        registerAdminChat();
        fixes.stream().forEach(f -> f.fix(this));
        readyWelc();

        // TODO: Convert to SkirtsCommand format
        getCommandManager().registerCommand(SkirtsCommand.builder().setName("audit")
                .setDescription("Show all punishments issued by a player").setUsage("/audit <player>")
                .setPermissionNode("control.audit").setExecutor(new CommandAudit(this)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("clearchat").addAlias("cc")
                .setDescription("Clears the chat").setUsage("/clearchat").setPermissionNode("control.clearchat")
                .setExecutor(new CommandClearChat(this)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("history")
                .setDescription("Show all punishments applied to a player").setUsage("/history <player>")
                .setPermissionNode("control.history").setExecutor(new CommandHistory(this)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("info").addAlias("specs")
                .setDescription("Show information about the hardware/software stack the server is running on")
                .setUsage("/info").setPermissionNode("control.info").setExecutor(new CommandInfo(this)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("o").addAlias("online")
                .setDescription("Show the number of users online").setUsage("/o").setPermissionNode("control.online")
                .setExecutor(new CommandOnline(this)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("ops")
                .setDescription("Show all server ops").setUsage("/ops").setPermissionNode("control.ops")
                .setExecutor(new CommandOps(this)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("plgrep").addAlias("plugingrep")
                .setDescription("Grep for a string in the plugin list. Not regex").setUsage("/plgrep <string>")
                .setPermissionNode("control.plgrep").build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("warns")
                .setDescription("Show all warnings for a player").setUsage("/warns <player>")
                .setPermissionNode("control.warns").setExecutor(new CommandWarns(this)).build());

        // Punishment commands
        getCommandManager().registerCommand(SkirtsCommand.builder().setName("ban").addAlias("banplayer")
                .setDescription("Ban a player, for minutes, hours, days, or weeks")
                .setUsage("/ban <player> [[t:]length<m|h|d|w>] [reason]").setPermissionNode("control.ban")
                .setExecutor(new GenericPunishmentCommand(this, Punishments.BAN)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("banip").addAlias("ipban")
                .setDescription("Ban an IP, by IP or player, for minutes, hours, days, or weeks")
                .setUsage("/ban <player|IP> [[t:]length<m|h|d|w>] [reason]")
                .setPermissionNode("control.banip")
                .setExecutor(new GenericPunishmentCommand(this, Punishments.IP_BAN)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("cmute")
                .setDescription("Prevents a player from using commands")
                .setUsage("/cmute <player> [[t:]length<m|h|d|w>] [reason]").setPermissionNode("control.cmute")
                .setExecutor(new GenericPunishmentCommand(this, Punishments.COMMAND_MUTE)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("mute")
                .setDescription("Prevents a player from sending chat messages")
                .setUsage("/mute <player> [[t:]length<m|h|d|w>] [reason]")
                .setPermissionNode("control.mute").setExecutor(new GenericPunishmentCommand(this, Punishments.MUTE))
                .build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("kick")
                .setDescription("Kicks a player from the server").setUsage("/kick <player> [reason]")
                .setPermissionNode("control.kick").setExecutor(new CommandKick(this)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("warn")
                .setDescription("Issue a warning to a player").setUsage("/warn <player> [reason]")
                .setPermissionNode("control.warn").setExecutor(new CommandWarn(this)).build());

        // Undo commands
        getCommandManager().registerCommand(SkirtsCommand.builder().setName("unban").addAlias("unbanplayer")
                .setDescription("Unban a player").setUsage("/unban <player>").setPermissionNode("control.unban")
                .setExecutor(new GenericPunishmentCommand(this, Punishments.BAN, true)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("unbanip").addAlias("unipban")
                .setDescription("Unban an IP by player or IP").setUsage("/ban <player|IP>")
                .setPermissionNode("control.unbanip")
                .setExecutor(new GenericPunishmentCommand(this, Punishments.IP_BAN, true)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("uncmute")
                .setDescription("Un-command-mute a player").setUsage("/uncmute <player>")
                .setPermissionNode("control.uncmute")
                .setExecutor(new GenericPunishmentCommand(this, Punishments.COMMAND_MUTE, true)).build());

        getCommandManager().registerCommand(SkirtsCommand.builder().setName("unmute")
                .setDescription("Unmute a player").setUsage("/unmute <player>").setPermissionNode("control.unmute")
                .setExecutor(new GenericPunishmentCommand(this, Punishments.MUTE, true)).build());

        // Admin chat
        getCommandManager().registerCommand(SkirtsCommand.builder().setName("a").addAlias("adminchat").addAlias("ac")
                .setDescription("Enter admin chat, or send a message to the channel").setUsage("/a [message]")
                .setPermissionNode("control.channels.use").setExecutor(new CommandA(this)).build());
    }

    public void onDisable() {
        if(activePunishments.getDatabaseBackend().disconnect() && inactivePunishments.getDatabaseBackend().disconnect()) {
            getLogger().info("Successfully disconnected from the databases!");
        } else {
            throw new IllegalStateException("Unable to disconnect from the databases!");
        }
    }

    public void sendMessage(final CommandSender commandSender, final String... message) {
        for(final String e : message) {
            commandSender.sendMessage(String.format("%s %s", chatPrefix, e));
        }
    }

    public void sendImportantMessage(final CommandSender commandSender, final String... message) {
        commandSender.sendMessage(String.format("%s%s%s", chatHeader, chatPrefix, chatHeader));
        for(final String e : message) {
            commandSender.sendMessage(e);
        }
        commandSender.sendMessage(String.format("%s%s%s", chatHeader, chatPrefix, chatHeader));
    }

    public void broadcastMessage(final String... message) {
        for(final Player player : Bukkit.getOnlinePlayers()) {
            sendMessage(player, message);
        }
        sendMessage(Bukkit.getConsoleSender(), message);
    }

    public void broadcastImportantMessage(final String... message) {
        for(final Player player : Bukkit.getOnlinePlayers()) {
            sendImportantMessage(player, message);
        }
        sendImportantMessage(Bukkit.getConsoleSender(), message);
    }

    private void prepDBs() {
        if(activePunishments.getDatabaseBackend().connect() && inactivePunishments.getDatabaseBackend().connect()) {
            getLogger().info("Connected to the databases!");
            if(activePunishments.getDatabaseBackend().initialize() && inactivePunishments.getDatabaseBackend().initialize()) {
                if(inactivePunishments.getLastPunishmentId() > activePunishments.getLastPunishmentId()) {
                    activePunishments.setLastPunishmentId(inactivePunishments.getLastPunishmentId());
                }
                getLogger().info("Initialised databases!");
                loadPunishments();
            } else {
                getLogger().warning("Unable to initialise databases!");
            }
        } else {
            Bukkit.getPluginManager().disablePlugin(this);
            throw new IllegalStateException("Unable to connect to the databases!");
        }
    }

    private void loadPunishments() {
        activePunishments.getAllPunishments().forEach(p -> {
            switch(p.getType()) {
                case Punishments.BAN:
                    bans.add(p.getTarget());
                    break;
                case Punishments.COMMAND_MUTE:
                    cmutes.add(p.getTarget());
                    break;
                case Punishments.MUTE:
                    mutes.add(p.getTarget());
                    break;
                case Punishments.IP_BAN:
                    ipBans.add(p.getTarget());
                    break;
                case Punishments.WARN:
                    break;
                default:
                    getLogger().warning("I don't know what \"" + p.getType() + "\" punishment type is?");
                    break;
            }
        });
    }

    /**
     * Cleans up inactive punishments once every minute. Warnings do not ever get removed.
     *
     * TODO: Actually make an unwarn?
     */
    private void scheduleCleanupTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> activePunishments.getExpiredPunishments().forEach(p -> {
            getLogger().info("Removing inactive punishment: " + p);
            activePunishments.removePunishment(p);
            switch(p.getType()) {
                case Punishments.BAN:
                    bans.remove(p.getTarget());
                    break;
                case Punishments.COMMAND_MUTE:
                    cmutes.remove(p.getTarget());
                    break;
                case Punishments.MUTE:
                    mutes.remove(p.getTarget());
                    break;
                case Punishments.IP_BAN:
                    ipBans.remove(p.getTarget());
                    break;
                default:
                    break;
            }
            inactivePunishments.insertPunishment(p);
        }), 0L, 20L * 60L);
    }

    private void registerEventBlockers() {
        Bukkit.getPluginManager().registerEvents(new Adblocker(this), this);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onPlayerChatEvent(final AsyncPlayerChatEvent e) {
                final String uuid = e.getPlayer().getUniqueId().toString();
                final String ip = e.getPlayer().getAddress().getAddress().toString();
                if(mutes.contains(uuid) || mutes.contains(ip)) {
                    sendMessage(e.getPlayer(), "§7You're still muted! You can't talk!");
                    e.setCancelled(true);
                }
            }
        }, this);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent e) {
                if(bans.contains(e.getUniqueId().toString())) {
                    final List<Punishment> p = activePunishments.getPunishments(e.getUniqueId().toString());
                    e.disallow(Result.KICK_BANNED, "§4Banned§r: " + p.get(p.size() - 1).getReason() + "\n\nExpires: " + p.get(p.size() - 1).getEnd());
                }
                if(ipBans.contains(e.getAddress().toString().replaceAll("/", ""))) {
                    final List<Punishment> p = activePunishments.getPunishments(e.getAddress().toString().replaceAll("/", ""));
                    e.disallow(Result.KICK_BANNED, "§4Banned§r: " + p.get(p.size() - 1).getReason() + "\n\nExpires: " + p.get(p.size() - 1).getEnd());
                }
            }
        }, this);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            // So it turns out that this actually can't be done by listening for chat events
            // I guess Bukkit does it that way because reasons or something
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onCommandPreprocess(final PlayerCommandPreprocessEvent e) {
                final String uuid = e.getPlayer().getUniqueId().toString();
                final String ip = e.getPlayer().getAddress().getAddress().toString();
                if(cmutes.contains(uuid) || cmutes.contains(ip)) {
                    if(cmuteCommandsToBlock.isEmpty()) {
                        sendMessage(e.getPlayer(), "§7You're still command-muted! You can't do that!");
                        e.setCancelled(true);
                    } else if(cmuteCommandsToBlock.contains(e.getMessage().split(" ")[0].replaceAll("/", ""))) {
                        sendMessage(e.getPlayer(), "§7You're still command-muted! You can't do that!");
                        e.setCancelled(true);
                    }
                }
            }
        }, this);
    }

    private void registerAdminChat() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new PlayerMapperTask(), 20L);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            @SuppressWarnings("unused")
            public void onPlayerLoginEvent(final PlayerLoginEvent event) {
                if(UserMap.getAdminChatUsers().stream()
                        .filter(a -> a.getUuid().equals(event.getPlayer().getUniqueId()))
                        .count() == 0L && event.getPlayer().hasPermission("control.channels.use")) {
                    UserMap.addUser(event.getPlayer());
                }
            }
        }, this);
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            @SuppressWarnings("unused")
            public void onAsyncChatEvent(final AsyncPlayerChatEvent event) {
                final Optional<AdminChatUser> userOptional = UserMap.getAdminChatUsers().stream()
                        .filter(a -> a.getUuid().equals(event.getPlayer().getUniqueId())).findFirst();

                if(userOptional.isPresent()) {
                    final AdminChatUser user = userOptional.get();
                    if(Bukkit.getPlayer(user.getUuid()).isOnline()
                            && user.isTalkingInChannel()
                            && (Bukkit.getPlayer(user.getUuid()).isOp()
                            || Bukkit.getPlayer(user.getUuid()).hasPermission(user.getCurrentChannel().getPermissionNode()))) {
                        event.setCancelled(true);
                        UserMap.sendMessageToChannel(user.getCurrentChannel(), event.getPlayer().getDisplayName(), event.getMessage());
                    }
                }
            }
        }, this);
    }

    private void readyWelc() {
        lastPlayer = "";
        welcEnabled = getConfig().getBoolean("welc-enabled", true);
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            @SuppressWarnings("unused")
            public void onPlayerLoginEvent(final PlayerLoginEvent e) {
                if(welcEnabled) {
                    lastPlayer = e.getPlayer().getPlayer().getName();
                }
            }
        }, this);
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            @SuppressWarnings("unused")
            public void onAsyncPlayerChatEvent(final AsyncPlayerChatEvent e) {
                if(welcEnabled) {
                    if(e.getMessage().equalsIgnoreCase("welc")) {
                        e.setMessage("Welcome, " + lastPlayer + '!');
                    }
                }
            }
        }, this);
    }
}
