package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * TODO: Intent is to effectively grep for a single plugin in the list
 *
 * @author audrey
 * @since 10/3/15.
 */
public class CommandPlgrep extends CCommand {
    public CommandPlgrep(Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length == 1) {
            return true;
        } else {
            return false;
        }
    }
}
