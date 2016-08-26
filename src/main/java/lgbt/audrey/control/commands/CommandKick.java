package lgbt.audrey.control.commands;

import lgbt.audrey.control.Control;
import lgbt.audrey.users.Users;
import lgbt.audrey.users.user.AudreyUser;
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
            final Optional<AudreyUser> audreyUserOptional = Users.getInstance().getAudreyUserMap().getUserByName(target);
            String reason = "§cKicked§7";

            if(args.length > 1) {
                reason = "";
                for(int i = 1; i < args.length; i++) {
                    reason += args[i] + ' ';
                }
                reason = reason.trim();
            }
            if(audreyUserOptional.isPresent()) {
                Bukkit.getPlayer(audreyUserOptional.get().getUuid()).kickPlayer(String.format("§4Kicked§r:\n\n§c%s§r", reason));
                getControl().broadcastImportantMessageWithPerm("control.notify.kick",
                        new String[] {
                                String.format("§c%s§7 was kicked by §c%s§7:§r", audreyUserOptional.get().getLastName(),
                        commandSender.getName()), "§c" + reason + "§r"
                        });
            } else {
                commandSender.sendMessage(invalidTargetString.replaceAll("<name>", args[0]));
            }
            return true;
        } else {
            return false;
        }
    }
}
