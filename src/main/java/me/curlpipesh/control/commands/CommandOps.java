package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author audrey
 * @since 10/3/15.
 */
public class CommandOps extends CCommand {
    public CommandOps(Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> messages = new ArrayList<>();
        messages.add("§7List of ops:");
        messages.addAll(Bukkit.getOperators().stream().map(o -> " §e*§7 " + o.getName()).collect(Collectors.toList()));
        getControl().sendImportantMessage(commandSender, messages.stream().toArray(String[]::new));
        return true;
    }
}
