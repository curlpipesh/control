package net.spacemc.control.commands;

import com.earth2me.essentials.User;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.punishment.Punishments;
import net.spacemc.control.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author audrey
 * @since 8/23/15.
 */
public class CommandCMute extends CCommand {
    public CommandCMute(SpaceControl control) {
        super(control);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length >= 1) {
            String playerName = args[0];
            User essUser;
            if((essUser = getEssentials().getOfflineUser(playerName)) != null) {
                int time = Integer.MAX_VALUE;
                boolean t = false;
                if(args.length > 1) {
                    if(TimeUtil.isValidTime(args[1])) {
                        time = TimeUtil.parseTimeIntoMinutes(args[1]);
                        t = true;
                    }
                }
                String reason = "§cCommand-mute§7";
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
                getControl().getActivePunishments()
                        .insertPunishment(Punishments.COMMAND_MUTE,
                                commandSender instanceof Player ? ((Player) commandSender).getUniqueId().toString() : "d9d85884-61dc-4925-aace-4e20f77ca03f", // Luv ya Slacks <3
                                essUser.getConfigUUID().toString(), reason, time);
                getControl().getCmutes().add(essUser.getConfigUUID());
                announce(commandSender.getName(), essUser.getName(), Punishments.COMMAND_MUTE, reason, t ? args[1] : "t:" + Integer.MAX_VALUE + "d");
                return true;
            } else {
                commandSender.sendMessage("§7\"§a" + args[0] + "§7\" was not recognized as a player!");
                return true;
            }
        } else {
            return false;
        }
    }
}
