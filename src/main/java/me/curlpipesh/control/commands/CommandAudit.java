package me.curlpipesh.control.commands;

import com.earth2me.essentials.User;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.control.Control;
import me.curlpipesh.control.punishment.Punishments;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sun.net.util.IPAddressUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TODO: Make this not shit
 *
 * @author audrey
 * @since 8/23/15.
 */
public class CommandAudit extends CCommand {
    public CommandAudit(Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length == 1) {
            String playerName = args[0];
            User essUser;
            if((essUser = getEssentials().getOfflineUser(playerName)) != null) {
                List<Punishment> active = new ArrayList<>(getControl().getActivePunishments().getPunishmentsBy(essUser.getConfigUUID().toString()));
                List<Punishment> inactive = new ArrayList<>(getControl().getInactivePunishments().getPunishmentsBy(essUser.getConfigUUID().toString()));
                active = active.stream().filter(p -> !p.getType().equals(Punishments.WARN)).collect(Collectors.<Punishment>toList());
                inactive = inactive.stream().filter(p -> !p.getType().equals(Punishments.WARN)).collect(Collectors.<Punishment>toList());
                if(!active.isEmpty() || !inactive.isEmpty()) {
                    commandSender.sendMessage("§a" + essUser.getName() + "§7's stats:");
                    commandSender.sendMessage("§7§m------------------------------------§7");
                    for(Punishment p : active) {
                        sendMessage(commandSender, p, true);
                    }
                    for(Punishment p : inactive) {
                        sendMessage(commandSender, p, false);
                    }
                    commandSender.sendMessage("§a" + essUser.getName() + "§7 has §c" + (active.size() + inactive.size())
                            + "§7 total punishments");
                    commandSender.sendMessage("§7§m------------------------------------§7");
                } else {
                    commandSender.sendMessage("§a" + essUser.getName() + "§7 has no issued punishments!");
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

    private void sendMessage(CommandSender commandSender, Punishment p, boolean active) {
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
        } else if(getEssentials().getUser(p.getTarget()) != null) {
            target = getEssentials().getUser(p.getTarget()).getName();
        } else if(Bukkit.getPlayer(UUID.fromString(p.getTarget())) != null) {
            target = Bukkit.getPlayer(UUID.fromString(p.getTarget())).getName();
        }

        TextComponent firstLine = new TextComponent(m);
        //TextComponent secondLine = new TextComponent(String.format("§7 * By §a%s§7 for §c%s§7.", issuer, p.getReason()));
        HoverEvent e = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("On: ").color(ChatColor.DARK_PURPLE).append(target).color(ChatColor.RESET)
                        .append("\n").append("From: ").color(ChatColor.DARK_PURPLE).append(p.getStart())
                        .color(ChatColor.RESET).append("\n").color(ChatColor.DARK_PURPLE).append("Until: ")
                        .append(p.getEnd()).color(ChatColor.RESET).append("\n").append("")
                        .append(active ? "Active" : "Inactive").color(active ? ChatColor.RED : ChatColor.GREEN)
                        .create());
        firstLine.setHoverEvent(e);
        //secondLine.setHoverEvent(e);
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.spigot().sendMessage(firstLine);
            //player.spigot().sendMessage(secondLine);
        } else {
            commandSender.sendMessage(m);
            commandSender.sendMessage(String.format("§7 * By §a%s§7 for §c%s§7.", target, p.getReason()));
        }
    }
}
