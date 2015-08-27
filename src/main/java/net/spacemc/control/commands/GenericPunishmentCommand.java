package net.spacemc.control.commands;

import com.earth2me.essentials.User;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.util.TimeUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sun.net.util.IPAddressUtil;

/**
 * @author audrey
 * @since 8/26/15.
 */
@SuppressWarnings("unused")
public class GenericPunishmentCommand extends CCommand {
    private String type;
    private boolean punishIP;

    public GenericPunishmentCommand(SpaceControl control, String type) {
        super(control);
        this.type = type;
        punishIP = type.startsWith("IP_");
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
                String finalTarget = punishIP ? essUser.getBase().getAddress().getHostName() : essUser.getConfigUUID().toString();
                getControl().getActivePunishments()
                        .insertPunishment(type,
                                commandSender instanceof Player ? ((Player) commandSender).getUniqueId().toString() : "Console",
                                essUser.getConfigUUID().toString(), reason, time);
                getControl().getCmutes().add(essUser.getConfigUUID().toString());
                announce(commandSender.getName(), finalTarget, type, reason, "" + time);
                return true;
            } else {
                if(IPAddressUtil.isIPv4LiteralAddress(target)) {
                    getControl().getActivePunishments()
                            .insertPunishment(type,
                                    commandSender instanceof Player ? ((Player) commandSender).getUniqueId().toString() : "Console",
                                    target, reason, time);
                    getControl().getCmutes().add(target);
                    announce(commandSender.getName(), hideIP(target), type, reason, "" + time);
                } else {
                    commandSender.sendMessage("§7\"§a" + args[0] + "§7\" is not a valid target!");
                }
                return true;
            }
        } else {
            return false;
        }
    }

    private String hideIP(String ip) {
        return ip.trim().replaceFirst("\\.[0-9]{1,3}\\.[0-9]{1,3}$", ".XXX.XXX");
    }
}
