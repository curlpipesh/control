package net.spacemc.control.commands;

import com.earth2me.essentials.Essentials;
import lombok.Getter;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.punishment.Punishments;
import net.spacemc.control.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import pw.slacks.space.util.SpaceUtils;

/**
 * @author audrey
 * @since 8/23/15.
 */
public abstract class CCommand implements CommandExecutor {
    @Getter
    private final SpaceControl control;

    @Getter
    private final Essentials essentials;

    public CCommand(SpaceControl control) {
        this.control = control;
        essentials = JavaPlugin.getPlugin(Essentials.class);
    }

    protected final void announcePunishment(String issuer, String player, String punishment, String reason, String length) {
        String m = String.format("§7%s §c%s§7 %s for %s: %s", issuer,
                Punishments.english(punishment), player, TimeUtil.english(length), reason);
        SpaceUtils.broadcastMessage(m);
        Bukkit.getConsoleSender().sendMessage(m);
    }
}
