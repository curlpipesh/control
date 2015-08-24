package net.spacemc.control;

import lombok.Getter;
import net.spacemc.control.commands.CommandAudit;
import net.spacemc.control.commands.CommandBan;
import net.spacemc.control.commands.CommandHistory;
import net.spacemc.control.db.Database;
import net.spacemc.control.db.SQLiteDB;
import net.spacemc.control.punishment.Punishment;
import net.spacemc.control.punishment.Punishments;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author audrey
 * @since 8/23/15.
 */
public class SpaceControl extends JavaPlugin {
    @Getter
    private final Database activePunishments = new SQLiteDB(this, "active_punishments");

    @Getter
    private final Database inactivePunishments = new SQLiteDB(this, "inactive_punishments");

    @Getter
    private List<UUID> mutes = new CopyOnWriteArrayList<>();

    @Getter
    private List<UUID> cmutes = new CopyOnWriteArrayList<>();

    @Getter
    private List<UUID> bans = new CopyOnWriteArrayList<>();

    @Getter
    private List<String> ipBans = new CopyOnWriteArrayList<>();

    @Getter
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void onEnable() {
        // Connect to dbs
        if(activePunishments.connect() && inactivePunishments.connect()) {
            getLogger().info("Connected to the databases!");
            if(activePunishments.initialize() && inactivePunishments.initialize()) {
                getLogger().info("Initialised databases!");
                // Load active mutes/cmutes/bans
                List<Punishment> all = activePunishments.getPunishmentsByType(Punishments.BAN, Punishments.MUTE, Punishments.COMMAND_MUTE);
                all.stream().forEach(p -> {
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
                        case Punishments.IPBAN:
                            ipBans.add(p.getTargetIP());
                            break;
                        default:
                            getLogger().warning("I don't know what \"" + p.getType() + "\" warning type is?");
                            break;
                    }
                });
            } else {
                getLogger().warning("Unable to initialise databases!");
            }
        } else {
            Bukkit.getPluginManager().disablePlugin(this);
            throw new IllegalStateException("Unable to connect to the databases!");
        }
        // Schedule the task to remove expired punishments and transfer them to the inactive db
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            List<Punishment> expired = activePunishments.getExpiredPunishments();
            expired.stream().forEach(p -> {
                activePunishments.removePunishment(p);
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
                    case Punishments.IPBAN:
                        ipBans.add(p.getTargetIP());
                        break;
                    default:
                        break;
                }
                inactivePunishments.insertPunishment(p);
            });
        }, 0L, 1200L);

        // Register event listener to handle mutes/bans
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onPlayerChatEvent(AsyncPlayerChatEvent e) {
                if(mutes.contains(e.getPlayer().getUniqueId())) {
                    if(!e.getMessage().startsWith("/")) {
                        e.setCancelled(true);
                    }
                }
                if(cmutes.contains(e.getPlayer().getUniqueId())) {
                    if(e.getMessage().startsWith("/")) {
                        e.setCancelled(true);
                    }
                }
            }

            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
                if(bans.contains(e.getUniqueId())) {
                    List<Punishment> p = activePunishments.getPunishmentsForUUID(e.getUniqueId());
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "§4Banned: §r" + p.get(0).getReason() + " \nExpires: " + p.get(0).getEnd());
                }
                if(ipBans.contains(e.getAddress().toString())) {
                    List<Punishment> p = activePunishments.getPunishmentsForIP(e.getAddress().toString());
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "§4Banned: §r" + p.get(0).getReason() + " \nExpires: " + p.get(0).getEnd());
                }
            }
        }, this);
        // Set up commands
        getCommand("audit").setExecutor(new CommandAudit(this));
        getCommand("ban").setExecutor(new CommandBan(this));
        getCommand("history").setExecutor(new CommandHistory(this));
    }

    public void onDisable() {
        if(activePunishments.disconnect() && inactivePunishments.disconnect()) {
            getLogger().info("Successfully disconnected from the databases!");
        } else {
            throw new IllegalStateException("Unable to disconnect from the databases!");
        }
    }
}
