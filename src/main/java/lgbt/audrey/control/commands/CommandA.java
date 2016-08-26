package lgbt.audrey.control.commands;

import lgbt.audrey.control.Control;
import lgbt.audrey.control.adminchat.UserMap;
import lgbt.audrey.control.adminchat.UserMap.Channel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Lets a user enter adminchat, or send a message to the channel
 *
 * @author audrey
 * @since 10/4/15.
 */
public class CommandA extends CCommand {
    public CommandA(final Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if(commandSender instanceof Player) {
            if(commandSender.hasPermission("control.channels.use")) {
                //noinspection TypeMayBeWeakened
                final Player player = (Player) commandSender;
                if(args.length > 0) {
                    String message = "";
                    for(final String e : args) {
                        message += e + ' ';
                    }
                    message = message.trim();
                    UserMap.sendMessageToChannel(Channel.ADMIN_CHAT,
                            ((Player)commandSender).getDisplayName(), message);
                } else {
                    final boolean flag = UserMap.getAdminChatUsers().stream()
                            .filter(u -> u.getUuid().equals(player.getUniqueId()))
                            .findFirst().get().isTalkingInChannel();
                    UserMap.getAdminChatUsers().stream().filter(u -> u.getUuid().equals(player.getUniqueId()))
                            .forEach(p -> p.setTalkingInChannel(!p.isTalkingInChannel()));
                    getControl().sendMessage(commandSender, "§7You are " +
                            (flag ? "no longer" : "now")
                            + " talking in adminchat!");
                }
                return true;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
