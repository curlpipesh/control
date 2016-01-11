package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.control.punishment.Punishment.PunishmentType;
import me.curlpipesh.control.util.TimeUtil;
import me.curlpipesh.users.SkirtsUser;
import me.curlpipesh.users.Users;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sun.net.util.IPAddressUtil;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * This class is bad and I should feel bad.
 *
 * @author audrey
 * @since 8/26/15.
 */
@SuppressWarnings({"unused", "Duplicates"})
public class GenericPunishmentCommand extends CCommand {
    /**
     * The type of punishment that this command issues
     */
    private final PunishmentType type;

    /**
     * Whether or not this command is intended to be used for punishing entire
     * IPs
     */
    private final boolean punishIP;

    /**
     * Whether or not this command is to undo commands
     */
    private final boolean isUndo;

    /**
     * Format string for ban kick messages
     */
    private final String banFormatString;

    /**
     * Nothing to be undone!
     */
    private final String noUndoString;

    /**
     * Target successfully unpunished.
     */
    private final String unpunishSuccessString;

    /**
     * IDK who that is ;-;
     */
    private final String invalidTargetString;

    /**
     * Creates a non-undo command
     *
     * @param control Plugin instance
     * @param type    Punishment type
     */
    public GenericPunishmentCommand(final Control control, final PunishmentType type) {
        this(control, type, false);
    }

    /**
     * Creates a command that may or may not be undo
     *
     * @param control Plugin instance
     * @param type    Punishment type
     * @param isUndo  Whether or not the command undoes a punishment
     */
    public GenericPunishmentCommand(final Control control, final PunishmentType type, final boolean isUndo) {
        super(control);
        this.type = type;
        this.isUndo = isUndo;
        punishIP = type == PunishmentType.IP_BAN;
        banFormatString = control.getConfig().getString("ban-message");
        noUndoString = control.getConfig().getString("no-undo");
        unpunishSuccessString = control.getConfig().getString("unpunish-success");
        invalidTargetString = control.getConfig().getString("invalid-target");
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        // If target is specified
        if(args.length >= 1) {
            final String target = args[0];
            final Optional<SkirtsUser> skirtsUser;
            int time = Integer.MAX_VALUE;
            // Whether or not we have a time specified
            boolean t = false;
            // Try to parse out a time length
            if(args.length > 1) {
                if(TimeUtil.isValidTime(args[1])) {
                    time = TimeUtil.parseTimeIntoMinutes(args[1]);
                    t = true;
                }
            }

            // If no reason is provided, use the type as the reason
            String reason = "§c" + type.getType() + "§7";
            // Otherwise, implode the rest of the array
            if(t) {
                if(args.length > 2) {
                    reason = "";
                    for(int i = 2; i < args.length; i++) {
                        reason += args[i] + ' ';
                    }
                    reason = reason.trim();
                }
            } else {
                if(args.length > 1) {
                    reason = "";
                    for(int i = 1; i < args.length; i++) {
                        reason += args[i] + ' ';
                    }
                    reason = reason.trim();
                }
            }
            // Try to find the user specified. Doesn't work if they have never logged on
            skirtsUser = Users.getInstance().getSkirtsUserMap().getUserByName(target);
            if(skirtsUser.isPresent()) {
                final String finalTarget;

                // If we're punishing an entire IP, we need to make the final target be the
                // old target's IP
                if(punishIP) {
                    finalTarget = skirtsUser.get().getIp().toString().replaceAll("/", "");
                } else {
                    finalTarget = skirtsUser.get().getUuid().toString();
                }

                if(isUndo) {
                    unpunish(finalTarget, commandSender, skirtsUser.get());
                    return true;
                } else {
                    // Insert punishment with required data
                    final Optional<Punishment> p = getControl().getActivePunishments()
                            .insertPunishment(type,
                                    commandSender instanceof Player
                                            ? ((Player) commandSender).getUniqueId().toString() : "Console",
                                    finalTarget, reason, time);

                    // Kick players as needed
                    if(type == PunishmentType.BAN) {
                        kickForBan(Bukkit.getPlayer(skirtsUser.get().getUuid()), formatBan(reason,
                                t ? TimeUtil.english(args[1]) : "Forever",
                                p.isPresent() ? new Date(p.get().getEnd()).toString() : "Some point in the future"),
                                () -> Bukkit.getOfflinePlayer(skirtsUser.get().getUuid()).isOnline());
                    } else if(punishIP) {
                        for(final Player player : Bukkit.getOnlinePlayers()) {
                            kickForBan(player, formatBan(reason,
                                    t ? TimeUtil.english(args[1]) : "Forever",
                                    p.isPresent() ? new Date(p.get().getEnd()).toString() : "Some point in the future"),
                                    () -> player.getAddress().getAddress().toString().replaceAll("/", "").equals(finalTarget));
                        }
                    }

                    // Do other handling
                    handlePunishment(type, finalTarget);
                    // Announce the punishment to the server. No silent punishing!
                    announcePunishment(commandSender.getName(), punishIP ? hideIP(finalTarget) : skirtsUser.get().getLastName(), type, reason, t ? args[1] : "" + time);
                }
            } else if(IPAddressUtil.isIPv4LiteralAddress(target.replaceFirst("/", ""))) {
                // Like above, just using IPs instead of players
                if(isUndo) {
                    unpunish(target, commandSender, null);
                    return true;
                } else {
                    final Optional<Punishment> p = getControl().getActivePunishments()
                            .insertPunishment(type,
                                    commandSender instanceof Player ? ((Player) commandSender).getUniqueId().toString() : "Console",
                                    target, reason, time);
                    if(type == PunishmentType.BAN || type == PunishmentType.IP_BAN) {
                        for(final Player player : Bukkit.getOnlinePlayers()) {
                            kickForBan(player, formatBan(reason,
                                    t ? TimeUtil.english(args[1]) : "Forever",
                                    p.isPresent() ? new Date(p.get().getEnd()).toString() : "Some point in the future"),
                                    () -> player.getAddress().getAddress().toString().replaceAll("/", "").equals(target));
                        }
                    }
                    handlePunishment(type, target);
                    announcePunishment(commandSender.getName(), hideIP(target), type, reason, t ? args[1] : "" + time);
                }
            } else {
                // That's not a target D:
                commandSender.sendMessage(invalidTargetString.replaceAll("<name>", args[0]));
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unpunishes the given target
     *
     * @param target Target to unpunish
     * @param commandSender Person doing the unpunish
     * @param skirtsUser TODO
     */
    private void unpunish(final String target, final CommandSender commandSender, final SkirtsUser skirtsUser) {
        final List<Punishment> activePunishments = getControl().getActivePunishments().getPunishments(target);
        if(activePunishments.isEmpty()) {
            getControl().sendMessage(commandSender, noUndoString);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(getControl(), () -> {
                final Punishment p = activePunishments.get(activePunishments.size() - 1);
                getControl().getActivePunishments().removePunishment(p);
                getControl().getInactivePunishments().insertPunishment(p);
                handlePunishment(type, target);
                getControl().sendMessage(commandSender,
                        formatUnpunish(punishIP ? hideIP(p.getTarget()) : skirtsUser != null ? skirtsUser.getLastName()
                                : target, p.getType()));
            }, 0L);
        }
    }

    /**
     * Hides the last two numbers in an IP to prevent problems.
     *
     * @param ip Probably the IP that we're hiding, unless I screwed up
     *           somewhere
     * @return Censored IP
     */
    private String hideIP(final String ip) {
        return ip.trim().replaceFirst("\\.[0-9]{1,3}\\.[0-9]{1,3}$", ".XXX.XXX");
    }

    /**
     * Format a ban with the given info
     *
     * @param reason Reason for ban
     * @param time Length of ban
     * @param end End of ban
     * @return Formatted ban
     */
    private String formatBan(final String reason, final String time, final String end) {
        return banFormatString.replaceAll("<reason>", reason).replaceAll("<time>", time).replaceAll("<end>", end);
    }

    /**
     * Formats an unpunish or something
     *
     * @param name User being unpunished
     * @param type Type of punishment
     * @return Formatted unpunish
     */
    private String formatUnpunish(final String name, final PunishmentType type) {
        return unpunishSuccessString.replaceAll("<name>", name)
                .replaceAll("<punishment>", PunishmentType.english(type));
    }

    /**
     * Kicks user if supplied condition evaluates to true
     *
     * @param player Player to kick
     * @param message Message to kick with
     * @param condition Condition that needs to be satisfied before the kick is done
     */
    private void kickForBan(final Player player, final String message, final BooleanSupplier condition) {
        if(condition.getAsBoolean()) {
            player.kickPlayer(message);
        }
    }

    /**
     * Insert active punishments into the lists in {@link Control}
     *
     * @param type Punishment type
     * @param target Punishment target
     */
    private void handlePunishment(final PunishmentType type, final String target) {
        switch(type) {
            case COMMAND_MUTE:
                if(isUndo) {
                    getControl().getCmutes().remove(target);
                } else {
                    getControl().getCmutes().add(target);
                }
                break;
            case MUTE:
                if(isUndo) {
                    getControl().getMutes().remove(target);
                } else {
                    getControl().getMutes().add(target);
                }
                break;
            case BAN:
                if(isUndo) {
                    getControl().getBans().remove(target);
                } else {
                    getControl().getBans().add(target);
                }
                break;
            case IP_BAN:
                if(isUndo) {
                    getControl().getIpBans().remove(target);
                } else {
                    getControl().getIpBans().add(target);
                }
                break;
            default:
                break;
        }
    }
}
