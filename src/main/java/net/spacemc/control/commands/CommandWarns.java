package net.spacemc.control.commands;

import com.earth2me.essentials.User;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.punishment.Punishment;
import net.spacemc.control.punishment.Punishments;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                        sendMessage(commandSender, p);
                    }
                    commandSender.sendMessage("§a" + essUser.getName() + "§7 has §c" + punishments.size()
                            + "§7 total warnings");
                    commandSender.sendMessage("§7§m------------------------------------§7");
                } else {
                    commandSender.sendMessage("§a" + essUser.getName() + "§7 has no warnings!");
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

    private void sendMessage(CommandSender commandSender, Punishment p) {
        String issuer =
                getEssentials().getUser(p.getIssuer()) == null ?
                        Bukkit.getPlayer(UUID.fromString(p.getIssuer())) != null ?
                                Bukkit.getPlayer(UUID.fromString(p.getIssuer())).getName() :
                                p.getIssuer() :
                        getEssentials().getUser(p.getIssuer()).getName();
        String m = String.format("§7#§a%s§7 - §a%s§7", p.getId(), issuer);
        TextComponent line = new TextComponent(m);
        HoverEvent e = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("On: ").color(ChatColor.DARK_PURPLE).append(p.getStart()).color(ChatColor.RESET)
                        .append("\nFor: ").color(ChatColor.DARK_PURPLE).append(p.getReason()).color(ChatColor.RESET).create());
        line.setHoverEvent(e);
        if(commandSender instanceof Player) {
            ((Player)commandSender).spigot().sendMessage(line);
        } else {
            commandSender.sendMessage(String.format("#%s - %s: %s", p.getId(), issuer, p.getReason()));
        }
    }
}
