package me.curlpipesh.control.commands;

import com.earth2me.essentials.Essentials;
import lombok.Getter;
import me.curlpipesh.control.Control;
import me.curlpipesh.control.punishment.Punishments;
import me.curlpipesh.control.util.TimeUtil;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author audrey
 * @since 8/23/15.
 */
public abstract class CCommand implements CommandExecutor {
    @Getter
    private final Control control;

    @Getter
    private final Essentials essentials;

    public CCommand(Control control) {
        this.control = control;
        essentials = JavaPlugin.getPlugin(Essentials.class);
    }

    protected final void announcePunishment(String issuer, String player, String punishment, String reason, String length) {
        String m = String.format("§7%s §c%s§7 %s for %s:", issuer,
                Punishments.english(punishment), player, TimeUtil.english(length));
        control.broadcastImportantMessage(m, "§c" + reason);
    }
}
