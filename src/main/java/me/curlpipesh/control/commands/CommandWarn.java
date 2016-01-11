package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.control.punishment.Punishment.PunishmentType;
import me.curlpipesh.users.SkirtsUser;
import me.curlpipesh.users.Users;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Issues a warning to a player
 *
 * @author audrey
 * @since 8/27/15.
 */
public class CommandWarn extends CCommand {
    private final String invalidTargetString;

    public CommandWarn(final Control control) {
        super(control);
        invalidTargetString = control.getConfig().getString("invalid-target");
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if(args.length >= 1) {
            final String target = args[0];
            final Optional<SkirtsUser> skirtsUser = Users.getInstance().getSkirtsUserMap().getUserByName(target);
            final String issuer = commandSender instanceof Player ? ((Player) commandSender).getUniqueId().toString()
                    : "Console";
            final String issuer2 = commandSender instanceof Player ? commandSender.getName() : "Console";

            if(skirtsUser.isPresent()) {
                List<Punishment> punishments = new ArrayList<>(getControl().getActivePunishments()
                        .getPunishments(skirtsUser.get().getUuid().toString()));
                punishments = punishments.stream().filter(p -> p.getType() == PunishmentType.WARN)
                        .collect(Collectors.<Punishment>toList());
                String reason = "§c" + PunishmentType.WARN + "§r";
                if(args.length > 1) {
                    reason = "§c";
                    for(int i = 1; i < args.length; i++) {
                        reason += args[i] + ' ';
                    }
                    reason = reason.trim() + "§r";
                }

                getControl().getActivePunishments().insertPunishment(PunishmentType.WARN,
                        issuer,
                        skirtsUser.get().getUuid().toString(), reason, Integer.MAX_VALUE);
                final String m = String.format("§7%s §cwarned§7 %s:", issuer2, target);
                getControl().broadcastMessage(m, "§c" + reason);
                if(!punishments.isEmpty()) {
                    commandSender.sendMessage("§c" + skirtsUser.get().getLastName() + "§7 already has §c"
                            + punishments.size() + "§7 warnings!§r");
                }
            } else {
                commandSender.sendMessage(invalidTargetString.replaceAll("<name>", args[0]));
            }
            return true;
        } else {
            return false;
        }
    }
}
