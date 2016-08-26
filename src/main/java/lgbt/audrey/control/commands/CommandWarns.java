package lgbt.audrey.control.commands;

import com.google.common.collect.Lists;
import lgbt.audrey.control.Control;
import lgbt.audrey.control.punishment.Punishment;
import lgbt.audrey.control.punishment.Punishment.PunishmentType;
import lgbt.audrey.users.Users;
import lgbt.audrey.users.user.AudreyUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Shows all warnings for a user
 *
 * TODO Fuck it I'll clean this up later
 *
 * @author audrey
 * @since 8/27/15.
 */
public class CommandWarns extends CCommand {
    public CommandWarns(final Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if(args.length == 1) {
            final String playerName = args[0];
            final Optional<AudreyUser> audreyUserOptional = Users.getInstance().getAudreyUserMap().getUserByName(playerName);
            if(audreyUserOptional.isPresent()) {
                List<Punishment> punishments = new ArrayList<>(getControl().getActivePunishments()
                        .getPunishments(audreyUserOptional.get().getUuid().toString()));
                punishments = Lists.reverse(punishments.stream().filter(p -> p.getType() == PunishmentType.WARN)
                        .collect(Collectors.<Punishment>toList()));
                if(!punishments.isEmpty()) {
                    commandSender.sendMessage("§a" + audreyUserOptional.get().getLastName() + "§7's warnings:");
                    commandSender.sendMessage("§7§m------------------------------------§7");
                    for(final Punishment p : punishments) {
                        sendMessage(commandSender, p);
                    }
                    commandSender.sendMessage("§a" + audreyUserOptional.get().getLastName() + "§7 has §c" + punishments.size()
                            + "§7 total warnings");
                    commandSender.sendMessage("§7§m------------------------------------§7");
                } else {
                    commandSender.sendMessage("§a" + audreyUserOptional.get().getLastName() + "§7 has no warnings!");
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

    private void sendMessage(final CommandSender commandSender, final Punishment p) {
        final String issuer = p.getIssuer().equalsIgnoreCase("console") ? "CONSOLE" :
                Users.getInstance().getUserForUUID(p.getIssuer()).isPresent() ?
                        Bukkit.getPlayer(UUID.fromString(p.getIssuer())) != null ?
                                Bukkit.getPlayer(UUID.fromString(p.getIssuer())).getName() :
                                Users.getInstance().getUserForUUID(p.getIssuer()).get().getLastName() :
                        p.getIssuer();
        if(commandSender instanceof Player) {
            final String m = String.format("§7#§a%s§7 - §a%s§7", p.getId(), issuer);
            final TextComponent line = new TextComponent(m);
            final HoverEvent e = new HoverEvent(Action.SHOW_TEXT,
                    new ComponentBuilder("On: ").color(ChatColor.DARK_PURPLE).append(new Date(p.getStart()).toString())
                            .color(ChatColor.RESET)
                            .append("\n").color(ChatColor.RESET).append("For: ").color(ChatColor.DARK_PURPLE)
                            .append(p.getReason()).color(ChatColor.RESET).create());
            line.setHoverEvent(e);
            ((Player) commandSender).spigot().sendMessage(line);
        } else {
            commandSender.sendMessage(String.format("#%s - %s: %s", p.getId(), issuer, p.getReason()));
        }
    }
}
