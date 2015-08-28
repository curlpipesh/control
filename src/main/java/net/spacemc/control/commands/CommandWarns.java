package net.spacemc.control.commands;

import com.earth2me.essentials.User;
import com.google.common.collect.Lists;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.punishment.Punishment;
import net.spacemc.control.punishment.Punishments;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TODO Fuck it I'll clean this up later
 *
 * @author audrey
 * @since 8/27/15.
 */
public class CommandWarns extends CCommand {
    public CommandWarns(SpaceControl control) {
        super(control);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length == 1) {
            String playerName = args[0];
            User essUser;
            if((essUser = getEssentials().getOfflineUser(playerName)) != null) {
                List<Punishment> punishments = new ArrayList<>();
                punishments.addAll(getControl().getActivePunishments().getPunishments(essUser.getConfigUUID().toString()));
                punishments = Lists.reverse(punishments.stream().filter(p -> p.getType().equals(Punishments.WARN)).collect(Collectors.<Punishment>toList()));
                if(punishments.size() > 0) {
                    commandSender.sendMessage("§a" + essUser.getName() + "§7's warnings:");
                    commandSender.sendMessage("§7§m------------------------------------§7");
                    for(Punishment p : punishments) {
                        String issuer =
                                getEssentials().getUser(p.getIssuer()) == null ?
                                        Bukkit.getPlayer(UUID.fromString(p.getIssuer())) != null ?
                                                Bukkit.getPlayer(UUID.fromString(p.getIssuer())).getName() :
                                                p.getIssuer() :
                                        getEssentials().getUser(p.getIssuer()).getName();
                        String m = String.format("§7#§a%s§7 - §a%s§7: %s", p.getId(), issuer, p.getType());

                        commandSender.sendMessage(m);
                        //commandSender.sendMessage(String.format("§7 * By §a%s§7 for §c%s§7.", issuer, p.getReason()));
                    }
                    commandSender.sendMessage("§a" + essUser.getName() + "§7 has §c" + punishments.size()
                            + "§7 total warnings");
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
