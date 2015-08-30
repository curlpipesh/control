package me.curlpipesh.control.commands;

import com.earth2me.essentials.User;
import me.curlpipesh.control.punishment.Punishments;
import me.curlpipesh.control.Control;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author audrey
 * @since 8/27/15.
 */
public class CommandWarn extends CCommand {
    private final String invalidTargetString;

    public CommandWarn(Control control) {
        super(control);
        invalidTargetString = control.getConfig().getString("invalid-target");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length >= 1) {
            String target = args[0];
            User essUser;
            String issuer = commandSender instanceof Player ? ((Player)commandSender).getUniqueId().toString() : "Console";
            String issuer2 = commandSender instanceof Player ? ((Player)commandSender).getName() : "Console";

            if((essUser = getEssentials().getUser(target)) != null) {
                String reason = "§c" + Punishments.WARN + "§r";
                if(args.length > 1) {
                    reason = "§c";
                    for(int i = 1; i < args.length; i++) {
                        reason += args[i] + " ";
                    }
                    reason = reason.trim() + "§r";
                }

                getControl().getActivePunishments().insertPunishment(Punishments.WARN,
                        issuer,
                        essUser.getConfigUUID().toString(), reason, Integer.MAX_VALUE);
                String m = String.format("§7%s §cwarned§7 %s: %s", issuer2, target, reason);
                getControl().broadcastMessage(m);
            } else {
                commandSender.sendMessage(invalidTargetString.replaceAll("<name>", args[0]));
            }
            return true;
        } else {
            return false;
        }
    }
}
