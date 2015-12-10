package me.curlpipesh.control.adminchat.bot.commands;

import me.curlpipesh.control.adminchat.bot.AdminChatBot;
import me.curlpipesh.control.adminchat.bot.BotCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author audrey
 * @since 12/5/15.
 */
public class CommandHelp extends BotCommand {
    public CommandHelp() {
        super("Help", "Shows the help", "help [command]");
    }

    @Override
    public void onCommand(final CommandSender sender, final String command, final String[] args) {
        if(args.length == 0) {
            final List<String> messages = new ArrayList<>();
            messages.add("§7Bot commands:");
            messages.addAll(AdminChatBot.getCommands().stream()
                    .filter(c -> c.getAllowedUuids().isEmpty() || (sender instanceof Player && c.getAllowedUuids().contains(((Player) sender).getUniqueId())))
                    .map(c -> "§7 * §e" + c.getName() + "§7: " + c.getDesc()).collect(Collectors.toList()));
            AdminChatBot.getControl().sendMessage(sender, messages.stream().toArray(String[]::new));
        } else {
            AdminChatBot.getCommands().stream().filter(c -> c.getName().equalsIgnoreCase(args[0])).forEach(c -> {
                final List<String> messages = new ArrayList<>();
                messages.add("§7Help for: §e" + c.getName());
                messages.add("§7Description: " + c.getDesc());
                messages.add("§7Usage: " + c.getUsage());
                AdminChatBot.getControl().sendMessage(sender, messages.stream().toArray(String[]::new));
            });
        }
    }
}
