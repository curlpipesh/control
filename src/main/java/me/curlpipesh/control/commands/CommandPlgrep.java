package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author audrey
 * @since 10/3/15.
 */
public class CommandPlgrep extends CCommand {
    public CommandPlgrep(Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length > 0) {
            String accum = "";
            for(final String e : args) {
                accum += e + " ";
            }
            accum = accum.trim();
            final String grep = accum.toLowerCase();
            final List<String> results =
            Arrays.stream(Bukkit.getPluginManager().getPlugins())
                    .filter(p -> p.getName().equalsIgnoreCase(grep) || p.getName().toLowerCase().contains(grep))
                    .map(p -> "§7" + p.getName().toLowerCase().replaceAll(grep, "§a" + grep + "§7")).collect(Collectors.toList());
            if(!results.isEmpty()) {
                getControl().sendMessage(commandSender, "§7Plugins matching '§a" + grep + "§7':");
                results.forEach(m -> getControl().sendMessage(commandSender, m));
            } else {
                getControl().sendMessage(commandSender, "§7Couldn't find match for '§c" + grep + "§7'!");
            }
        } else {
            getControl().sendMessage(commandSender, "§7At least one argument required!");
        }
        return true;
    }
}
