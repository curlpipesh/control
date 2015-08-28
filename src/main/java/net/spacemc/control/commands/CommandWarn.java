package net.spacemc.control.commands;

import com.earth2me.essentials.User;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.punishment.Punishments;
import net.spacemc.control.util.TimeUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.slacks.space.util.SpaceUtils;

/**
 * @author audrey
 * @since 8/27/15.
 */
public class CommandWarn extends CCommand {
    private final String invalidTargetString;

    public CommandWarn(SpaceControl control) {
        super(control);
        invalidTargetString = control.getConfig().getString("invalid-target");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length >= 1) {
            String target = args[0];
            User essUser;
            String issuer = commandSender instanceof Player ? ((Player)commandSender).getUniqueId().toString() : "Console";

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
                String m = String.format("§7%s §cwarned§7 %s: %s", issuer, target, reason);
                SpaceUtils.broadcastMessage(m);
            } else {
                commandSender.sendMessage(invalidTargetString.replaceAll("<name>", args[0]));
            }
            return true;
        } else {
            return false;
        }
    }
}
