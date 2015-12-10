package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author audrey
 * @since 8/29/15.
 */
public class CommandClearChat extends CCommand {
    public CommandClearChat(Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for(int i = 0; i < 100; i++) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(""));
        }
        this.getControl().broadcastMessage("§7Chat was cleared by §c" + commandSender.getName() + "§7!");
        return true;
    }
}
