package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import me.curlpipesh.control.adminchat.UserMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author audrey
 * @since 10/4/15.
 */
public class CommandA extends CCommand {
    public CommandA(Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            if(commandSender.hasPermission("control.channels.use")) {
                Player player = (Player) commandSender;
                boolean flag = UserMap.getAdminChatUsers().stream().filter(u -> u.getUuid().equals(player.getUniqueId()))
                        .findFirst().get().isTalkingInChannel();
                UserMap.getAdminChatUsers().stream().filter(u -> u.getUuid().equals(player.getUniqueId()))
                        .forEach(p -> p.setTalkingInChannel(!p.isTalkingInChannel()));
                getControl().sendMessage(commandSender, "You are " +
                        (flag ? "no longer" : "now")
                        + " talking in adminchat!");
                return true;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
