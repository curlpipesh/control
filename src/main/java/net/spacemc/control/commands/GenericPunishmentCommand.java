package net.spacemc.control.commands;

import com.earth2me.essentials.User;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.punishment.Punishment;
import net.spacemc.control.punishment.Punishments;
import net.spacemc.control.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.slacks.space.util.SpaceUtils;
import sun.net.util.IPAddressUtil;

import java.util.List;
import java.util.Optional;

/**
 * @author audrey
 * @since 8/26/15.
 */
@SuppressWarnings("unused")
public class GenericPunishmentCommand extends CCommand {
    private String type;
    private boolean punishIP;
    private boolean isUndo;
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
    public GenericPunishmentCommand(SpaceControl control, String type) {
        this(control, type, false);
    }

    /**
     * Creates a command that may or may not be undo
     *
     * @param control Plugin instance
     * @param type    Punishment type
     * @param isUndo  Whether or not the command undoes a punishment
     */
    public GenericPunishmentCommand(SpaceControl control, String type, boolean isUndo) {
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
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length >= 1) {
            String target = args[0];
            User essUser;
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
                        reason += args[i] + " ";
                    }
                    reason = reason.trim();
                }
            } else {
                if(args.length > 1) {
                    reason = "";
                    for(int i = 1; i < args.length; i++) {
                        reason += args[i] + " ";
                    }
                    reason = reason.trim();
                }
            }
            if((essUser = getEssentials().getOfflineUser(target)) != null) {
                String finalTarget = punishIP ? essUser.getBase().getAddress().getAddress().toString()
                        : essUser.getConfigUUID().toString();
                if(isUndo) {
                    List<Punishment> activePunishments = getControl().getActivePunishments()
                            .getPunishments(finalTarget);
                    if(activePunishments.isEmpty()) {
                        // Nothing to do here!
                        SpaceUtils.sendMessage(commandSender, noUndoString);
                    } else {
                        Punishment p = activePunishments.get(activePunishments.size() - 1);
                        getControl().getActivePunishments().removePunishment(p);
                        getControl().getInactivePunishments().insertPunishment(p);
                        handlePunishment(type, finalTarget);
                        SpaceUtils.sendMessage(commandSender,
                                formatUnpunish(punishIP ? hideIP(p.getTarget()) : essUser.getName(), p.getType()));
                    }
                    return true;
                } else {
                    Optional<Punishment> p = getControl().getActivePunishments()
                            .insertPunishment(type,
                                    commandSender instanceof Player
                                            ? ((Player) commandSender).getUniqueId().toString() : "Console",
                                    finalTarget, reason, time);

                    if(type.equals(Punishments.BAN)) {
                        if(Bukkit.getOfflinePlayer(essUser.getConfigUUID()).isOnline()) {
                            Bukkit.getPlayer(essUser.getConfigUUID()).kickPlayer(formatBan(reason,
                                    (t ? TimeUtil.english(args[1]) : "Forever"),
                                    p.isPresent() ? p.get().getEnd() : "Some point in the future"));
                        }
                    } else if(punishIP) {
                        for(Player player : Bukkit.getOnlinePlayers()) {
                            if(player.getAddress().getAddress().toString().equals(finalTarget)) {
                                if(Bukkit.getOfflinePlayer(essUser.getConfigUUID()).isOnline()) {
                                    player.kickPlayer(formatBan(reason,
                                            (t ? TimeUtil.english(args[1]) : "Forever"),
                                            p.isPresent() ? p.get().getEnd() : "Some point in the future"));
                                }
                            }
                        }
                    }

                    handlePunishment(type, finalTarget);
                    announcePunishment(commandSender.getName(), punishIP ? hideIP(finalTarget) : essUser.getName(), type, reason, t ? args[1] : "" + time);
                }
            } else if(IPAddressUtil.isIPv4LiteralAddress(target)) {
                if(isUndo) {
                    List<Punishment> activePunishments = getControl().getActivePunishments().getPunishments(target);
                    if(activePunishments.isEmpty()) {
                        // Nothing to do here!
                        SpaceUtils.sendMessage(commandSender, noUndoString);
                    } else {
                        Punishment p = activePunishments.get(activePunishments.size() - 1);
                        getControl().getActivePunishments().removePunishment(p);
                        getControl().getInactivePunishments().insertPunishment(p);
                        handlePunishment(type, target);
                        SpaceUtils.sendMessage(commandSender, formatUnpunish(hideIP(p.getTarget()), p.getType()));
                    }
                    return true;
                } else {
                    Optional<Punishment> p = getControl().getActivePunishments()
                            .insertPunishment(type,
                                    commandSender instanceof Player ? ((Player) commandSender).getUniqueId().toString() : "Console",
                                    target, reason, time);
                    if(punishIP) {
                        for(Player player : Bukkit.getOnlinePlayers()) {
                            if(player.getAddress().getAddress().toString().equals(target)) {
                                player.kickPlayer(formatBan(reason,
                                        (t ? TimeUtil.english(args[1]) : "Forever"),
                                        p.isPresent() ? p.get().getEnd() : "Some point in the future"));
                            }
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

    private String hideIP(String ip) {
        return ip.trim().replaceFirst("\\.[0-9]{1,3}\\.[0-9]{1,3}$", ".XXX.XXX");
    }

    private String formatBan(String reason, String time, String end) {
        return banFormatString.replaceAll("<reason>", reason).replaceAll("<time>", time).replaceAll("<end>", end);
    }

    private String formatUnpunish(String name, String type) {
        return unpunishSuccessString.replaceAll("<name>", name)
                .replaceAll("<punishment>", Punishments.english(type));
    }

    private void handlePunishment(String type, String target) {
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
