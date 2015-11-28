package me.curlpipesh.control;

import lombok.Getter;
import me.curlpipesh.control.adblock.Adblocker;
import me.curlpipesh.control.adminchat.PlayerMapperTask;
import me.curlpipesh.control.adminchat.UserMap;
import me.curlpipesh.control.commands.*;
import me.curlpipesh.control.db.IPunishmentDB;
import me.curlpipesh.control.db.PunishmentDB;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.control.punishment.Punishments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author audrey
 * @since 8/23/15.
 */
public class Control extends JavaPlugin {
    @Getter
    private final IPunishmentDB activePunishments = new PunishmentDB(this, "active_punishments");

    @Getter
    private final IPunishmentDB inactivePunishments = new PunishmentDB(this, "inactive_punishments");

    @Getter
    private List<String> mutes = new CopyOnWriteArrayList<>();

    @Getter
    private List<String> cmutes = new CopyOnWriteArrayList<>();

    @Getter
    private List<String> bans = new CopyOnWriteArrayList<>();

    @Getter
    private List<String> ipBans = new CopyOnWriteArrayList<>();

    /**
     * DO NOT EVER CHANGE THIS FOR ANY REASON
     */
    @Getter
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Getter
    private String chatPrefix;

    @Getter
    private String chatHeader;

    public void onEnable() {
        if(!this.getDataFolder().exists()) {
            getLogger().info("Data folder doesn't exist, making...");
            if(this.getDataFolder().mkdir()) {
                getLogger().info("Data folder made!");
            }
        }
        getLogger().info("Saving default config...");
        saveDefaultConfig();
        chatPrefix = getConfig().getString("chat-prefix");
        chatHeader = getConfig().getString("chat-header");
        prepDBs();
        scheduleCleanupTask();
        registerEventBlockers();
        registerAdminChat();
        fixSignHack();

        // Utility commands
        getCommand("audit").setExecutor(new CommandAudit(this));
        getCommand("clearchat").setExecutor(new CommandClearChat(this));
        getCommand("cc").setExecutor(new CommandClearChat(this));
        getCommand("history").setExecutor(new CommandHistory(this));
        // Punishment commands
        getCommand("ban").setExecutor(new GenericPunishmentCommand(this, Punishments.BAN));
        getCommand("banip").setExecutor(new GenericPunishmentCommand(this, Punishments.IP_BAN));
        getCommand("cmute").setExecutor(new GenericPunishmentCommand(this, Punishments.COMMAND_MUTE));
        getCommand("mute").setExecutor(new GenericPunishmentCommand(this, Punishments.MUTE));
        getCommand("kick").setExecutor(new CommandKick(this));
        // Undo commands
        getCommand("unban").setExecutor(new GenericPunishmentCommand(this, Punishments.BAN, true));
        getCommand("unbanip").setExecutor(new GenericPunishmentCommand(this, Punishments.IP_BAN, true));
        getCommand("uncmute").setExecutor(new GenericPunishmentCommand(this, Punishments.COMMAND_MUTE, true));
        getCommand("unmute").setExecutor(new GenericPunishmentCommand(this, Punishments.MUTE, true));
        // Warning commands
        getCommand("warn").setExecutor(new CommandWarn(this));
        getCommand("warns").setExecutor(new CommandWarns(this));
        // Adminchat
        getCommand("a").setExecutor(new CommandA(this));
        // Other commands
        getCommand("o").setExecutor(new CommandOnline(this));
        getCommand("ops").setExecutor(new CommandOps(this));
    }

    public void onDisable() {
        if(activePunishments.disconnect() && inactivePunishments.disconnect()) {
            getLogger().info("Successfully disconnected from the databases!");
        } else {
            throw new IllegalStateException("Unable to disconnect from the databases!");
        }
    }

    public void sendMessage(CommandSender commandSender, String... message) {
        for(String e : message) {
            commandSender.sendMessage(String.format("%s %s", chatPrefix, e));
        }
    }

    public void sendImportantMessage(CommandSender commandSender, String... message) {
        commandSender.sendMessage(String.format("%s%s%s", chatHeader, chatPrefix, chatHeader));
        for(String e : message) {
            commandSender.sendMessage(e);
        }
        commandSender.sendMessage(String.format("%s%s%s", chatHeader, chatPrefix, chatHeader));
    }

    public void broadcastMessage(String... message) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            sendMessage(player, message);
        }
        sendMessage(Bukkit.getConsoleSender(), message);
    }

    public void broadcastImportantMessage(String... message) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            sendImportantMessage(player, message);
        }
        sendImportantMessage(Bukkit.getConsoleSender(), message);
    }

    private void prepDBs() {
        if(activePunishments.connect() && inactivePunishments.connect()) {
            getLogger().info("Connected to the databases!");
            if(activePunishments.initialize() && inactivePunishments.initialize()) {
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
                    getLogger().warning("I don't know what \"" + p.getType() + "\" warning type is?");
                    break;
            }
        });
    }

    private void scheduleCleanupTask() {
        // Checks every second because fuck TPS I guess? It's not like there's gonna be millions of rows selected...
        // I hope (. _ . )
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> activePunishments.getExpiredPunishments().forEach(p -> {
            getLogger().info("Removing inactive punishment: " + p.toString());
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
        }), 0L, 20L);
    }

    private void registerEventBlockers() {
        Bukkit.getPluginManager().registerEvents(new Adblocker(this), this);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onPlayerChatEvent(AsyncPlayerChatEvent e) {
                String uuid = e.getPlayer().getUniqueId().toString();
                String ip = e.getPlayer().getAddress().getAddress().toString();
                if(mutes.contains(uuid) || mutes.contains(ip)) {
                    sendMessage(e.getPlayer(), "§7You're still muted! You can't talk!");
                    e.setCancelled(true);
                }
            }
        }, this);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
                if(bans.contains(e.getUniqueId().toString())) {
                    List<Punishment> p = activePunishments.getPunishments(e.getUniqueId().toString());
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "§4Banned§r: " + p.get(p.size() - 1).getReason() + "\n\nExpires: " + p.get(p.size() - 1).getEnd());
                }
                if(ipBans.contains(e.getAddress().toString().replaceAll("/", ""))) {
                    List<Punishment> p = activePunishments.getPunishments(e.getAddress().toString().replaceAll("/", ""));
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "§4Banned§r: " + p.get(p.size() - 1).getReason() + "\n\nExpires: " + p.get(p.size() - 1).getEnd());
                }
            }
        }, this);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            // So it turns out that this actually can't be done by listening for chat events
            // I guess Bukkit does it that way because reasons or something
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
                String uuid = e.getPlayer().getUniqueId().toString();
                String ip = e.getPlayer().getAddress().getAddress().toString();
                if(cmutes.contains(uuid) || cmutes.contains(ip)) {
                    e.setCancelled(true);
                }
            }
        }, this);
    }

    private void registerAdminChat() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new PlayerMapperTask(), 20L * 50L);
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
        this.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            @SuppressWarnings("unused")
            public void onAsyncChatEvent(final AsyncPlayerChatEvent event) {
                final Optional<UserMap.AdminChatUser> userOptional = UserMap.getAdminChatUsers().stream()
                        .filter(a -> a.getUuid().equals(event.getPlayer().getUniqueId())).findFirst();

                if (userOptional.isPresent()) {
                    final UserMap.AdminChatUser user = userOptional.get();
                    if (Bukkit.getPlayer(user.getUuid()).isOnline()
                            && user.isTalkingInChannel()
                            && (Bukkit.getPlayer(user.getUuid()).isOp()
                                || Bukkit.getPlayer(user.getUuid()).hasPermission(user.getCurrentChannel().getPermissionNode()))) {
                        event.setCancelled(true);
                        UserMap.sendMessageToChannel(user.getCurrentChannel(), event.getPlayer().getDisplayName(), event.getMessage());
                    }
                }
            }
        }, this);
        // TODO: /a, /channel
    }

    /**
     * Blocks the sign-related exploit that was used in <1.8.7 to crash
     * clients/servers
     */
    private void fixSignHack() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onChunkLoad(ChunkLoadEvent event) {
                for(final BlockState state : event.getChunk().getTileEntities()) {
                    if(!(state instanceof Sign)) {
                        continue;
                    }

                    final Sign sign = (Sign) state;

                    for(int x = 0; x < sign.getLines().length; x++) {
                        // if the line isn't super long, move on
                        if(!(sign.getLine(x).length() > 25)) {
                            continue;
                        }

                        // otherwise, replace the long string with "???"
                        sign.setLine(x, "???");

                        // Force-update the sign, without calling a physics update
                        // UNSURE IF NEEDED //
                        sign.update(true, false);
                        Bukkit.getOperators().stream().filter(OfflinePlayer::isOnline)
                                .forEach(o -> o.getPlayer().sendMessage(String.format("§c###§7 Removed signhack at [%s] (%s, %s, %s) §c###§r",
                                        event.getWorld().getName(), sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ())));
                    }
                }
            }
        }, this);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void blockSignHack(SignChangeEvent sign) {
                for(int x = 0; x < sign.getLines().length; x++) {
                    // if the line isn't super long, move on
                    if(!(sign.getLine(x).length() > 25)) {
                        continue;
                    }

                    // otherwise, replace the long string with "???"
                    sign.setLine(x, "???");
                    //sign.setCancelled(true);

                    Bukkit.getOperators().stream().filter(OfflinePlayer::isOnline)
                            .forEach(o -> o.getPlayer().sendMessage(String.format("§c###§7 Blocked signhack at [%s] (%s, %s, %s) by §c%s ###§r",
                                    sign.getPlayer().getWorld().getName(), sign.getPlayer().getLocation().getBlockX(),
                                    sign.getPlayer().getLocation().getBlockY(), sign.getPlayer().getLocation().getBlockZ(),
                                    sign.getPlayer().getName())));
                    /*Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            String.format("kick %s Attempting to place a crashy sign", sign.getPlayer().getName()));*/
                }
            }
        }, this);
    }
}
