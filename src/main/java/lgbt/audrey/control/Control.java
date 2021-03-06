package lgbt.audrey.control;

import lgbt.audrey.users.Users;
import lgbt.audrey.users.user.AudreyUser;
import lgbt.audrey.util.command.AudreyCommand;
import lgbt.audrey.util.plugin.AudreyPlugin;
import lombok.Getter;
import lgbt.audrey.control.adblock.Adblocker;
import lgbt.audrey.control.adminchat.PlayerMapperTask;
import lgbt.audrey.control.adminchat.UserMap;
import lgbt.audrey.control.adminchat.UserMap.AdminChatUser;
import lgbt.audrey.control.commands.*;
import lgbt.audrey.control.db.IPunishmentDB;
import lgbt.audrey.control.db.IPunishmentDB.DBMode;
import lgbt.audrey.control.db.PunishmentDB;
import lgbt.audrey.control.punishment.Punishment;
import lgbt.audrey.control.punishment.Punishment.PunishmentType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Main class of the plugin. Is filled with a bazillion event listeners and
 * other such nonsenses. It really really really needs to be cleaned up but so
 * far I've been too lazy to do that.
 * <p>
 * Punishments are stored in two separate databases, because I was too lazy to
 * make it store them all in one database with a flag to indicate whether or
 * not the specific punishment is active. All active punishments are
 * <code>SELECT</code>ed from the active punishments database once every
 * minute. Hopefully this doesn't kill performance when there's thousands of
 * active punishments for a server.
 * <p>
 * Continuing with the trend of odd design choices, active
 * mutes/command-mutes/bans/IP-bans are stored in lists of Strings that hold
 * IPs or UUIDs. Again, I'm not sure why I thought that this was a good idea.
 * This is probably gonna change in the future when I figure out a better way
 * to handle this, probably by just storing actual {@link Punishment}s.
 * <p>
 * <b>TL;DR:</b> You're on your own. Good luck.
 * <p>
 * TODO: Make active punishments store more than just UUIDs/IPs
 * TODO: Make punishments be one DB with a flag?
 * TODO: MySQL support for databases
 * TODO: Holy fuck this class is complicated
 * TODO: Collect event handlers into a single class
 *
 * @author audrey
 * @since 8/23/15.
 */
public class Control extends AudreyPlugin {
    /**
     * Database that stores the active punishments.
     */
    @Getter
    private final IPunishmentDB activePunishments;

    /**
     * Database that stores inactive punishments
     */
    @Getter
    private final IPunishmentDB inactivePunishments;

    /**
     * All mutes that are currently active. May be UUIDs or IPs.
     */
    @Getter
    private final Collection<String> mutes = new CopyOnWriteArrayList<>();

    /**
     * All command-mutes that are currently active. May be UUIDs or IPs.
     */
    @Getter
    private final Collection<String> cmutes = new CopyOnWriteArrayList<>();

    /**
     * All bans that are currently active.
     */
    @Getter
    private final Collection<String> bans = new CopyOnWriteArrayList<>();

    /**
     * All IP-bans that are currently active.
     */
    @Getter
    private final Collection<String> ipBans = new CopyOnWriteArrayList<>();

    /**
     * The prefix for chat messages. For instance, when a command sends a
     * single message to a player, it will be used.
     * <p>
     * Example:
     * <p>
     * "<code>[Control] This is a message!</code>", where "<tt>[Control]</tt>" is the prefix.
     */
    @Getter
    private String chatPrefix;

    /**
     * The header (but also footer) used for important chat messages or
     * broadcasts.
     * <p>
     * Example:
     * <p>
     * <code>
     * --------[Control]--------<br />
     * This is an extremely     <br />
     * important message!       <br />
     * :O                       <br />
     * --------[Control]--------<br />
     * </code>
     */
    @Getter
    private String chatHeader;

    /**
     * The last player to join. Used for the 'welc' functionality.
     * <p>
     * TODO: Extract to another class?
     */
    private String lastPlayer = "";

    /**
     * Whether or not the 'welc' functionality is enabled.
     * <p>
     * TODO: Extract to another class?
     */
    @SuppressWarnings("FieldCanBeLocal")
    private boolean welcEnabled = true;

    /**
     * Commands that a command-mute will block. This is a list read in from
     * <code>config.yml</code>.
     */
    private List<String> cmuteCommandsToBlock;

    /**
     * Whether or not private messages should be blocked when a player is
     * muted. This is to prevent players from continuing to spam/advertise/etc.
     * while they are muted.
     */
    private boolean blockPmsWhenMuted = true;

    /**
     * Whether or not communication over the network (utilizing a
     * non-Bukkit/Spigot/Bungeecord API) is to be used. The intended use-case
     * for this is for communicating punishments across a server network, as
     * frankly the existing APIs for this all suck (as evidenced by the fact
     * that some guy on Bukkit forums wrote an API/implementation specifically
     * to get around it...).
     */
    @Getter
    private boolean isNetworkEnabled;

    /**
     * List of all commands known to be usable for private messaging/spamming.
     * Taken from Essentials. There may be others that aren't listed.
     */
    private final List<String> pmCommands = Arrays.asList(
            "msg", "w", "m", "t", "pm", "emsg", "epm", "tell", "etell", "whisper", "ewhisper", "r", "er", "reply",
            "ereply", "helpop", "ac", "eac", "amsg", "eamsg", "ehelpop", "mail", "email", "eemail", "memo", "ememo"
    );

    public Control() {
        final String sqlmode = getConfig().getString("sql-mode");
        if(sqlmode.equalsIgnoreCase("sqlite")) {
            activePunishments = new PunishmentDB(this, "active_punishments", DBMode.SQLITE);
            inactivePunishments = new PunishmentDB(this, "inactive_punishments", DBMode.SQLITE);
        }/* else if(sqlmode.equalsIgnoreCase("mysql")) { // TODO: Implement MySQL mode
            activePunishments = new PunishmentDB(this, "active_punishments", PunishmentDB.DBMode.MYSQL);
            inactivePunishments = new PunishmentDB(this, "inactive_punishments", PunishmentDB.DBMode.MYSQL);
        } */ else {
            throw new IllegalArgumentException('\'' + sqlmode + "' is not a valid SQL mode!");
        }
    }

    public void onEnable() {
        // TODO: Why?
        if(!getDataFolder().exists()) {
            getLogger().info("Data folder doesn't exist, making...");
            if(getDataFolder().mkdir()) {
                getLogger().info("Data folder made!");
            }
        }
        // Save default config if it doesn't exist, and update existing config
        // files with new options, if applicable
        getLogger().info("Saving default config...");
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        // Load config values
        chatPrefix = getConfig().getString("chat-prefix");
        chatHeader = getConfig().getString("chat-header");
        cmuteCommandsToBlock = getConfig().getStringList("cmute-blocked-cmds");
        blockPmsWhenMuted = getConfig().getBoolean("block-pms-when-muted");
        prepDBs();
        scheduleCleanupTask();
        registerEventBlockers();
        registerAdminChat();
        readyWelc();

        // Register commands. This is ugly, I know. It works, and avoids plugin.yml.

        // Utility commands
        getCommandManager().registerCommand(AudreyCommand.builder().setName("audit")
                .setDescription("Show all punishments issued by a player").setUsage("/audit <player>")
                .setPermissionNode("control.audit").setExecutor(new CommandAudit(this)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("clearchat").addAlias("cc")
                .setDescription("Clears the chat").setUsage("/clearchat").setPermissionNode("control.clearchat")
                .setExecutor(new CommandClearChat(this)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("history")
                .setDescription("Show all punishments applied to a player").setUsage("/history <player>")
                .setPermissionNode("control.history").setExecutor(new CommandHistory(this)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("info").addAlias("specs")
                .setDescription("Show information about the hardware/software stack the server is running on")
                .setUsage("/info").setPermissionNode("control.info").setExecutor(new CommandInfo(this))
                .setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("o").addAlias("online")
                .setDescription("Show the number of users online").setUsage("/o").setPermissionNode("control.online")
                .setExecutor(new CommandOnline(this)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("ops")
                .setDescription("Show all server ops").setUsage("/ops").setPermissionNode("control.ops")
                .setExecutor(new CommandOps(this)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("plgrep").addAlias("plugingrep")
                .setDescription("Grep for a string in the plugin list. Not regex").setUsage("/plgrep <string>")
                .setPermissionNode("control.plgrep").setExecutor(new CommandPlgrep(this)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("warns")
                .setDescription("Show all warnings for a player").setUsage("/warns <player>")
                .setPermissionNode("control.warns").setExecutor(new CommandWarns(this)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("alts")
                .setDescription("Show all possible alts for a player").setUsage("/alts <player>")
                .setPermissionNode("control.alts").setExecutor(new CommandAlts(this)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("disconnect").addAlias("dc")
                .setDescription("Silently disconnect a player").setUsage("/disconnect <player>")
                .setPermissionNode("control.disconnect").setExecutor(new CommandDisconnect(this))
                .setPlugin(this).build());

        // Punishment commands
        getCommandManager().registerCommand(AudreyCommand.builder().setName("ban").addAlias("banplayer")
                .setDescription("Ban a player, for minutes, hours, days, or weeks")
                .setUsage("/ban <player> [[t:]length<m|h|d|w>] [reason]").setPermissionNode("control.ban")
                .setExecutor(new GenericPunishmentCommand(this, PunishmentType.BAN)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("banip").addAlias("ipban")
                .setDescription("Ban an IP, by IP or player, for minutes, hours, days, or weeks")
                .setUsage("/ban <player|IP> [[t:]length<m|h|d|w>] [reason]")
                .setPermissionNode("control.banip")
                .setExecutor(new GenericPunishmentCommand(this, PunishmentType.IP_BAN)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("cmute")
                .setDescription("Prevents a player from using commands")
                .setUsage("/cmute <player> [[t:]length<m|h|d|w>] [reason]").setPermissionNode("control.cmute")
                .setExecutor(new GenericPunishmentCommand(this, PunishmentType.COMMAND_MUTE)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("mute")
                .setDescription("Prevents a player from sending chat messages")
                .setUsage("/mute <player> [[t:]length<m|h|d|w>] [reason]")
                .setPermissionNode("control.mute").setExecutor(new GenericPunishmentCommand(this, PunishmentType.MUTE))
                .setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("kick")
                .setDescription("Kicks a player from the server").setUsage("/kick <player> [reason]")
                .setPermissionNode("control.kick").setExecutor(new CommandKick(this)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("warn")
                .setDescription("Issue a warning to a player").setUsage("/warn <player> [reason]")
                .setPermissionNode("control.warn").setExecutor(new CommandWarn(this)).setPlugin(this).build());

        // Undo commands
        getCommandManager().registerCommand(AudreyCommand.builder().setName("unban").addAlias("unbanplayer")
                .setDescription("Unban a player").setUsage("/unban <player>").setPermissionNode("control.unban")
                .setExecutor(new GenericPunishmentCommand(this, PunishmentType.BAN, true)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("unbanip").addAlias("unipban")
                .setDescription("Unban an IP by player or IP").setUsage("/ban <player|IP>")
                .setPermissionNode("control.unbanip")
                .setExecutor(new GenericPunishmentCommand(this, PunishmentType.IP_BAN, true)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("uncmute")
                .setDescription("Un-command-mute a player").setUsage("/uncmute <player>")
                .setPermissionNode("control.uncmute")
                .setExecutor(new GenericPunishmentCommand(this, PunishmentType.COMMAND_MUTE, true)).setPlugin(this).build());

        getCommandManager().registerCommand(AudreyCommand.builder().setName("unmute")
                .setDescription("Unmute a player").setUsage("/unmute <player>").setPermissionNode("control.unmute")
                .setExecutor(new GenericPunishmentCommand(this, PunishmentType.MUTE, true)).setPlugin(this).build());

        // Admin chat
        getCommandManager().registerCommand(AudreyCommand.builder().setName("a").addAlias("adminchat").addAlias("ac")
                .setDescription("Enter admin chat, or send a message to the channel").setUsage("/a [message]")
                .setPermissionNode("control.channels.use").setExecutor(new CommandA(this)).setPlugin(this).build());
    }

    public void onDisable() {
        if(activePunishments.getDatabaseBackend().disconnect() && inactivePunishments.getDatabaseBackend().disconnect()) {
            getLogger().info("Successfully disconnected from the databases!");
        } else {
            throw new IllegalStateException("Unable to disconnect from the databases!");
        }
    }

    /**
     * Send a message or series of messages to a {@link CommandSender}. This
     * can be used to send messages to players, but can also be used to send
     * formatted messages to the console.
     * <p>
     * Messages sent with this will have {@link #chatPrefix} appended to the
     * beginning of each message in the series.
     *
     * @param commandSender The receiver of the messages
     * @param message       The message or series of messages to be sent
     */
    public void sendMessage(final CommandSender commandSender, final String... message) {
        for(final String e : message) {
            commandSender.sendMessage(String.format("%s %s", chatPrefix, e));
        }
    }

    /**
     * Send an important message or series of important messages to a
     * {@link CommandSender}. May be used for the same purposes as
     * {@link #sendMessage(CommandSender, String...)}.
     * <p>
     * Messages sent with this will be sent in the following order:
     * <code>
     * {@link #chatHeader}<br />
     * &lt;series of messages&gt;<br />
     * {@link #chatHeader}
     * </code>
     *
     * @param commandSender The receiver of the messages
     * @param message       The message or series of messages to be sent
     */
    public void sendImportantMessage(final CommandSender commandSender, final String... message) {
        commandSender.sendMessage(chatHeader);
        for(final String e : message) {
            commandSender.sendMessage(e);
        }
        commandSender.sendMessage(chatHeader);
    }

    /**
     * Broadcast a message to the entire server using
     * {@link #sendMessage(CommandSender, String...)}.
     *
     * @param message The message or series of messages to be sent
     */
    public void broadcastMessage(final String... message) {
        for(final Player player : Bukkit.getOnlinePlayers()) {
            sendMessage(player, message);
        }
        sendMessage(Bukkit.getConsoleSender(), message);
    }

    /**
     * Broadcast a message to the entire server using
     * {@link #sendImportantMessage(CommandSender, String...)}. Generally used
     * for announcing punishments.
     *
     * @param message The message or series of messages to be sent.
     */
    public void broadcastImportantMessage(final String... message) {
        for(final Player player : Bukkit.getOnlinePlayers()) {
            sendImportantMessage(player, message);
        }
        sendImportantMessage(Bukkit.getConsoleSender(), message);
    }

    /**
     * Broadcast an important message to every player with the given perm. The
     * second parameter is a <tt>String[]</tt> instead of a <tt>String...</tt>
     * in order to avoid ambiguous method call problems.
     *
     * @param perm The permission for broadcast reception
     * @param message The message or messages to broadcast
     */
    public void broadcastImportantMessageWithPerm(final String perm, final String[] message) {
        if(perm.isEmpty()) {
            broadcastImportantMessage(message);
            return;
        }
        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission(perm))
                .forEach(player -> sendImportantMessage(player, message));
        sendImportantMessage(Bukkit.getConsoleSender(), message);
    }

    /**
     * Connects to the databases and calls initialization methods.
     *
     * @throws IllegalStateException If the connection or initialization to the
     *                               databases fails for any reason.
     */
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
                Bukkit.getPluginManager().disablePlugin(this);
                throw new IllegalStateException("Unable to initialise databases!");
            }
        } else {
            Bukkit.getPluginManager().disablePlugin(this);
            throw new IllegalStateException("Unable to connect to the databases!");
        }
    }

    /**
     * Loads all active punishments out of the databases
     */
    private void loadPunishments() {
        activePunishments.getAllPunishments().forEach(p -> {
            switch(p.getType()) {
                case BAN:
                    bans.add(p.getTarget());
                    break;
                case COMMAND_MUTE:
                    cmutes.add(p.getTarget());
                    break;
                case MUTE:
                    mutes.add(p.getTarget());
                    break;
                case IP_BAN:
                    ipBans.add(p.getTarget());
                    break;
                case WARN:
                    break;
                default:
                    getLogger().warning("I don't know what \"" + p.getType() + "\" punishment type is?");
                    break;
            }
        });
    }

    /**
     * Cleans up inactive punishments once every minute. Warnings do not ever get removed.
     * <p>
     * TODO: Actually make an unwarn?
     */
    private void scheduleCleanupTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> activePunishments.getExpiredPunishments().forEach(p -> {
            getLogger().info("Removing inactive punishment: " + p);
            activePunishments.removePunishment(p);
            switch(p.getType()) {
                case BAN:
                    bans.remove(p.getTarget());
                    break;
                case COMMAND_MUTE:
                    cmutes.remove(p.getTarget());
                    break;
                case MUTE:
                    mutes.remove(p.getTarget());
                    break;
                case IP_BAN:
                    ipBans.remove(p.getTarget());
                    break;
                default:
                    break;
            }
            inactivePunishments.insertPunishment(p);
        }), 0L, 20L * 60L);
    }

    /**
     * Register event listeners to block chat messages for muted players,
     * commands for command-muted players, login for banned players, ...
     * <p>
     * TODO: Cleaner?
     */
    private void registerEventBlockers() {
        if(getConfig().getBoolean("adblock-enabled")) {
            Bukkit.getPluginManager().registerEvents(new Adblocker(this), this);
        }
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent e) {
                if(bans.contains(e.getUniqueId().toString())) {
                    final List<Punishment> p = activePunishments.getPunishments(e.getUniqueId().toString());
                    final String end = p.get(p.size() - 1).getEnd() == Integer.MAX_VALUE ? "The end of time"
                            : new Date(p.get(p.size() - 1).getEnd()).toString();
                    e.disallow(Result.KICK_BANNED, "§4Banned§r: " + p.get(p.size() - 1).getReason() +
                            "\n\nExpires: " + end);
                    return;
                }
                if(ipBans.contains(e.getAddress().toString().replaceAll("/", ""))) {
                    final List<Punishment> p = activePunishments.getPunishments(e.getAddress().toString().replaceAll("/", ""));
                    final String end = p.get(p.size() - 1).getEnd() == Integer.MAX_VALUE ? "The end of time"
                            : new Date(p.get(p.size() - 1).getEnd()).toString();
                    e.disallow(Result.KICK_BANNED, "§4Banned§r: " + p.get(p.size() - 1).getReason() +
                            "\n\nExpires: " + end);
                    return;
                }
                final List<String> bannedNames = Users.getInstance().getAudreyUserMap().getAudreyUsers().stream()
                        .filter(audreyUser -> audreyUser.getIp().equals(e.getAddress()))
                        .filter(audreyUser -> bans.contains(audreyUser.getUuid().toString()))
                        .map(AudreyUser::getLastName).collect(Collectors.toList());
                if(!bannedNames.isEmpty()) {
                    String nameString = "";
                    for(int i = 0; i < bannedNames.size(); i++) {
                        nameString += ChatColor.RED + bannedNames.get(i);
                        if(i != bannedNames.size() - 1) {
                            nameString += ChatColor.RESET + ", ";
                        }
                    }
                    nameString += ChatColor.RESET;
                    broadcastImportantMessageWithPerm("control.notify.sameipasbanned", new String[] {
                            ChatColor.RED + "Player " + ChatColor.DARK_RED + e.getName() + ChatColor.RED + " has the " +
                                    "same IP as the following banned players: ",
                            nameString
                    });
                }
            }

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

            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onCommandPreprocess(final PlayerCommandPreprocessEvent e) {
                final String uuid = e.getPlayer().getUniqueId().toString();
                final String ip = e.getPlayer().getAddress().getAddress().toString();
                if(blockPmsWhenMuted) {
                    if(mutes.contains(uuid) || mutes.contains(ip)) {
                        if(pmCommands.contains(e.getMessage().split(" ")[0].replaceAll("/", ""))) {
                            sendMessage(e.getPlayer(), "§7You're still muted! You can't talk!");
                            e.setCancelled(true);
                        }
                    }
                }
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

    /**
     * Register the event listeners required for AdminChat to function.
     */
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
                final String uuid = event.getPlayer().getUniqueId().toString();
                final String ip = event.getPlayer().getAddress().getAddress().toString();
                if(mutes.contains(uuid) || mutes.contains(ip)) {
                    return;
                }
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

    /**
     * Ready the event listeners required for 'welc' functionality to function.
     */
    private void readyWelc() {
        lastPlayer = "";
        welcEnabled = getConfig().getBoolean("welc-enabled", true);
        if(welcEnabled) {
            getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                @SuppressWarnings("unused")
                public void onPlayerLoginEvent(final PlayerLoginEvent e) {
                    lastPlayer = e.getPlayer().getPlayer().getName();
                }
            }, this);
            getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                @SuppressWarnings("unused")
                public void onAsyncPlayerChatEvent(final AsyncPlayerChatEvent e) {
                    if(e.getMessage().equalsIgnoreCase("welc")) {
                        e.setMessage("Welcome, " + lastPlayer + '!');
                    }
                }
            }, this);
        }
    }
}
