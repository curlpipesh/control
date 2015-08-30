package me.curlpipesh.control;

import lombok.Getter;
import me.curlpipesh.control.adblock.Adblocker;
import me.curlpipesh.control.commands.*;
import me.curlpipesh.control.db.IPunishmentDB;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.control.punishment.Punishments;
import me.curlpipesh.control.db.PunishmentDB;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.List;
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
        // Undo commands
        getCommand("unban").setExecutor(new GenericPunishmentCommand(this, Punishments.BAN, true));
        getCommand("unbanip").setExecutor(new GenericPunishmentCommand(this, Punishments.IP_BAN, true));
        getCommand("uncmute").setExecutor(new GenericPunishmentCommand(this, Punishments.COMMAND_MUTE, true));
        getCommand("unmute").setExecutor(new GenericPunishmentCommand(this, Punishments.MUTE, true));
        // Warning commands
        getCommand("warn").setExecutor(new CommandWarn(this));
        getCommand("warns").setExecutor(new CommandWarns(this));
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
        // ???
        // commandSender.sendMessage(String.format("%s%s", chatHeader, chatHeader));
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
                    sendMessage(e.getPlayer(), "You're still muted! You can't talk!");
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
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "§4Banned§r: " + p.get(0).getReason() + "\n\nExpires: " + p.get(0).getEnd());
                }
                if(ipBans.contains(e.getAddress().toString().replaceAll("/", ""))) {
                    List<Punishment> p = activePunishments.getPunishments(e.getAddress().toString().replaceAll("/", ""));
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "§4Banned§r: " + p.get(0).getReason() + "\n\nExpires: " + p.get(0).getEnd());
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
}
