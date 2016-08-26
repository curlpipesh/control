package lgbt.audrey.control.commands;

import lgbt.audrey.control.Control;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Shocklingly, this clears the chat.
 *
 * @author audrey
 * @since 8/29/15.
 */
public class CommandClearChat extends CCommand {
    public CommandClearChat(final Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        for(int i = 0; i < 100; i++) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(""));
        }
        this.getControl().broadcastMessage("§7Chat was cleared by §c" + commandSender.getName() + "§7!");
        return true;
    }
}
