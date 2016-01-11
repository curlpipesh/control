package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Shows the number of players online
 *
 * @author audrey
 * @since 10/2/15.
 */
public class CommandOnline extends CCommand {
    public CommandOnline(final Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        commandSender.sendMessage(String.format("§7There are §a%s§7 players online.§r", Bukkit.getOnlinePlayers().size()));
        return true;
    }
}
