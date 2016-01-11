package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.control.punishment.Punishment.PunishmentType;
import me.curlpipesh.users.SkirtsUser;
import me.curlpipesh.users.Users;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sun.net.util.IPAddressUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Shows the punishments issued by a player
 *
 * TODO: Make this not shit
 *
 * @author audrey
 * @since 8/23/15.
 */
public class CommandAudit extends CCommand {
    public CommandAudit(final Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if(args.length == 1) {
            final String playerName = args[0];
            final Optional<SkirtsUser> skirtsUser = Users.getInstance().getSkirtsUserMap().getUserByName(playerName);
            if(skirtsUser.isPresent()) {
                List<Punishment> active = new ArrayList<>(getControl().getActivePunishments()
                        .getPunishmentsBy(skirtsUser.get().getUuid().toString()));
                List<Punishment> inactive = new ArrayList<>(getControl().getInactivePunishments()
                        .getPunishmentsBy(skirtsUser.get().getUuid().toString()));
                active = active.stream().filter(p -> p.getType() != PunishmentType.WARN).collect(Collectors.<Punishment>toList());
                inactive = inactive.stream().filter(p -> p.getType() != PunishmentType.WARN).collect(Collectors.<Punishment>toList());
                if(!active.isEmpty() || !inactive.isEmpty()) {
                    commandSender.sendMessage("§a" + skirtsUser.get().getLastName() + "§7's stats:");
                    commandSender.sendMessage("§7§m------------------------------------§7");
                    for(final Punishment p : active) {
                        sendMessage(commandSender, p, true);
                    }
                    for(final Punishment p : inactive) {
                        sendMessage(commandSender, p, false);
                    }
                    commandSender.sendMessage("§a" + skirtsUser.get().getLastName() + "§7 has §c" +
                            (active.size() + inactive.size()) + "§7 total punishments");
                    commandSender.sendMessage("§7§m------------------------------------§7");
                } else {
                    commandSender.sendMessage("§a" + skirtsUser.get().getLastName() + "§7 has no issued punishments!");
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

    private void sendMessage(final CommandSender commandSender, final Punishment p, final boolean active) {
        String m = "§7#";
        if(active) {
            m += "§a";
        } else {
            m += "§7";
        }
        m += String.format("%s§7 - %s: %s", p.getId(), p.getType(), p.getReason());
        String target = p.getTarget();

        if(IPAddressUtil.isIPv4LiteralAddress(p.getTarget())) {
            target += " (IP)";
        } else if(Users.getInstance().getUserForUUID(p.getTarget()).isPresent()) {
            target = Users.getInstance().getUserForUUID(p.getTarget()).get().getLastName();
        } else if(Bukkit.getPlayer(UUID.fromString(p.getTarget())) != null) {
            target = Bukkit.getPlayer(UUID.fromString(p.getTarget())).getName();
        }

        final TextComponent firstLine = new TextComponent(m);
        //TextComponent secondLine = new TextComponent(String.format("§7 * By §a%s§7 for §c%s§7.", issuer, p.getReason()));
        final HoverEvent e = new HoverEvent(Action.SHOW_TEXT,
                new ComponentBuilder("On: ").color(ChatColor.DARK_PURPLE).append(target).color(ChatColor.RESET)
                        .append("\n").append("From: ").color(ChatColor.DARK_PURPLE).append(new Date(p.getStart()).toString())
                        .color(ChatColor.RESET).append("\n").color(ChatColor.DARK_PURPLE).append("Until: ")
                        .append(p.getEnd() == Integer.MAX_VALUE ? "The end of time" : new Date(p.getEnd()).toString())
                        .color(ChatColor.RESET).append("\n").append("").append(active ? "Active" : "Inactive")
                        .color(active ? ChatColor.RED : ChatColor.GREEN).create());
        firstLine.setHoverEvent(e);
        //secondLine.setHoverEvent(e);
        if(commandSender instanceof Player) {
            @SuppressWarnings("TypeMayBeWeakened") final Player player = (Player) commandSender;
            player.spigot().sendMessage(firstLine);
            //player.spigot().sendMessage(secondLine);
        } else {
            commandSender.sendMessage(m);
            commandSender.sendMessage(String.format("§7 * on §a%s§7 for §c%s§7.", target, p.getReason()));
        }
    }
}
