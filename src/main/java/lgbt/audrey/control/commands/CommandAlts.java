package lgbt.audrey.control.commands;

import lgbt.audrey.control.Control;
import lgbt.audrey.users.Users;
import lgbt.audrey.users.user.AudreyUser;
import lgbt.audrey.util.utils.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author audrey
 * @since 3/26/16.
 */
public class CommandAlts extends CCommand {
    public CommandAlts(final Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if(args.length != 1) {
            return false;
        }
        final Optional<AudreyUser> first = Users.getInstance().getAudreyUserMap().getAudreyUsers().stream()
                .filter(u -> u.getLastName().equals(args[0])).findFirst();
        if(first.isPresent()) {
            final List<AudreyUser> alts = Users.getInstance().getAudreyUserMap().getAudreyUsers().stream()
                    .filter(u -> !u.getUuid().equals(first.get().getUuid()))
                    .filter(u -> u.getIp().equals(first.get().getIp()))
                    .collect(Collectors.toList());
            if(alts.isEmpty()) {
                MessageUtil.sendMessage(commandSender, ChatColor.GREEN + "No alts found.");
            } else {
                String a = "";
                for(final AudreyUser e : alts) {
                    a += e.getLastName() + ", ";
                }
                a = a.substring(0, a.length() - 2);
                MessageUtil.sendMessage(commandSender, ChatColor.RED + "Alts found:");
                MessageUtil.sendMessage(commandSender, ChatColor.RED + a);
            }
        } else {
            return false;
        }
        return true;
    }
}
