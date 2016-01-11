package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import me.curlpipesh.users.SkirtsUser;
import me.curlpipesh.users.Users;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * Kicks a player from the server, broadcasting the kick.
 *
 * @author audrey
 * @since 10/2/15.
 */
public class CommandKick extends CCommand {
    private final String invalidTargetString;

    public CommandKick(final Control control) {
        super(control);
        invalidTargetString = control.getConfig().getString("invalid-target");
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if(args.length >= 1) {
            final String target = args[0];
            final Optional<SkirtsUser> skirtsUserOptional = Users.getInstance().getSkirtsUserMap().getUserByName(target);
            String reason = "§cKicked§7";

            if(args.length > 1) {
                reason = "";
                for(int i = 1; i < args.length; i++) {
                    reason += args[i] + ' ';
                }
                reason = reason.trim();
            }
            if(skirtsUserOptional.isPresent()) {
                Bukkit.getPlayer(skirtsUserOptional.get().getUuid()).kickPlayer(String.format("§4Kicked§r:\n\n§c%s§r", reason));
                getControl().broadcastImportantMessage(String.format("§c%s§7 was kicked by §c%s§7:§r", skirtsUserOptional.get().getLastName(),
                        commandSender.getName()), "§c" + reason + "§r");
            } else {
                commandSender.sendMessage(invalidTargetString.replaceAll("<name>", args[0]));
            }
            return true;
        } else {
            return false;
        }
    }
}
