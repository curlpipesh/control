package net.spacemc.control.commands;

import com.earth2me.essentials.User;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.punishment.Punishment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * @author audrey
 * @since 8/23/15.
 */
public class CommandHistory extends CCommand {
    public CommandHistory(SpaceControl control) {
        super(control);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length == 1) {
            String playerName = args[0];
            User essUser;
            if((essUser = getEssentials().getOfflineUser(playerName)) != null) {
                List<Punishment> punishments = new ArrayList<>();
                punishments.addAll(getControl().getInactivePunishments().getPunishments(essUser.getConfigUUID().toString()));
                punishments.addAll(getControl().getActivePunishments().getPunishments(essUser.getConfigUUID().toString()));
                if(punishments.size() > 0) {
                    commandSender.sendMessage("§a" + essUser.getName() + "§7's stats:");
                    commandSender.sendMessage("§7------------------------------------");
                    for(Punishment p : punishments) {
                        String m = "";
                        if(p.over()) {
                            m += "§7";
                        } else {
                            m += "§a";
                        }
                        m += String.format("#%s: %s", p.getId(), p.getType());
                        commandSender.sendMessage(m);
                        commandSender.sendMessage(String.format("§7 * By §a%s§7 for §c%s§7.", getEssentials().getUser(p.getIssuer()), p.getReason()));
                    }
                } else {
                    commandSender.sendMessage("§a" + essUser.getName() + "§7 has a clean record!");
                }
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
