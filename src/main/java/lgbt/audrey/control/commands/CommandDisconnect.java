package lgbt.audrey.control.commands;

import lgbt.audrey.control.Control;
import lgbt.audrey.util.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author audrey
 * @since 3/27/16.
 */
public class CommandDisconnect extends CCommand {
    public CommandDisconnect(final Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if(!commandSender.isOp()) {
            if(commandSender instanceof Player) {
                disconnect((Player) commandSender);
            }
            return false;
        }
        if(args.length != 1) {
            return false;
        }
        boolean flag = false;
        String name = "";
        for(final Player player : Bukkit.getOnlinePlayers()) {
            if(player.getName().equalsIgnoreCase(args[0])) {
                name = player.getName();
                disconnect(player);
                flag = true;
                break;
            }
        }
        if(flag) {
            MessageUtil.sendMessage(commandSender, ChatColor.GREEN + "Disconnected " + name + '!');
        } else {
            MessageUtil.sendMessage(commandSender, ChatColor.RED + "Couldn't find " + args[0] + '!');
        }
        return true;
    }

    private void disconnect(final Player player) {
        player.kickPlayer("Internal Server Error");
    }
}
