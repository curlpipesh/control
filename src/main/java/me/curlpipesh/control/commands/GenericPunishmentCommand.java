package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.control.punishment.Punishments;
import me.curlpipesh.control.util.TimeUtil;
import me.curlpipesh.users.SkirtsUser;
import me.curlpipesh.users.Users;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sun.net.util.IPAddressUtil;

import javax.annotation.Nullable;
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
    private final String type;
    private final boolean punishIP;
    private final boolean isUndo;
    private final String banFormatString;
    private final String noUndoString;
    private final String unpunishSuccessString;
    private final String invalidTargetString;

    /**
     * Creates a non-undo command
     *
     * @param control Plugin instance
     * @param type    Punishment type
     */
    public GenericPunishmentCommand(final Control control, final String type) {
        this(control, type, false);
    }

    /**
     * Creates a command that may or may not be undo
     *
     * @param control Plugin instance
     * @param type    Punishment type
     * @param isUndo  Whether or not the command undoes a punishment
     */
    public GenericPunishmentCommand(final Control control, final String type, final boolean isUndo) {
        super(control);
        this.type = type;
        this.isUndo = isUndo;
        punishIP = type.equals(Punishments.IP_BAN);
        banFormatString = control.getConfig().getString("ban-message");
        noUndoString = control.getConfig().getString("no-undo");
        unpunishSuccessString = control.getConfig().getString("unpunish-success");
        invalidTargetString = control.getConfig().getString("invalid-target");
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if(args.length >= 1) {
            final String target = args[0];
            final Optional<SkirtsUser> skirtsUser;
            int time = Integer.MAX_VALUE;
            boolean t = false;
            if(args.length > 1) {
                if(TimeUtil.isValidTime(args[1])) {
                    time = TimeUtil.parseTimeIntoMinutes(args[1]);
                    t = true;
                }
            }

            String reason = "§c" + type + "§7";
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
            skirtsUser = Users.getInstance().getSkirtsUserMap().getUserByName(target);
            if(skirtsUser.isPresent()) {
                final String finalTarget;

                if(punishIP) {
                    /*if(essUser.get().getIp() != null) {
                        finalTarget = essUser.get().getIp().toString().replaceAll("/", "");
                        // ((User)essUser).getBase().getAddress().getAddress().toString().replaceAll("/", "");
                    } else {
                        finalTarget = essUser.getLastLoginAddress().replaceAll("/", "");
                    }*/
                    finalTarget = skirtsUser.get().getIp().toString().replaceAll("/", "");
                } else {
                    //finalTarget = essUser.getConfigUUID().toString();
                    finalTarget = skirtsUser.get().getUuid().toString();
                }

                if(isUndo) {
                    unpunish(finalTarget, commandSender, skirtsUser.get());
                    return true;
                } else {
                    final Optional<Punishment> p = getControl().getActivePunishments()
                            .insertPunishment(type,
                                    commandSender instanceof Player
                                            ? ((Player) commandSender).getUniqueId().toString() : "Console",
                                    finalTarget, reason, time);

                    if(type.equals(Punishments.BAN)) {
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

                    handlePunishment(type, finalTarget);
                    announcePunishment(commandSender.getName(), punishIP ? hideIP(finalTarget) : skirtsUser.get().getLastName(), type, reason, t ? args[1] : "" + time);
                }
            } else if(IPAddressUtil.isIPv4LiteralAddress(target.replaceFirst("/", ""))) {
                if(isUndo) {
                    unpunish(target, commandSender, null);
                    return true;
                } else {
                    final Optional<Punishment> p = getControl().getActivePunishments()
                            .insertPunishment(type,
                                    commandSender instanceof Player ? ((Player) commandSender).getUniqueId().toString() : "Console",
                                    target, reason, time);
                    if(type.equals(Punishments.BAN) || type.equals(Punishments.IP_BAN)) {
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
                commandSender.sendMessage(invalidTargetString.replaceAll("<name>", args[0]));
            }
            return true;
        } else {
            return false;
        }
    }

    private void unpunish(final String target, final CommandSender commandSender, @Nullable final SkirtsUser essUser) {
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
                        formatUnpunish(punishIP ? hideIP(p.getTarget()) : essUser != null ? essUser.getLastName() : target, p.getType()));
            }, 0L);
        }
    }

    private String hideIP(final String ip) {
        return ip.trim().replaceFirst("\\.[0-9]{1,3}\\.[0-9]{1,3}$", ".XXX.XXX");
    }

    private String formatBan(final String reason, final String time, final String end) {
        return banFormatString.replaceAll("<reason>", reason).replaceAll("<time>", time).replaceAll("<end>", end);
    }

    private String formatUnpunish(final String name, final String type) {
        return unpunishSuccessString.replaceAll("<name>", name)
                .replaceAll("<punishment>", Punishments.english(type));
    }

    private void kickForBan(final Player player, final String message, final BooleanSupplier condition) {
        if(condition.getAsBoolean()) {
            player.kickPlayer(message);
        }
    }

    private void handlePunishment(final String type, final String target) {
        switch(type) {
            case Punishments.COMMAND_MUTE:
                if(isUndo) {
                    getControl().getCmutes().remove(target);
                } else {
                    getControl().getCmutes().add(target);
                }
                break;
            case Punishments.MUTE:
                if(isUndo) {
                    getControl().getMutes().remove(target);
                } else {
                    getControl().getMutes().add(target);
                }
                break;
            case Punishments.BAN:
                if(isUndo) {
                    getControl().getBans().remove(target);
                } else {
                    getControl().getBans().add(target);
                }
                break;
            case Punishments.IP_BAN:
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
